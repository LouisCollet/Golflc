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
import static exceptions.LCException.handleGenericException;

public class psCreateUpdatePlayer implements Serializable{
    
 public static PreparedStatement psMapUpdate(PreparedStatement ps, Player player){
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
        LOG.debug("entering psMapUpdate with player = {}", player);
            ps.setString(1, player.getPlayerFirstName());
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
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
   //// ps. 12 modificationDate non nécessaire (faite par DB System)
return ps;
  }catch(Exception e){
    handleGenericException(e, methodName);
    return null;
  }
} //end method

    /**
     * Prépare le PreparedStatement pour un update de Player.
     */
    public static void mapUpdate(PreparedStatement ps, Player player)throws Exception {
        int index = 0;
        try {
            LOG.debug("entering mapUpdate for player = {}", player);
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
            LOG.debug("at the end ps is = {}", ps);
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
        final String methodName = LCUtil.getCurrentMethodName();
        try {
            int index = 0;
            LOG.debug("entering mapCreate with player = {}", player);
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