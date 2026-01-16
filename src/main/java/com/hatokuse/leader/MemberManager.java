package com.hatokuse.leader;

import com.hatokuse.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Üye yönetimi sınıfı.
 * Dinamik üye kayıt/çıkış, yük dengeleme ve mesaj-üye eşleştirmesi yapar.
 */
public class MemberManager {

    // Kayıtlı üyeler
    private final ConcurrentHashMap<String, MemberInfo> members;

    // Mesaj ID -> Üye listesi eşleştirmesi (hangi mesaj hangi üyelerde var)
    private final ConcurrentHashMap<String, Set<String>> messageToMembers;

    // Yük dengeleme için grup sayacı
    private final AtomicInteger groupCounter;

    // Hata tolerans değeri
    private final int tolerance;

    // Heartbeat timeout (ms)
    private static final long HEARTBEAT_TIMEOUT = 10000;

    // Health check scheduler
    private final ScheduledExecutorService scheduler;

    public MemberManager(int tolerance) {
        this.tolerance = tolerance;
        this.members = new ConcurrentHashMap<>();
        this.messageToMembers = new ConcurrentHashMap<>();
        this.groupCounter = new AtomicInteger(0);
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Periyodik health check başlat
        startHealthCheck();
    }

    /**
     * Yeni üye kaydeder.
     */
    public synchronized boolean registerMember(String memberId, String host, int port) {
        if (members.containsKey(memberId)) {
            System.out.println("[MEMBER_MANAGER] Üye zaten kayıtlı: " + memberId);
            return false;
        }

        MemberInfo info = new MemberInfo(memberId, host, port);
        members.put(memberId, info);
        System.out.println("[MEMBER_MANAGER] Yeni üye kaydedildi: " + memberId + " (" + host + ":" + port + ")");
        System.out.println("[MEMBER_MANAGER] Toplam üye sayısı: " + members.size());
        return true;
    }

    /**
     * Üyeyi sistemden çıkarır.
     */
    public synchronized boolean unregisterMember(String memberId) {
        MemberInfo info = members.remove(memberId);
        if (info != null) {
            info.closeChannel();
            System.out.println("[MEMBER_MANAGER] Üye silindi: " + memberId);
            return true;
        }
        return false;
    }

    /**
     * Heartbeat günceller.
     */
    public void updateHeartbeat(String memberId, int messageCount) {
        MemberInfo info = members.get(memberId);
        if (info != null) {
            info.updateHeartbeat(messageCount);
        }
    }

    /**
     * Mesaj için replikasyon yapılacak üyeleri seçer.
     * Round-robin grup bazlı yük dengeleme uygular.
     */
    public List<MemberInfo> selectMembersForReplication() {
        List<MemberInfo> activeMembers = getActiveMembers();

        if (activeMembers.size() < tolerance) {
            System.out.println("[MEMBER_MANAGER] UYARI: Aktif üye sayısı toleranstan az! " +
                    "Aktif: " + activeMembers.size() + ", Tolerans: " + tolerance);
            return activeMembers; // Mevcut tüm üyelere gönder
        }

        // Round-robin grup seçimi
        int groupIndex = groupCounter.getAndIncrement();
        int totalGroups = activeMembers.size() / tolerance;

        if (totalGroups == 0) {
            return activeMembers;
        }

        int selectedGroup = groupIndex % totalGroups;
        int startIndex = selectedGroup * tolerance;

        List<MemberInfo> selectedMembers = new ArrayList<>();
        for (int i = 0; i < tolerance && (startIndex + i) < activeMembers.size(); i++) {
            selectedMembers.add(activeMembers.get(startIndex + i));
        }

        return selectedMembers;
    }

