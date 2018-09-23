package lc.golfnew;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.maps.model.LatLng;
import googlemaps.GoogleResponse;
import googlemaps.GoogleTimeZone;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Objects;
import javax.inject.Named;
import javax.net.ssl.HttpsURLConnection;
import utils.LCUtil;
/*** 
 * 
 * @author Abhishek Somani
 * http://www.javaroots.com/2013/08/convert-location-lat-long-java.html
 */
@Named("googlegeoapiC")
public class GoogleGeoApiController 
{
 /*
  * Geocode request URL. Here see we are passing "json" it means we will get
  * the output in JSON format. You can also pass "xml" instead of "json" for
  * XML output. For XML output URL will be
  * "http://maps.googleapis.com/maps/api/geocode/xml";
  */

 private static final String URL = "https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyACXDPdyVSXu-qCcvegAyoL2ykdbahQ3Lc";
 private static InputStream iStream; 
 private static HttpsURLConnection urlConnection;
 private static GoogleResponse response;

    public GoogleGeoApiController() {  // constructor
        GoogleGeoApiController.iStream = null;
        GoogleGeoApiController.urlConnection = null;
        GoogleGeoApiController.response = null;
    }
 /*https://maps.googleapis.com/maps/api/timezone/json?location=39.6034810,-119.6822510&timestamp=1331161200&key=YOUR
  * Here the fullAddress String is in format like
  * "address,city,state,zipcode". Here address means "street number + route"
  * .
  */
 public static LatLng findLatLng(String fullAddress) throws IOException, Exception {
  /*
   * Create an java.net.URL object by passing the request URL in constructor.
     Here you can see I am converting the fullAddress String in UTF-8 format.
     You will get Exception if you don't convert your address in UTF-8 format. 
   */
  try{
      Objects.requireNonNull(fullAddress, "requireNonNull - fullAddress = null - nothing found in findLatLng - Back to sender");
  //    Objects.requireNonNull(anotherPointer, "anotherPointer cannot be null!");

      if (fullAddress == null)
      { LOG.info("fullAddress = null - nothing found in findLatLng - Back to sender");
        return null;} 
 //     if (country == null)
 //     { LOG.info("Country = null - nothing in findLatLng");
 //       return null;} 
      
  //    String fullAddress = city + "," + country;
       LOG.info("welcome to findLatLng with fullAddress =  " + fullAddress);

     URL url = new URL(URL + "&address=" + URLEncoder.encode(fullAddress, "UTF-8") );
        LOG.info("URL = " + url);
  //   url = url + "&key= ???";
    urlConnection = (HttpsURLConnection) url.openConnection(); // Open the Connection
      LOG.info("URL after connection= " );
    // see https://github.com/FasterXML/jackson-databind
    iStream = urlConnection.getInputStream() ;
       LOG.info("after iStream = " );
    ObjectMapper mapper = new ObjectMapper();  // create once, reuse
      LOG.info("after mapper = " );
 
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);// to prevent exception when encountering unknown property:
  //      LOG.info("after mapper disable = " );
    response = (GoogleResponse)mapper.readValue(iStream, GoogleResponse.class);
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true); // equivalent Pretty Print
        LOG.info("From Google Api :\n" + mapper.writeValueAsString(response));
  //     LOG.info("after response status = " + response ); // + response.getStatus());
    //   LOG.info("response length = " + response.getResults().length);
 //   if(response.getStatus() == null)
 //           LOG.info("response = null");
   //    LOG.info("response LatLng = " + response.getResults()[0].getGeometry()..getLocation().toString());
   if(!response.getStatus().equals("ZERO_RESULTS"))
    {     // resultat correct - on prend la première situation ?? que faire si trop de solutions ?
        // voir la solution de ??
            LOG.info("How many results ? = " + response.getResults().length);
        LatLng latlng = response.getResults()[0].getGeometry().getLocation().getLatlng(); // les 2 en même temps !!!
            LOG.info("Geoapi LatLng latlng  : " + latlng);
        return latlng;
    } else {
            String msg =  LCUtil.prepareMessageBean("zero.result"); 
       //     String msg = "Error_message = ZERO_RESULTS";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
       //     throw new Exception(msg);
            return null;
        }
  //   return latlng;
  } catch (JsonGenerationException e) {
	String msg = "Â£Â£ JsonGenerationException in findTimeZone = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  } catch (JsonMappingException e) {
            String msg = "Â£Â£ JsonMappingException in findTimeZone = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  } catch (IOException e) {
            String msg = "Â£Â£ Exception in finLatLng = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
   }finally{ 
      iStream.close();
   }
 
 } //end method
 
 // développé par lc sur base de l'exemple précédent
 public static GoogleTimeZone findTimeZone(LatLng latlng, String locale) throws IOException {
  try{
   // documentation   https://developers.google.com/maps/documentation/timezone/intro
// location — Paire de valeurs de latitude et longitude séparées par une virgule (location=-33.86,151.20, par exemple), représentant le point géographique à rechercher.
// timestamp — Spécifie l'heure souhaitée, en secondes depuis le 1er janvier 1970 à minuit UTC. Google Maps Time Zone API utilise le paramètre timestamp pour déterminer si l'heure d'été doit être appliquée. Les heures antérieures à 1970 peuvent être exprimées sous forme de valeurs négatives.
   
      //https://maps.googleapis.com/maps/api/timezone/json?location=39.6034810,-119.6822510&timestamp=1331161200&key=YOUR1331161200
        LOG.info("welcome to findTimeZone with LatLng latlng =  " + latlng + " with locale = " + locale);
        if (latlng == null)
       { LOG.info("latlng = null - nothing in findLatLng");
        return null;} 
        
 //     long t = System.currentTimeMillis() / 1000;
 //       LOG.info("long timestamp in seconds = " + t);
 //     String ts = String.valueOf(t);
 //       LOG.info("String timestamp in seconds = " + ts);
   //   String lang = "es";
      String string_url = "https://maps.googleapis.com/maps/api/timezone/json"
              + "?location="  + latlng
              + "&key=AIzaSyACXDPdyVSXu-qCcvegAyoL2ykdbahQ3Lc"  // new 09-08-2018
              + "&timestamp=" + String.valueOf(System.currentTimeMillis()/1000) // google api demande des secondes 
              + "&language="  + locale // paramètre supplémentaire
      ;
 //     LOG.info("String_url = " + string_url);
 // attention ssl est nécessaire si on utilise la key
///  URL url = new URL(URL_TZ + "?location=" + URLEncoder.encode(latlng, "UTF-8") + "&timestamp=133116120" ); //location=50.8262271%2C4.3571382
//URL url = new URL("https://maps.googleapis.com/maps/api/timezone/json?location=50.8262271,4.3571382&timestamp=1331161200&language=es");
    URL url = new URL(string_url);
        LOG.info("URL = " + url);
  /// https://maps.googleapis.com/maps/api/timezone/json?location=39.6034810,-119.6822510&timestamp=1331161200&key
    urlConnection = (HttpsURLConnection) url.openConnection(); // Open the Connection utilise ssl
        
    //	con.setRequestMethod("GET");// optional default is GET
     LOG.info("timeout is = " + urlConnection.getReadTimeout());
     urlConnection.setConnectTimeout(5000); // 5 sec
     urlConnection.setReadTimeout(10000); // 10 sec
     iStream = urlConnection.getInputStream() ;

 //         String test = "{\"age\":29,\"messages\":[\"msg 1\",\"msg 2\",\"msg 3\"],\"name\":\"mkyong\"}";
 //         ObjectMapper map = new ObjectMapper();  // create once, reuse
 //         Object json = map.readValue(test, Object.class);
 //         LOG.info("test mapper = " + map.writerWithDefaultPrettyPrinter().writeValueAsString(json));
 
    // see https://github.com/FasterXML/jackson-databind
    ObjectMapper mapper = new ObjectMapper();  // create once, reuse
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);// to prevent exception when encountering unknown property:
    GoogleTimeZone tz = (GoogleTimeZone)mapper.readValue(iStream, GoogleTimeZone.class);
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true); // equivalent Pretty Print
    LOG.info("mapper avec indent\n" + mapper.writeValueAsString(tz));
 //       LOG.info("Pretty Print tz = \n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tz));
    if(tz.getStatus().equals("OK"))
    {     //LOG.info("tz getstatuts is = OK");
        LOG.info("Founded TimeZone tz = " + tz.toString());
     //   LOG.info("tz dstOffset = " + tz.getDstOffset());
     //   LOG.info("tz rawOffset = " + tz.getRawOffset());
     //   LOG.info("tz ZoneId = " + tz.getTimeZoneId());
     //   LOG.info("tz ZoneName = " + tz.getTimeZoneName());
        return tz;
    } else {
        if (tz.getError_message()!= null) {
            String msg = "Cannot find TimeStamp for = " + latlng + ", error_message =  " + tz.getError_message();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }else{
            String msg = ("Error_message = null");
            LOG.error(msg + " ,status = " + tz.getStatus());
            LCUtil.showMessageFatal(msg);
            throw new Exception(msg);
   //         return null;
        }
   //     return null;
    }
  } catch (JsonGenerationException e) {
	String msg = "Â£Â£ JsonGenerationException in findTimeZone = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        return null;
  } catch (JsonMappingException e) {
	String msg = "Â£Â£ JsonMappingException in findTimeZone = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        return null;
  } catch (Exception e) {
            String msg = "Â£Â£ Exception in findTimeZone = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
   }finally{ 
      iStream.close();
   }
  } //end method
 
 
 // suivant n'est pas testé/utilisé
 public GoogleResponse convertFromLatLong(String latlongString) throws IOException {
  try{
        URL url = new URL(URL + "?latlng=" + URLEncoder.encode(latlongString, "UTF-8"));
        URLConnection conn = url.openConnection(); // Open the Connection
        iStream = conn.getInputStream() ;
        ObjectMapper mapper = new ObjectMapper(); 
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);// to prevent exception when encountering unknown property:
        response = (GoogleResponse)mapper.readValue(iStream, GoogleResponse.class);
            LOG.info("response status = " + response.getStatus());
            LOG.info("response length = " + response.getResults().length);
            LOG.info("response print = " + response.getResults()[0].toString());
        iStream.close();
    return response;
  } catch (Exception e) {
        String msg = "Â£Â£ Exception in convertFromLatLong = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
   }
