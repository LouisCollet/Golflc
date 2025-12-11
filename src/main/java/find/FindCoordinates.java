package find;

import br.com.esign.google.geocode.GoogleGeocode;
import br.com.esign.google.geocode.model.GeocodeResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import entite.Club;
import entite.Player;
import static interfaces.GolfInterface.GoogleApiKey;
import static interfaces.Log.LOG;
import java.sql.SQLException;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class FindCoordinates {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 // https://github.com/googlemaps/google-maps-services-java
/*
 public GeocodingResult[] findGeocodingResult(String fullAddress) {
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
try{
             LOG.debug("entering " + methodName + " ,searching for fullAddress = " + fullAddress);
          GeocodingResult[] results;
          try (GeoApiContext context = new GeoApiContext.Builder()  // automatic close of context
               .apiKey(GoogleApiKey)
               .maxRetries(2)
               .build()){
                   results = GeocodingApi.geocode(context, fullAddress).await(); // Synchronous : attend la réponse
               } 
           LOG.debug("status = " + results[0]);
         if(results.length == 0){
             throw new Exception(" <br/>no GPS coordinates found : incomplete address data");
         }
           LOG.debug("there are number of results = " + results.length);
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
             LOG.debug("Results gson format : " + gson.toJson(results[0])); //.addressComponents));
         if(results[0].partialMatch){
            String msg = "info : this is a partial match ! - consider completing the address data ";
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
         }
    // new 15-10-2024  https://github.com/esign-consulting/google-geocode   
         
         GoogleGeocode googleGeocode = new GoogleGeocode(GoogleApiKey, fullAddress); // the address must not be encoded
     //    String jsonString = googleGeocode.getJsonString(); // may throw IOException
      //    LOG.debug("jsonString = " + gson.toJson(jsonString));
//Alternatively, an object representing the Google Geocoding API json response can be returned:
        GeocodeResponse geocodeResponse = googleGeocode.getResponseObject(); // may throw IOException
        LOG.debug("response object = " + geocodeResponse);
// This object is usefull to get the response content:
      if (geocodeResponse.isStatusOK()) {
          LOG.debug("status is OK);");
      }else{
          LOG.debug("error message = " + geocodeResponse.getErrorMessage());
       //  String country = getCountryShortName(); // returns the country short name of the first result
      }
         
        LOG.debug("country short name = " + geocodeResponse.getCountryShortName());  // BE
        LOG.debug("formatted adress = " + geocodeResponse.getFormattedAddress());
    //    BigDecimal d = geocodeResponse.getGeometry().getLocation().getLat();

        LOG.debug("lng = " + geocodeResponse.getGeometry().getLocation().getLng()); // c'est un bigdecimal
        entite.LatLng latlng = new entite.LatLng(); // double
        latlng.setLat(geocodeResponse.getGeometry().getLocation().getLat().doubleValue());
        LOG.debug("geometry location = " + geocodeResponse.getGeometry().getLocation()); // Location
        LOG.debug("formatted adress = " + geocodeResponse.getFormattedAddress());
        LOG.debug("long namet = " + geocodeResponse.getLocalityLongName());
     //   LOG.debug("formatted results = " + geocodeResponse.getResults());
      return results;

}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{}
} //end method
*/
/*    
    
  public GeocodeResponse findGeocoding(String fullAddress) {
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
try{
             LOG.debug("entering " + methodName + " ,searching for fullAddress = " + fullAddress);
        /*  GeocodingResult[] results;
          try (GeoApiContext context = new GeoApiContext.Builder()  // automatic close of context
               .apiKey(GoogleApiKey)
               .maxRetries(2)
               .build()){
                   results = GeocodingApi.geocode(context, fullAddress).await(); // Synchronous : attend la réponse
               } 
           LOG.debug("status = " + results[0]);
         if(results.length == 0){
             throw new Exception(" <br/>no GPS coordinates found : incomplete address data");
         }
           LOG.debug("there are number of results = " + results.length);
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
             LOG.debug("Results[0] are : " + gson.toJson(results[0])); //.addressComponents));
         if(results[0].partialMatch){
            String msg = "info : this is a partial match ! - consider completing the address data ";
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
         }
         
    // new 15-10-2024  https://github.com/esign-consulting/google-geocode   
         GoogleGeocode googleGeocode = new GoogleGeocode(GoogleApiKey, fullAddress); // the address must not be encoded
     //    String jsonString = googleGeocode.getJsonString(); // may throw IOException
      //    LOG.debug("jsonString = " + gson.toJson(jsonString));
//Alternatively, an object representing the Google Geocoding API json response can be returned:
        GeocodeResponse geocodeResponse = googleGeocode.getResponseObject(); // may throw IOException
        LOG.debug("response object = " + geocodeResponse);
// This object is usefull to get the response content:
      if (geocodeResponse.isStatusOK()) {
          LOG.debug("status is OK);");
      }else{
          LOG.debug("error message = " + geocodeResponse.getErrorMessage());
       //  String country = getCountryShortName(); // returns the country short name of the first result
      }
         
        LOG.debug("country short name = " + geocodeResponse.getCountryShortName());  // BE
        LOG.debug("formatted adress = " + geocodeResponse.getFormattedAddress());
    //    BigDecimal d = geocodeResponse.getGeometry().getLocation().getLat();

        LOG.debug("lng = " + geocodeResponse.getGeometry().getLocation().getLng()); // c'est un bigdecimal
        entite.LatLng latlng = new entite.LatLng(); // double
        latlng.setLat(geocodeResponse.getGeometry().getLocation().getLat().doubleValue());
        LOG.debug("geometry location = " + geocodeResponse.getGeometry().getLocation()); // Location
        LOG.debug("formatted adress = " + geocodeResponse.getFormattedAddress());
        LOG.debug("long namet = " + geocodeResponse.getLocalityLongName());
     //   LOG.debug("formatted results = " + geocodeResponse.getResults());
      return geocodeResponse;

}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{}
} //end method
  */
 public Club clubCoordinates(Club club) throws Exception {
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
try{
          LOG.debug("entering " + methodName + " ,searching for : " + club);
    String fullAddress = club.getAddress().getStreet() + "," + club.getAddress().getCity() + "," + club.getAddress().getCountry().getCode(); 
  //  GoogleGeocode googleGeocode = new GoogleGeocode(GoogleApiKey, fullAddress); // the address must not be encoded
  //  GeocodeResponse geocodeResponse = googleGeocode.getResponseObject(); // may throw IOException
    GeocodeResponse geocodeResponse = new GoogleGeocode(GoogleApiKey, fullAddress).getResponseObject();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LOG.debug("geocode Response in gson format : " + gson.toJson(geocodeResponse)); //.addressComponents));
    if(geocodeResponse.isStatusOK()) {
          LOG.debug("status is OK;");
    }else{
          String msg = "ClubCoordinates not found ! " + geocodeResponse.getErrorMessage();
          LOG.info(msg);
          showMessageFatal(msg);
    }
        LOG.debug("country short name = " + geocodeResponse.getCountryShortName());  // BE
        LOG.debug("formatted adress = " + geocodeResponse.getFormattedAddress());
    entite.LatLng latlng = new entite.LatLng(); // double
    latlng.setLat(geocodeResponse.getGeometry().getLocation().getLat().doubleValue()); // BigDecimal to double
    latlng.setLng(geocodeResponse.getGeometry().getLocation().getLng().doubleValue());
        LOG.debug("latlng converted from BigDecimal = " + latlng);
        LOG.debug("long name = " + geocodeResponse.getLocalityLongName());
    club.getAddress().setLatLng(latlng);
    java.util.TimeZone timeZone = find.FindTimeZone.find(latlng);
        LOG.debug("TimeZone found = " + timeZone);
    club.getAddress().setZoneId(timeZone.getID());
    club.setShowCoordinatesManual(true); // new 07-12-2023 affiche possibilité corrections manuelles
    String msg = "Coordinates LatLng and timeZone inserted in club " + club;
    LOG.info(msg);
    showMessageInfo(msg);    
   return club;
}catch (Exception ex){
    String msg =  LCUtil.prepareMessageBean("exception.findcoordinates.club")
            + "<br>"+ "street = " + club.getAddress().getStreet()
            + "<br>"+ "city = " + club.getAddress().getCity()
            + "<br>"+ "country code= " + club.getAddress().getCountry().getCode()
            + "<br>"+ "country name = " + club.getAddress().getCountry().getName();
    LOG.error(msg); 
    LCUtil.showMessageFatal(msg);
    return null;
}finally{}
} //end method
 /*
public Club clubCoordinates_old(Club club) throws Exception {
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
try{
          LOG.debug("entering " + methodName + " ,searching for : " + club);
    String fullAddress = club.getAddress().getStreet() + "," + club.getAddress().getCity() + "," + club.getAddress().getCountry().getCode(); 
    GeocodingResult[] results = findGeocodingResult(fullAddress);
    if(results.length > 0){
         //     LOG.debug("Club Coordinates found = " + results[0].toString());
           var v = results[0];
       //    LOG.debug("placeId = " + v.placeId);
       //    LOG.debug("plusCode = " + v.plusCode);
           entite.LatLng latlng = new entite.LatLng(); // pas onfondre avec import com.google.maps.model.LatLng;
           LOG.debug("geometry location= " + v.geometry.location); // LatLng
           latlng.setLat(v.geometry.location.lat);
           latlng.setLng(v.geometry.location.lng);
           club.getAddress().setLatLng(latlng);
           com.google.maps.model.LatLng latlngGoogle = new com.google.maps.model.LatLng();
           latlngGoogle.lat = v.geometry.location.lat;
           java.util.TimeZone timezone = find.FindTimeZone.find(results); // utilise 
           
           club.getAddress().setZoneId(timezone.getID());
           club.setShowCoordinatesManual(true); // new 07-12-2023 affiche possibilité corrections manuelles
              LOG.debug("club with latlng and timezone inserted = " + club);
        return club;
    }else{
         LOG.debug("no results found ! ");
        return null;
    }
}catch (Exception ex){
    String msg =  LCUtil.prepareMessageBean("exception.findcoordinates.club")
            + "<br>"+ "street = " + club.getAddress().getStreet()
            + "<br>"+ "city = " + club.getAddress().getCity()
            + "<br>"+ "country code= " + club.getAddress().getCountry().getCode()
            + "<br>"+ "country name = " + club.getAddress().getCountry().getName();
    LOG.error(msg); 
    LCUtil.showMessageFatal(msg);
    return null;
}finally{}
} //end method
*/
 
public Player playerCoordinates(Player player) throws SQLException, Exception{ // mod 11/04/2022
try{
           LOG.debug("entering playerCoordinates");
      String fullAddress = player.getAddress().getStreet() + ", " + player.getAddress().getZipCode() + player.getAddress().getCity()// + ", "
                          + ", " + player.getAddress().getCountry().getCode(); 
 /*     GeocodingResult[] result = new FindCoordinates().findGeocodingResult(fullAddress);
      if(result.length > 0){
              LOG.debug("number of results = " + result.length);
           var v = result[0];
     //         LOG.debug ("findPlayerCoordinates - v geometry location lat = " + v.geometry.location.lat); // mod 29-10-2023
     //         LOG.debug ("findPlayerCoordinates - v geometry location lng = " + v.geometry.location.lng);
           entite.LatLng latlng = new entite.LatLng();
           latlng.setLat(v.geometry.location.lat);
           latlng.setLng(v.geometry.location.lng);
      */     
    GeocodeResponse geocodeResponse = new GoogleGeocode(GoogleApiKey, fullAddress).getResponseObject();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LOG.debug("geocode Response in gson format : " + gson.toJson(geocodeResponse)); //.addressComponents));
    if(geocodeResponse.isStatusOK()) {
          LOG.debug("status is OK;");
    }else{
          String msg = "playerCoordinates not found ! " + geocodeResponse.getErrorMessage();
          LOG.info(msg);
          showMessageFatal(msg);
          player = null;
          return player;
    }
        LOG.debug("country short name = " + geocodeResponse.getCountryShortName());  // BE
        LOG.debug("formatted adress = " + geocodeResponse.getFormattedAddress());
    entite.LatLng latlng = new entite.LatLng(); // double
    latlng.setLat(geocodeResponse.getGeometry().getLocation().getLat().doubleValue()); // BigDecimal to double
    latlng.setLng(geocodeResponse.getGeometry().getLocation().getLng().doubleValue());
        LOG.debug("latlng converted from BigDecimal = " + latlng);
        LOG.debug("long name = " + geocodeResponse.getLocalityLongName());
    player.getAddress().setLatLng(latlng);
    //    LOG.debug("for country - = " + v.types.toString());
    //    LOG.debug("for country - = " + v.addressComponents.toString());
    java.util.TimeZone timezone = find.FindTimeZone.find(latlng);
    player.getAddress().setZoneId(timezone.getID());  // Europe/Brussels
           String msg = "Coordinates inserted in player " + player;
           LOG.info(msg);
           showMessageInfo(msg);
  //     }else{
  //          return null; //player.setFormattedAddress("no valid address found in google - try again");
  //     }
       return player;
 }catch (Exception ex){
            String msg = "Exception in playerCoordinates " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method playerCoordinates

void main() throws SQLException, Exception{
  //  String fullAddress = "Rue de l'Amazone 55,B-1060 Brussels,BE"; 
    String fullAddress = "amazone,belgium"; 
  //  GeocodingResult[] gso = new FindCoordinates().findGeocodingResult(fullAddress);
   //    LOG.debug("there are number of results = " + gso.length);
  //  var v = gso[0];
    
    //   LOG.debug("formatted address = " + v.formattedAddress);
  //     LOG.debug("lat lng = " + v.geometry.location);
  //  boolean b = v.partialMatch;
    //   LOG.debug("is there a partial match ? = " + b);
}// end main
} // end Class