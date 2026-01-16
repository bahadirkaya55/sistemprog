package com.hatokuse.leader;

import com.hatokuse.config.ConfigReader;
import com.hatokuse.protocol.HaToKuSeProtocol;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Lider Sunucu.
 * Text tabanlı istemci bağlantısı kabul eder ve gRPC ile üyelerle haberleşir.
 */
public class LeaderServer {

    // Varsayılan portlar
    private static final int DEFAULT_CLIENT_PORT = 5000; // İstemci bağlantısı için
    private static final int DEFAULT_GRPC_PORT = 5001; // gRPC (üye) bağlantısı için

    // Periyodik istatistik yazdırma aralığı (saniye)
    private static final int STATS_INTERVAL = 10;

    private final int clientPort;
    private final int grpcPort;
    private final MemberManager memberManager;
    private final ConfigReader config;

    private Server grpcServer;
    private ServerSocket clientServerSocket;
    private ExecutorService clientExecutor;
    private ScheduledExecutorService statsScheduler;
    private volatile boolean running = true;

    public LeaderServer(int clientPort, int grpcPort, int tolerance) {
        this.clientPort = clientPort;
        this.grpcPort = grpcPort;
        this.memberManager = new MemberManager(tolerance);
        this.config = new ConfigReader();
    }

    /**
     * Sunucuyu başlatır.
     */
    public void start() throws Exception {
        System.out.println("========================================");
        System.out.println("    HaToKuSe LİDER SUNUCU");
        System.out.println("========================================");
        System.out.println("İstemci portu: " + clientPort);
        System.out.println("gRPC portu: " + grpcPort);

        // gRPC sunucusunu başlat
        startGrpcServer();

        // İstemci sunucusunu başlat
        startClientServer();

        // Periyodik istatistik yazdırma
        startStatsReporter();

        System.out.println("Lider sunucu başlatıldı.");
        System.out.println("Üyelerin bağlanması bekleniyor...");
    }

