package com.hatokuse.proto;

import io.grpc.*;
import io.grpc.stub.*;

/**
 * Member Service gRPC - Liderden üyeye haberleşme
 */
public final class MemberServiceGrpc {

    private MemberServiceGrpc() {
    }

    public static final String SERVICE_NAME = "hatokuse.MemberService";

    // Method descriptors
    private static final MethodDescriptor<ReplicateRequest, ReplicateResponse> METHOD_REPLICATE_MESSAGE = MethodDescriptor
            .<ReplicateRequest, ReplicateResponse>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(SERVICE_NAME + "/ReplicateMessage")
            .setRequestMarshaller(new ReplicateRequestMarshaller())
            .setResponseMarshaller(new ReplicateResponseMarshaller())
            .build();

    private static final MethodDescriptor<GetMessageRequest, GetMessageResponse> METHOD_GET_MESSAGE = MethodDescriptor
            .<GetMessageRequest, GetMessageResponse>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(SERVICE_NAME + "/GetMessage")
            .setRequestMarshaller(new GetMessageRequestMarshaller())
            .setResponseMarshaller(new GetMessageResponseMarshaller())
            .build();

    private static final MethodDescriptor<MessageCountRequest, MessageCountResponse> METHOD_GET_MESSAGE_COUNT = MethodDescriptor
            .<MessageCountRequest, MessageCountResponse>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(SERVICE_NAME + "/GetMessageCount")
            .setRequestMarshaller(new MessageCountRequestMarshaller())
            .setResponseMarshaller(new MessageCountResponseMarshaller())
            .build();

    private static final MethodDescriptor<HealthCheckRequest, HealthCheckResponse> METHOD_HEALTH_CHECK = MethodDescriptor
            .<HealthCheckRequest, HealthCheckResponse>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(SERVICE_NAME + "/HealthCheck")
            .setRequestMarshaller(new HealthCheckRequestMarshaller())
            .setResponseMarshaller(new HealthCheckResponseMarshaller())
            .build();

    private static final MethodDescriptor<DeleteRequest, DeleteResponse> METHOD_DELETE_MESSAGE = MethodDescriptor
            .<DeleteRequest, DeleteResponse>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(SERVICE_NAME + "/DeleteMessage")
            .setRequestMarshaller(new DeleteRequestMarshaller())
            .setResponseMarshaller(new DeleteResponseMarshaller())
            .build();

    /**
     * Blocking Stub - Senkron çağrılar için
     */
    public static MemberServiceBlockingStub newBlockingStub(Channel channel) {
        return new MemberServiceBlockingStub(channel);
    }

    /**
     * Servis implementasyonu için base class
     */
    public static abstract class MemberServiceImplBase implements BindableService {

        public void replicateMessage(ReplicateRequest request, StreamObserver<ReplicateResponse> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_REPLICATE_MESSAGE, responseObserver);
        }

