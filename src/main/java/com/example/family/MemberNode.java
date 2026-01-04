package com.example.family;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Üye node - GUI desteği ile.
 */
public class MemberNode {

    private static final int DEFAULT_GRPC_PORT = 7000;
    private static final String DEFAULT_LEADER_HOST = "localhost";
    private static final int DEFAULT_LEADER_PORT = 6000;

    private final int grpcPort;
    private final String leaderHost;
    private final int leaderPort;
    private final DiskStorage storage;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Consumer<String> logCallback;
    private Server server;
    private volatile boolean running = false;

    public MemberNode(int grpcPort) {
        this(grpcPort, DiskStorage.WriteMode.BUFFERED);
    }

    public MemberNode(int grpcPort, DiskStorage.WriteMode writeMode) {
        this(grpcPort, DEFAULT_LEADER_HOST, DEFAULT_LEADER_PORT, writeMode);
    }

    public MemberNode(int grpcPort, String leaderHost, int leaderPort, DiskStorage.WriteMode writeMode) {
        this.grpcPort = grpcPort;
        this.leaderHost = leaderHost;
        this.leaderPort = leaderPort;
        this.storage = new DiskStorage(Path.of("data", "member-" + grpcPort), writeMode);
    }

    public void setLogCallback(Consumer<String> callback) {
        this.logCallback = callback;
    }

    private void log(String message) {
        System.out.println(message);
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }

    public void start() {
        log("╔══════════════════════════════════════════════════════════════╗");
        log("║                    ÜYE NODE BAŞLATILIYOR                     ║");
        log("╠══════════════════════════════════════════════════════════════╣");
        log("║ gRPC Port: " + grpcPort);
        log("║ Lider: " + leaderHost + ":" + leaderPort);
        log("║ I/O Modu: " + storage.getWriteMode());
        log("╚══════════════════════════════════════════════════════════════╝");

        server = ServerBuilder.forPort(grpcPort)
                .addService(new ReplicationServiceImpl(storage))
                .build();
        try {
            server.start();
            running = true;
            log("✓ gRPC server ayakta. Mevcut mesaj sayısı: " + storage.messageCount());

            registerWithLeader();
            startStatsPrinter();

            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            log("✗ Üye node başlatılamadı: " + e.getMessage());
        }
    }

    public void startAsync() {
        new Thread(this::start).start();
    }

    public void stop() {
        running = false;
        scheduler.shutdownNow();
        if (server != null) {
            server.shutdown();
            log("Üye node durduruldu.");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getMessageCount() {
        return storage.messageCount();
    }

    private void registerWithLeader() {
        try {
            log("→ Lidere bağlanılıyor: " + leaderHost + ":" + leaderPort);
            
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(leaderHost, leaderPort)
                    .usePlaintext()
                    .build();

            FamilyReplicationServiceGrpc.FamilyReplicationServiceBlockingStub stub = 
                    FamilyReplicationServiceGrpc.newBlockingStub(channel);

            String myHost = java.net.InetAddress.getLocalHost().getHostAddress();
            String nodeId = "member-" + grpcPort;
            
            RegisterNodeRequest request = RegisterNodeRequest.newBuilder()
                    .setNodeId(nodeId)
                    .setHost(myHost)
                    .setPort(grpcPort)
                    .build();

            RegisterNodeResponse response = stub.registerNode(request);
            if (response.getAccepted()) {
                log("✓ Lidere başarıyla kaydolundu: " + nodeId);
            } else {
                log("✗ Lidere kayıt reddedildi: " + response.getError());
            }

            channel.shutdown();
        } catch (Exception e) {
            log("⚠ Lidere kayıt hatası: " + e.getMessage());
        }
    }

    private void startStatsPrinter() {
        scheduler.scheduleAtFixedRate(() -> {
            if (running) {
                log("[Üye " + grpcPort + " | " + storage.getWriteMode() + "] Mesaj sayısı: " + storage.messageCount());
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
}
