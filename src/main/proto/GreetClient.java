
package proto;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
//import java.util.logging.Logger;

import static interfaces.Log.LOG;

public class GreetClient {
//   private static final Logger logger = Logger.getLogger(GreetClient.class.getName());
   private final GreeterGrpc.GreeterBlockingStub blockingStub;
   
   public GreetClient(Channel channel) {
      blockingStub = GreeterGrpc.newBlockingStub(channel);
   }
   public void makeGreeting(String greeting, String username) {
       LOG.debug("entering start of GreetServer ");
      LOG.debug("Sending greeting to server: " + greeting + " for name: " + username);
      ClientInput request = ClientInput.newBuilder().setName(username).setGreeting(greeting).build();
      LOG.debug("Sending to server: " + request);
      ServerOutput response;
      try {
         response = blockingStub.greet(request);
      } catch (StatusRuntimeException e) {
         LOG.debug("RPC failed: {0}", e.getStatus());
         return;
      }
      LOG.debug("Got following from the server: " + response.getMessage());
   }
   
  public static void main(String args[])throws Exception{     
        LOG.debug("starting main of GreetClient");
      String greeting = args[0];
      String username = args[1];
      String serverAddress = "localhost:50051";
	   ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
         .usePlaintext()
         .build();
      try {
         GreetClient client = new GreetClient(channel);
         client.makeGreeting(greeting, username);
      } finally {
         channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
      }
   }
} // end class