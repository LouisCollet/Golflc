package find;

import com.google.maps.model.LatLng;
import entite.Club;
import exceptions.LCCustomException;
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

// liste des tee d'un course //
public class FindClubCoordinates implements interfaces.Log
{
 //  private static List<String> liste = null;
   final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
   
public Club findClubLatLngTz (final Club club) //throws SQLException
{   
try
{   
        LOG.info("club address = " + club.getClubAddress() );  // a été complété par playerCityListener, 
        LOG.info("club city = " + club.getClubCity() );  // a été complété par playerCityListener, 
        LOG.info("club country = " + club.getClubCountry() ); // a été complété par playerCountryListener, 
        LOG.info("club name = " + club.getClubName() ); // a été complété par playerNameListener, 
        //ici on peut chipoter avc GoogleGeoApiController enfin enfin !!!!
        String country_completed = ListCountry.getExtendedCountry(club.getClubCountry() );
            LOG.info("club country completed = " + country_completed) ;
    //    de "HU" on obtient "Hungary" pas sûr que ce soit nécessaire !
///---------- 
        String fullAddress = club.getClubAddress() + "," + club.getClubCity() + "," + country_completed; 
            LOG.info("club fullAdddress = " + fullAddress) ;
        LatLng latlng = GoogleGeoApiController.findLatLng(fullAddress);
            LOG.info(" returned Google club latlng = " + latlng);
        if(latlng == null){
            club.setClubLatLng(null);
            String msg = "Incorrect or insuffisant Club address - Please correct and retry !! = ";
             LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null; // retourne d'où îl vient = player.xhtml
            
        }else{
            club.setClubLatLng(latlng);
 /// enlevé 01/08/2017 modifié dans club.java   club.setClubStringLatLng(club.getClubLatLng().toString());  // pour affichage dans player.xhtml
                LOG.info("ClubLatLng = " + club.getClubStringLatLng());
        }
 ///--------------
            GoogleTimeZone tz = GoogleGeoApiController.findTimeZone(latlng, "en");  // à modifier ultérieurement
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
             byte ptext[] = tz.getTimeZoneName().getBytes(ISO_8859_1); 
             String value = new String(ptext, UTF_8); 
              LOG.info("value UTF8 = " + value);
        }
            LOG.info("Club ZoneId      = " + club.getClubTimeZone().getTimeZoneId());
            LOG.info("Club Zone Name   = " + club.getClubTimeZone().getTimeZoneName() );
        
    return club;

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
  //  Course course = new Course();
  //  course.setIdcourse(102);
  
  // ne fonctionne pas !
    Club club = new Club();
  //  FindTarifData ftd = new FindTarifData();
    Club t1 = new FindClubCoordinates().findClubLatLngTz(club);
     LOG.info("Tarif extracted from database = "  + t1.toString());
//findPlayerHandicap(player,round, conn);
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main




}  // end class