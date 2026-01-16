package com.hatokuse.client;

import com.hatokuse.protocol.HaToKuSeProtocol;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * HaToKuSe İstemci.
 * Text tabanlı TCP bağlantısı ile lidere bağlanır.
 */
public class HaToKuSeClient {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean connected = false;

    public HaToKuSeClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Lidere bağlanır.
     */
    public boolean connect() {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            System.out.println("Lidere bağlandı: " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Bağlantı hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * Bağlantıyı kapatır.
     */
    public void disconnect() {
        try {
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
            if (socket != null)
                socket.close();
            connected = false;
            System.out.println("Bağlantı kapatıldı.");
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * SET komutu gönderir.
     */
    public boolean set(String messageId, String message) {
        if (!connected) {
            System.err.println("Bağlantı yok!");
            return false;
        }

        String command = HaToKuSeProtocol.createSetCommand(messageId, message);
        writer.println(command);

        try {
            String response = reader.readLine();
            HaToKuSeProtocol.ParsedResponse parsed = HaToKuSeProtocol.parseResponse(response);

            if (parsed.isSuccess()) {
                System.out.println("OK");
                return true;
            } else {
                System.err.println("ERROR: " + parsed.getError());
                return false;
            }

        } catch (IOException e) {
            System.err.println("Okuma hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * GET komutu gönderir.
     */
    public String get(String messageId) {
        if (!connected) {
            System.err.println("Bağlantı yok!");
            return null;
        }

        String command = HaToKuSeProtocol.createGetCommand(messageId);
        writer.println(command);

        try {
            String response = reader.readLine();
            HaToKuSeProtocol.ParsedResponse parsed = HaToKuSeProtocol.parseResponse(response);

            if (parsed.isSuccess()) {
                String message = parsed.getMessage();
                System.out.println("OK " + message);
                return message;
            } else {
                System.err.println("ERROR: " + parsed.getError());
                return null;
            }

        } catch (IOException e) {
            System.err.println("Okuma hatası: " + e.getMessage());
            return null;
        }
    }

    /**
     * Ham komut gönderir.
     */
    public String sendRawCommand(String command) {
        if (!connected) {
            System.err.println("Bağlantı yok!");
            return null;
        }

        writer.println(command);

        try {
            return reader.readLine();
        } catch (IOException e) {
            System.err.println("Okuma hatası: " + e.getMessage());
            return null;
        }
    }

    /**
     * İnteraktif mod başlatır.
     */
    public void startInteractiveMode() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n========================================");
        System.out.println("    HaToKuSe İstemci - İnteraktif Mod");
        System.out.println("========================================");
        System.out.println("Komutlar:");
        System.out.println("  SET <id> <mesaj>  - Mesaj kaydet");
        System.out.println("  GET <id>          - Mesaj getir");
        System.out.println("  QUIT              - Çıkış");
        System.out.println("========================================\n");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (input.equalsIgnoreCase("QUIT") || input.equalsIgnoreCase("EXIT")) {
                System.out.println("Çıkılıyor...");
                break;
            }

            String response = sendRawCommand(input);
            if (response != null) {
                System.out.println(response);
            }
        }

        scanner.close();
    }

    /**
     * Toplu SET testi yapar.
     */
    public void batchSetTest(int count) {
        System.out.println("Toplu SET testi başlıyor: " + count + " mesaj");
        long startTime = System.currentTimeMillis();
        int successCount = 0;

        for (int i = 1; i <= count; i++) {
            String messageId = String.valueOf(i);
            String message = "Test mesajı #" + i + " - " + System.currentTimeMillis();

            if (set(messageId, message)) {
                successCount++;
            }

            // İlerleme göster
            if (i % 100 == 0) {
                System.out.println("İlerleme: " + i + "/" + count);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("\n========================================");
        System.out.println("Toplu SET testi tamamlandı");
        System.out.println("Toplam: " + count);
        System.out.println("Başarılı: " + successCount);
        System.out.println("Süre: " + duration + " ms");
        System.out.println("========================================\n");
    }

    /**
     * Ana metod.
     */
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        boolean interactive = true;
        int batchCount = 0;

        // Komut satırı argümanlarını işle
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host":
                    if (i + 1 < args.length) {
                        host = args[++i];
                    }
                    break;
                case "--port":
                    if (i + 1 < args.length) {
                        port = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--batch":
                    if (i + 1 < args.length) {
                        batchCount = Integer.parseInt(args[++i]);
                        interactive = false;
                    }
                    break;
                case "--help":
                    printUsage();
                    return;
            }
        }

        HaToKuSeClient client = new HaToKuSeClient(host, port);

        if (!client.connect()) {
            System.err.println("Bağlantı kurulamadı!");
            return;
        }

        try {
            if (batchCount > 0) {
                client.batchSetTest(batchCount);
            } else if (interactive) {
                client.startInteractiveMode();
            }
        } finally {
            client.disconnect();
        }
    }

    private static void printUsage() {
        System.out.println("HaToKuSe İstemci");
        System.out.println("Kullanım: java com.hatokuse.client.HaToKuSeClient [seçenekler]");
        System.out.println("");
        System.out.println("Seçenekler:");
        System.out.println("  --host <host>    Lider sunucu adresi (varsayılan: localhost)");
        System.out.println("  --port <port>    Lider istemci portu (varsayılan: 5000)");
        System.out.println("  --batch <count>  Toplu SET testi yap");
        System.out.println("  --help           Bu yardım mesajını gösterir");
    }
}
