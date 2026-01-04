package com.example.family;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.63.0)",
    comments = "Source: family.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class FamilyReplicationServiceGrpc {

  private FamilyReplicationServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "family.FamilyReplicationService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.example.family.ReplicateMessageRequest,
      com.example.family.ReplicateMessageResponse> getReplicateMessageMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ReplicateMessage",
      requestType = com.example.family.ReplicateMessageRequest.class,
      responseType = com.example.family.ReplicateMessageResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.family.ReplicateMessageRequest,
      com.example.family.ReplicateMessageResponse> getReplicateMessageMethod() {
    io.grpc.MethodDescriptor<com.example.family.ReplicateMessageRequest, com.example.family.ReplicateMessageResponse> getReplicateMessageMethod;
    if ((getReplicateMessageMethod = FamilyReplicationServiceGrpc.getReplicateMessageMethod) == null) {
      synchronized (FamilyReplicationServiceGrpc.class) {
        if ((getReplicateMessageMethod = FamilyReplicationServiceGrpc.getReplicateMessageMethod) == null) {
          FamilyReplicationServiceGrpc.getReplicateMessageMethod = getReplicateMessageMethod =
              io.grpc.MethodDescriptor.<com.example.family.ReplicateMessageRequest, com.example.family.ReplicateMessageResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReplicateMessage"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.family.ReplicateMessageRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.family.ReplicateMessageResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FamilyReplicationServiceMethodDescriptorSupplier("ReplicateMessage"))
              .build();
        }
      }
    }
    return getReplicateMessageMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.family.FetchMessageRequest,
      com.example.family.FetchMessageResponse> getFetchMessageMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FetchMessage",
      requestType = com.example.family.FetchMessageRequest.class,
      responseType = com.example.family.FetchMessageResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.family.FetchMessageRequest,
      com.example.family.FetchMessageResponse> getFetchMessageMethod() {
    io.grpc.MethodDescriptor<com.example.family.FetchMessageRequest, com.example.family.FetchMessageResponse> getFetchMessageMethod;
    if ((getFetchMessageMethod = FamilyReplicationServiceGrpc.getFetchMessageMethod) == null) {
      synchronized (FamilyReplicationServiceGrpc.class) {
        if ((getFetchMessageMethod = FamilyReplicationServiceGrpc.getFetchMessageMethod) == null) {
          FamilyReplicationServiceGrpc.getFetchMessageMethod = getFetchMessageMethod =
              io.grpc.MethodDescriptor.<com.example.family.FetchMessageRequest, com.example.family.FetchMessageResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "FetchMessage"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.family.FetchMessageRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.family.FetchMessageResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FamilyReplicationServiceMethodDescriptorSupplier("FetchMessage"))
              .build();
        }
      }
    }
    return getFetchMessageMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.family.RegisterNodeRequest,
      com.example.family.RegisterNodeResponse> getRegisterNodeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RegisterNode",
      requestType = com.example.family.RegisterNodeRequest.class,
      responseType = com.example.family.RegisterNodeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.family.RegisterNodeRequest,
      com.example.family.RegisterNodeResponse> getRegisterNodeMethod() {
    io.grpc.MethodDescriptor<com.example.family.RegisterNodeRequest, com.example.family.RegisterNodeResponse> getRegisterNodeMethod;
    if ((getRegisterNodeMethod = FamilyReplicationServiceGrpc.getRegisterNodeMethod) == null) {
      synchronized (FamilyReplicationServiceGrpc.class) {
        if ((getRegisterNodeMethod = FamilyReplicationServiceGrpc.getRegisterNodeMethod) == null) {
          FamilyReplicationServiceGrpc.getRegisterNodeMethod = getRegisterNodeMethod =
              io.grpc.MethodDescriptor.<com.example.family.RegisterNodeRequest, com.example.family.RegisterNodeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RegisterNode"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.family.RegisterNodeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.family.RegisterNodeResponse.getDefaultInstance()))
              .setSchemaDescriptor(new FamilyReplicationServiceMethodDescriptorSupplier("RegisterNode"))
              .build();
        }
      }
    }
    return getRegisterNodeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static FamilyReplicationServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FamilyReplicationServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FamilyReplicationServiceStub>() {
        @java.lang.Override
        public FamilyReplicationServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FamilyReplicationServiceStub(channel, callOptions);
        }
      };
    return FamilyReplicationServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static FamilyReplicationServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FamilyReplicationServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FamilyReplicationServiceBlockingStub>() {
        @java.lang.Override
        public FamilyReplicationServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FamilyReplicationServiceBlockingStub(channel, callOptions);
        }
      };
    return FamilyReplicationServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static FamilyReplicationServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FamilyReplicationServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FamilyReplicationServiceFutureStub>() {
        @java.lang.Override
        public FamilyReplicationServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FamilyReplicationServiceFutureStub(channel, callOptions);
        }
      };
    return FamilyReplicationServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Lider, gelen SET isteğini hata toleransına göre üyelere bu RPC ile çoğaltır.
     * </pre>
     */
    default void replicateMessage(com.example.family.ReplicateMessageRequest request,
        io.grpc.stub.StreamObserver<com.example.family.ReplicateMessageResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReplicateMessageMethod(), responseObserver);
    }

    /**
     * <pre>
     * Lider, GET isteği geldiğinde, ilgili mesajı tutan üyelerden birinden bu RPC ile çeker.
     * </pre>
     */
    default void fetchMessage(com.example.family.FetchMessageRequest request,
        io.grpc.stub.StreamObserver<com.example.family.FetchMessageResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFetchMessageMethod(), responseObserver);
    }

    /**
     * <pre>
     * Üyelerin aileye dinamik katılımı için basit kayıt RPC'si.
     * </pre>
     */
    default void registerNode(com.example.family.RegisterNodeRequest request,
        io.grpc.stub.StreamObserver<com.example.family.RegisterNodeResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRegisterNodeMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service FamilyReplicationService.
   */
  public static abstract class FamilyReplicationServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return FamilyReplicationServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service FamilyReplicationService.
   */
  public static final class FamilyReplicationServiceStub
      extends io.grpc.stub.AbstractAsyncStub<FamilyReplicationServiceStub> {
    private FamilyReplicationServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FamilyReplicationServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FamilyReplicationServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Lider, gelen SET isteğini hata toleransına göre üyelere bu RPC ile çoğaltır.
     * </pre>
     */
    public void replicateMessage(com.example.family.ReplicateMessageRequest request,
        io.grpc.stub.StreamObserver<com.example.family.ReplicateMessageResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReplicateMessageMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Lider, GET isteği geldiğinde, ilgili mesajı tutan üyelerden birinden bu RPC ile çeker.
     * </pre>
     */
    public void fetchMessage(com.example.family.FetchMessageRequest request,
        io.grpc.stub.StreamObserver<com.example.family.FetchMessageResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFetchMessageMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Üyelerin aileye dinamik katılımı için basit kayıt RPC'si.
     * </pre>
     */
    public void registerNode(com.example.family.RegisterNodeRequest request,
        io.grpc.stub.StreamObserver<com.example.family.RegisterNodeResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRegisterNodeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service FamilyReplicationService.
   */
  public static final class FamilyReplicationServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<FamilyReplicationServiceBlockingStub> {
    private FamilyReplicationServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FamilyReplicationServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FamilyReplicationServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Lider, gelen SET isteğini hata toleransına göre üyelere bu RPC ile çoğaltır.
     * </pre>
     */
    public com.example.family.ReplicateMessageResponse replicateMessage(com.example.family.ReplicateMessageRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReplicateMessageMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Lider, GET isteği geldiğinde, ilgili mesajı tutan üyelerden birinden bu RPC ile çeker.
     * </pre>
     */
    public com.example.family.FetchMessageResponse fetchMessage(com.example.family.FetchMessageRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFetchMessageMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Üyelerin aileye dinamik katılımı için basit kayıt RPC'si.
     * </pre>
     */
    public com.example.family.RegisterNodeResponse registerNode(com.example.family.RegisterNodeRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRegisterNodeMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service FamilyReplicationService.
   */
  public static final class FamilyReplicationServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<FamilyReplicationServiceFutureStub> {
    private FamilyReplicationServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FamilyReplicationServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FamilyReplicationServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Lider, gelen SET isteğini hata toleransına göre üyelere bu RPC ile çoğaltır.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.family.ReplicateMessageResponse> replicateMessage(
        com.example.family.ReplicateMessageRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReplicateMessageMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Lider, GET isteği geldiğinde, ilgili mesajı tutan üyelerden birinden bu RPC ile çeker.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.family.FetchMessageResponse> fetchMessage(
        com.example.family.FetchMessageRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFetchMessageMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Üyelerin aileye dinamik katılımı için basit kayıt RPC'si.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.family.RegisterNodeResponse> registerNode(
        com.example.family.RegisterNodeRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRegisterNodeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REPLICATE_MESSAGE = 0;
  private static final int METHODID_FETCH_MESSAGE = 1;
  private static final int METHODID_REGISTER_NODE = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REPLICATE_MESSAGE:
          serviceImpl.replicateMessage((com.example.family.ReplicateMessageRequest) request,
              (io.grpc.stub.StreamObserver<com.example.family.ReplicateMessageResponse>) responseObserver);
          break;
        case METHODID_FETCH_MESSAGE:
          serviceImpl.fetchMessage((com.example.family.FetchMessageRequest) request,
              (io.grpc.stub.StreamObserver<com.example.family.FetchMessageResponse>) responseObserver);
          break;
        case METHODID_REGISTER_NODE:
          serviceImpl.registerNode((com.example.family.RegisterNodeRequest) request,
              (io.grpc.stub.StreamObserver<com.example.family.RegisterNodeResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getReplicateMessageMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.family.ReplicateMessageRequest,
              com.example.family.ReplicateMessageResponse>(
                service, METHODID_REPLICATE_MESSAGE)))
        .addMethod(
          getFetchMessageMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.family.FetchMessageRequest,
              com.example.family.FetchMessageResponse>(
                service, METHODID_FETCH_MESSAGE)))
        .addMethod(
          getRegisterNodeMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.family.RegisterNodeRequest,
              com.example.family.RegisterNodeResponse>(
                service, METHODID_REGISTER_NODE)))
        .build();
  }

  private static abstract class FamilyReplicationServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    FamilyReplicationServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.example.family.FamilyProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("FamilyReplicationService");
    }
  }

  private static final class FamilyReplicationServiceFileDescriptorSupplier
      extends FamilyReplicationServiceBaseDescriptorSupplier {
    FamilyReplicationServiceFileDescriptorSupplier() {}
  }

  private static final class FamilyReplicationServiceMethodDescriptorSupplier
      extends FamilyReplicationServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    FamilyReplicationServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (FamilyReplicationServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new FamilyReplicationServiceFileDescriptorSupplier())
              .addMethod(getReplicateMessageMethod())
              .addMethod(getFetchMessageMethod())
              .addMethod(getRegisterNodeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
