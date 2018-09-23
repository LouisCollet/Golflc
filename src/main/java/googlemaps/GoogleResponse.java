package googlemaps;
import static interfaces.Log.LOG;
import java.util.Arrays;

public class GoogleResponse {
 
 private GoogleResult[] results ;
 private String status ;
 
 public GoogleResult[] getResults() {
  return results;
 }
 public void setResults(GoogleResult[] results) {
  this.results = results;
    LOG.info("setResults = " + Arrays.deepToString(results) );
 }
 public String getStatus() {
  return status;
 }
 public void setStatus(String status) {
  this.status = status;
   LOG.info("setStatus = " + status);
 }
 
} //end class