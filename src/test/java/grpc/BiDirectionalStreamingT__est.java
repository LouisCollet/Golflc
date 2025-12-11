
package grpc;

import grpc.gps.GPSServer;
import grpc.gps.TripResponseStreamObserver;
import static interfaces.Log.LOG;
import io.grpc.ConnectivityState;
import navigate.gps.NavigationServiceGrpc;
import navigate.gps.TripRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@DisplayName("I'm a Test Class")
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class BiDirectionalStreamingT__est {

    private  ManagedChannel channel;
    private  NavigationServiceGrpc.NavigationServiceStub clientStub;
    
/*    
    @BeforeAll
    public static void startServer(){
          LOG.debug("this is the startServer");
   try{
    //  String[] args = null;
      GPSServer.startServer(); //main(args);
           LOG.debug("returned from server start");
    } catch (Exception e) {
            String msg = "Â£Â£ Exception in startServer = " + e.getMessage();
           LOG.error(msg);
}
   }
*/
 //   @BeforeEach mod 12-09-2022
    @BeforeAll
 public void setup() throws IOException, InterruptedException{
     LOG.debug("this is the setup");
  try{
        channel = ManagedChannelBuilder
                .forAddress("localhost", 6565)
                .usePlaintext()
                .enableRetry()
                .maxRetryAttempts(5)
                .build();
        
      ConnectivityState initialState = channel.getState(false);
         LOG.info("Channel {} Initializing logging in state {}", channel.toString(), initialState.toString());
        
        clientStub = NavigationServiceGrpc.newStub(channel);
        LOG.debug("setup clientStub = " + clientStub.toString());
   /*      Runnable notif = new Runnable() {
            @Override
            public void run() {
                ConnectivityState currentState = channel.getState(false);
                channel.notifyWhenStateChanged(currentState, this);
                LOG.info("Channel {} Changed state to {}", channel, currentState.toString());
            }
        };
        notif.run();
   */    
      } catch (Exception e) {
            String msg = "Â£Â£ Exception in setUp = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{ }
 }
        
        

    @Test
    public void tripTest() throws InterruptedException {
           LOG.debug("starting tripTest test navigate");
        TripResponseStreamObserver tripResponseStreamObserver = new TripResponseStreamObserver();
        StreamObserver<TripRequest> requestStreamObserver = clientStub.navigate(tripResponseStreamObserver);
    //    requestStreamObserver.
        tripResponseStreamObserver.startTrip(requestStreamObserver);
           LOG.debug("tripTest launched !");
        // just for testing
        // Thread.sleep(1000);
         //Assertions.assertTimeout(Duration.ofMillis(100000), () -> Thread.sleep(100));
    }

    @AfterEach
    public void teardown(){
        LOG.debug("this is the teardown - channel shutdown");
        channel.shutdown();
        
    }
    
  @AfterAll
  static void tearDownAll() {
    LOG.debug("afterAll");
  }
    
    
    
    
    
  //  pas nécessaire car on utilise Junit !
 void main() throws Exception {
  try{
      BiDirectionalStreamingT__est result = new BiDirectionalStreamingT__est();
      result.setup();
      result.tripTest();
      result.teardown();
    LOG.debug("result = " + result);
    
  } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{ }
 } // end main   

} // end class