package googlemaps;
import static interfaces.Log.LOG;
import java.util.Arrays;

public class SunriseSunsetResponse {
 
 private SunriseSunsetResult results ;
 private String status ;
 
 public SunriseSunsetResult getResults() {
  return results;
 }
 public void setResults(SunriseSunsetResult results) {
  this.results = results;
    LOG.info("setResults = " + results );
 }
 public String getStatus() {
  return status;
 }
 public void setStatus(String status) {
  this.status = status;
   LOG.info("setStatus = " + status);
 }
 
} //end class