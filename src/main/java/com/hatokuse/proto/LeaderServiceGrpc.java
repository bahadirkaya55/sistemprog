package com.hatokuse.proto;

import io.grpc.*;
import io.grpc.stub.*;

/**
 * Leader Service gRPC - Üyeden lidere haberleşme
 */
public final class LeaderServiceGrpc {

    private LeaderServiceGrpc() {
    }

    public static final String SERVICE_NAME = "hatokuse.LeaderService";

    // Method descriptors
    private static final MethodDescriptor<RegisterRequest, RegisterResponse> METHOD_REGISTER_MEMBER = MethodDescriptor
            .<RegisterRequest, RegisterResponse>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(SERVICE_NAME + "/RegisterMember")
            .setRequestMarshaller(new RegisterRequestMarshaller())
            .setResponseMarshaller(new RegisterResponseMarshaller())
            .build();

    private static final MethodDescriptor<UnregisterRequest, UnregisterResponse> METHOD_UNREGISTER_MEMBER = MethodDescriptor
            .<UnregisterRequest, UnregisterResponse>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(SERVICE_NAME + "/UnregisterMember")
            .setRequestMarshaller(new UnregisterRequestMarshaller())
            .setResponseMarshaller(new UnregisterResponseMarshaller())
            .build();

    private static final MethodDescriptor<HeartbeatRequest, HeartbeatResponse> METHOD_HEARTBEAT = MethodDescriptor
            .<HeartbeatRequest, HeartbeatResponse>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(SERVICE_NAME + "/Heartbeat")
            .setRequestMarshaller(new HeartbeatRequestMarshaller())
            .setResponseMarshaller(new HeartbeatResponseMarshaller())
            .build();

    /**
     * Blocking Stub - Senkron çağrılar için
     */
    public static LeaderServiceBlockingStub newBlockingStub(Channel channel) {
        return new LeaderServiceBlockingStub(channel);
    }

    /**
     * Servis implementasyonu için base class
     */
    public static abstract class LeaderServiceImplBase implements BindableService {

        public void registerMember(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_REGISTER_MEMBER, responseObserver);
        }

