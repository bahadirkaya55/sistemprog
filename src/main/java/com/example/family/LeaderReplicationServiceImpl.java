package com.example.family;

import com.example.family.FamilyReplicationServiceGrpc.FamilyReplicationServiceImplBase;
import io.grpc.stub.StreamObserver;

/**
 * Lider node üzerinde çalışacak gRPC servis implementasyonu.
 * RegisterNode RPC ile dinamik üye kayıtlarını kabul eder.
 */
public class LeaderReplicationServiceImpl extends FamilyReplicationServiceImplBase {

    private final LeaderState leaderState;

    public LeaderReplicationServiceImpl(LeaderState leaderState) {
        this.leaderState = leaderState;
    }

    @Override
    public void registerNode(RegisterNodeRequest request,
            StreamObserver<RegisterNodeResponse> responseObserver) {
        String nodeId = request.getNodeId();
        String host = request.getHost();
        int port = request.getPort();

        boolean success = leaderState.addDynamicMember(nodeId, host, port);

        RegisterNodeResponse response = RegisterNodeResponse.newBuilder()
                .setAccepted(success)
                .setError(success ? "" : "Kayıt başarısız")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void replicateMessage(ReplicateMessageRequest request,
            StreamObserver<ReplicateMessageResponse> responseObserver) {
        // Lider tarafında bu çağrı desteklenmiyor
        ReplicateMessageResponse response = ReplicateMessageResponse.newBuilder()
                .setSuccess(false)
                .setError("Lider node ReplicateMessage desteklemez")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void fetchMessage(FetchMessageRequest request,
            StreamObserver<FetchMessageResponse> responseObserver) {
        // Lider tarafında bu çağrı desteklenmiyor
        FetchMessageResponse response = FetchMessageResponse.newBuilder()
                .setFound(false)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
