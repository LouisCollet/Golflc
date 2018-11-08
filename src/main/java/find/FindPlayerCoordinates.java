package find;

import com.google.maps.model.LatLng;
import entite.Player;
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


public class FindPlayerCoordinates implements interfaces.Log
{
   final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
 //  final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
public Player findPlayerLatLngTz (final Player player) //throws SQLException
{   
LOG.info("entering findPlayerLatLngTz " );
   try{     
        LOG.info("player city = " + player.getPlayerCity() );  // a été complété par playerCityListener, 
        LOG.info("player country = " + player.getPlayerCountry() ); // a été complété par playerCountryListener, 
        //ici on peut chipoter avc GoogleGeoApiController enfin enfin !!!!
        String country_completed = ListCountry.getExtendedCountry(player.getPlayerCountry());
            LOG.info("Player country completed = " + country_completed) ;
    //    String address = player.getPlayerCity() + "," + address_completed;
        GoogleGeoApiController ggeo = new GoogleGeoApiController();
       
        GoogleResult gr = ggeo.findLatLng(player.getPlayerCity() + ", " + country_completed);
        LatLng latlng = gr.getGeometry().getLocation().getLatlng();// = ggeo.findLatLng(player.getPlayerCity() + ", " + country_completed);
        ; //
            LOG.info(" returned Google Player latlng = " + latlng);
        if(latlng == null){
            player.setPlayerLatLng(null);
            String msg = "Incorrect or insuffisant Player address - Please correct and retry !! = ";
             LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null; // retrourne d'où îl vient
            
        }else{
            player.setPlayerLatLng(latlng);
            player.setPlayerStringLatLng(player.getPlayerLatLng().toString());  // pour affichage dans player.xhtml
                LOG.info("Player PlayerLatLng = " + player.getPlayerStringLatLng());
        }
            LOG.info("playerLanguage = " + player.getPlayerLanguage()); // a été complété par playerLanguageListener, 
///--------------
    //        GoogleGeoApiController ggeo = new GoogleGeoApiController();
            GoogleTimeZone tz = ggeo.findTimeZone(latlng, player.getPlayerLanguage());  // à modifier ultérieurement
            LOG.info(" returned Player Google TimeZone = " + tz);
       
        if(tz == null){
             player.setPlayerZoneId("null"); //.setPlayerZoneId().timeZoneId(null); //.   ("Europe/Brussels");
             String msg = "Time Zone Player = null - please retry with other address !! Back to sender ! = ";
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
             return null; // retrourne d'où îl vient
        }else{
             player.setPlayerTimeZone(tz);  // new
                LOG.info("tz.getTimeZoneId() = " + tz.getTimeZoneId());
             player.setPlayerZoneId(tz.getTimeZoneId());
             // new fonctionne ?
             byte ptext[] = tz.getTimeZoneName().getBytes(ISO_8859_1); 
             String value = new String(ptext, UTF_8); 
             player.setPlayerZoneName(value);
             // old
             player.setPlayerZoneName(tz.getTimeZoneName());
             /// pourquoi pas DST ?
          //   player.setPlayerTimeZone(tz); //.getTimeZoneId() ); // + " / " + tz.getTimeZoneName() );
        }
            LOG.info("Player ZoneId      = " + player.getPlayerTimeZone().getTimeZoneId());
            LOG.info("Player Zone Name   = " + player.getPlayerTimeZone().getTimeZoneName() );
            LOG.info("Player Zone Name   = " + player.getPlayerTimeZone().getTimeZoneName() );
   //     return "selectHomeClub.xhtml?faces-redirect=true";
    return player;

}catch (LCCustomException e){
  //  String msg = " SQL Exception in getScoreCardList1() " + e;
  //  LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return null;    
}catch (NullPointerException npe){
    String msg = "NullPointerException in " + CLASSNAME + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (SQLException e){
    String msg = "SQL Exception in FindPlayerCoordinates : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in FindPlayerCoordinates() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
finally
{
}
} //end method
public static void main(String[] args) throws Exception , Exception{

        LOG.info("starting main of FindPlyerCoordinates");
    Connection conn = new DBConnection().getConnection();
  //  Course course = new Course();
  //  course.setIdcourse(102);
    Player player = new Player();
    player.setPlayerCity("Brussels");
    player.setPlayerCountry("BE");
    // fille in the fields
  //  FindTarifData ftd = new FindTarifData();
    player = new FindPlayerCoordinates().findPlayerLatLngTz(player); 
     LOG.info("Plyer is now = "  +player.toString());
//findPlayerHandicap(player,round, conn);
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main

}  // end class