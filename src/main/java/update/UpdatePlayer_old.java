package update;

import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class UpdatePlayer implements Serializable, interfaces.GolfInterface{

public boolean update(final Player player, final Connection conn) throws Exception{
        PreparedStatement ps = null;
        int row = 0;
        boolean b = false;
 try {
          LOG.debug("entering UpdatePlayer.update");
          LOG.debug(" with player = " + player);
    String pl = utils.DBMeta.listMetaColumnsUpdate(conn, "player");
          LOG.debug("String from listMetaColumns = " + pl);
          LOG.debug("String modified for encryption password sha2 = " + pl);
        final String query = """
          UPDATE player
          SET %s
          WHERE player.idplayer=?;
         """.formatted(pl) ; 
    LOG.debug("query formatted Update Player = " + query);
        // mod 14-04-2020
    ps = conn.prepareStatement(query);
    ps = Player.psPlayerModify(ps,player);
    utils.LCUtil.logps(ps);
    row = ps.executeUpdate(); // write into database
  //              LOG.debug("row = " + row);
            if (row != 0){
                String msg =  LCUtil.prepareMessageBean("player.modify");
                msg = msg // + "<h1> successful modify Player : "
                            + " <br/>ID = " + player.getIdplayer()
                            + " <br/>first = " + player.getPlayerFirstName()
                            + " <br/>last = " + player.getPlayerLastName();
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
            }else{
                    String msg = "row = 0 - Could not modify player";
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
// new 28/12/2014 - à tester                    
                    throw (new SQLException(msg));
                //    return false; pas compatible avec throw
            }
return true;
}catch (SQLException sqle) {
            String msg = "£££ SQLException in Modify Player = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw (new SQLException(msg)); // new 01-09-2019
        //    return false;
   } catch (Exception nfe) {
            String msg = "£££ Exception in Modify Player = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end modifyPlayer
} //end Class