package com.hatokuse.leader;

import com.hatokuse.proto.*;
import io.grpc.stub.StreamObserver;

/**
 * Lider gRPC servis implementasyonu.
 * Üyelerden gelen kayıt ve heartbeat isteklerini işler.
 */
public class LeaderServiceImpl extends LeaderServiceGrpc.LeaderServiceImplBase {

    private final MemberManager memberManager;

    public LeaderServiceImpl(MemberManager memberManager) {
        this.memberManager = memberManager;
    }

    @Override
    public void registerMember(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        try {
            String memberId = request.getMemberId();
            String host = request.getHost();
            int port = request.getPort();

            System.out.println("[LEADER_SERVICE] Üye kayıt isteği: " + memberId + " (" + host + ":" + port + ")");

            boolean success = memberManager.registerMember(memberId, host, port);

            RegisterResponse response = RegisterResponse.newBuilder()
                    .setSuccess(success)
                    .setErrorMessage(success ? "" : "Üye zaten kayıtlı")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println(
                    "[LEADER_SERVICE] registerMember EXCEPTION: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void unregisterMember(UnregisterRequest request, StreamObserver<UnregisterResponse> responseObserver) {
        String memberId = request.getMemberId();

        System.out.println("[LEADER_SERVICE] Üye çıkış isteği: " + memberId);

        boolean success = memberManager.unregisterMember(memberId);

        UnregisterResponse response = UnregisterResponse.newBuilder()
                .setSuccess(success)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        String memberId = request.getMemberId();
        int messageCount = request.getMessageCount();

        memberManager.updateHeartbeat(memberId, messageCount);

        HeartbeatResponse response = HeartbeatResponse.newBuilder()
                .setAcknowledged(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
