package com.example.family;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Lider node:
 * - İstemciden gelen text tabanlı HaToKuSe (SET/GET) isteklerini TCP soket
 * üzerinden dinler.
 * - Mesajları gRPC ile aile üyelerine (ReplicationService) çoğaltır.
 * - tolerance.conf dosyasından hata tolerans değerini okur.
 * - gRPC sunucusu ile dinamik üye kayıtlarını kabul eder.
 * - Farklı I/O modlarını destekler (buffered, unbuffered, zero-copy,
 * memory-mapped)
 */
public class LeaderNode {

    private static final int DEFAULT_CLIENT_PORT = 5000;
    private static final int DEFAULT_GRPC_PORT = 6000;

    private final int clientPort;
    private final int grpcPort;
    private final ToleranceConfig toleranceConfig;
    private final LeaderState state;
    private final DiskStorage.WriteMode writeMode;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public LeaderNode() {
        this(DiskStorage.WriteMode.BUFFERED);
    }

    public LeaderNode(DiskStorage.WriteMode writeMode) {
        this(DEFAULT_CLIENT_PORT, DEFAULT_GRPC_PORT, writeMode);
    }

    public LeaderNode(int clientPort) {
        this(clientPort, DEFAULT_GRPC_PORT, DiskStorage.WriteMode.BUFFERED);
    }

    public LeaderNode(int clientPort, int grpcPort) {
        this(clientPort, grpcPort, DiskStorage.WriteMode.BUFFERED);
    }

    public LeaderNode(int clientPort, int grpcPort, DiskStorage.WriteMode writeMode) {
        this.clientPort = clientPort;
        this.grpcPort = grpcPort;
        this.writeMode = writeMode;
        this.toleranceConfig = ToleranceConfigLoader.loadOrDefault();
        List<MemberConfig> members = MembersConfigLoader.load();
        DiskStorage leaderStorage = new DiskStorage(Path.of("data", "leader"), writeMode);
        this.state = new LeaderState(leaderStorage, members);
    }

    public void start() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                   LİDER NODE BAŞLATILIYOR                    ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ Client Port: " + padRight(String.valueOf(clientPort), 48) + "║");
        System.out.println("║ gRPC Port: " + padRight(String.valueOf(grpcPort), 50) + "║");
        System.out.println("║ Hata Toleransı: " + padRight(String.valueOf(toleranceConfig.tolerance()), 45) + "║");
        System.out.println("║ I/O Modu: " + padRight(writeMode.toString(), 51) + "║");
        System.out.println("║ Çalışma Dizini: " + padRight(new File(".").getAbsolutePath(), 45) + "║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        // gRPC sunucusunu başlat (dinamik üye kayıtları için)
        startGrpcServer();

        // İstatistik bastırıcıyı başlat
        startStatsPrinter();

        try (ServerSocket serverSocket = new ServerSocket(clientPort)) {
            System.out.println("✓ HaToKuSe lider soketi port " + clientPort + " üzerinde dinleniyor...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("→ Yeni client bağlandı: " + clientSocket.getRemoteSocketAddress());
                // Her bağlantıyı ayrı bir handler'a ver, state'i aktar.
                new Thread(new LeaderTextClientHandler(clientSocket, state, toleranceConfig)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scheduler.shutdownNow();
            state.shutdown();
        }
    }

    /**
     * Dinamik üye kayıtları için gRPC sunucusunu başlatır.
     */
    private void startGrpcServer() {
        new Thread(() -> {
            try {
                Server server = ServerBuilder.forPort(grpcPort)
                        .addService(new LeaderReplicationServiceImpl(state))
                        .build()
                        .start();
                System.out.println("✓ Lider gRPC sunucusu port " + grpcPort
                        + " üzerinde dinleniyor (dinamik üye kayıtları için)...");
                Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
                server.awaitTermination();
            } catch (IOException | InterruptedException e) {
                System.err.println("✗ Lider gRPC sunucusu başlatılamadı: " + e.getMessage());
            }
        }).start();
    }

    private void startStatsPrinter() {
        scheduler.scheduleAtFixedRate(() -> {
            int total = state.leaderTotalMessages();
            Map<String, Integer> perMember = state.memberMessageCountsSnapshot();
            System.out.println("╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║               İSTATİSTİKLER [" + writeMode + "]");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║ Toplam mesaj (lider diski): " + padRight(String.valueOf(total), 33) + "║");
            for (Map.Entry<String, Integer> e : perMember.entrySet()) {
                System.out.println("║ Üye " + padRight(e.getKey() + " -> " + e.getValue() + " mesaj", 56) + "║");
            }
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
        }, 10, 10, TimeUnit.SECONDS);
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
