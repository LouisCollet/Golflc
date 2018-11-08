package lists;

import entite.Player;
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
 try{       
        liste = new ArrayList<>();
        String p = utils.DBMeta.listMetaColumnsLoad(conn, "player");  // fields list, comma separated
        final String query =
            "SELECT " + p 
       //         + " idplayer, PlayerFirstName, PlayerLastName, PlayerCity, PlayerZoneId, PlayerCountry, "
       //     + "PlayerBirthDate, PlayerGender, PlayerLatLng,"
       //     + "PlayerHomeClub, PlayerLanguage, PlayerEmail, PlayerPhotoLocation, PlayerModificationDate, PlayerPassword"
            + " FROM Player"
            + " WHERE PlayerActivation = '1' "
            + " ORDER BY idplayer;";

            ps = conn.prepareStatement(query);
//                LOG.info("line03");
             utils.LCUtil.logps(ps);
 //               LOG.info("line04");
            rs = ps.executeQuery();
            while (rs.next())
            {
                liste.add(entite.Player.mapPlayer(rs));
            }
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
    


    public static List<Player> getListe() {
        return liste;
    }

    public static void setListe(List<Player> liste) {
        PlayersList.liste = liste;
    }

} //end Class