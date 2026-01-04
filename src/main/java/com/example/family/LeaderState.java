package com.example.family;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Liderin, üyeler ve mesajlar ile ilgili tuttuğu tüm durum bilgisi.
 * - Üye listesi ve gRPC stub'ları
 * - Mesaj -> üye listesi eşlemesi
 * - Üye başına kaç mesaj saklandığı
 * - Leader'in kendi DiskStorage'ı
 */
public class LeaderState {

    private final DiskStorage leaderStorage;
    private final Map<String, MemberClient> membersById = new ConcurrentHashMap<>();
    private final List<MemberClient> memberList = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, List<String>> messageToMembers = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> memberMessageCounts = new ConcurrentHashMap<>();
    private final AtomicInteger rrIndex = new AtomicInteger(0);

    public LeaderState(DiskStorage leaderStorage, List<MemberConfig> memberConfigs) {
        this.leaderStorage = leaderStorage;
        for (MemberConfig cfg : memberConfigs) {
            addMember(cfg);
        }
    }

    private void addMember(MemberConfig cfg) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(cfg.host(), cfg.port())
                .usePlaintext()
                .build();

        MemberClient client = new MemberClient(
                cfg.nodeId(),
                cfg.host(),
                cfg.port(),
                channel);
        membersById.put(cfg.nodeId(), client);
        memberList.add(client);
        memberMessageCounts.put(cfg.nodeId(), new AtomicInteger(0));
    }

    /**
     * Dinamik olarak yeni bir üye ekler (runtime'da aileye katılım).
     * 
     * @param nodeId üye ID
     * @param host   üye host adresi
     * @param port   üye gRPC portu
     * @return true eğer başarıyla eklendiyse
     */
    public boolean addDynamicMember(String nodeId, String host, int port) {
        if (membersById.containsKey(nodeId)) {
            System.out.println("Üye zaten kayıtlı: " + nodeId);
            return true; // Zaten kayıtlı, sorun yok
        }

        MemberConfig cfg = new MemberConfig(nodeId, host, port);
        addMember(cfg);
        System.out.println("Yeni üye dinamik olarak eklendi: " + nodeId + " (" + host + ":" + port + ")");
        return true;
    }

    /**
     * Dinamik olarak bir üyeyi kaldırır (aileden çıkış).
     * 
     * @param nodeId kaldırılacak üye ID
     * @return true eğer başarıyla kaldırıldıysa
     */
    public boolean removeDynamicMember(String nodeId) {
        MemberClient client = membersById.remove(nodeId);
        if (client == null) {
            return false;
        }
        memberList.remove(client);
        memberMessageCounts.remove(nodeId);
        client.shutdown();
        System.out.println("Üye aileden çıkarıldı: " + nodeId);
        return true;
    }

    public void shutdown() {
        for (MemberClient client : memberList) {
            client.shutdown();
        }
    }

    public int leaderTotalMessages() {
        return leaderStorage.messageCount();
    }

    public Map<String, Integer> memberMessageCountsSnapshot() {
        Map<String, Integer> snapshot = new ConcurrentHashMap<>();
        for (Map.Entry<String, AtomicInteger> e : memberMessageCounts.entrySet()) {
            snapshot.put(e.getKey(), e.getValue().get());
        }
        return snapshot;
    }

    /**
     * SET isteği için:
     * - Mesajı lider diskine yazar.
     * - replicationFactor kadar üyeye gRPC ile gönderir.
     */
    public boolean handleSet(String messageId, String body, int replicationFactor) {
        leaderStorage.store(messageId, body);

        List<MemberClient> selected = selectMembers(replicationFactor);
        if (selected.isEmpty()) {
            System.err.println("Replikasyon için hiç üye yok!");
            return false;
        }

        List<String> storedOn = new ArrayList<>();
        for (MemberClient client : selected) {
            boolean ok = client.replicate(messageId, body);
            if (ok) {
                storedOn.add(client.nodeId());
                memberMessageCounts.get(client.nodeId()).incrementAndGet();
            }
        }

        if (storedOn.isEmpty()) {
            System.err.println("Mesaj hiçbir üyeye yazılamadı: " + messageId);
            return false;
        }

        messageToMembers.put(messageId, storedOn);
        return true;
    }

    /**
     * GET isteği için:
     * - Önce lider diskinden dene.
     * - Bulunamazsa, daha önce kaydeden üyelerden ayakta olan birinden gRPC ile
     * çek.
     */
    public Optional<String> handleGet(String messageId) {
        var local = leaderStorage.fetch(messageId);
        if (local.isPresent()) {
            return local;
        }

        List<String> nodeIds = messageToMembers.get(messageId);
        if (nodeIds == null || nodeIds.isEmpty()) {
            return Optional.empty();
        }

        // Crash toleransı için üyeleri rastgele sırayla dene
        List<String> copy = new ArrayList<>(nodeIds);
        Collections.shuffle(copy, ThreadLocalRandom.current());

        for (String nodeId : copy) {
            MemberClient client = membersById.get(nodeId);
            if (client == null) {
                continue;
            }
            Optional<String> fetched = client.fetch(messageId);
            if (fetched.isPresent()) {
                return fetched;
            }
        }
        return Optional.empty();
    }

    private List<MemberClient> selectMembers(int replicationFactor) {
        List<MemberClient> snapshot;
        synchronized (memberList) {
            snapshot = new ArrayList<>(memberList);
        }
        if (snapshot.isEmpty()) {
            return Collections.emptyList();
        }
        int n = snapshot.size();
        int r = Math.min(replicationFactor, n);

        List<MemberClient> result = new ArrayList<>(r);
        int start = Math.abs(rrIndex.getAndIncrement());

        for (int i = 0; i < r; i++) {
            int idx = (start + i) % n;
            result.add(snapshot.get(idx));
        }
        return result;
    }
}
