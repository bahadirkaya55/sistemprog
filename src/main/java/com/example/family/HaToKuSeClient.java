package com.example.family;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HaToKuSe istemci uygulaması.
 * Lider node'a bağlanarak SET ve GET komutları gönderir.
 * 
 * Kullanım modları:
 * 
 * 1. İnteraktif mod:
 * java HaToKuSeClient [host] [port]
 * Örnek: java HaToKuSeClient localhost 5000
 * 
 * 2. Otomatik test modu (arka arkaya istek):
 * java HaToKuSeClient [host] [port] auto [mesaj_sayısı]
 * Örnek: java HaToKuSeClient localhost 5000 auto 1000
 * 
 * 3. GET test modu (rastgele okuma testi):
 * java HaToKuSeClient [host] [port] get-test [mesaj_sayısı]
 * Örnek: java HaToKuSeClient localhost 5000 get-test 100
 * 
 * Komutlar (interaktif modda):
 * SET <message_id> <message> - Mesaj kaydet
 * GET <message_id> - Mesaj oku
 * QUIT - Çıkış
 */
public class HaToKuSeClient {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                // args[1] might be a mode, not a port
                if (!args[1].equals("auto") && !args[1].equals("get-test")) {
                    System.err.println("Geçersiz port: " + args[1]);
                    return;
                }
            }
        }

        // Mod kontrolü
        String mode = "interactive";
        int messageCount = 1000;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("auto")) {
                mode = "auto";
                if (i + 1 < args.length) {
                    try {
                        messageCount = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else if (args[i].equals("get-test")) {
                mode = "get-test";
                if (i + 1 < args.length) {
                    try {
                        messageCount = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        switch (mode) {
            case "auto":
                runAutoTest(host, port, messageCount);
                break;
            case "get-test":
                runGetTest(host, port, messageCount);
                break;
            default:
                runInteractive(host, port);
        }
    }

    /**
     * Otomatik test modu: Arka arkaya SET komutları gönderir.
     * Performans ve dağıtım testi için kullanılır.
     */
    private static void runAutoTest(String host, int port, int messageCount) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           HaToKuSe OTOMATİK TEST MODU                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ Host: " + padRight(host, 55) + "║");
        System.out.println("║ Port: " + padRight(String.valueOf(port), 55) + "║");
        System.out.println("║ Mesaj Sayısı: " + padRight(String.valueOf(messageCount), 47) + "║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        try (Socket socket = new Socket(host, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("✓ Bağlantı başarılı!");
            System.out.println();
            System.out.println("SET işlemleri başlatılıyor...");
            System.out.println();

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            long startTime = System.currentTimeMillis();

            for (int i = 1; i <= messageCount; i++) {
                String messageId = "msg" + i;
                String messageBody = "Test mesajı #" + i + " - " + System.currentTimeMillis();
                String command = "SET " + messageId + " " + messageBody;

                out.println(command);
                String response = in.readLine();

                if (response != null && response.startsWith("OK")) {
                    successCount.incrementAndGet();
                } else {
                    errorCount.incrementAndGet();
                    System.err.println("  HATA [" + i + "]: " + response);
                }

                // İlerleme göstergesi
                if (i % 100 == 0 || i == messageCount) {
                    int percent = (i * 100) / messageCount;
                    System.out.print("\r  İlerleme: [" + getProgressBar(percent) + "] " + percent + "% (" + i + "/"
                            + messageCount + ")");
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            double messagesPerSecond = messageCount / (duration / 1000.0);

            System.out.println();
            System.out.println();
            System.out.println("╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                     TEST SONUÇLARI                           ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║ Toplam Mesaj: " + padRight(String.valueOf(messageCount), 47) + "║");
            System.out.println("║ Başarılı: " + padRight(String.valueOf(successCount.get()), 51) + "║");
            System.out.println("║ Başarısız: " + padRight(String.valueOf(errorCount.get()), 50) + "║");
            System.out.println("║ Süre: " + padRight(duration + " ms", 55) + "║");
            System.out.println("║ Hız: " + padRight(String.format("%.2f mesaj/saniye", messagesPerSecond), 56) + "║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");

        } catch (IOException e) {
            System.err.println("Bağlantı hatası: " + e.getMessage());
            System.err.println("Lider node'un çalıştığından emin olun!");
        }
    }

    /**
     * GET test modu: Önceden yazılmış mesajları okur.
     * Crash toleransı testi için kullanılır.
     */
    private static void runGetTest(String host, int port, int messageCount) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           HaToKuSe GET TEST MODU                             ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ Host: " + padRight(host, 55) + "║");
        System.out.println("║ Port: " + padRight(String.valueOf(port), 55) + "║");
        System.out.println("║ Mesaj Sayısı: " + padRight(String.valueOf(messageCount), 47) + "║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        try (Socket socket = new Socket(host, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("✓ Bağlantı başarılı!");
            System.out.println();
            System.out.println("GET işlemleri başlatılıyor...");
            System.out.println();

            AtomicInteger foundCount = new AtomicInteger(0);
            AtomicInteger notFoundCount = new AtomicInteger(0);
            long startTime = System.currentTimeMillis();

            for (int i = 1; i <= messageCount; i++) {
                String messageId = "msg" + i;
                String command = "GET " + messageId;

                out.println(command);
                String response = in.readLine();

                if (response != null && response.startsWith("OK")) {
                    foundCount.incrementAndGet();
                } else {
                    notFoundCount.incrementAndGet();
                }

                // İlerleme göstergesi
                if (i % 100 == 0 || i == messageCount) {
                    int percent = (i * 100) / messageCount;
                    System.out.print("\r  İlerleme: [" + getProgressBar(percent) + "] " + percent + "% (" + i + "/"
                            + messageCount + ")");
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            System.out.println();
            System.out.println();
            System.out.println("╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                     GET TEST SONUÇLARI                       ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║ Toplam Sorgu: " + padRight(String.valueOf(messageCount), 47) + "║");
            System.out.println("║ Bulunan: " + padRight(String.valueOf(foundCount.get()), 52) + "║");
            System.out.println("║ Bulunamayan: " + padRight(String.valueOf(notFoundCount.get()), 48) + "║");
            System.out.println("║ Süre: " + padRight(duration + " ms", 55) + "║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");

        } catch (IOException e) {
            System.err.println("Bağlantı hatası: " + e.getMessage());
        }
    }

    /**
     * İnteraktif mod: Manuel komut girişi.
     */
    private static void runInteractive(String host, int port) {
        System.out.println("HaToKuSe İstemci");
        System.out.println("================");
        System.out.println("Lider node'a bağlanılıyor: " + host + ":" + port);
        System.out.println();
        System.out.println("Kullanılabilir komutlar:");
        System.out.println("  SET <message_id> <message>  - Mesaj kaydet");
        System.out.println("  GET <message_id>            - Mesaj oku");
        System.out.println("  QUIT                        - Çıkış");
        System.out.println();

        try (Socket socket = new Socket(host, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)) {

            System.out.println("Bağlantı başarılı!");
            System.out.println();

            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.equalsIgnoreCase("QUIT") || line.equalsIgnoreCase("EXIT")) {
                    System.out.println("Çıkış yapılıyor...");
                    break;
                }

                // Komutu gönder
                out.println(line);

                // Yanıtı oku
                String response = in.readLine();
                if (response == null) {
                    System.err.println("Sunucu bağlantısı kapandı.");
                    break;
                }

                System.out.println("< " + response);
            }

        } catch (IOException e) {
            System.err.println("Bağlantı hatası: " + e.getMessage());
            System.err.println(
                    "Lider node'un çalıştığından emin olun: java -jar target/distributed-disk-register-0.0.1-SNAPSHOT.jar leader");
        }
    }

    private static String getProgressBar(int percent) {
        int filled = percent / 5;
        int empty = 20 - filled;
        return "█".repeat(filled) + "░".repeat(empty);
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