        public void getMessage(GetMessageRequest request, StreamObserver<GetMessageResponse> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_GET_MESSAGE, responseObserver);
        }

        public void getMessageCount(MessageCountRequest request,
                StreamObserver<MessageCountResponse> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_GET_MESSAGE_COUNT, responseObserver);
        }

        public void healthCheck(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_HEALTH_CHECK, responseObserver);
        }

        public void deleteMessage(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_DELETE_MESSAGE, responseObserver);
        }

        @Override
        public ServerServiceDefinition bindService() {
            return ServerServiceDefinition.builder(SERVICE_NAME)
                    .addMethod(METHOD_REPLICATE_MESSAGE, ServerCalls.asyncUnaryCall(
                            (request, observer) -> replicateMessage(request, observer)))
                    .addMethod(METHOD_GET_MESSAGE, ServerCalls.asyncUnaryCall(
                            (request, observer) -> getMessage(request, observer)))
                    .addMethod(METHOD_GET_MESSAGE_COUNT, ServerCalls.asyncUnaryCall(
                            (request, observer) -> getMessageCount(request, observer)))
                    .addMethod(METHOD_HEALTH_CHECK, ServerCalls.asyncUnaryCall(
                            (request, observer) -> healthCheck(request, observer)))
                    .addMethod(METHOD_DELETE_MESSAGE, ServerCalls.asyncUnaryCall(
                            (request, observer) -> deleteMessage(request, observer)))
                    .build();
        }
    }

    /**
     * Blocking Stub implementation
     */
    public static final class MemberServiceBlockingStub extends AbstractStub<MemberServiceBlockingStub> {

        private MemberServiceBlockingStub(Channel channel) {
            super(channel);
        }

        private MemberServiceBlockingStub(Channel channel, CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected MemberServiceBlockingStub build(Channel channel, CallOptions callOptions) {
            return new MemberServiceBlockingStub(channel, callOptions);
        }

        public ReplicateResponse replicateMessage(ReplicateRequest request) {
            return ClientCalls.blockingUnaryCall(
                    getChannel(), METHOD_REPLICATE_MESSAGE, getCallOptions(), request);
        }

        public GetMessageResponse getMessage(GetMessageRequest request) {
            return ClientCalls.blockingUnaryCall(
                    getChannel(), METHOD_GET_MESSAGE, getCallOptions(), request);
        }

        public MessageCountResponse getMessageCount(MessageCountRequest request) {
            return ClientCalls.blockingUnaryCall(
                    getChannel(), METHOD_GET_MESSAGE_COUNT, getCallOptions(), request);
        }

        public HealthCheckResponse healthCheck(HealthCheckRequest request) {
            return ClientCalls.blockingUnaryCall(
                    getChannel(), METHOD_HEALTH_CHECK, getCallOptions(), request);
        }

        public DeleteResponse deleteMessage(DeleteRequest request) {
            return ClientCalls.blockingUnaryCall(
                    getChannel(), METHOD_DELETE_MESSAGE, getCallOptions(), request);
        }
    }

    private static <T> void asyncUnimplementedUnaryCall(MethodDescriptor<?, ?> method, StreamObserver<T> observer) {
        observer.onError(Status.UNIMPLEMENTED
                .withDescription("Method " + method.getFullMethodName() + " is unimplemented")
                .asRuntimeException());
    }

    // ============== Marshallers ==============

    private static class ReplicateRequestMarshaller implements MethodDescriptor.Marshaller<ReplicateRequest> {
        @Override
        public java.io.InputStream stream(ReplicateRequest value) {
            String data = value.getMessageId() + "\n" + value.getMessageContent();
            return new java.io.ByteArrayInputStream(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public ReplicateRequest parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = data.split("\n", 2);
                return ReplicateRequest.newBuilder()
                        .setMessageId(parts.length > 0 ? parts[0] : "")
                        .setMessageContent(parts.length > 1 ? parts[1] : "")
                        .build();
            } catch (Exception e) {
                return ReplicateRequest.newBuilder().build();
            }
        }
    }

    private static class ReplicateResponseMarshaller implements MethodDescriptor.Marshaller<ReplicateResponse> {
        @Override
        public java.io.InputStream stream(ReplicateResponse value) {
            String data = value.getSuccess() + "\n" + value.getErrorMessage();
            return new java.io.ByteArrayInputStream(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public ReplicateResponse parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = data.split("\n", 2);
                return ReplicateResponse.newBuilder()
                        .setSuccess(parts.length > 0 && Boolean.parseBoolean(parts[0]))
                        .setErrorMessage(parts.length > 1 ? parts[1] : "")
                        .build();
            } catch (Exception e) {
                return ReplicateResponse.newBuilder().build();
            }
        }
    }

    private static class GetMessageRequestMarshaller implements MethodDescriptor.Marshaller<GetMessageRequest> {
        @Override
        public java.io.InputStream stream(GetMessageRequest value) {
            return new java.io.ByteArrayInputStream(
                    value.getMessageId().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public GetMessageRequest parse(java.io.InputStream stream) {
            try {
                String messageId = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return GetMessageRequest.newBuilder().setMessageId(messageId).build();
            } catch (Exception e) {
                return GetMessageRequest.newBuilder().build();
            }
        }
    }

    private static class GetMessageResponseMarshaller implements MethodDescriptor.Marshaller<GetMessageResponse> {
        @Override
        public java.io.InputStream stream(GetMessageResponse value) {
            String data = value.getFound() + "\n" + value.getMessageContent() + "\n" + value.getErrorMessage();
            return new java.io.ByteArrayInputStream(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public GetMessageResponse parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = data.split("\n", 3);
                return GetMessageResponse.newBuilder()
                        .setFound(parts.length > 0 && Boolean.parseBoolean(parts[0]))
                        .setMessageContent(parts.length > 1 ? parts[1] : "")
                        .setErrorMessage(parts.length > 2 ? parts[2] : "")
                        .build();
            } catch (Exception e) {
                return GetMessageResponse.newBuilder().build();
            }
        }
    }

    private static class MessageCountRequestMarshaller implements MethodDescriptor.Marshaller<MessageCountRequest> {
        @Override
        public java.io.InputStream stream(MessageCountRequest value) {
            return new java.io.ByteArrayInputStream(new byte[0]);
        }

        @Override
        public MessageCountRequest parse(java.io.InputStream stream) {
            return MessageCountRequest.getDefaultInstance();
        }
    }

    private static class MessageCountResponseMarshaller implements MethodDescriptor.Marshaller<MessageCountResponse> {
        @Override
        public java.io.InputStream stream(MessageCountResponse value) {
            return new java.io.ByteArrayInputStream(
                    String.valueOf(value.getCount()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public MessageCountResponse parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return MessageCountResponse.newBuilder().setCount(Integer.parseInt(data)).build();
            } catch (Exception e) {
                return MessageCountResponse.newBuilder().build();
            }
        }
    }

    private static class HealthCheckRequestMarshaller implements MethodDescriptor.Marshaller<HealthCheckRequest> {
        @Override
        public java.io.InputStream stream(HealthCheckRequest value) {
            return new java.io.ByteArrayInputStream(new byte[0]);
        }

        @Override
        public HealthCheckRequest parse(java.io.InputStream stream) {
            return HealthCheckRequest.getDefaultInstance();
        }
    }

    private static class HealthCheckResponseMarshaller implements MethodDescriptor.Marshaller<HealthCheckResponse> {
        @Override
        public java.io.InputStream stream(HealthCheckResponse value) {
            String data = value.getHealthy() + "\n" + value.getMessageCount();
            return new java.io.ByteArrayInputStream(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public HealthCheckResponse parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = data.split("\n", 2);
                return HealthCheckResponse.newBuilder()
                        .setHealthy(parts.length > 0 && Boolean.parseBoolean(parts[0]))
                        .setMessageCount(parts.length > 1 ? Integer.parseInt(parts[1]) : 0)
                        .build();
            } catch (Exception e) {
                return HealthCheckResponse.newBuilder().build();
            }
        }
    }

    private static class DeleteRequestMarshaller implements MethodDescriptor.Marshaller<DeleteRequest> {
        @Override
        public java.io.InputStream stream(DeleteRequest value) {
            return new java.io.ByteArrayInputStream(
                    value.getMessageId().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public DeleteRequest parse(java.io.InputStream stream) {
            try {
                String messageId = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return DeleteRequest.newBuilder().setMessageId(messageId).build();
            } catch (Exception e) {
                return DeleteRequest.newBuilder().build();
            }
        }
    }

    private static class DeleteResponseMarshaller implements MethodDescriptor.Marshaller<DeleteResponse> {
        @Override
        public java.io.InputStream stream(DeleteResponse value) {
            String data = value.getSuccess() + "\n" + value.getErrorMessage();
            return new java.io.ByteArrayInputStream(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public DeleteResponse parse(java.io.InputStream stream) {
            try {
                String data = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = data.split("\n", 2);
                return DeleteResponse.newBuilder()
                        .setSuccess(parts.length > 0 && Boolean.parseBoolean(parts[0]))
                        .setErrorMessage(parts.length > 1 ? parts[1] : "")
                        .build();
            } catch (Exception e) {
                return DeleteResponse.newBuilder().build();
            }
        }
    }
}
