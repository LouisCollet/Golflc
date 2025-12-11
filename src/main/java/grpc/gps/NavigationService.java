package grpc.gps;

import static interfaces.Log.LOG;
import navigate.gps.NavigationServiceGrpc;
import navigate.gps.TripRequest;
import navigate.gps.TripResponse;
import io.grpc.stub.StreamObserver;

public class NavigationService extends NavigationServiceGrpc.NavigationServiceImplBase {

    @Override
    public StreamObserver<TripRequest> navigate(StreamObserver<TripResponse> responseObserver) {
            LOG.debug("entering NavigatonService");
        return new TripRequestObserver(responseObserver);
    }

}