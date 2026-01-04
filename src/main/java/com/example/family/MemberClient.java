package com.example.family;

import io.grpc.ManagedChannel;

import java.util.Optional;

/**
 * Liderin her bir üye ile konuşmak için kullandığı gRPC istemcisi.
 */
public class MemberClient {

    private final String nodeId;
    private final String host;
    private final int port;
    private final ManagedChannel channel;
    private final FamilyReplicationServiceGrpc.FamilyReplicationServiceBlockingStub stub;

    public MemberClient(String nodeId, String host, int port, ManagedChannel channel) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.channel = channel;
        this.stub = FamilyReplicationServiceGrpc.newBlockingStub(channel);
    }

    public String nodeId() {
        return nodeId;
    }

    public boolean replicate(String messageId, String body) {
        StoredMessage msg = StoredMessage.newBuilder()
                .setId(messageId)
                .setBody(body)
                .build();
        ReplicateMessageRequest request = ReplicateMessageRequest.newBuilder()
                .setMessage(msg)
                .build();
        try {
            ReplicateMessageResponse response = stub.replicateMessage(request);
            return response.getSuccess();
        } catch (Exception e) {
            System.err.printf("Replikasyon hatası (node=%s:%d, id=%s): %s%n", host, port, messageId, e.getMessage());
            return false;
        }
    }

    public Optional<String> fetch(String messageId) {
        FetchMessageRequest request = FetchMessageRequest.newBuilder()
                .setId(messageId)
                .build();
        try {
            FetchMessageResponse response = stub.fetchMessage(request);
            if (response.getFound()) {
                return Optional.of(response.getMessage().getBody());
            }
        } catch (Exception e) {
            System.err.printf("Fetch hatası (node=%s:%d, id=%s): %s%n", host, port, messageId, e.getMessage());
        }
        return Optional.empty();
    }

    public void shutdown() {
        channel.shutdownNow();
    }
}


