package grpc;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
// https://www.baeldung.com/grpc-server-global-exception-interceptor
public class GlobalExceptionInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata headers,
        ServerCallHandler<ReqT, RespT> next) {
        ServerCall.Listener<ReqT> delegate = null;
        try {
            delegate = next.startCall(serverCall, headers);
        } catch(Exception ex) {
            return handleInterceptorException(ex, serverCall);
        }
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception ex) {
                    handleEndpointException(ex, serverCall);
                }
            }
        };
    }

    private static <ReqT, RespT> void handleEndpointException(Exception ex, ServerCall<ReqT, RespT> serverCall) {
  //      String ticket = new TicketService().createTicket(ex.getMessage());
        serverCall.close(Status.INTERNAL
            .withCause(ex)
          //  .withDescription(ex.getMessage() + ", Ticket raised:" + ticket), new Metadata());
          .withDescription(ex.getMessage() + ", Ticket raised:" + " ticket"), new Metadata());
    }

    private <ReqT, RespT> ServerCall.Listener<ReqT> handleInterceptorException(Throwable t, ServerCall<ReqT, RespT> serverCall) {
  //      String ticket = new TicketService().createTicket(t.getMessage());
        serverCall.close(Status.INTERNAL
            .withCause(t)
        //    .withDescription("An exception occurred in a **subsequent** interceptor:" + ", Ticket raised:" + ticket), new Metadata());
         .withDescription("An exception occurred in a **subsequent** interceptor:" + ", Ticket raised:" + " ticket"), new Metadata());

        return new ServerCall.Listener<ReqT>() {
            // no-op
        };
    }
}