package com.example.family;

import com.example.family.FamilyReplicationServiceGrpc.FamilyReplicationServiceImplBase;
import io.grpc.stub.StreamObserver;

/**
 * Üye node üzerinde çalışacak gRPC servis implementasyonu.
 * Lider, ReplicateMessage ile mesajları gönderir; FetchMessage ile okur.
 */
public class ReplicationServiceImpl extends FamilyReplicationServiceImplBase {

    private final DiskStorage storage;

    public ReplicationServiceImpl(DiskStorage storage) {
        this.storage = storage;
    }

    @Override
    public void replicateMessage(ReplicateMessageRequest request,
                                 StreamObserver<ReplicateMessageResponse> responseObserver) {
        StoredMessage message = request.getMessage();
        storage.store(message.getId(), message.getBody());

        ReplicateMessageResponse response = ReplicateMessageResponse.newBuilder()
                .setSuccess(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void fetchMessage(FetchMessageRequest request,
                             StreamObserver<FetchMessageResponse> responseObserver) {
        String id = request.getId();
        var fetched = storage.fetch(id);

        FetchMessageResponse.Builder builder = FetchMessageResponse.newBuilder();
        if (fetched.isPresent()) {
            builder.setFound(true)
                    .setMessage(StoredMessage.newBuilder()
                            .setId(id)
                            .setBody(fetched.get())
                            .build());
        } else {
            builder.setFound(false);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void registerNode(RegisterNodeRequest request,
                             StreamObserver<RegisterNodeResponse> responseObserver) {
        // Üye tarafında register çağrısı zorunlu değil; kabul ederek cevaplıyoruz.
        RegisterNodeResponse response = RegisterNodeResponse.newBuilder()
                .setAccepted(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}


