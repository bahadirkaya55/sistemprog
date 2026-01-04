package com.example.family;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * HaToKuSe text protokolü:
 *
 * SET <message_id> <message>
 * GET <message_id>
 *
 * Şimdilik sadece komutu ayrıştıran ve iskelet cevap dönen bir handler.
 * Gerçek dağıtık kayıt/okuma ve replikasyon mantığı daha sonra doldurulacak.
 */
public class LeaderTextClientHandler implements Runnable {

    private final Socket socket;
    private final LeaderState state;
    private final ToleranceConfig toleranceConfig;

    public LeaderTextClientHandler(Socket socket, LeaderState state, ToleranceConfig toleranceConfig) {
        this.socket = socket;
        this.state = state;
        this.toleranceConfig = toleranceConfig;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(" ", 3);
                String command = parts[0].toUpperCase();

                switch (command) {
                    case "SET" -> handleSet(parts, writer);
                    case "GET" -> handleGet(parts, writer);
                    default -> writer.println("ERROR Unknown command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void handleSet(String[] parts, PrintWriter writer) {
        if (parts.length < 3) {
            writer.println("ERROR Usage: SET <message_id> <message>");
            return;
        }
        String id = parts[1];
        String message = parts[2];

        boolean ok = state.handleSet(id, message, toleranceConfig.tolerance());
        if (ok) {
            writer.println("OK");
        } else {
            writer.println("ERROR ReplicationFailed");
        }
    }

    private void handleGet(String[] parts, PrintWriter writer) {
        if (parts.length < 2) {
            writer.println("ERROR Usage: GET <message_id>");
            return;
        }
        String id = parts[1];

        var fetched = state.handleGet(id);
        if (fetched.isPresent()) {
            writer.println("OK " + fetched.get());
        } else {
            writer.println("ERROR NotFound");
        }
    }
}