    /**
     * Mesajı belirtilen üyelere replike eder.
     * 
     * @return Başarılı replikasyon sayısı
     */
    public int replicateMessage(String messageId, String content, List<MemberInfo> targetMembers) {
        int successCount = 0;
        Set<String> successMembers = ConcurrentHashMap.newKeySet();

        for (MemberInfo member : targetMembers) {
            try {
                MemberServiceGrpc.MemberServiceBlockingStub stub = member.getStub();

                ReplicateRequest request = ReplicateRequest.newBuilder()
                        .setMessageId(messageId)
                        .setMessageContent(content)
                        .build();

                ReplicateResponse response = stub.replicateMessage(request);

                if (response.getSuccess()) {
                    successCount++;
                    successMembers.add(member.getMemberId());
                    System.out.println(
                            "[MEMBER_MANAGER] Mesaj replike edildi: " + messageId + " -> " + member.getMemberId());
                } else {
                    System.err.println("[MEMBER_MANAGER] Replikasyon başarısız: " + member.getMemberId() + " - "
                            + response.getErrorMessage());
                }

            } catch (StatusRuntimeException e) {
                System.err.println(
                        "[MEMBER_MANAGER] Üye iletişim hatası: " + member.getMemberId() + " - " + e.getStatus());
                member.markUnhealthy();
            }
        }

        // Mesaj-üye eşleştirmesini kaydet
        if (!successMembers.isEmpty()) {
            messageToMembers.put(messageId, successMembers);
        }

        return successCount;
    }

    /**
     * Mesajı üyelerden getirir.
     */
    public String getMessageFromMembers(String messageId) {
        Set<String> memberIds = messageToMembers.get(messageId);

        if (memberIds == null || memberIds.isEmpty()) {
            System.out.println("[MEMBER_MANAGER] Mesaj için üye bulunamadı: " + messageId);
            return null;
        }

        // Üyelerden sırayla dene
        for (String memberId : memberIds) {
            MemberInfo member = members.get(memberId);

            if (member == null || !member.isHealthy()) {
                System.out.println("[MEMBER_MANAGER] Üye erişilemez, sonraki deneniyor: " + memberId);
                continue;
            }

            try {
                MemberServiceGrpc.MemberServiceBlockingStub stub = member.getStub();

                GetMessageRequest request = GetMessageRequest.newBuilder()
                        .setMessageId(messageId)
                        .build();

                GetMessageResponse response = stub.getMessage(request);

                if (response.getFound()) {
                    System.out.println("[MEMBER_MANAGER] Mesaj alındı: " + messageId + " <- " + memberId);
                    return response.getMessageContent();
                }

            } catch (StatusRuntimeException e) {
                System.err.println("[MEMBER_MANAGER] Üye iletişim hatası: " + memberId + " - " + e.getStatus());
                member.markUnhealthy();
            }
        }

        System.out.println("[MEMBER_MANAGER] Mesaj hiçbir üyeden alınamadı: " + messageId);
        return null;
    }

    /**
     * Mesajı tüm üyelerden siler.
     */
    public boolean deleteMessageFromMembers(String messageId) {
        Set<String> memberIds = messageToMembers.get(messageId);

        // Eğer mapping yoksa, tüm aktif üyelerde dene (lider restart durumu için)
        Collection<MemberInfo> targetMembers;
        if (memberIds == null || memberIds.isEmpty()) {
            System.out.println("[MEMBER_MANAGER] Mesaj mapping bulunamadı, tüm üyelerde denenecek: " + messageId);
            targetMembers = members.values();
        } else {
            targetMembers = new ArrayList<>();
            for (String mid : memberIds) {
                MemberInfo m = members.get(mid);
                if (m != null) {
                    ((ArrayList<MemberInfo>) targetMembers).add(m);
                }
            }
        }

        int successCount = 0;

        // Üyelerden sil
        for (MemberInfo member : targetMembers) {
            if (member == null || !member.isHealthy()) {
                continue;
            }

            try {
                MemberServiceGrpc.MemberServiceBlockingStub stub = member.getStub();

                DeleteRequest request = DeleteRequest.newBuilder()
                        .setMessageId(messageId)
                        .build();

                DeleteResponse response = stub.deleteMessage(request);

                if (response.getSuccess()) {
                    successCount++;
                    System.out.println("[MEMBER_MANAGER] Mesaj silindi: " + messageId + " <- " + member.getMemberId());
                }

            } catch (StatusRuntimeException e) {
                System.err.println(
                        "[MEMBER_MANAGER] Üye iletişim hatası: " + member.getMemberId() + " - " + e.getStatus());
                member.markUnhealthy();
            }
        }

        // Mesaj-üye eşleştirmesini kaldır
        if (successCount > 0) {
            messageToMembers.remove(messageId);
            return true;
        }

        return false;
    }

