package com.hatokuse.storage;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Mesajları diske kaydeden ve okuyan sınıf.
 * Her mesaj ayrı bir dosya olarak saklanır.
 * 
 * ÖZELLİKLER:
 * - Zero-copy IO (FileChannel ile)
 * - Buffered IO (BufferedOutputStream/BufferedInputStream)
 * - Memory-mapped IO (MappedByteBuffer)
 * - Unbuffered direct IO
 */
public class DiskStorage {

    // IO modları
    public enum IOMode {
        STANDARD, // Normal Files.write/read
        BUFFERED, // BufferedOutputStream/BufferedInputStream
        ZERO_COPY, // FileChannel ile transferTo/transferFrom
        MEMORY_MAPPED // MappedByteBuffer ile
    }

    private final Path storageDirectory;
    private final Map<String, String> cache;
    private int messageCount;
    private IOMode currentIOMode;

    // Buffer boyutu (8KB)
    private static final int BUFFER_SIZE = 8192;

    public DiskStorage(String storagePath) throws IOException {
        this(storagePath, IOMode.BUFFERED); // Varsayılan: Buffered IO
    }

    public DiskStorage(String storagePath, IOMode ioMode) throws IOException {
        this.storageDirectory = Paths.get(storagePath);
        this.cache = new ConcurrentHashMap<>();
        this.messageCount = 0;
        this.currentIOMode = ioMode;

        // Dizin yoksa oluştur
        if (!Files.exists(storageDirectory)) {
            Files.createDirectories(storageDirectory);
            System.out.println("[STORAGE] Dizin oluşturuldu: " + storageDirectory.toAbsolutePath());
        }

        System.out.println("[STORAGE] IO Modu: " + currentIOMode);

        // Mevcut dosyaları say
        countExistingMessages();
    }

    /**
     * IO modunu değiştirir.
     */
    public void setIOMode(IOMode mode) {
        this.currentIOMode = mode;
        System.out.println("[STORAGE] IO Modu değiştirildi: " + mode);
    }

    /**
     * Mevcut IO modunu döner.
     */
    public IOMode getIOMode() {
        return currentIOMode;
    }

