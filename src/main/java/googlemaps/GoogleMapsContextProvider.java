
package googlemaps;

import com.google.maps.GeoApiContext;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.TimeUnit;
//@Singleton
//@Startup
//@Named() // new 
//@ApplicationScoped
public class GoogleMapsContextProvider {
private GeoApiContext context;
// non utilisé !!
@PostConstruct
  void init() {
   try{
        LOG.debug("GoogleMapsContextProvider INITIALISED");
        context = new GeoApiContext.Builder()
                .apiKey(System.getenv("GOOGLE_MAPS_API_KEY")) // new 14-12-2025
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();
    } catch (Exception e) {
            String msg = "Exception in GoogleMapsContextProvider = " + e.getMessage();
            LOG.error(msg);
   }finally{ }
}
    public GeoApiContext getContext() {
        return context;
    }
    
    @PreDestroy
    void shutdown() {
        LOG.debug("shutdown context GoogleMapsContextProvider");
        context.shutdown();
    }
} //end class