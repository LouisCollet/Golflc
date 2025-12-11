package lists;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entite.Club;
import entite.Flight;
import entite.Round;
import googlemaps.SunriseSunsetResponse;
import jakarta.inject.Named;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import utils.DBConnection;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@Named("sunrisesunsetapiC")
public class SunriseSunsetList implements interfaces.Log, interfaces.GolfInterface{
 /*
  * Geocode request URL. Here see we are passing "json" it means we will get
  * the output in JSON format. You can also pass "xml" instead of "json" for
  * XML output. For XML output URL will be
  * "http://maps.googleapis.com/maps/api/geocode/xml";
  */

// private static final String URL = "https://maps.googleapis.com/maps/api/geocode/json";

// https://www.baeldung.com/java-9-http-client
 // see also FindOpenWeather
 private static Flight liste = null;
 private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))// default infinite
    //        .authenticator(Authenticator.getDefault()) // new 30-11-2021
            .build();
 
// https://sunrise-sunset.org/api
 public SunriseSunsetList() {  // constructor
      //  FindSunriseSunset.iStream = null;
    //    FindSunriseSunset.urlConnection = null;
    //    FindSunriseSunset.responseSS = null;
    } 

public Flight list(Round round, Club club, Connection conn) throws IOException {
 if(liste == null){ 
  try{
   // documentation   https://sunrise-sunset.org/api
   // exemple: https://maps.googleapis.com/maps/api/timezone/json?location=39.6034810,-119.6822510&timestamp=1331161200&key=YOUR1331161200
        LOG.debug("welcome to findSunriseSunset with date =  " + round.getRoundDate());  // format 2017-04-09
        LOG.debug("welcome to findSunriseSunset with timezone =  " + club.getAddress().getZoneId());  // format 2017-04-09
        
        Objects.requireNonNull(round, "requireNonNull - date_in = null -  - Back to sender");
        Objects.requireNonNull(club, "requireNonNull - tz = null -  - Back to sender");
        if(! utils.LCUtil.isValidTimeZone(club.getAddress().getZoneId())){
            String msg = "time zone not valid - immediatly exiting findSunriseSunset";
            LOG.debug(msg);
            showMessageFatal(msg);
            return null;
       } 
   /*     http://stackoverflow.com/questions/13092865/timezone-validation-in-java
    https://api.sunrise-sunset.org/json?lat=36.7201600&lng=-4.4203400&date=today
/*
    Parameters

lat: Latitude in decimal degrees. Required.
lng: Longitude in decimal degrees. Required.
date: Date in YYYY-MM-DD format. Also accepts other date formats and even relative date formats.
    If not present, date defaults to current date. Optional.
callback: Callback function name for JSONP response. Optional.
formatted: 0 or 1 (1 is default). Time values in response will be expressed following ISO 8601
and day_length will be expressed in seconds. Optional.

*/
      String string_url = "https://api.sunrise-sunset.org/json"
              + "?lat="  + String.valueOf(club.getAddress().getLatLng().getLat()) //   + "?lat="  + club.getClubLatitude().toString()
              + "&lng="  + String.valueOf(club.getAddress().getLatLng().getLng())//  + "&lng="  + club.getClubLongitude().toString()
              + "&date=" + ZDF_YEAR_MONTH_DAY.format(round.getRoundDate()) // format 2021-05-11
              + "&formatted=0" //default = 1
      ;
       LOG.debug("URL = " + string_url);
   // mod 11/08/2022 - non    
      HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(string_url))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();
      LOG.debug("request = " + request.toString());
      LOG.debug("request method = " + request.method());
  // The synchronous API, blocks until the HttpResponse is available.
       HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      
      LOG.debug("response uri = " + response.uri());
           LOG.debug("response statuscode = " + response.statusCode()); // print status code
           LOG.debug("response body = " + response.body()); // print response body
           LOG.debug("response version = " + response.version()); // print response body
   //        LOG.debug("response headers = " + response.headers()); // print response headers
          response.headers().map().forEach((k, v) -> LOG.debug("header = " + k + ":" + v));

    // see https://github.com/FasterXML/jackson-databind
    // also http://tutorials.jenkov.com/java-json/jackson-objectmapper.html

        ObjectMapper om = new ObjectMapper(); 
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);// to prevent exception when encountering unknown property:
    // on utilise le package googlemaps pour ne pas en créer un autre ...
        SunriseSunsetResponse sunriseSunset = om.readValue(response.body(), SunriseSunsetResponse.class);
 // String wd = null;
//        LOG.debug("line 02");
        om.configure(SerializationFeature.INDENT_OUTPUT, true); // equivalent Pretty Print
        LOG.debug("mapper avec indent\n" + om.writeValueAsString(sunriseSunset));
    if(sunriseSunset.getStatus().equals("OK")){
           LOG.debug("Found sunrisesunset response = " + sunriseSunset.getResults().toString());
        LOG.debug("Found sunrise = " + sunriseSunset.getResults().getSunrise());
        LOG.debug("Found sunset  = " + sunriseSunset.getResults().getSunset());
        //NOTE: All times are in UTC and summer time adjustments are not included in the returned data !!
        // on transforme ci-après la date UTC reçue en date tenant compte de la tz locale
        ZonedDateTime sunrise = ZonedDateTime.parse(sunriseSunset.getResults().getSunrise(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            LOG.debug("ZonedDateTime with iso-offset_date_time: " + sunrise);
        sunrise = sunrise.toInstant().atZone(ZoneId.of(club.getAddress().getZoneId())); // mod 08/02/2019
            LOG.debug ("formatted HH:mm sunrise = " + ZDF_HOURS.format(sunrise)); 
            LOG.debug ("offset = " + sunrise.getOffset());
        ZonedDateTime sunset = ZonedDateTime.parse(sunriseSunset.getResults().getSunset(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        sunset = sunset.toInstant().atZone(ZoneId.of(club.getAddress().getZoneId())); // mod 08/02/2019
            LOG.debug ("formatted HH:mm sunset = " + ZDF_HOURS.format(sunset));
        Flight flight = new Flight();
            LOG.debug("sunrise Zoned= " + sunrise);
            LOG.debug("sunset  Zoned= " + sunset);
        flight.setSunrise(sunrise);
        flight.setSunset(sunset);
            LOG.debug("flight returned = " + flight);
        return flight;
    } else {
        String msg = "sunriseSunset.getStatus() not OK - no Sunrise and Sunset found : " + sunriseSunset.getStatus();
      //  "INVALID_REQUEST": indicates that either lat or lng parameters are missing or invalid;
      //  "INVALID_DATE": indicates that date parameter is missing or invalid;
      //  "UNKNOWN_ERROR": indicates that the request could not be processed due to a server error. The request may succeed if you try again.
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
    }
  }catch(Exception e) {
      // mod 11/08/2022 site indisponible !!!
            if(e.getMessage().contains("PKIX path validation failed")){
                LOG.error("no connection detected in Exception !!!");
                Flight flight = new Flight();
                LocalDateTime ldt = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);
                ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.of(club.getAddress().getZoneId()));
                flight.setSunrise(zdt);
                flight.setSunset(flight.getSunrise().plusHours(10));
                String msg = "sunrise and sunset completed manually !!! + " + flight.getSunrise() + " / " + flight.getSunset();
                LOG.info(msg);
                showMessageInfo(msg);
                return flight;
            }
            String msg = "££ Exception in findSunriseSunset = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
   }finally{ 

   }
  
   }else{
       LOG.debug("escaped to findSunriseSunset repetition thanks to lazy loading");
     return liste;  //plusieurs fois ??
    }
  
  } //end method

    public static Flight getListe() {
        return liste;
    }

    public static void setListe(Flight liste) {
        lists.SunriseSunsetList.liste = liste;
    }

   
 void main() throws IOException {
  try{
 
   Date date = SDF.parse("23/07/2018");
   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    LocalDate localDate = LocalDate.now();
 //   LOG.debug(dtf.format(localDate)); //2016/11/16
 //   DBConnection dbc = 
   Connection conn = new DBConnection().getConnection();
  // Course course = new Course();
  // course.setIdcourse(13);
   String tz = "Europe/Brussels";
/// à modifier
 //  find.SunriseSunset ssac = new find.SunriseSunset();
 ////////// à modifier Flight fl = ssac.find(date,BigDecimal.valueOf(50.202764), BigDecimal.valueOf(5.013203),tz,conn);
////           LOG.debug("response in main = :"  + fl);
   } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }
  
   } // end main//
} // end class addressConvertter