    /**
     * Aktif (sağlıklı) üyeleri döner.
     */
    public List<MemberInfo> getActiveMembers() {
        List<MemberInfo> active = new ArrayList<>();
        for (MemberInfo member : members.values()) {
            if (member.isHealthy()) {
                active.add(member);
            }
        }
        // Tutarlı sıralama için ID'ye göre sırala
        active.sort(Comparator.comparing(MemberInfo::getMemberId));
        return active;
    }

    /**
     * Tüm üyeleri döner.
     */
    public Collection<MemberInfo> getAllMembers() {
        return members.values();
    }

    /**
     * Üye sayısını döner.
     */
    public int getMemberCount() {
        return members.size();
    }

    /**
     * Aktif üye sayısını döner.
     */
    public int getActiveMemberCount() {
        return (int) members.values().stream().filter(MemberInfo::isHealthy).count();
    }

    /**
     * Tüm üyelerin mesaj sayısını yazdırır.
     */
    public void printMemberStats() {
        System.out.println("\n========== ÜYE İSTATİSTİKLERİ ==========");
        System.out.println("Toplam üye: " + members.size());
        System.out.println("Aktif üye: " + getActiveMemberCount());
        System.out.println("Toplam mesaj kayıtları: " + messageToMembers.size());

        for (MemberInfo member : members.values()) {
            String status = member.isHealthy() ? "AKTIF" : "INACTIVE";
            System.out.println("  - " + member.getMemberId() + ": " +
                    member.getMessageCount() + " mesaj [" + status + "]");
        }
        System.out.println("==========================================\n");
    }

    /**
     * Periyodik health check başlatır.
     */
    private void startHealthCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (MemberInfo member : members.values()) {
                if (member.isHealthy() && (now - member.getLastHeartbeat()) > HEARTBEAT_TIMEOUT) {
                    System.out.println("[HEALTH_CHECK] Üye timeout: " + member.getMemberId());
                    member.markUnhealthy();
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * Manager'ı kapatır.
     */
    public void shutdown() {
        scheduler.shutdown();
        for (MemberInfo member : members.values()) {
            member.closeChannel();
        }
    }

    /**
     * Üye bilgisi sınıfı.
     */
    public static class MemberInfo {
        private final String memberId;
        private final String host;
        private final int port;
        private ManagedChannel channel;
        private MemberServiceGrpc.MemberServiceBlockingStub stub;
        private volatile long lastHeartbeat;
        private volatile int messageCount;
        private volatile boolean healthy;

        public MemberInfo(String memberId, String host, int port) {
            this.memberId = memberId;
            this.host = host;
            this.port = port;
            this.lastHeartbeat = System.currentTimeMillis();
            this.messageCount = 0;
            this.healthy = true;
            initChannel();
        }

        private void initChannel() {
            // NettyChannelBuilder ile explicit InetSocketAddress kullan
            java.net.InetSocketAddress address = new java.net.InetSocketAddress(host, port);
            this.channel = io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder.forAddress(address)
                    .usePlaintext()
                    .build();
            this.stub = MemberServiceGrpc.newBlockingStub(channel);
        }

        public MemberServiceGrpc.MemberServiceBlockingStub getStub() {
            if (channel == null || channel.isShutdown()) {
                initChannel();
            }
            return stub;
        }

        public void updateHeartbeat(int msgCount) {
            this.lastHeartbeat = System.currentTimeMillis();
            this.messageCount = msgCount;
            this.healthy = true;
        }

        public void markUnhealthy() {
            this.healthy = false;
        }

        public void closeChannel() {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown();
            }
        }

        // Getters
        public String getMemberId() {
            return memberId;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public long getLastHeartbeat() {
            return lastHeartbeat;
        }

        public int getMessageCount() {
            return messageCount;
        }

        public boolean isHealthy() {
            return healthy;
        }
    }
}