    /**
     * Mevcut mesaj dosyalarını sayar.
     */
    private void countExistingMessages() throws IOException {
        if (Files.exists(storageDirectory)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDirectory, "*.msg")) {
                for (Path entry : stream) {
                    messageCount++;
                    // Önbelleğe yükle (buffered IO ile)
                    String messageId = entry.getFileName().toString().replace(".msg", "");
                    String content = readWithBufferedIO(entry);
                    cache.put(messageId, content);
                }
            }
        }
        System.out.println("[STORAGE] Mevcut mesaj sayısı: " + messageCount);
    }

    /**
     * Mesajı diske kaydeder (aktif IO moduna göre).
     */
    public synchronized boolean saveMessage(String messageId, String content) {
        try {
            Path filePath = storageDirectory.resolve(messageId + ".msg");

            switch (currentIOMode) {
                case BUFFERED:
                    writeWithBufferedIO(filePath, content);
                    break;
                case ZERO_COPY:
                    writeWithZeroCopy(filePath, content);
                    break;
                case MEMORY_MAPPED:
                    writeWithMemoryMapped(filePath, content);
                    break;
                default:
                    writeWithStandardIO(filePath, content);
            }

            // Önbelleğe ekle
            if (!cache.containsKey(messageId)) {
                messageCount++;
            }
            cache.put(messageId, content);

            System.out.println("[STORAGE] Mesaj kaydedildi (" + currentIOMode + "): " + messageId);
            return true;
        } catch (IOException e) {
            System.err.println("[STORAGE] Mesaj kaydetme hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mesajı diskten okur (aktif IO moduna göre).
     */
    public String getMessage(String messageId) {
        // Önce önbellekten kontrol et
        if (cache.containsKey(messageId)) {
            return cache.get(messageId);
        }

        try {
            Path filePath = storageDirectory.resolve(messageId + ".msg");
            if (Files.exists(filePath)) {
                String content;

                switch (currentIOMode) {
                    case BUFFERED:
                        content = readWithBufferedIO(filePath);
                        break;
                    case ZERO_COPY:
                        content = readWithZeroCopy(filePath);
                        break;
                    case MEMORY_MAPPED:
                        content = readWithMemoryMapped(filePath);
                        break;
                    default:
                        content = readWithStandardIO(filePath);
                }

                cache.put(messageId, content);
                return content;
            }
        } catch (IOException e) {
            System.err.println("[STORAGE] Mesaj okuma hatası: " + e.getMessage());
        }
        return null;
    }

    // ==================== STANDARD IO ====================

    private void writeWithStandardIO(Path filePath, String content) throws IOException {
        Files.write(filePath, content.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String readWithStandardIO(Path filePath) throws IOException {
        return new String(Files.readAllBytes(filePath));
    }

    // ==================== BUFFERED IO ====================

    /**
     * Buffered IO ile yazar - disk erişimini optimize eder.
     */
    private void writeWithBufferedIO(Path filePath, String content) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filePath.toFile()), BUFFER_SIZE)) {
            bos.write(content.getBytes());
            bos.flush();
        }
    }

    /**
     * Buffered IO ile okur - disk erişimini optimize eder.
     */
    private String readWithBufferedIO(Path filePath) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(filePath.toFile()), BUFFER_SIZE)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toString();
        }
    }

    // ==================== ZERO-COPY IO (FileChannel) ====================

    /**
     * Zero-copy IO ile yazar - kernel bypass ile hızlı transfer.
     * FileChannel.transferFrom kullanarak CPU kullanımını minimize eder.
     */
    private void writeWithZeroCopy(Path filePath, String content) throws IOException {
        byte[] data = content.getBytes();

        try (FileChannel destChannel = FileChannel.open(filePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            // ByteBuffer'dan doğrudan channel'a yaz
            ByteBuffer buffer = ByteBuffer.wrap(data);
            while (buffer.hasRemaining()) {
                destChannel.write(buffer);
            }
            destChannel.force(true); // fsync - veri güvenliği
        }
    }

    /**
     * Zero-copy IO ile okur - kernel bypass ile hızlı transfer.
     */
    private String readWithZeroCopy(Path filePath) throws IOException {
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            int size = (int) channel.size();
            ByteBuffer buffer = ByteBuffer.allocateDirect(size); // Direct buffer - heap dışı

            while (buffer.hasRemaining()) {
                channel.read(buffer);
            }

            buffer.flip();
            byte[] data = new byte[size];
            buffer.get(data);
            return new String(data);
        }
    }

    // ==================== MEMORY-MAPPED IO ====================

    /**
     * Memory-mapped IO ile yazar - dosyayı belleğe map'ler.
     * Çok büyük dosyalar için en verimli yöntem.
     */
    private void writeWithMemoryMapped(Path filePath, String content) throws IOException {
        byte[] data = content.getBytes();

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "rw");
                FileChannel channel = raf.getChannel()) {

            // Dosyayı belleğe map'le
            MappedByteBuffer mappedBuffer = channel.map(
                    FileChannel.MapMode.READ_WRITE, 0, data.length);

            // Doğrudan belleğe yaz
            mappedBuffer.put(data);
            mappedBuffer.force(); // Disk'e flush
        }
    }

    /**
     * Memory-mapped IO ile okur - dosyayı belleğe map'ler.
     */
    private String readWithMemoryMapped(Path filePath) throws IOException {
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            int size = (int) channel.size();

            // Dosyayı belleğe map'le (read-only)
            MappedByteBuffer mappedBuffer = channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, size);

            byte[] data = new byte[size];
            mappedBuffer.get(data);
            return new String(data);
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Mesajın mevcut olup olmadığını kontrol eder.
     */
    public boolean hasMessage(String messageId) {
        if (cache.containsKey(messageId)) {
            return true;
        }
        Path filePath = storageDirectory.resolve(messageId + ".msg");
        return Files.exists(filePath);
    }

    /**
     * Toplam mesaj sayısını döner.
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * Storage dizinini döner.
     */
    public String getStoragePath() {
        return storageDirectory.toAbsolutePath().toString();
    }

    /**
     * Mesajı siler.
     */
    public synchronized boolean deleteMessage(String messageId) {
        try {
            Path filePath = storageDirectory.resolve(messageId + ".msg");
            if (Files.deleteIfExists(filePath)) {
                cache.remove(messageId);
                messageCount--;
                return true;
            }
        } catch (IOException e) {
            System.err.println("[STORAGE] Mesaj silme hatası: " + e.getMessage());
        }
        return false;
    }

    /**
     * IO modlarının performans testini yapar.
     */
    public static void benchmarkIOModes(String testDir, int iterations) throws IOException {
        System.out.println("\n========== IO MODU PERFORMANS TESTİ ==========");
        System.out.println("İterasyon: " + iterations);

        String testContent = "Bu bir test mesajıdır. ".repeat(100); // ~2.3KB

        for (IOMode mode : IOMode.values()) {
            DiskStorage storage = new DiskStorage(testDir + "/" + mode.name(), mode);

            // Yazma testi
            long startWrite = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                storage.saveMessage("test_" + i, testContent);
            }
            long writeTime = (System.nanoTime() - startWrite) / 1_000_000;

            // Cache temizle
            storage.cache.clear();

            // Okuma testi
            long startRead = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                storage.getMessage("test_" + i);
            }
            long readTime = (System.nanoTime() - startRead) / 1_000_000;

            System.out.printf("[%s] Yazma: %d ms, Okuma: %d ms%n",
                    mode.name(), writeTime, readTime);

            // Temizlik
            for (int i = 0; i < iterations; i++) {
                storage.deleteMessage("test_" + i);
            }
        }
        System.out.println("==============================================\n");
    }
}
