package sql.preparedstatement;

import entite.Player;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;
import static utils.LCUtil.getCurrentMethodName;

public class psCreateUpdatePlayer implements Serializable, interfaces.Log, interfaces.GolfInterface{
    
 public static PreparedStatement psMapUpdate(PreparedStatement ps, Player player){
 //   final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
      // voir aussi http://www.javased.com/index.php?source_dir=archaius/archaius-core/src/main/java/com/netflix/config/sources/JDBCConfigurationSource.java
        LOG.debug("entering psMapUpdate with player = " + player);
      // voir aussi http://www.javased.com/index.php?source_dir=archaius/archaius-core/src/main/java/com/netflix/config/sources/JDBCConfigurationSource.java
      int index = 0;
            ps.setString(++index, player.getPlayerFirstName());
            ps.setString(2, player.getPlayerLastName());
            ps.setString(3, player.getAddress().getStreet()); // new 05-09-2022
            ps.setString(4, player.getAddress().getCity());
      //      ps.setString(5, player.getAddress().getCountry());
            // mod 22-12-2022
            ps.setString(5, player.getAddress().getCountry().getCode().toUpperCase());
            ps.setTimestamp(6,Timestamp.valueOf(player.getPlayerBirthDate())); // BirthDate format LocalDateTime
            ps.setString(7, player.getPlayerGender());
            ps.setInt(8, player.getPlayerHomeClub());
            ps.setString(9, player.getPlayerLanguage());
            ps.setString(10, player.getPlayerEmail());
            ps.setString(11, player.getAddress().getZoneId() ); // mod 06-12-2023
            String s = Double.toString(player.getAddress().getLatLng().getLat()) 
                    + "," + Double.toString(player.getAddress().getLatLng().getLng());
    //           LOG.debug("String latlng for DB = " + s);
            ps.setString(12, s); // mod 04-04-2021
     //      le mot de passe est modifié par ModifyPassword.java
            ps.setString(13, player.getPlayerRole());
        //clé de recherche used for WHERE ?    
            ps.setInt(14, player.getIdplayer()); 
            ps.getWarnings(); // new 27-04-2025
   //// ps. 12 modificationDate non nécessaire (faite par DB System)
return ps;
  }catch(Exception e){
   String msg = "£££ Exception in psClubUpdate = " + getCurrentMethodName() + " / "+ e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
 /*
 public static PreparedStatement psMapCreate(PreparedStatement ps, Player player, String batch){
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
      int index = 0;
        LOG.debug("entering psPlayerCreate");
      ps.setInt(1, player.getIdplayer());
      ps.setString(2, player.getPlayerFirstName());
      ps.setString(3, player.getPlayerLastName());
      ps.setString(4, player.getAddress().getStreet()); // new 05/09/2022
      ps.setString(5, player.getAddress().getZipCode() + " " + player.getAddress().getCity()); // mod 31-12-2022
      ps.setString(6, player.getAddress().getCountry().getCode()); // mod 22/12/2022
      ps.setTimestamp(7,Timestamp.valueOf(player.getPlayerBirthDate()));
      ps.setString(8, player.getPlayerGender());
      ps.setInt(9, player.getPlayerHomeClub());
            if(player.iseID()){ // player with belgian eID
                 ps.setString(10,player.getPlayerPhotoLocation());
 //                       LOG.debug("photo file for database = " + photo );
            }else{
                 ps.setString(10,"no photo.jpeg");
//                 LOG.debug("no photo.jpeg !! " + photo );
            }
       ps.setString(11, player.getPlayerLanguage());
       ps.setString(12, player.getPlayerEmail());
            if(batch.equals("B")){
                LOG.debug("is batch execution ");
                ps.setShort(13, Short.parseShort("1"));  // player activation
            }else{
                ps.setShort(13, (short) 0);}
       //     ps.setString(13, player.getPlayerTimeZone().getTimeZoneId() ); // new 28/03/2017 using GoogleTimeZone
      ps.setString(14, player.getAddress().getZoneId());
            String s = Double.toString(player.getAddress().getLatLng().getLat())
                    + "," + Double.toString(player.getAddress().getLatLng().getLng());
  //             LOG.debug("String latlng for DB = " + s);
      ps.setString(15, s);
      ps.setString(16, null);  // le password est null à la création !!
      ps.setString(17, null);  // les previous passwords sont null à la création !!
      ps.setString(18, "PLAYER"); // PlayerRole = default
      ps.setTimestamp(19, Timestamp.from(Instant.now()));
            ps.getWarnings(); // new 27-04-2025
return ps;
  }catch(Exception e){
   String msg = "£££ Exception in psClubCreate = " + methodName + " / "+ e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} //end class

package sql.preparedstatement;

import entite.Player;
import static interfaces.Log.LOG;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;

public class psCreateUpdatePlayer {
*/
    /**
     * Prépare le PreparedStatement pour un update de Player.
     */
    public static void mapUpdate(PreparedStatement ps, Player player)throws Exception {
        int index = 0;
        try {
            LOG.debug("entering mapUpdate for player = player");
            ps.setString(++index, player.getPlayerFirstName());
            ps.setString(++index, player.getPlayerLastName());
            ps.setString(++index, player.getAddress().getStreet());
            ps.setString(++index, player.getAddress().getCity());
            ps.setString(++index, player.getAddress().getCountry().getCode().toUpperCase());
            ps.setTimestamp(++index, Timestamp.valueOf(player.getPlayerBirthDate()));
            ps.setString(++index, player.getPlayerGender());
            ps.setInt(++index, player.getPlayerHomeClub());
            ps.setString(++index, player.getPlayerLanguage());
            ps.setString(++index, player.getPlayerEmail());
            ps.setString(++index, player.getAddress().getZoneId());
            String s = player.getAddress().getLatLng().getLat() + "," + player.getAddress().getLatLng().getLng();
            ps.setString(++index, s);
            ps.setString(++index, player.getPlayerRole());
            ps.setInt(++index, player.getIdplayer()); // WHERE idplayer
            LOG.debug("at the end ps is = " + ps);
        } catch (Exception e) {
            String msg = "Exception in mapUpdate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }

    /**
     * Prépare le PreparedStatement pour un insert de Player. sans return !!
     */
    public static void mapCreate(PreparedStatement ps, Player player, String batch) throws Exception {
   //     final String methodName = LCUtil.getCurrentMethodName();
        try {
            int index = 0;
            LOG.debug("entering mapCreate with player = " + player);
            ps.setInt(++index, player.getIdplayer());
            ps.setString(++index, player.getPlayerFirstName());
            ps.setString(++index, player.getPlayerLastName());
            ps.setString(++index, player.getAddress().getStreet());
            ps.setString(++index, player.getAddress().getZipCode() + " " + player.getAddress().getCity());
            ps.setString(++index, player.getAddress().getCountry().getCode());
            ps.setTimestamp(++index, Timestamp.valueOf(player.getPlayerBirthDate()));
            ps.setString(++index, player.getPlayerGender());
            ps.setInt(++index, player.getPlayerHomeClub());
            ps.setString(++index, player.iseID() ? player.getPlayerPhotoLocation() : "no photo.jpeg");
            ps.setString(++index, player.getPlayerLanguage());
            ps.setString(++index, player.getPlayerEmail());
            ps.setShort(++index, batch.equals("B") ? (short) 1 : (short) 0);
            ps.setString(++index, player.getAddress().getZoneId());
            String s = player.getAddress().getLatLng().getLat() + "," + player.getAddress().getLatLng().getLng();
            ps.setString(++index, s);
            ps.setString(++index, null); // password
            ps.setString(++index, null); // previous password
            ps.setString(++index, "PLAYER"); // role par défaut
            ps.setTimestamp(++index, Timestamp.from(Instant.now()));

        } catch (Exception e) {
            String msg = "Exception in mapCreate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }
} // end class