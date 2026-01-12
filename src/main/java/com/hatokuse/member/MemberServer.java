package com.hatokuse.member;

import com.hatokuse.proto.*;
import com.hatokuse.storage.DiskStorage;
import io.grpc.*;

import java.util.concurrent.*;

/**
 * Üye Sunucu.
 * Lidere kayıt olur, heartbeat gönderir ve gRPC ile mesaj alır/gönderir.
 */
public class MemberServer {

    private static final int DEFAULT_PORT = 5002;
    private static final String DEFAULT_LEADER_HOST = "localhost";
    private static final int DEFAULT_LEADER_PORT = 5001;
    private static final String DEFAULT_DATA_DIR = "./data/members";

    // Heartbeat gönderme aralığı (saniye)
    private static final int HEARTBEAT_INTERVAL = 5;

    // İstatistik yazdırma aralığı (saniye)
    private static final int STATS_INTERVAL = 10;

    private final String memberId;
    private final int port;
    private final String leaderHost;
    private final int leaderPort;
    private final DiskStorage storage;

    private Server grpcServer;
    private ManagedChannel leaderChannel;
    private LeaderServiceGrpc.LeaderServiceBlockingStub leaderStub;
    private ScheduledExecutorService scheduler;
    private volatile boolean running = true;

    public MemberServer(String memberId, int port, String leaderHost, int leaderPort, String dataDir) throws Exception {
        this.memberId = memberId;
        this.port = port;
        this.leaderHost = leaderHost;
        this.leaderPort = leaderPort;

        // Her üye için ayrı dizin
        String memberDataDir = dataDir + "/" + memberId;
        this.storage = new DiskStorage(memberDataDir);
    }

    /**
     * Sunucuyu başlatır.
     */
    public void start() throws Exception {
        System.out.println("========================================");
        System.out.println("    HaToKuSe ÜYE SUNUCU: " + memberId);
        System.out.println("========================================");
        System.out.println("Port: " + port);
        System.out.println("Lider: " + leaderHost + ":" + leaderPort);
        System.out.println("Veri dizini: " + storage.getStoragePath());

        // gRPC sunucusunu başlat
        startGrpcServer();

        // Lidere bağlan
        connectToLeader();

        // Lidere kayıt ol
        registerWithLeader();

        // Heartbeat ve istatistik zamanlayıcısını başlat
        startSchedulers();

        System.out.println("Üye sunucu başlatıldı.");
    }

