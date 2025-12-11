package proto.baeldung;

import static interfaces.Log.LOG;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClient {
    public static void main(String[] args) throws InterruptedException {
        LOG.debug("entering main");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
      //      .usePlaintext(true)
            .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub 
          = HelloServiceGrpc.newBlockingStub(channel);

        HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder()
            .setFirstName("Baeldung")
            .setLastName("gRPC")
            .build());

        LOG.debug("Response received from server:\n" + helloResponse);

        channel.shutdown();
    }
}
