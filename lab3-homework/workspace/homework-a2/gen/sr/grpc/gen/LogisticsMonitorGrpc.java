package sr.grpc.gen;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class LogisticsMonitorGrpc {

  private LogisticsMonitorGrpc() {}

  public static final java.lang.String SERVICE_NAME = "logistics.LogisticsMonitor";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<sr.grpc.gen.SubscriptionRequest,
      sr.grpc.gen.TransportEvent> getSubscribeTransportMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SubscribeTransport",
      requestType = sr.grpc.gen.SubscriptionRequest.class,
      responseType = sr.grpc.gen.TransportEvent.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<sr.grpc.gen.SubscriptionRequest,
      sr.grpc.gen.TransportEvent> getSubscribeTransportMethod() {
    io.grpc.MethodDescriptor<sr.grpc.gen.SubscriptionRequest, sr.grpc.gen.TransportEvent> getSubscribeTransportMethod;
    if ((getSubscribeTransportMethod = LogisticsMonitorGrpc.getSubscribeTransportMethod) == null) {
      synchronized (LogisticsMonitorGrpc.class) {
        if ((getSubscribeTransportMethod = LogisticsMonitorGrpc.getSubscribeTransportMethod) == null) {
          LogisticsMonitorGrpc.getSubscribeTransportMethod = getSubscribeTransportMethod =
              io.grpc.MethodDescriptor.<sr.grpc.gen.SubscriptionRequest, sr.grpc.gen.TransportEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SubscribeTransport"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sr.grpc.gen.SubscriptionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sr.grpc.gen.TransportEvent.getDefaultInstance()))
              .setSchemaDescriptor(new LogisticsMonitorMethodDescriptorSupplier("SubscribeTransport"))
              .build();
        }
      }
    }
    return getSubscribeTransportMethod;
  }

  private static volatile io.grpc.MethodDescriptor<sr.grpc.gen.UnsubscribeRequest,
      sr.grpc.gen.UnsubscribeResponse> getUnsubscribeTransportMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UnsubscribeTransport",
      requestType = sr.grpc.gen.UnsubscribeRequest.class,
      responseType = sr.grpc.gen.UnsubscribeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<sr.grpc.gen.UnsubscribeRequest,
      sr.grpc.gen.UnsubscribeResponse> getUnsubscribeTransportMethod() {
    io.grpc.MethodDescriptor<sr.grpc.gen.UnsubscribeRequest, sr.grpc.gen.UnsubscribeResponse> getUnsubscribeTransportMethod;
    if ((getUnsubscribeTransportMethod = LogisticsMonitorGrpc.getUnsubscribeTransportMethod) == null) {
      synchronized (LogisticsMonitorGrpc.class) {
        if ((getUnsubscribeTransportMethod = LogisticsMonitorGrpc.getUnsubscribeTransportMethod) == null) {
          LogisticsMonitorGrpc.getUnsubscribeTransportMethod = getUnsubscribeTransportMethod =
              io.grpc.MethodDescriptor.<sr.grpc.gen.UnsubscribeRequest, sr.grpc.gen.UnsubscribeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UnsubscribeTransport"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sr.grpc.gen.UnsubscribeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sr.grpc.gen.UnsubscribeResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LogisticsMonitorMethodDescriptorSupplier("UnsubscribeTransport"))
              .build();
        }
      }
    }
    return getUnsubscribeTransportMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static LogisticsMonitorStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LogisticsMonitorStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LogisticsMonitorStub>() {
        @java.lang.Override
        public LogisticsMonitorStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LogisticsMonitorStub(channel, callOptions);
        }
      };
    return LogisticsMonitorStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static LogisticsMonitorBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LogisticsMonitorBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LogisticsMonitorBlockingV2Stub>() {
        @java.lang.Override
        public LogisticsMonitorBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LogisticsMonitorBlockingV2Stub(channel, callOptions);
        }
      };
    return LogisticsMonitorBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static LogisticsMonitorBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LogisticsMonitorBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LogisticsMonitorBlockingStub>() {
        @java.lang.Override
        public LogisticsMonitorBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LogisticsMonitorBlockingStub(channel, callOptions);
        }
      };
    return LogisticsMonitorBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static LogisticsMonitorFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LogisticsMonitorFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LogisticsMonitorFutureStub>() {
        @java.lang.Override
        public LogisticsMonitorFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LogisticsMonitorFutureStub(channel, callOptions);
        }
      };
    return LogisticsMonitorFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void subscribeTransport(sr.grpc.gen.SubscriptionRequest request,
        io.grpc.stub.StreamObserver<sr.grpc.gen.TransportEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSubscribeTransportMethod(), responseObserver);
    }

    /**
     */
    default void unsubscribeTransport(sr.grpc.gen.UnsubscribeRequest request,
        io.grpc.stub.StreamObserver<sr.grpc.gen.UnsubscribeResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUnsubscribeTransportMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service LogisticsMonitor.
   */
  public static abstract class LogisticsMonitorImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return LogisticsMonitorGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service LogisticsMonitor.
   */
  public static final class LogisticsMonitorStub
      extends io.grpc.stub.AbstractAsyncStub<LogisticsMonitorStub> {
    private LogisticsMonitorStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LogisticsMonitorStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LogisticsMonitorStub(channel, callOptions);
    }

    /**
     */
    public void subscribeTransport(sr.grpc.gen.SubscriptionRequest request,
        io.grpc.stub.StreamObserver<sr.grpc.gen.TransportEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getSubscribeTransportMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void unsubscribeTransport(sr.grpc.gen.UnsubscribeRequest request,
        io.grpc.stub.StreamObserver<sr.grpc.gen.UnsubscribeResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUnsubscribeTransportMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service LogisticsMonitor.
   */
  public static final class LogisticsMonitorBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<LogisticsMonitorBlockingV2Stub> {
    private LogisticsMonitorBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LogisticsMonitorBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LogisticsMonitorBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, sr.grpc.gen.TransportEvent>
        subscribeTransport(sr.grpc.gen.SubscriptionRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getSubscribeTransportMethod(), getCallOptions(), request);
    }

    /**
     */
    public sr.grpc.gen.UnsubscribeResponse unsubscribeTransport(sr.grpc.gen.UnsubscribeRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getUnsubscribeTransportMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service LogisticsMonitor.
   */
  public static final class LogisticsMonitorBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<LogisticsMonitorBlockingStub> {
    private LogisticsMonitorBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LogisticsMonitorBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LogisticsMonitorBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<sr.grpc.gen.TransportEvent> subscribeTransport(
        sr.grpc.gen.SubscriptionRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getSubscribeTransportMethod(), getCallOptions(), request);
    }

    /**
     */
    public sr.grpc.gen.UnsubscribeResponse unsubscribeTransport(sr.grpc.gen.UnsubscribeRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUnsubscribeTransportMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service LogisticsMonitor.
   */
  public static final class LogisticsMonitorFutureStub
      extends io.grpc.stub.AbstractFutureStub<LogisticsMonitorFutureStub> {
    private LogisticsMonitorFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LogisticsMonitorFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LogisticsMonitorFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<sr.grpc.gen.UnsubscribeResponse> unsubscribeTransport(
        sr.grpc.gen.UnsubscribeRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUnsubscribeTransportMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SUBSCRIBE_TRANSPORT = 0;
  private static final int METHODID_UNSUBSCRIBE_TRANSPORT = 1;

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
        case METHODID_SUBSCRIBE_TRANSPORT:
          serviceImpl.subscribeTransport((sr.grpc.gen.SubscriptionRequest) request,
              (io.grpc.stub.StreamObserver<sr.grpc.gen.TransportEvent>) responseObserver);
          break;
        case METHODID_UNSUBSCRIBE_TRANSPORT:
          serviceImpl.unsubscribeTransport((sr.grpc.gen.UnsubscribeRequest) request,
              (io.grpc.stub.StreamObserver<sr.grpc.gen.UnsubscribeResponse>) responseObserver);
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
          getSubscribeTransportMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              sr.grpc.gen.SubscriptionRequest,
              sr.grpc.gen.TransportEvent>(
                service, METHODID_SUBSCRIBE_TRANSPORT)))
        .addMethod(
          getUnsubscribeTransportMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              sr.grpc.gen.UnsubscribeRequest,
              sr.grpc.gen.UnsubscribeResponse>(
                service, METHODID_UNSUBSCRIBE_TRANSPORT)))
        .build();
  }

  private static abstract class LogisticsMonitorBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    LogisticsMonitorBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return sr.grpc.gen.LogisticsProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("LogisticsMonitor");
    }
  }

  private static final class LogisticsMonitorFileDescriptorSupplier
      extends LogisticsMonitorBaseDescriptorSupplier {
    LogisticsMonitorFileDescriptorSupplier() {}
  }

  private static final class LogisticsMonitorMethodDescriptorSupplier
      extends LogisticsMonitorBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    LogisticsMonitorMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (LogisticsMonitorGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new LogisticsMonitorFileDescriptorSupplier())
              .addMethod(getSubscribeTransportMethod())
              .addMethod(getUnsubscribeTransportMethod())
              .build();
        }
      }
    }
    return result;
  }
}
