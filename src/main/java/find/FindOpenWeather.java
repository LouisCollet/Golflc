package find;

import Controllers.LanguageController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entite.Club;
import entite.OpenWeather;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import utils.DBConnection;
import static utils.LCUtil.showMessageFatal;
// @Named
public class FindOpenWeather{           // concerne current weather !!!
final private static String WIND_DIRECTION[] = {
          "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
      "S", "SSW", "SW", "WSW", "W", "West-Northwest", "NW", "NNW"};
 // private static InputStream inputStream; 
// https://openweathermap.org/current
//https://www.baeldung.com/java-9-http-client
//http://openjdk.java.net/groups/net/httpclient/intro.html
 private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))// default infinite
    //        .authenticator(Authenticator.getDefault()) // new 30-11-2021
            .build();
 
 public String find(Club club, Connection conn) throws IOException {
  try{
//    System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
  //    System.setProperty("net.fortuna.ical4j.timezone.cache.impl", "");
        LOG.debug("entering FindOpenweather with club =  =  " + club);
      String language = LanguageController.getLanguage();
 //      String language = "fr"; // testing only
      String string_url = "https://api.openweathermap.org/data/2.5/weather"
           //   + "?lat="  + club.getClubLatitude().toString()
              + "?lat="  + String.valueOf(club.getAddress().getLatLng().getLat())
           //   + "&lon="  + club.getClubLongitude().toString()
              + "&lon="  + String.valueOf(club.getAddress().getLatLng().getLng())
              + "&appid=6c7ad5efe2fef5799fb0277381f5ec7e"  // API key
              + "&units=metric"
              + "&lang=" + language
              ;
 //  https://mkyong.com/java/how-to-send-http-request-getpost-in-java/
 //  https://mkyong.com/java/java-11-httpclient-examples/
 //  https://openjdk.java.net/groups/net/httpclient/recipes.html
           LOG.debug("Testing 1 - Send Http GET request");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(string_url))  //  .uri(URI.create("http://openjdk.java.net/"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();
  // The synchronous API, blocks until the HttpResponse is available.
       HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

   //     httpClient.sendAsync(request, BodyHandlers.ofString())
   //      .thenApply(HttpResponse::body)
   //      .thenAccept(System.out::println)
   //      .join();

           LOG.debug("response uri = " + response.uri()); // if redirection !    
           LOG.debug("response statuscode = " + response.statusCode()); // print status code
           LOG.debug("response body = " + response.body()); // print response body
           LOG.debug("response version = " + response.version()); // print response body
   //        LOG.debug("response headers = " + response.headers()); // print response headers
          response.headers().map().forEach((k, v) -> LOG.debug("header = " + k + ":" + v));
 
    ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT); //pretty print
 //   om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);// to prevent exception when encountering unknown property:
     OpenWeather weather = om.readValue(response.body(), OpenWeather.class);
  String wd = null;
  if(weather.getWind().getDeg() != null) {
       LOG.debug("wind degree: " + weather.getWind().getDeg() );
     wd = WIND_DIRECTION[(int)Math.floor((weather.getWind().getDeg() % 360) / 22.5)];
       LOG.debug("wind direction : " + wd );
  }else{
       LOG.debug("wind direction : unknown" );
}
//LOG.debug("line 12");
   Instant instant = Instant.ofEpochSecond(weather.getDt());  //default = UTC
   ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of(club.getAddress().getZoneId()));// "Europe/Brussels"
      LOG.debug("time zoned = " + zdt);

// icon URL is http://openweathermap.org/img/wn/10d@2x.png
StringBuilder sb = new StringBuilder ();
 sb.append("Infos météos").append(" at time = ").append(zdt)
   .append("<img src=http://openweathermap.org/img/wn/").append(weather.getWeather().getFirst().getIcon()).append("@2x.png>")    
   .append(" <b>general = </b>").append(weather.getWeather().getFirst().getMain())
 //  .append(" , ").append(weather.getWeather().get(0).getDescription())
   .append(" , ").append(weather.getWeather().getFirst().getDescription()) // 15-10-2024 get(0) replaced by getFirst()
              
   .append(" <b>Wind direction = </b>").append(wd)
   .append(" <b>Speed = </b>").append(weather.getWind().getSpeed())
   .append(" <b>Temperature = </b>").append(weather.getMain().getTemp())
   .append(" <b>feels like = </b>").append(weather.getMain().getFeelsLike())
   .append(" <b>humidity = </b>").append(weather.getMain().getHumidity())
   ;
  //  pour vérifier le contenu
    String json = om.writeValueAsString(weather);
	LOG.debug("json = \n" + json);
 return sb.toString();
  }catch(Exception e) {
            String msg = "££ Exception in FindOpenWeather  = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
   }finally{ 
//      inputStream.close();
   }
  } //end method
 
 /*public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
*/

 void main() throws IOException {
try{
   Connection conn = new DBConnection().getConnection();
   
   Club club = new Club();
   club.setIdclub(113); // anderlecht
   club = new read.ReadClub().read(club, conn);
   FindOpenWeather fow = new FindOpenWeather();
   String s = fow.find(club, conn);
          LOG.debug("response in main = :" + s );
 }catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }
  
   } // end main//
 
} // end class
   // response tester sur    
  //  if(HttpURLConnection.HTTP_OK;
    /*  
      
     URL url = new URL(string_url);
        LOG.debug("URL = " + url);
     HttpsURLConnection httpConnection = (HttpsURLConnection) url.openConnection(); // Open the Connection utilise ssl
     httpConnection.setConnectTimeout(5000); // 5 sec
     httpConnection.setReadTimeout(10000); // 10 sec
     LOG.debug("Response Code : " + httpConnection.getResponseCode()); // 200 = OK
     System.out.println("Cipher Suite : " + httpConnection.getCipherSuite());
	LOG.debug("\n");
	Certificate[] certs = httpConnection.getServerCertificates();
	for(Certificate cert : certs){
	   LOG.debug("Cert Type : " + cert.getType());
	   LOG.debug("Cert Hash Code : " + cert.hashCode());
	   LOG.debug("Cert Public Key Algorithm : "  + cert.getPublicKey().getAlgorithm());
	   LOG.debug("Cert Public Key Format : "     + cert.getPublicKey().getFormat());
	   LOG.debug("\n");
	}
     */
     // https://www.baeldung.com/java-download-file
     //The first thing we should know is that we can read the size of a file from a given URL
     // without actually downloading it by using the HTTP HEAD method:
  //   httpConnection.setRequestMethod("HEAD");
   //  long fileSize = httpConnection.getContentLengthLong();
  //     LOG.debug("content long = " + fileSize);
  
 ////    inputStream = httpConnection.getInputStream();
     
     
 //    BufferedInputStream reader = new BufferedInputStream(inputStream);
 //    Now that we have the total content size of the file, we can check whether our file is partially downloaded.
/*     If so, we'll resume the download from the last byte recorded on disk:
     long existingFileSize = outputFile.length();
     if (existingFileSize < fileLength) {
      httpFileConnection.setRequestProperty(
      "Range", 
      "bytes=" + existingFileSize + "-" + fileLength
    );
}
 */
//  String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
//     LOG.debug("result  = "  + result);
