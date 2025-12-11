package proto.baeldung;

import static interfaces.Log.LOG;
import java.io.IOException;
import io.grpc.Server;
import io.grpc.ServerBuilder;
 //
public class GrpcServer {
    
    public static void main(String[] args) throws IOException, InterruptedException {
        LOG.debug("entering main");
       // @Override
        Server server = ServerBuilder.forPort(8080)
          .addService(new HelloServiceImpl())
         // .addService(proto.baeldung.HelloRequestHelloServiceImpl())
          .build();

        LOG.debug("Starting server...");
        server.start();
        System.out.println("Server started!");
        server.awaitTermination();
    }
}