//  return null;
 } // end method
 
  
 public static void main(String[] args) throws IOException {
  try{
 // GoogleResponse res = new GoogleGeoApiController().convertToLatLong("Apollo Bunder,Mumbai ,Maharashtra, India");
 // String adr = "Rue de l'Amazone 55, 1060 Brussels, Belgium";
  String adr = "Rue de l'Amazone 55, Belgium";
  LatLng latlng = new GoogleGeoApiController().findLatLng(adr);
        LOG.info("LatLng rs = :" );
        LOG.info("LatLng rs = :"  + latlng);
         /*
  GoogleResponse res2 = new GoogleGeoApiController().convertFromLatLong("18.92038860,72.83013059999999");
  if(res2.getStatus().equals("OK"))
  {
   for(GoogleResult result : res2.getResults())
   {
    LOG.info("address is :"  +result.getFormatted_address());
   }
  } else {
    LOG.info("Fatal error in Status = " + res2.getStatus());
  }
  
    String[] latlng = "50.8262271,4.3571382".split(",");
 // String[] latlng =  "-34.8799074,174.7565664";
    double latitude = Double.parseDouble(latlng[0]);
    double longitude = Double.parseDouble(latlng[1]);
    LatLng location = new LatLng(latitude, longitude);
    
    
    */
  // GoogleTimeZone tz1 = new GoogleGeoApiController().findTimeZone(location);
  GoogleTimeZone tz1 = GoogleGeoApiController.findTimeZone(latlng,"es");
   //  LOG.info("after GoogleResponse in main = ");
   if(tz1.getStatus().equals("OK"))
  {     //LOG.info("tz1 getstatuts is = OK");
        LOG.info("tz1 dstOffset = " + tz1.getDstOffset());
        LOG.info("tz1 rawOffset = " + tz1.getRawOffset());
        LOG.info("tz1 ZoneId = " + tz1.getTimeZoneId());
        LOG.info("tz1 ZoneName = " + tz1.getTimeZoneName());
        LOG.info("tz1 ZoneName = " + tz1.getError_message());
   } else {
        LOG.info("Fatal error in Status = " + tz1.getStatus() + " " + tz1.getError_message());
  }

  } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }
  
   } // end main//
 

} // end class addressConvertter

