package find;

import com.google.maps.GeoApiContext;
import com.google.maps.TimeZoneApi;
import com.google.maps.errors.ApiException;
import static interfaces.Log.LOG;
import java.sql.SQLException;
import java.util.TimeZone;
import utils.LCUtil;

public class FindTimeZone{
    
// used for Club and Player !!

 public static TimeZone find(entite.LatLng latlng) throws ApiException{   
   final String methodName = utils.LCUtil.getCurrentMethodName();
try{
       LOG.debug("entering " + methodName);
       java.util.TimeZone timeZone;
       try (GeoApiContext context = new GeoApiContext.Builder().apiKey(System.getenv("GOOGLE_MAPS_API_KEY")).build()) {
        //   LOG.debug("results 0 =" + results[0]);
         //  timeZone = TimeZoneApi.getTimeZone(context,results[0].geometry.location).await();
           com.google.maps.model.LatLng latlngGoogle = new com.google.maps.model.LatLng();
           latlngGoogle.lat = latlng.getLat();
           latlngGoogle.lng = latlng.getLng();
           timeZone = TimeZoneApi.getTimeZone(context,latlngGoogle).await();
        //   Gson gson = new GsonBuilder().setPrettyPrinting().create();
         //    LOG.debug("timeZone found : " + gson.toJson(gson));
              }
  return timeZone;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{}
} //end method

void main() throws SQLException, Exception{
  //  https://stackoverflow.com/questions/27261670/convert-string-to-latlng/33875637
 //   double latitude = Double.parseDouble("50.82622600");
  //  double longitude = Double.parseDouble("4.35714760");
    
 //   LatLng latlng = new LatLng(Double.parseDouble("50.82622600"), longitude);
 //   String fullAddress = "Rue de l'Amazone 55,B-1060 Brussels,BE"; 
  //  TimeZone dd = new FindTimeZone().find(fullAddress);
//    TimeZone tz= FindTimeZone.find(fullAddress);
 //       LOG.debug("from main - TimeZone = " + tz);
  //      LOG.debug("from main - TimeZoneId = " + tz.getID());
}// end main
} // end Class