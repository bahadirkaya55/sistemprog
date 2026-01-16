package com.hatokuse.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Konfigürasyon dosyalarını okuyan sınıf.
 * tolerance.conf dosyasından hata tolerans değerini okur.
 */
public class ConfigReader {
    
    private static final String DEFAULT_TOLERANCE_FILE = "tolerance.conf";
    
    private final Map<String, String> config;
    
    public ConfigReader() {
        this.config = new HashMap<>();
    }
    
    /**
     * Varsayılan tolerance.conf dosyasını okur.
     */
    public void loadToleranceConfig() throws IOException {
        loadConfig(DEFAULT_TOLERANCE_FILE);
    }
    
    /**
     * Belirtilen konfigürasyon dosyasını okur.
     */
    public void loadConfig(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("Konfigürasyon dosyası bulunamadı: " + filePath);
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Yorum satırlarını ve boş satırları atla
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // key=value formatını parse et
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    config.put(key, value);
                }
            }
        }
    }
    
    /**
     * Hata tolerans değerini döner.
     * Bu değer, her mesajın kaç üyede replike edileceğini belirler.
     */
    public int getTolerance() {
        String value = config.get("tolerance");
        if (value == null) {
            System.out.println("[CONFIG] Tolerance değeri bulunamadı, varsayılan 2 kullanılıyor.");
            return 2;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.out.println("[CONFIG] Geçersiz tolerance değeri: " + value + ", varsayılan 2 kullanılıyor.");
            return 2;
        }
    }
    
    /**
     * Belirtilen anahtar için değeri döner.
     */
    public String getValue(String key) {
        return config.get(key);
    }
    
    /**
     * Belirtilen anahtar için değeri döner, bulunamazsa varsayılan değeri döner.
     */
    public String getValue(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }
    
    /**
     * Belirtilen anahtar için integer değeri döner.
     */
    public int getIntValue(String key, int defaultValue) {
        String value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
