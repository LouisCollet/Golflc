
package modify;

import entite.Player;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyPlayer implements Serializable, interfaces.Log, interfaces.GolfInterface{

public boolean modify(final Player player, final Connection conn) throws Exception{
        PreparedStatement ps = null;
        int row = 0;
        boolean b = false;
        try {
            LOG.info("player   = " + player.toString());
    String s = utils.DBMeta.listMetaColumnsUpdate(conn, "player");
        LOG.info("String from listMetaColumns = " + s);
        // encrypted password with SHA2 function of mysql 
 ///   s = s.replace("playerpassword=?" , "playerpassword=sha2(?,256)"); // new 07-08-2018 
        LOG.info("String modified for encryption password sha2 = " + s);
        
    String query = "UPDATE player"
            + " SET " + s
            + " WHERE player.idplayer=?";
        LOG.info("query Modify Player 1 = " + query);

            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setString(1, player.getPlayerFirstName());
            ps.setString(2, player.getPlayerLastName());
            ps.setString(3, player.getPlayerCity());
            ps.setString(4, player.getPlayerCountry());
            ps.setDate(5, LCUtil.getSqlDate(player.getPlayerBirthDate()));
            ps.setString(6, player.getPlayerGender());
            ps.setInt(7, player.getPlayerHomeClub());
            ps.setString(8, player.getPlayerLanguage());
            ps.setString(9, player.getPlayerEmail()); // new 15/11/2012
            ps.setString(10, player.getPlayerTimeZone().getTimeZoneId() ); // new 28/03/2017 using GoogleTimeZone
            ps.setString(11, player.getPlayerLatLng().toString()); // new 28/03/2017
     //      le mot de passe est modifié par ModifyPassword.java
            ps.setString(12, player.getPlayerRole());
            
            ps.setInt(13, player.getIdplayer()); //used fo WHERE ?
    //        ps.setTimestamp(15, LCUtil.getCurrentTimeStamp());
            utils.LCUtil.logps(ps);
      //      ps = ps.replace();
            row = ps.executeUpdate(); // write into database
                LOG.info("row = " + row);
            if (row != 0) 
            {
                String msg =  LCUtil.prepareMessageBean("player.modify");
                msg = msg // + "<h1> successful modify Player : "
                            + " <br/>ID = " + player.getIdplayer()
                            + " <br/>first = " + player.getPlayerFirstName()
                            + " <br/>last = " + player.getPlayerLastName();
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
            }else{
                    String msg = "-- NOT NOT successful modify Player row = 0 !!! ";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
// new 28/12/2014 - à tester                    
                    throw (new SQLException("row = 0 - Could not modify player"));
                //    return false; pas compatible avec throw
            }
return true;
}catch (SQLException sqle) {
            String msg = "£££ SQLException in Modify Player = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in Modify Player = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end modifyPlayer
} //end Class