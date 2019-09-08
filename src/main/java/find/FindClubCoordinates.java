package find;

import com.google.maps.model.LatLng;
import entite.Club;
import exceptions.LCCustomException;
import googlemaps.GoogleResult;
import googlemaps.GoogleTimeZone;
import static interfaces.Log.LOG;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.sql.Connection;
import java.sql.SQLException;
import lc.golfnew.GoogleGeoApiController;
import lc.golfnew.ListCountry;
import utils.DBConnection;
import utils.LCUtil;

public class FindClubCoordinates implements interfaces.Log
{
   final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
   
public Club findClubLatLngTz (final Club club){
try{
        LOG.info("entering findClubLatLngTz");
        LOG.info("club = " + club.toString()); //getClubAddress() );  // a été complété par playerCityListener, 
  //      LOG.info("club city = " + club.getClubCity() );  // a été complété par playerCityListener, 
  //      LOG.info("club country = " + club.getClubCountry() ); // a été complété par playerCountryListener, 
  //      LOG.info("club name = " + club.getClubName() ); // a été complété par playerNameListener, 
        //ici on peut chipoter avc GoogleGeoApiController enfin enfin !!!!
        String country_completed = ListCountry.getExtendedCountry(club.getClubCountry() );
            LOG.info("club country completed = " + country_completed) ;
    //    de "HU" on obtient "Hungary" pas sûr que ce soit nécessaire !
///---------- 
        if(country_completed == null){
            String msgerr = "Please complete the country !!!";
            LOG.error(msgerr);
            LCUtil.showMessageFatal(msgerr);
             throw new Exception("Exception throwed" + msgerr);
  //          return null;
        }
        String fullAddress = club.getClubAddress() + "," + club.getClubCity() + "," + country_completed; 
            LOG.info("Assembled club fullAdddress = " + fullAddress) ;
        GoogleGeoApiController ggeo = new GoogleGeoApiController();
        GoogleResult gr = ggeo.findLatLng(fullAddress + ", " + country_completed); // retourne Latitude et Longitude
        if(gr == null){
            String msg="GoogleResult == null";
            // forcer Bruxelles ??
            LOG.info (msg);
            
        }
        
        LatLng latlng = gr.getGeometry().getLocation().getLatlng();
    //    LatLng latlng = ggeo.findLatLng(fullAddress);
            LOG.info(" returned Google club latlng = " + latlng);
        
        if(latlng == null){
            club.setClubLatLng(null);
            String msg = "Incorrect or insuffisant Club address - Please correct and retry !! = ";
             LOG.error(msg);
             club.setClubFormattedAddress(msg);
       // new 03-02-2019      
             GoogleTimeZone gtz = new GoogleTimeZone();

                        if(gtz.getTimeZoneId() == null){
                            LOG.info("GoogleTimeZone is null, forced to Brussels");
                              gtz.setDstOffset(01); //Standard timezone: UTC/GMT +01:00
                              gtz.setTimeZoneId("Europe/Brussels"); // le même pour tous ! par defaut
                              gtz.setTimeZoneName("Central European Time ( CET )");
                         }
             
             
             club.setClubTimeZone(gtz);
             LCUtil.showMessageFatal(msg);
             return club;  // mod 13/02/2019
         //    return null; // retourne d'où îl vient = player.xhtml
             
         }else{
            club.setClubLatLng(latlng);
 /// enlevé 01/08/2017 modifié dans club.java   club.setClubStringLatLng(club.getClubLatLng().toString());  // pour affichage dans player.xhtml
            LOG.info("ClubLatLng = " + club.getClubStringLatLng());
        }
 ///--------------
            GoogleTimeZone tz = ggeo.findTimeZone(latlng, "en");  // à modifier ultérieurement
            LOG.info(" returned Google Club TimeZone = " + tz);
        if(tz == null){
             club.setClubTimeZone(null); 
             String msg = "Time Zone = null - please retry with other address !!b = ";
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
             return null; // retourne d'où il vient
        }else{
             club.setClubTimeZone(tz);  // move global de 3 fields 
                LOG.info("club timezone = " + club.getClubTimeZone());
             club.setClubFormattedAddress(gr.getFormatted_address());
             byte ptext[] = tz.getTimeZoneName().getBytes(ISO_8859_1); 
             String value = new String(ptext, UTF_8); 
              LOG.info("value UTF8 = " + value);
        }
            LOG.info("Club ZoneId      = " + club.getClubTimeZone().getTimeZoneId());
            LOG.info("Club Zone Name   = " + club.getClubTimeZone().getTimeZoneName() );
            LOG.info("Club Formatted Address  = " + club.getClubFormattedAddress() );
    return club;

}catch (LCCustomException e){
   //  LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return null;    
}catch (SQLException e){
    String msg = "SQL Exception in FindClubCoordinates : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in FindClubCoordinates() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
finally
{
      //  DBConnection.closeQuietly(conn, null, rs, ps);
   //     DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

public static void main(String[] args) throws Exception , Exception{

    Connection conn = new DBConnection().getConnection();
    Club club = new Club();
  //  club.setClubAddress("Rua dos Sobreiros da Marinha");
  //  club.setClubCity("2750-005 Cascais");
  //  club.setClubCountry("PT");
  //  club.setClubName("Oitavos Dunes Club");
    
 ///   club.setClubAddress("Grand Del Mar Way 5200");
  //  club.setClubCity("CA 92130 San Diego");
  //  c//lub.setClubCountry("US");
  //  club.setClubName("Grand Del Mar Golf Club");
    
      club.setClubAddress("Carretera Federal km 294, Solidaridad");
    club.setClubCity("77710 Playa del Carmen QR Mexico");
    club.setClubCountry("MX");
    club.setClubName("Riviera Maya");
    
    Club t1 = new FindClubCoordinates().findClubLatLngTz(club);
     LOG.info("Tarif extracted from database = "  + t1.toString());
DBConnection.closeQuietly(conn, null, null, null);
}// end main
}  // end class