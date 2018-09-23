package lists;

import com.google.maps.model.LatLng;
import entite.Player;
import googlemaps.GoogleTimeZone;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class PlayersList implements interfaces.Log
{
    private static List<Player> liste = null;
    
public List<Player> getListAllPlayers(final Connection conn) throws Exception
{
    
if(liste == null)
{    
    LOG.debug("starting listAllPlayers() with conn = " + conn );
        PreparedStatement ps = null;
        ResultSet rs = null;
        liste = new ArrayList<>();
        final String query =
            "SELECT idplayer, PlayerFirstName, PlayerLastName, PlayerCity, PlayerZoneId, PlayerCountry, "
            + "PlayerBirthDate, PlayerGender, PlayerLatLng,"
            + "PlayerHomeClub, PlayerLanguage, PlayerEmail, PlayerPhotoLocation, PlayerModificationDate, PlayerPassword"
            + " FROM Player"
            + " WHERE PlayerActivation = '1' "
            + " ORDER BY idplayer;";
try
{
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next())
            {
                liste.add(mapPlayer(rs));
            }
  //  LOG.debug("closing listAllPlayers() with players = " + Arrays.deepToString(liste.toArray()) );
  
  //   liste.forEach(item -> LOG.info("Players list " + item));  // java 8 lambda
  
return liste;

} catch(SQLException sqle){
    String msg = "£££ SQL exception in ListAllPlayers = " + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch(Exception e){
    String msg = "£££ Exception in ListAllPlayers = " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
           DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

}else{
  ////   LOG.debug("escaped to listallplayers repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
   //then you should introduce lazy loading inside the getter method. I.e. if the property is null,
    //then load and assign it to the property, else return it.
}
    //end if

} //end method
    
    private static Player mapPlayer(ResultSet rs) throws SQLException
{
        Player p = new Player();
        p.setIdplayer(rs.getInt("idplayer"));
 //           LOG.info(" -- map : playerId = " + p.getIdplayer() );
        p.setPlayerFirstName(rs.getString("playerfirstname"));
        p.setPlayerLastName(rs.getString("playerlastname"));
        p.setPlayerCity(rs.getString("playercity"));
        p.setPlayerLanguage(rs.getString("PlayerLanguage"));
        p.setPlayerEmail(rs.getString("PlayerEmail"));
  //      p.setPlayerCity(rs.getString("playercity"));
// try{
      GoogleTimeZone tz = new GoogleTimeZone();
      tz.setTimeZoneId(rs.getString("PlayerZoneId"));
      if(tz.getTimeZoneId() == null){
          tz.setTimeZoneId("Europe/Brussels"); // le même pour tous ! par defaut
      }
      p.setPlayerTimeZone(tz);
  //      LOG.info("playerTimeZoneId = " + p.getPlayerTimeZone().getTimeZoneId());
  //  String s = rs.getString("PlayerLatLng");
    String[] latlng = null;
    if(rs.getString("PlayerLatLng") == null){ 
          latlng = "50.8262271,4.3571382".split(",");  // le même pour tous ! par defaut
    }else{
          latlng = rs.getString("PlayerLatLng").split(",");
    }
        double latitude = Double.parseDouble(latlng[0]);
        double longitude = Double.parseDouble(latlng[1]);
        LatLng location = new LatLng(latitude, longitude);
        p.setPlayerLatLng(location);
  ////       LOG.info("PlayerLatLng = " + p.getPlayerLatLng());
 //       LOG.info("step 6");
//  }catch(ClassCastException e){
//    String msg = "£££ ClassCastException in rs " + e.getMessage()+ " for player = " + p.getPlayerLastName();
//    LOG.error(msg);
//    LCUtil.showMessageFatal(msg);
//  }catch(Exception e){
//    String msg = "£££ Exception in rs = " + e.getMessage()+ " for player = " + p.getPlayerLastName();
//    LOG.error(msg);
//    LCUtil.showMessageFatal(msg);
//  }
        p.setPlayerCountry(rs.getString("playerCountry"));
        p.setPlayerBirthDate(rs.getDate("playerbirthdate"));
        p.setPlayerGender(rs.getString("playergender"));
        p.setPlayerHomeClub(rs.getInt("playerhomeclub"));
        p.setPlayerLanguage(rs.getString("playerLanguage"));
        p.setPlayerEmail(rs.getString("playerEmail"));
        p.setPlayerPhotoLocation(rs.getString("PlayerPhotoLocation"));
        p.setPlayerPassword(rs.getString("PlayerPassword"));
        p.setPlayerModificationDate(rs.getTimestamp("playerModificationDate"));
     //   p.set
   //        LOG.info("map = success !!! " + p.toString());
return p;
} //end method

    public static List<Player> getListe() {
        return liste;
    }

    public static void setListe(List<Player> liste) {
        PlayersList.liste = liste;
    }

} //end Class