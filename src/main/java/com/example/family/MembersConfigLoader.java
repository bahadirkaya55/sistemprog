package com.example.family;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * members.conf dosyasından üye bilgilerini okur.
 * Format (her satır):
 *   nodeId host port
 * Örn:
 *   n1 localhost 7000
 *   n2 localhost 7001
 */
public class MembersConfigLoader {

    private static final String DEFAULT_FILE = "members.conf";

    public static List<MemberConfig> load() {
        return load(Path.of(DEFAULT_FILE));
    }

    public static List<MemberConfig> load(Path path) {
        List<MemberConfig> result = new ArrayList<>();
        if (!Files.exists(path)) {
            System.err.println("members.conf bulunamadı, statik üye listesi boş kalacak.");
            return result;
        }
        try {
            for (String line : Files.readAllLines(path)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    System.err.println("members.conf satır formatı hatalı: " + line);
                    continue;
                }
                String nodeId = parts[0];
                String host = parts[1];
                int port;
                try {
                    port = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    System.err.println("members.conf port hatalı: " + line);
                    continue;
                }
                result.add(new MemberConfig(nodeId, host, port));
            }
        } catch (IOException e) {
            System.err.println("members.conf okunamadı: " + e.getMessage());
        }
        return result;
    }
}