//Here are the java classes to convert json response in java objects . I have ignored some fields for simplicity .
//GoogleResponse in the root class which will contain the GoogleResult array and status .


//}
/*
Here is the example output of the program :

GeoCoding Example :
Lattitude of address is :18.92038860
Longitude of address is :72.83013059999999
GoogleLocation is APPROXIMATE

Reverse GeoCoding Example
address is :Shahid Bhagat Singh Marg, Apollo Bandar, Colaba, Mumbai, Maharashtra 400001, India
address is :Colaba Depot, Shahid Bhagat Singh Marg, Apollo Bandar, Colaba, Mumbai, Maharashtra 400001, India
address is :Cusrow Baug Colony, Colaba, Mumbai, Maharashtra 400001, India
address is :Apollo Bandar, Colaba, Mumbai, Maharashtra, India
address is :400001, India
address is :Colaba, Mumbai, Maharashtra, India
address is :Mumbai, Maharashtra, India
address is :Mumbai, Maharashtra, India
address is :Maharashtra, India
address is :India

if you do not encode the url params in UTF-8 , you may get error like this :

Exception in thread "main" java.io.IOException: Server returned HTTP response code: 400 for URL: http://maps.googleapis.com/maps/api/geocode/json?address=Apollo Bunder,Mumbai ,Maharashtra, India ,400001&sensor=false
 at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1625)
 at com.javaroots.latlong.GoogleGeoApiController.getJSONByGoogle(GoogleGeoApiController.java:58)
 at com.javaroots.latlong.GoogleGeoApiController.main(GoogleGeoApiController.java:72)

Post Comments and Suggestions !!

Update :
There are two limiting factors in using this api (Thanks to ralph for pointing out this ) .First , Google is limiting the number of requests to the Geocoding API to 2,500 requests per day and second ,Geocoding results without displaying them on a map is prohibited .

You can have a look at GeoNames which does not have these restrictions .
*/
