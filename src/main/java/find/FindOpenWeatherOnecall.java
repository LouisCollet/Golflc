package find;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entite.Club;
import entite.OpenWeatherOnecall;
import static interfaces.Log.LOG;
import jakarta.inject.Named;
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
import net.fortuna.ical4j.util.MapTimeZoneCache;
import utils.DBConnection;
import static utils.LCUtil.showMessageFatal;

//genrated by https://www.jsonschema2pojo.org/
// faire une classe principale et mettre les inner class en static !!!
@Named
public class FindOpenWeatherOnecall{ // not used !!
  /*/  https://openweathermap.org/api/one-call-api
    &exclude={part}  exclude	optional	
    By using this parameter you can exclude some parts of the weather data from the API response.
    It should be a comma-delimited list (without spaces).
Available values:
current
minutely
hourly
daily
alerts
    */

final private static String WIND_DIRECTION[] = {
          "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
      "S", "SSW", "SW", "WSW", "W", "West-Northwest", "NW", "NNW"};
private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))// default infinite
            .build();

 public String find(Club club, Connection conn) throws IOException {
  try{
        LOG.debug("entering FindOpenweather with club =  =  " + club);
        System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
 //       String language = new LanguageController().getLocale().getLanguage();
        String language = "fr"; // for testing only
        String string_url = "https://api.openweathermap.org/data/2.5/onecall"
   ///    to be modified       + "?lat="  + club.getClubLatitude().toString()
   ///           + "&lon="  + club.getClubLongitude().toString()
              + "&appid=6c7ad5efe2fef5799fb0277381f5ec7e"
              + "&units=metric"
              + "&lang=" + language
      ;
       HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(string_url))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      
      
    /*  
     URL url = new URL(string_url);
        LOG.debug("URL = " + url);
     HttpsURLConnection httpConnection = (HttpsURLConnection) url.openConnection(); // Open the Connection utilise ssl
     httpConnection.setConnectTimeout(5000); // 5 sec
     httpConnection.setReadTimeout(10000); // 10 sec
        LOG.debug("Response Code : " + httpConnection.getResponseCode());
     inputStream = httpConnection.getInputStream();
    */       
     ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT); //pretty print
     OpenWeatherOnecall weather = om.readValue(response.body(), OpenWeatherOnecall.class);
  
     StringBuilder sb = new StringBuilder ();
      LOG.debug("size daily : " + weather.getDaily().size()); // 8 jours
      for(int i = 0; i < weather.getDaily().size(); i++){
            Instant instant = Instant.ofEpochSecond(weather.getDaily().get(i).getDt());  //default = UTC
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of(club.getAddress().getZoneId()));// "Europe/Brussels"
            LOG.debug("daily zoned = " + zdt);
        }
  //    weather.getDaily().get(0).getWeather().get(0).getMain();
      
      LOG.debug("size hourly : " + weather.getHourly().size()); // 48 heures
      for(int i = 0; i < weather.getHourly().size(); i++){
            Instant instant = Instant.ofEpochSecond(weather.getHourly().get(i).getDt());  //default = UTC
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of(club.getAddress().getZoneId()));// "Europe/Brussels"
            LOG.debug("hourly zoned = " + zdt);
        }
      LOG.debug("size minutely : " + weather.getMinutely().size());   // 60 minutes
       for(int i = 0; i < weather.getMinutely().size(); i++){
            Instant instant = Instant.ofEpochSecond(weather.getMinutely().get(i).getDt());  //default = UTC
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of(club.getAddress().getZoneId()));// "Europe/Brussels"
            LOG.debug("minutely zoned = " + zdt);
        }
      
  /*
  String wd = null;
if(openWeather.getWind().getDeg() != null) {
    LOG.debug("wind degree: " + openWeather.getWind().getDeg() );
     wd = WIND_DIRECTION[(int)Math.floor((openWeather.getWind().getDeg() % 360) / 22.5)];
    LOG.debug("wind direction : " + wd );
}else{
    LOG.debug("wind direction : unknown" );
}
openWeather
// icon 
// http://openweathermap.org/img/wn/10d@2x.png
// URL is http://openweathermap.org/img/wn/10d@2x.png
// icon = 01d
// construire le string
// à faire https://openweathermap.org/api/one-call-api prévision pour les 7 prochains jours : prendre occurence en fonction
// de la différence de jours entre current et date du round
StringBuilder sb = new StringBuilder ();
sb.append("Infos météos")
 //  .append(" <b>longitude = </b>").append(openWeather.getCoord().getLon())
 //  .append(" <b>latitude = </b>").append(openWeather.getCoord().getLat())
   .append("<img src=http://openweathermap.org/img/wn/").append(openWeather.getWeather().get(0).getIcon()).append("@2x.png>")    
   .append(" <b>general = </b>").append(openWeather.getWeather().get(0).getMain())
   .append(" , ").append(openWeather.getWeather().get(0).getDescription())
   //.append(" icon = ").append(openWeather.getWeather().get(0).getIcon())
   .append(" <b>Wind direction = </b>").append(wd)
   .append(" <b>Speed = </b>").append(openWeather.getWind().getSpeed())
 //  .append(" <b>weather= </b>").append)(openWeather.getWeather().g 
   .append(" <b>Temperature = </b>").append(openWeather.getMain().getTemp())
   .append(" <b>feels like = </b>").append(openWeather.getMain().getFeelsLike())
   .append(" <b>humidity = </b>").append(openWeather.getMain().getHumidity())
   .append("<img src=http://openweathermap.org/img/wn/").append(openWeather.getWeather().get(0).getIcon()).append("@2x.png>")
        
;
*/
  //  pour vérifier le contenu
  //  String json = om.writeValueAsString(openWeather);
     String json = om.writeValueAsString(weather);
	LOG.debug("json = \n" + json);
 return sb.toString();
  }catch(JsonGenerationException e) {
	String msg = "££ JsonGenerationException in FindOpenWeather = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
        return null;
  }catch(JsonMappingException e) {
	String msg = "££ JsonMappingException in FindOpenWeather  = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
        return null;
  }catch(Exception e) {
            String msg = "££ Exception in FindOpenWeather  = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
   }finally{ 
 //     inputStream.close();
   }
  
//   }else{
//       LOG.debug("escaped to findSunriseSunset repetition thanks to lazy loading");
//     return liste;  //plusieurs fois ??
//    }
  
  } //end method

//    public static Flight getListe() {
//        return liste;
//    }

 //   public static void setListe(Flight liste) {
 //       find.SunriseSunset.liste = liste;
 //   }


   
 void main() throws IOException {
  try{
   Connection conn = new DBConnection().getConnection();
   FindOpenWeatherOnecall fow = new FindOpenWeatherOnecall();
   Club club = new Club();
   club.setIdclub(113); // anderlecht
   club = new read.ReadClub().read(club, conn);
   String s = fow.find(club, conn);
          LOG.debug("response in main = :" + s );
   } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }
  
   } // end main//
 
 

} // end class addressConvertter