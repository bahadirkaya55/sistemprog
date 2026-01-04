package com.example.family;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Farklı I/O modlarını destekleyen dosya tabanlı depolama.
 * Her mesajı <baseDir>/<messageId>.txt dosyasına yazar ve indeksini bellekte
 * tutar.
 * 
 * Desteklenen modlar:
 * - BUFFERED: BufferedOutputStream ile tamponlanmış yazma
 * - UNBUFFERED: Direct FileOutputStream ile tamponsuz yazma
 * - ZERO_COPY: FileChannel.transferFrom ile zero-copy yazma
 * - MEMORY_MAPPED: MappedByteBuffer ile bellek eşlemeli yazma
 */
public class DiskStorage {

    public enum WriteMode {
        BUFFERED, // Tamponlanmış I/O (varsayılan, performanslı)
        UNBUFFERED, // Tamponsuz doğrudan I/O
        ZERO_COPY, // Zero-copy prensibi ile FileChannel
        MEMORY_MAPPED // Memory-mapped file I/O
    }

    private final Path baseDir;
    private final WriteMode writeMode;
    private final ConcurrentMap<String, Path> index = new ConcurrentHashMap<>();

    public DiskStorage(Path baseDir) {
        this(baseDir, WriteMode.BUFFERED);
    }

    public DiskStorage(Path baseDir, WriteMode writeMode) {
        this.baseDir = baseDir;
        this.writeMode = writeMode;
        ensureDir();
        System.out.println("DiskStorage başlatıldı - Mod: " + writeMode + ", Dizin: " + baseDir);
    }

    public WriteMode getWriteMode() {
        return writeMode;
    }

    public void store(String messageId, String body) {
        Path target = baseDir.resolve(messageId + ".txt");
        byte[] data = body.getBytes(StandardCharsets.UTF_8);

        try {
            switch (writeMode) {
                case BUFFERED:
                    storeBuffered(target, data);
                    break;
                case UNBUFFERED:
                    storeUnbuffered(target, data);
                    break;
                case ZERO_COPY:
                    storeZeroCopy(target, data);
                    break;
                case MEMORY_MAPPED:
                    storeMemoryMapped(target, data);
                    break;
            }
            index.put(messageId, target);
        } catch (IOException e) {
            throw new RuntimeException("Mesaj diske yazılırken hata (" + writeMode + "): " + messageId, e);
        }
    }

    /**
     * BUFFERED: BufferedOutputStream ile tamponlanmış yazma.
     * Küçük yazımları birleştirerek disk I/O'yu azaltır.
     */
    private void storeBuffered(Path target, byte[] data) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(target.toFile()), 8192)) {
            bos.write(data);
            bos.flush();
        }
    }

    /**
     * UNBUFFERED: Doğrudan FileOutputStream ile tamponsuz yazma.
     * Her yazma işlemi anında diske gider, tampon yok.
     */
    private void storeUnbuffered(Path target, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(target.toFile())) {
            fos.write(data);
            fos.getFD().sync(); // Veriyi hemen diske yaz
        }
    }

    /**
     * ZERO_COPY: FileChannel ile zero-copy prensibi.
     * Kullanıcı alanı kopyalamasını minimize eder.
     * ByteBuffer.wrap ile doğrudan kanal yazımı yapılır.
     */
    private void storeZeroCopy(Path target, byte[] data) throws IOException {
        try (FileChannel channel = FileChannel.open(target,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            ByteBuffer buffer = ByteBuffer.wrap(data);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true); // Metadata dahil diske sync
        }
    }

    /**
     * MEMORY_MAPPED: MappedByteBuffer ile bellek eşlemeli yazma.
     * Dosya doğrudan belleğe haritalanır, OS sayfa önbelleği kullanılır.
     */
    private void storeMemoryMapped(Path target, byte[] data) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(target.toFile(), "rw");
                FileChannel channel = raf.getChannel()) {

            MappedByteBuffer mappedBuffer = channel.map(
                    FileChannel.MapMode.READ_WRITE, 0, data.length);

            mappedBuffer.put(data);
            mappedBuffer.force(); // Değişiklikleri diske yaz
        }
    }

    public Optional<String> fetch(String messageId) {
        Path path = index.get(messageId);
        if (path == null || !Files.exists(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public int messageCount() {
        return index.size();
    }

    private void ensureDir() {
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Depo dizini oluşturulamadı: " + baseDir, e);
        }
    }
}
