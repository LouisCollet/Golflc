
package grpc.gps;

import com.google.rpc.Status;
import static interfaces.Log.LOG;
import io.grpc.Status.Code;
import io.grpc.protobuf.StatusProto;
import navigate.gps.TripRequest;
import navigate.gps.TripResponse;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.time.LocalTime;

public class TripRequestObserver implements StreamObserver<TripRequest> {

    private final int totalDistance = 100;
    private final LocalTime startTime = LocalTime.now();
    private int distanceTraveled;
    private final StreamObserver<TripResponse> tripResponseStreamObserver;

   public TripRequestObserver(StreamObserver<TripResponse> tripResponseStreamObserver) {
       
        this.tripResponseStreamObserver = tripResponseStreamObserver;
        LOG.debug(" tripResponseStreamObserver = " +  tripResponseStreamObserver);
    }

    @Override
  public void onNext(TripRequest tripRequest) {
          LOG.debug("onNext tripRequest " + tripRequest);
   try{
          this.distanceTraveled = Math.min(totalDistance, (this.distanceTraveled + tripRequest.getDistanceTravelled()));
          LOG.debug("distanceTraveled = " + distanceTraveled);
        int remainingDistance = Math.max(0, (totalDistance - distanceTraveled));
           LOG.debug("remainingDistance = " + remainingDistance);
     // the client has reached destination
        if(remainingDistance == 0){
               LOG.debug("remaining distance = 0");
            this.tripResponseStreamObserver.onNext(TripResponse.getDefaultInstance());
            return;
        }

     // client has not yet reached destination
        long elapsedDuration = Duration.between(this.startTime, LocalTime.now()).getSeconds();
        elapsedDuration = elapsedDuration < 1 ? 1 : elapsedDuration;
           LOG.debug("elapsed duration = " + elapsedDuration);
        double currentSpeed = (distanceTraveled * 1.0d) / elapsedDuration;
           LOG.debug("current speed = " + currentSpeed);
        int timeToReach = (int) (remainingDistance / currentSpeed);
        TripResponse tripResponse = TripResponse.newBuilder()
                .setRemainingDistance(remainingDistance)
                .setTimeToDestination(timeToReach)
                .build();
        
  //      Status status = Status.newBuilder()
   // .setCode(Code.INVALID_ARGUMENT.getNumber())
   // .setMessage("Email or password malformed")
  //  .addDetails(Any.pack(registerUserResponse))
  //  .build();
//tripResponse.onError(StatusProto.toStatusRuntimeException(status));
        
        
        
        
        
        this.tripResponseStreamObserver.onNext(tripResponse);
    } catch (Exception e) {
            String msg = "Â£Â£ Exception onNext = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{ }
        
        
    }

    @Override
    public void onError(Throwable throwable) {
           LOG.debug("RequestObserver on Error" + throwable.toString());
            LOG.debug("RequestObserver on Error"); // + throwable.toString());
    }

    @Override
    public void onCompleted() {
        this.tripResponseStreamObserver.onCompleted();
        LOG.debug("onCompleted - Client reached safely");
    }
} // end class