    /**
     * gRPC sunucusunu başlatır (üye bağlantıları için).
     */
    private void startGrpcServer() throws IOException {
        grpcServer = ServerBuilder.forPort(grpcPort)
                .addService(new LeaderServiceImpl(memberManager))
                .build()
                .start();

        System.out.println("[GRPC] Sunucu başlatıldı, port: " + grpcPort);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[GRPC] Sunucu kapatılıyor...");
            grpcServer.shutdown();
        }));
    }

    /**
     * İstemci sunucusunu başlatır (text tabanlı bağlantılar için).
     */
    private void startClientServer() throws IOException {
        clientServerSocket = new ServerSocket(clientPort);
        clientExecutor = Executors.newCachedThreadPool();

        System.out.println("[CLIENT] Sunucu başlatıldı, port: " + clientPort);

        // İstemci bağlantılarını kabul eden thread
        Thread acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = clientServerSocket.accept();
                    System.out.println("[CLIENT] Yeni bağlantı: " + clientSocket.getRemoteSocketAddress());
                    clientExecutor.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[CLIENT] Bağlantı kabul hatası: " + e.getMessage());
                    }
                }
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    /**
     * İstemci bağlantısını işler.
     */
    private void handleClient(Socket socket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[CLIENT] Komut alındı: " + line);
                String response = processCommand(line);
                writer.println(response);
                System.out.println("[CLIENT] Yanıt gönderildi: " + response);
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] İstemci işleme hatası: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Komutu işler ve yanıt döner.
     */
    private String processCommand(String rawCommand) {
        HaToKuSeProtocol.ParsedCommand cmd = HaToKuSeProtocol.parseCommand(rawCommand);

        if (!cmd.isValid()) {
            return HaToKuSeProtocol.createErrorResponse(cmd.getError());
        }

        if (cmd.isSet()) {
            return handleSetCommand(cmd.getMessageId(), cmd.getMessageContent());
        } else if (cmd.isGet()) {
            return handleGetCommand(cmd.getMessageId());
        } else if (cmd.isDel()) {
            return handleDelCommand(cmd.getMessageId());
        } else {
            return HaToKuSeProtocol.createErrorResponse("Bilinmeyen komut");
        }
    }

    /**
     * SET komutunu işler.
     */
    private String handleSetCommand(String messageId, String content) {
        // Replikasyon için üyeleri seç
        List<MemberManager.MemberInfo> targetMembers = memberManager.selectMembersForReplication();

        if (targetMembers.isEmpty()) {
            return HaToKuSeProtocol.createErrorResponse("Aktif üye bulunamadı");
        }

        System.out.println("[LEADER] SET " + messageId + " -> " + targetMembers.size() + " üyeye gönderiliyor");

        // Mesajı replike et
        int successCount = memberManager.replicateMessage(messageId, content, targetMembers);

        if (successCount > 0) {
            System.out.println("[LEADER] Mesaj kaydedildi: " + messageId + " (" + successCount + " üyede)");
            return HaToKuSeProtocol.createOkResponse();
        } else {
            return HaToKuSeProtocol.createErrorResponse("Replikasyon başarısız");
        }
    }

    /**
     * GET komutunu işler.
     */
    private String handleGetCommand(String messageId) {
        System.out.println("[LEADER] GET " + messageId);

        String content = memberManager.getMessageFromMembers(messageId);

        if (content != null) {
            return HaToKuSeProtocol.createOkResponse(content);
        } else {
            return HaToKuSeProtocol.createErrorResponse("Mesaj bulunamadı: " + messageId);
        }
    }

    /**
     * DEL komutunu işler.
     */
    private String handleDelCommand(String messageId) {
        System.out.println("[LEADER] DEL " + messageId);

        boolean success = memberManager.deleteMessageFromMembers(messageId);

        if (success) {
            System.out.println("[LEADER] Mesaj silindi: " + messageId);
            return HaToKuSeProtocol.createOkResponse();
        } else {
            return HaToKuSeProtocol.createErrorResponse("Mesaj silinemedi veya bulunamadı: " + messageId);
        }
    }

    /**
     * Periyodik istatistik raporlayıcısını başlatır.
     */
    private void startStatsReporter() {
        statsScheduler = Executors.newScheduledThreadPool(1);
        statsScheduler.scheduleAtFixedRate(() -> {
            memberManager.printMemberStats();
        }, STATS_INTERVAL, STATS_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Sunucuyu kapatır.
     */
    public void shutdown() {
        running = false;

        if (statsScheduler != null) {
            statsScheduler.shutdown();
        }

        if (clientExecutor != null) {
            clientExecutor.shutdown();
        }

        if (clientServerSocket != null) {
            try {
                clientServerSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        if (grpcServer != null) {
            grpcServer.shutdown();
        }

        memberManager.shutdown();

        System.out.println("Lider sunucu kapatıldı.");
    }

    /**
     * Ana metod.
     */
    public static void main(String[] args) {
        int clientPort = DEFAULT_CLIENT_PORT;
        int grpcPort = DEFAULT_GRPC_PORT;
        int tolerance = 2;

        // Komut satırı argümanlarını işle
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--client-port":
                    if (i + 1 < args.length) {
                        clientPort = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--grpc-port":
                    if (i + 1 < args.length) {
                        grpcPort = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--tolerance":
                    if (i + 1 < args.length) {
                        tolerance = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--help":
                    printUsage();
                    return;
            }
        }

        // tolerance.conf dosyasından oku
        try {
            ConfigReader config = new ConfigReader();
            config.loadToleranceConfig();
            tolerance = config.getTolerance();
            System.out.println("[CONFIG] Tolerans değeri: " + tolerance);
        } catch (IOException e) {
            System.out.println(
                    "[CONFIG] Konfigürasyon dosyası okunamadı, varsayılan tolerans kullanılıyor: " + tolerance);
        }

        try {
            LeaderServer server = new LeaderServer(clientPort, grpcPort, tolerance);
            server.start();

            // Kapanma bekle
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Sunucu başlatma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("HaToKuSe Lider Sunucu");
        System.out.println("Kullanım: java com.hatokuse.leader.LeaderServer [seçenekler]");
        System.out.println("");
        System.out.println("Seçenekler:");
        System.out.println("  --client-port <port>  İstemci bağlantı portu (varsayılan: 5000)");
        System.out.println("  --grpc-port <port>    gRPC sunucu portu (varsayılan: 5001)");
        System.out.println("  --tolerance <n>       Hata tolerans değeri (varsayılan: tolerance.conf)");
        System.out.println("  --help                Bu yardım mesajını gösterir");
    }
}
