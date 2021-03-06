package find;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entite.Club;
import entite.Flight;
import entite.Round;
import googlemaps.SunriseSunsetResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import javax.inject.Named;
import javax.net.ssl.HttpsURLConnection;
import utils.DBConnection;

@Named("sunrisesunsetapiC")
public class SunriseSunset implements interfaces.Log, interfaces.GolfInterface{
 /*
  * Geocode request URL. Here see we are passing "json" it means we will get
  * the output in JSON format. You can also pass "xml" instead of "json" for
  * XML output. For XML output URL will be
  * "http://maps.googleapis.com/maps/api/geocode/xml";
  */

// private static final String URL = "https://maps.googleapis.com/maps/api/geocode/json";
 private static InputStream iStream; 
 private static HttpsURLConnection urlConnection;
 private static SunriseSunsetResponse responseSS;
// private static ArrayList<Flight> liste = null;
 private static Flight liste = null;

// https://sunrise-sunset.org/api
    public SunriseSunset() {  // constructor
        SunriseSunset.iStream = null;
        SunriseSunset.urlConnection = null;
        SunriseSunset.responseSS = null;
    } 

// public ArrayList<Flight> findSunriseSunset(@NotNull Date date_in, String in_lat, String in_lng,
 //// public Flight findSunriseSunset(@NotNull Date date_in, String in_lat, String in_lng,        
 ////         @NotNull String tz, Course course, Connection conn) throws IOException {
 
//   public Flight findSunriseSunset(Date date_in, BigDecimal in_lat, BigDecimal in_lng, String tz,
//           Connection conn) throws IOException {
  public Flight find(Round round, Club club, Connection conn) throws IOException {
 if(liste == null){ 
  try{
   // documentation   https://sunrise-sunset.org/api
   // exemple: https://maps.googleapis.com/maps/api/timezone/json?location=39.6034810,-119.6822510&timestamp=1331161200&key=YOUR1331161200
        LOG.info("welcome to findSunriseSunset with date =  " + round.getRoundDate());  // format 2017-04-09
        LOG.info("welcome to findSunriseSunset with timezone =  " + club.getClubZoneId());  // format 2017-04-09
        
        Objects.requireNonNull(round, "requireNonNull - date_in = null -  - Back to sender");
        Objects.requireNonNull(club, "requireNonNull - tz = null -  - Back to sender");
        
  //      if (date_in == null){ 
  //              LOG.info("date_in == null - immediatly exiting findSunriseSunset");
  //          return null;} 
     //   boolean b = utils.LCUtil.isValidTimeZone(club.getClubZoneId()); // mod 08/02/2019
      //      LOG.info("TimeZone is Valid or Not : " + b);
     //  if(b == false){
        if(! utils.LCUtil.isValidTimeZone(club.getClubZoneId())){
            String msg = "time zone not valid - immediatly exiting findSunriseSunset";
            LOG.info(msg);
            utils.LCUtil.showMessageFatal(msg);
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
          //    + "?lat="  + in_lat.toString()club
              + "?lat="  + club.getClubLatitude().toString() // mod 08/02/2019
         //     + "&lng="  + in_lng.toString()
              + "&lng="  + club.getClubLongitude().toString()
              + "&date=" + SDF_YYYY.format(round.getWorkDate()) // mod 08/02/22019
              + "&formatted=0" //default = 1
      ;
    URL url = new URL(string_url);
        LOG.info("URL = " + url);
     urlConnection = (HttpsURLConnection) url.openConnection(); // Open the Connection utilise ssl
    //	con.setRequestMethod("GET");// optional default is GET
 //    LOG.info("timeout is = " + urlConnection.getReadTimeout());
     urlConnection.setConnectTimeout(5000); // 5 sec
     urlConnection.setReadTimeout(10000); // 10 sec
     iStream = urlConnection.getInputStream() ;
     
    // see https://github.com/FasterXML/jackson-databind
    // also http://tutorials.jenkov.com/java-json/jackson-objectmapper.html
    
    ObjectMapper mapper = new ObjectMapper(); 
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);// to prevent exception when encountering unknown property:
    // on utilise le package googlemaps pour ne pas en créer un autre ...
 //   LOG.info("line 01");
    responseSS = mapper.readValue(iStream, SunriseSunsetResponse.class);
//        LOG.info("line 02");
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true); // equivalent Pretty Print
        LOG.info("mapper avec indent\n" + mapper.writeValueAsString(responseSS));
    if(responseSS.getStatus().equals("OK"))
    {     //LOG.info("tz getstatuts is = OK");
        LOG.info("Found sunrisesunset response = " + responseSS.getResults().toString());
        LOG.info("Found sunrise = " + responseSS.getResults().getSunrise());
        LOG.info("Found sunset  = " + responseSS.getResults().getSunset());
        //NOTE: All times are in UTC and summer time adjustments are not included in the returned data !!
        // on transforme ci-après la date UTC reçue en date tenant compte de la tz locale
        ZonedDateTime sunrise = ZonedDateTime.parse(responseSS.getResults().getSunrise(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            LOG.info("ZonedDateTime with iso-offset_date_time: " + sunrise);
        Instant instant = sunrise.toInstant();
            LOG.info("instant = " + instant);
     //   sunrise = instant.atZone(ZoneId.of(tz));
        sunrise = instant.atZone(ZoneId.of(club.getClubZoneId())); // mod 08/02/2019
            LOG.info ("formatted HH:mm sunrise = " + ZDF_HOURS.format(sunrise)); 
            LOG.info ("offset = " + sunrise.getOffset());
       ZonedDateTime sunset = ZonedDateTime.parse(responseSS.getResults().getSunset(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
     //   sunset = sunset.toInstant().atZone(ZoneId.of(tz)); // présentation synthétique
        sunset = sunset.toInstant().atZone(ZoneId.of(club.getClubZoneId())); // mod 08/02/2019
            LOG.info ("formatted HH:mm sunset = " + ZDF_HOURS.format(sunset));
            
    // mod 28-11-2018
            Flight f = new Flight();
            f.setSunrise(sunrise);
            f.setSunset(sunset);
            
            return f;
    } else {
        String msg = "responseSS.getStatus() not OK - no Sunrise and Sunset found : " + responseSS.getStatus();
      //  "INVALID_REQUEST": indicates that either lat or lng parameters are missing or invalid;
      //  "INVALID_DATE": indicates that date parameter is missing or invalid;
      //  "UNKNOWN_ERROR": indicates that the request could not be processed due to a server error. The request may succeed if you try again.
        LOG.error(msg);
        return null;
    }
  }catch(JsonGenerationException e) {
	String msg = "££ JsonGenerationException in findSunriseSunset = " + e.getMessage();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
        return null;
  }catch(JsonMappingException e) {
	String msg = "££ JsonMappingException in findSunriseSunset = " + e.getMessage();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
        return null;
  }catch(Exception e) {
            String msg = "££ Exception in findSunriseSunset = " + e.getMessage();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
            return null;
   }finally{ 
      iStream.close();
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
        find.SunriseSunset.liste = liste;
    }


   
 public static void main(String[] args) throws IOException {
  try{
 
   Date date = SDF.parse("23/07/2018");
   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    LocalDate localDate = LocalDate.now();
 //   LOG.info(dtf.format(localDate)); //2016/11/16
    DBConnection dbc = new DBConnection();
   Connection conn = dbc.getConnection();
  // Course course = new Course();
  // course.setIdcourse(13);
   String tz = "Europe/Brussels";
/// à modifier
 //  find.SunriseSunset ssac = new find.SunriseSunset();
 ////////// à modifier Flight fl = ssac.find(date,BigDecimal.valueOf(50.202764), BigDecimal.valueOf(5.013203),tz,conn);
////           LOG.info("response in main = :"  + fl);
   } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }
  
   } // end main//

} // end class addressConvertter