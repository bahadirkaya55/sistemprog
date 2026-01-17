package com.hatokuse.member;

import com.hatokuse.proto.*;
import com.hatokuse.storage.DiskStorage;
import io.grpc.stub.StreamObserver;

/**
 * Üye gRPC servis implementasyonu.
 * Liderden gelen replikasyon ve sorgulama isteklerini işler.
 */
public class MemberServiceImpl extends MemberServiceGrpc.MemberServiceImplBase {

    private final DiskStorage storage;
    private final String memberId;

    public MemberServiceImpl(String memberId, DiskStorage storage) {
        this.memberId = memberId;
        this.storage = storage;
    }

    @Override
    public void replicateMessage(ReplicateRequest request, StreamObserver<ReplicateResponse> responseObserver) {
        String messageId = request.getMessageId();
        String content = request.getMessageContent();

        System.out.println("[" + memberId + "] Replikasyon isteği: " + messageId);

        boolean success = storage.saveMessage(messageId, content);

        ReplicateResponse response = ReplicateResponse.newBuilder()
                .setSuccess(success)
                .setErrorMessage(success ? "" : "Kayıt hatası")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getMessage(GetMessageRequest request, StreamObserver<GetMessageResponse> responseObserver) {
        String messageId = request.getMessageId();

        System.out.println("[" + memberId + "] Mesaj sorgusu: " + messageId);

        String content = storage.getMessage(messageId);

        GetMessageResponse.Builder builder = GetMessageResponse.newBuilder();

        if (content != null) {
            builder.setFound(true).setMessageContent(content);
        } else {
            builder.setFound(false).setErrorMessage("Mesaj bulunamadı");
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getMessageCount(MessageCountRequest request, StreamObserver<MessageCountResponse> responseObserver) {
        int count = storage.getMessageCount();

        MessageCountResponse response = MessageCountResponse.newBuilder()
                .setCount(count)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void healthCheck(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
        HealthCheckResponse response = HealthCheckResponse.newBuilder()
                .setHealthy(true)
                .setMessageCount(storage.getMessageCount())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteMessage(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        String messageId = request.getMessageId();

        System.out.println("[" + memberId + "] Silme isteği: " + messageId);

        boolean success = storage.deleteMessage(messageId);

        DeleteResponse response = DeleteResponse.newBuilder()
                .setSuccess(success)
                .setErrorMessage(success ? "" : "Mesaj silinemedi veya bulunamadı")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