    /**
     * gRPC sunucusunu başlatır.
     */
    private void startGrpcServer() throws Exception {
        grpcServer = ServerBuilder.forPort(port)
                .addService(new MemberServiceImpl(memberId, storage))
                .build()
                .start();

        System.out.println("[GRPC] Sunucu başlatıldı, port: " + port);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[" + memberId + "] Sunucu kapatılıyor...");
            shutdown();
        }));
    }

    /**
     * Lidere bağlanır.
     */
    private void connectToLeader() {
        leaderChannel = ManagedChannelBuilder.forAddress(leaderHost, leaderPort)
                .usePlaintext()
                .build();
        leaderStub = LeaderServiceGrpc.newBlockingStub(leaderChannel);

        System.out.println("[" + memberId + "] Lidere bağlantı kuruldu");
    }

    /**
     * Lidere kayıt olur.
     */
    private void registerWithLeader() {
        try {
            RegisterRequest request = RegisterRequest.newBuilder()
                    .setMemberId(memberId)
                    .setHost("localhost") // Gerçek ortamda IP adresi kullanılır
                    .setPort(port)
                    .build();

            RegisterResponse response = leaderStub.registerMember(request);

            if (response.getSuccess()) {
                System.out.println("[" + memberId + "] Lidere kayıt başarılı");
            } else {
                System.err.println("[" + memberId + "] Kayıt başarısız: " + response.getErrorMessage());
            }

        } catch (StatusRuntimeException e) {
            System.err.println("[" + memberId + "] Lider iletişim hatası: " + e.getStatus());
        }
    }

    /**
     * Heartbeat ve istatistik zamanlayıcılarını başlatır.
     */
    private void startSchedulers() {
        scheduler = Executors.newScheduledThreadPool(2);

        // Heartbeat
        scheduler.scheduleAtFixedRate(this::sendHeartbeat,
                HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);

        // İstatistik yazdırma
        scheduler.scheduleAtFixedRate(this::printStats,
                STATS_INTERVAL, STATS_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Lidere heartbeat gönderir.
     */
    private void sendHeartbeat() {
        if (!running)
            return;

        try {
            HeartbeatRequest request = HeartbeatRequest.newBuilder()
                    .setMemberId(memberId)
                    .setMessageCount(storage.getMessageCount())
                    .build();

            HeartbeatResponse response = leaderStub.heartbeat(request);

            if (!response.getAcknowledged()) {
                System.err.println("[" + memberId + "] Heartbeat onaylanmadı");
            }

        } catch (StatusRuntimeException e) {
            System.err.println("[" + memberId + "] Heartbeat hatası: " + e.getStatus());
        }
    }

    /**
     * İstatistikleri yazdırır.
     */
    private void printStats() {
        if (!running)
            return;

        System.out.println("\n[" + memberId + "] ===== İSTATİSTİK =====");
        System.out.println("[" + memberId + "] Saklanan mesaj sayısı: " + storage.getMessageCount());
        System.out.println("[" + memberId + "] Veri dizini: " + storage.getStoragePath());
        System.out.println("[" + memberId + "] ========================\n");
    }

    /**
     * Sunucuyu kapatır.
     */
    public void shutdown() {
        running = false;

        // Liderden çıkış
        try {
            UnregisterRequest request = UnregisterRequest.newBuilder()
                    .setMemberId(memberId)
                    .build();
            leaderStub.unregisterMember(request);
        } catch (Exception e) {
            // Ignore
        }

        if (scheduler != null) {
            scheduler.shutdown();
        }

        if (leaderChannel != null) {
            leaderChannel.shutdown();
        }

        if (grpcServer != null) {
            grpcServer.shutdown();
        }

        System.out.println("[" + memberId + "] Sunucu kapatıldı.");
    }

    /**
     * Ana metod.
     */
    public static void main(String[] args) {
        String memberId = "member-" + System.currentTimeMillis();
        int port = DEFAULT_PORT;
        String leaderHost = DEFAULT_LEADER_HOST;
        int leaderPort = DEFAULT_LEADER_PORT;
        String dataDir = DEFAULT_DATA_DIR;

        // Komut satırı argümanlarını işle
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--id":
                    if (i + 1 < args.length) {
                        memberId = args[++i];
                    }
                    break;
                case "--port":
                    if (i + 1 < args.length) {
                        port = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--leader-host":
                    if (i + 1 < args.length) {
                        leaderHost = args[++i];
                    }
                    break;
                case "--leader-port":
                    if (i + 1 < args.length) {
                        leaderPort = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--data-dir":
                    if (i + 1 < args.length) {
                        dataDir = args[++i];
                    }
                    break;
                case "--help":
                    printUsage();
                    return;
            }
        }

        try {
            MemberServer server = new MemberServer(memberId, port, leaderHost, leaderPort, dataDir);
            server.start();

            // Kapanma bekle
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Sunucu başlatma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("HaToKuSe Üye Sunucu");
        System.out.println("Kullanım: java com.hatokuse.member.MemberServer [seçenekler]");
        System.out.println("");
        System.out.println("Seçenekler:");
        System.out.println("  --id <member_id>       Üye kimliği (varsayılan: otomatik)");
        System.out.println("  --port <port>          gRPC sunucu portu (varsayılan: 5002)");
        System.out.println("  --leader-host <host>   Lider sunucu adresi (varsayılan: localhost)");
        System.out.println("  --leader-port <port>   Lider gRPC portu (varsayılan: 5001)");
        System.out.println("  --data-dir <path>      Veri dizini (varsayılan: ./data/members)");
        System.out.println("  --help                 Bu yardım mesajını gösterir");
    }
}
