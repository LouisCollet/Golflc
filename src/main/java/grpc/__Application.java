
package grpc;

import java.io.IOException;
import io.grpc.Server;
import io.grpc.ServerBuilder;


// fake
public class __Application {

    void main() throws IOException, InterruptedException {
        Server server = ServerBuilder
                .forPort(8080)
                .addService(new OrderServiceImpl()).build();

        server.start();
        server.awaitTermination();
    }

}