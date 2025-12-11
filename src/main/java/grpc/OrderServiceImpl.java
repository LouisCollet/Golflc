package grpc;

import static interfaces.Log.LOG;
import io.grpc.stub.StreamObserver;
import protobuf.order.OrderRequest;
import protobuf.order.OrderResponse;
import protobuf.order.OrderServiceGrpc;

public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {
    @Override
    public void executeOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
           LOG.debug("entering executeOrder");
        OrderResponse response = OrderResponse.newBuilder()
                                              .setInfo("Hi " + request.getEmail() + ", your order has been executed for " + request.getProduct())
                                              .build();
           LOG.debug("response = " + response.getInfo());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}