        public void unregisterMember(UnregisterRequest request, StreamObserver<UnregisterResponse> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_UNREGISTER_MEMBER, responseObserver);
        }

        public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_HEARTBEAT, responseObserver);
        }

        @Override
        public ServerServiceDefinition bindService() {
            return ServerServiceDefinition.builder(SERVICE_NAME)
                    .addMethod(METHOD_REGISTER_MEMBER, ServerCalls.asyncUnaryCall(
                            (request, observer) -> registerMember(request, observer)))
                    .addMethod(METHOD_UNREGISTER_MEMBER, ServerCalls.asyncUnaryCall(
                            (request, observer) -> unregisterMember(request, observer)))
                    .addMethod(METHOD_HEARTBEAT, ServerCalls.asyncUnaryCall(
                            (request, observer) -> heartbeat(request, observer)))
                    .build();
        }
    }

    /**
     * Blocking Stub implementation
     */
    public static final class LeaderServiceBlockingStub extends AbstractStub<LeaderServiceBlockingStub> {

        private LeaderServiceBlockingStub(Channel channel) {
            super(channel);
        }

        private LeaderServiceBlockingStub(Channel channel, CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected LeaderServiceBlockingStub build(Channel channel, CallOptions callOptions) {
            return new LeaderServiceBlockingStub(channel, callOptions);
        }

        public RegisterResponse registerMember(RegisterRequest request) {
            return ClientCalls.blockingUnaryCall(
                    getChannel(), METHOD_REGISTER_MEMBER, getCallOptions(), request);
        }

        public UnregisterResponse unregisterMember(UnregisterRequest request) {
            return ClientCalls.blockingUnaryCall(
                    getChannel(), METHOD_UNREGISTER_MEMBER, getCallOptions(), request);
        }

        public HeartbeatResponse heartbeat(HeartbeatRequest request) {
            return ClientCalls.blockingUnaryCall(
                    getChannel(), METHOD_HEARTBEAT, getCallOptions(), request);
        }
    }

    private static <T> void asyncUnimplementedUnaryCall(MethodDescriptor<?, ?> method, StreamObserver<T> observer) {
        observer.onError(Status.UNIMPLEMENTED
                .withDescription("Method " + method.getFullMethodName() + " is unimplemented")
                .asRuntimeException());
    }

    // ============== Marshallers ==============

    private static class RegisterRequestMarshaller implements MethodDescriptor.Marshaller<RegisterRequest> {
        @Override
        public java.io.InputStream stream(RegisterRequest value) {
            String data = value.getMemberId() + "\n" + value.getHost() + "\n" + value.getPort();
            return new java.io.ByteArrayInputStream(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public RegisterRequest parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = data.split("\n", 3);
                return RegisterRequest.newBuilder()
                        .setMemberId(parts.length > 0 ? parts[0] : "")
                        .setHost(parts.length > 1 ? parts[1] : "")
                        .setPort(parts.length > 2 ? Integer.parseInt(parts[2]) : 0)
                        .build();
            } catch (Exception e) {
                return RegisterRequest.newBuilder().build();
            }
        }
    }

    private static class RegisterResponseMarshaller implements MethodDescriptor.Marshaller<RegisterResponse> {
        @Override
        public java.io.InputStream stream(RegisterResponse value) {
            String data = value.getSuccess() + "\n" + value.getErrorMessage();
            return new java.io.ByteArrayInputStream(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public RegisterResponse parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = data.split("\n", 2);
                return RegisterResponse.newBuilder()
                        .setSuccess(parts.length > 0 && Boolean.parseBoolean(parts[0]))
                        .setErrorMessage(parts.length > 1 ? parts[1] : "")
                        .build();
            } catch (Exception e) {
                return RegisterResponse.newBuilder().build();
            }
        }
    }

    private static class UnregisterRequestMarshaller implements MethodDescriptor.Marshaller<UnregisterRequest> {
        @Override
        public java.io.InputStream stream(UnregisterRequest value) {
            return new java.io.ByteArrayInputStream(
                    value.getMemberId().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public UnregisterRequest parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return UnregisterRequest.newBuilder().setMemberId(data).build();
            } catch (Exception e) {
                return UnregisterRequest.newBuilder().build();
            }
        }
    }

    private static class UnregisterResponseMarshaller implements MethodDescriptor.Marshaller<UnregisterResponse> {
        @Override
        public java.io.InputStream stream(UnregisterResponse value) {
            return new java.io.ByteArrayInputStream(
                    String.valueOf(value.getSuccess()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public UnregisterResponse parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return UnregisterResponse.newBuilder().setSuccess(Boolean.parseBoolean(data)).build();
            } catch (Exception e) {
                return UnregisterResponse.newBuilder().build();
            }
        }
    }

    private static class HeartbeatRequestMarshaller implements MethodDescriptor.Marshaller<HeartbeatRequest> {
        @Override
        public java.io.InputStream stream(HeartbeatRequest value) {
            String data = value.getMemberId() + "\n" + value.getMessageCount();
            return new java.io.ByteArrayInputStream(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public HeartbeatRequest parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = data.split("\n", 2);
                return HeartbeatRequest.newBuilder()
                        .setMemberId(parts.length > 0 ? parts[0] : "")
                        .setMessageCount(parts.length > 1 ? Integer.parseInt(parts[1]) : 0)
                        .build();
            } catch (Exception e) {
                return HeartbeatRequest.newBuilder().build();
            }
        }
    }

    private static class HeartbeatResponseMarshaller implements MethodDescriptor.Marshaller<HeartbeatResponse> {
        @Override
        public java.io.InputStream stream(HeartbeatResponse value) {
            return new java.io.ByteArrayInputStream(
                    String.valueOf(value.getAcknowledged()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public HeartbeatResponse parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return HeartbeatResponse.newBuilder().setAcknowledged(Boolean.parseBoolean(data)).build();
            } catch (Exception e) {
                return HeartbeatResponse.newBuilder().build();
            }
        }
    }
}
