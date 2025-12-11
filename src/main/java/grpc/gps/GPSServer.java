
package grpc.gps;

//import grpc.gps.NavigationService;
import static interfaces.Log.LOG;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;

import java.io.IOException;
import java.util.List;
//import org.junit.jupiter.api.BeforeAll;
// 
public class GPSServer {
    static Server server = null;
    void main() throws IOException, InterruptedException {
   // public static void startServer(){ // throws IOException, InterruptedException {
        // build gRPC server
           LOG.debug("main of GPSServer");
           
    try{
         server = ServerBuilder.forPort(6565)
                .addService(new NavigationService())
                .build();
    //    LOG.debug("line 01");
        server.start();
           String msg = "GPSServer started = " + server.toString() + " , listening op port : " + server.getPort(); //  + server.getServices().toString();
           LOG.debug(msg);
       //     List<ServerServiceDefinition> services = server.getServices();
        //    LOG.debug("services = " + services.toString() + " / " + services.size());
        //    services.forEach(item -> LOG.debug("Services list : " + item.toString()));
           LOG.debug("server is shutdown ? " + server.isShutdown());
           LOG.debug("server is terminated ? " + server.isTerminated());
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.debug("GPS is shutting down!");
            server.shutdown();
        }));

        server.awaitTermination();
    } catch (Exception e) {
            String msg = "Â£Â£ Exception in startServer = " + e.getMessage();
            LOG.error(msg);
       //     server.shutdown();
      //      LCUtil.showMessageFatal(msg);
   }finally{ }
    }

}