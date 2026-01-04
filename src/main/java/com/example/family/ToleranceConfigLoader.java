package com.example.family;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * tolerance.conf dosyasından hata toleransı değerini okur.
 * Basit format: sadece tek satırda bir tamsayı (örn: 2 veya 3).
 */
public class ToleranceConfigLoader {

    private static final String DEFAULT_FILE = "tolerance.conf";
    private static final int DEFAULT_TOLERANCE = 2;

    public static ToleranceConfig loadOrDefault() {
        return loadOrDefault(Path.of(DEFAULT_FILE));
    }

    public static ToleranceConfig loadOrDefault(Path path) {
        try {
            if (Files.exists(path)) {
                String content = Files.readString(path).trim();
                int value = Integer.parseInt(content);
                if (value <= 0) {
                    System.err.println("tolerance.conf değeri pozitif olmalı, varsayılan kullanılacak: " + DEFAULT_TOLERANCE);
                    return new ToleranceConfig(DEFAULT_TOLERANCE);
                }
                return new ToleranceConfig(value);
            } else {
                System.err.println("tolerance.conf bulunamadı, varsayılan kullanılacak: " + DEFAULT_TOLERANCE);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("tolerance.conf okunurken hata, varsayılan kullanılacak: " + DEFAULT_TOLERANCE);
        }
        return new ToleranceConfig(DEFAULT_TOLERANCE);
    }
}


