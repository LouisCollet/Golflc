package proto;

import static interfaces.Log.LOG;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
//import utils.LCUtil;

public class GreetServer {
  // private static final Logger logger = Logger.getLogger(GreetServer.class.getName());
   private Server server;
   private void start() throws IOException {
        LOG.debug("entering start of GreetServer ");
      int port = 50051;
      server = ServerBuilder.forPort(port).addService(new GreeterImpl()).build().start();
       
      LOG.debug("Server started, listening on " + port);
 
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            LOG.error("Shutting down gRPC server");
            try {
               server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
               e.printStackTrace(System.err);
            }
         }
      });
   }
   
   static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
      @Override
      public void greet(ClientInput req, StreamObserver<ServerOutput> responseObserver) {
         LOG.debug("Got request from client: " + req);
         ServerOutput reply = ServerOutput.newBuilder().setMessage(
            "Server says " + "\"" + req.getGreeting() + " " + req.getName() + "\""
         ).build();
         responseObserver.onNext(reply);
         responseObserver.onCompleted();
      }
   }
   
  public static void main(String args[])throws Exception{
   try{
       LOG.debug("entering main server");
      final GreetServer greetServer = new GreetServer();
      greetServer.start();
      greetServer.server.awaitTermination();
     }catch (Exception e) {
            String msg = "£££ Exception in " + e.getMessage();
            LOG.error(msg);
       //     LCUtil.showMessageFatal(msg);
       //     return false;
   }
   } //end main
} //end class 