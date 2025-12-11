
package grpc;

import static interfaces.Log.LOG;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import protobuf.order.OrderServiceGrpc;
import protobuf.order.OrderRequest;
import protobuf.order.OrderResponse;


public class __ApplicationClient {
  public String sendMessage(){
      LOG.debug("entering sendMessage");
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 8083)
                                                      .usePlaintext()
                                                      .build();

        OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub
                = OrderServiceGrpc.newBlockingStub(managedChannel);

        OrderRequest orderRequest = OrderRequest.newBuilder()
                                             .setEmail("hello@word.com")
                                             .setProduct("no-name")
                                             .setAmount(3)
                                             .build();
        OrderResponse orderResponse = orderServiceBlockingStub.executeOrder(orderRequest);
           LOG.debug("Received response: "+ orderResponse.getInfo());
        managedChannel.shutdown();
        return "OK";
    }
    
 void main() {
    
}
}