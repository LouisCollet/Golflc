
package grpc.gps;

import com.google.common.util.concurrent.Uninterruptibles;
import static interfaces.Log.LOG;
import navigate.gps.TripRequest;
import navigate.gps.TripResponse;
import io.grpc.stub.StreamObserver;
import java.time.Duration;

import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class TripResponseStreamObserver implements StreamObserver<TripResponse> {

    private StreamObserver<TripRequest> requestStreamObserver;

    @Override
    public void onNext(TripResponse tripResponse) {
        LOG.debug("entering onNext");
        LOG.debug("Trip onNext time to destination = " + tripResponse.getTimeToDestination());
        LOG.debug("Trip onNext remaining distance = " + tripResponse.getRemainingDistance());
       if(tripResponse.getRemainingDistance() > 0){
           print(tripResponse);
        //   Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        
           this.drive();
       }else{
            LOG.debug("going to onCompleted");
           this.requestStreamObserver.onCompleted();
       }
    }

    @Override
    public void onError(Throwable throwable) {
       LOG.debug("StreamObserver on Error" + throwable.toString());
    }

    @Override
    public void onCompleted() {
        LOG.debug("Trip Completed");
    }

    public void startTrip(StreamObserver<TripRequest> requestStreamObserver){
         LOG.debug("this is startTrip");
        this.requestStreamObserver = requestStreamObserver;
        this.drive();
    }

  private void drive(){
         LOG.debug("entering drive");
      try{  
          // TODO ne fonctionne pas !!
          
       Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
   //  Thread.sleep(3000);
        //Assertions.assertTimeout(Duration.ofMillis(100000), () -> Thread.sleep(100));
          LOG.debug("line after waiting 3 secs sleepUninterrutibly 01");
        TripRequest tripRequest = TripRequest
                .newBuilder()
                .setDistanceTravelled(ThreadLocalRandom.current().nextInt(1, 10))
                .build();
   //       LOG.debug("tripRequest initialization errors ?= " + tripRequest.getInitializationErrorString());
          LOG.debug("drive : random distance travelled = " +  tripRequest.getDistanceTravelled());
        
  //      LOG.debug("this is requestStreamObserver " + requestStreamObserver);
        requestStreamObserver.onNext(tripRequest);
     } catch (Exception e) {
            String msg = "Â£Â£ Exception in drive = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{ }
    }

    private void print(TripResponse tripResponse){
        LOG.debug(LocalTime.now() + ": Remaining Distance : " + tripResponse.getRemainingDistance());
        LOG.debug(LocalTime.now() + ": Time To Reach (sec): " + tripResponse.getTimeToDestination());
        LOG.debug("------------------------------");
    }

}