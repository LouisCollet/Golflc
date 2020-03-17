
package modify;

import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyPlayerActivation implements interfaces.Log, interfaces.GolfInterface{
    
 public String modify(Player player, Connection conn) throws SQLException{
  //  Connection conn = null;
    PreparedStatement ps = null;
try{
  //  DBConnection dbc = new DBConnection();
 //   conn = dbc.getConnection();
        LOG.info("starting update activation table Player.. = " + player);
    String query = "UPDATE player "
            + " SET PlayerActivation = 1 "
            + " WHERE idplayer = ?";
    ps = conn.prepareStatement(query);
    ps.setInt(1,player.getIdplayer() );
         //    String p = ps.toString();
         utils.LCUtil.logps(ps);
   int row = ps.executeUpdate();
      if (row!=0){
          LOG.info("-- successful UPDATE player - PlayerActivation is now = 1");
          return "updated" + row ;
        }else{
             String msg = "-- UNsuccessful result in UPDATE for player : " + player.getIdplayer();
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
          return null;
        }
}catch(SQLException e){
    String msg = "SQL Exception in update player = " + e.toString() + ", SQLState = " + e.getSQLState().toString()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch(Exception ex){
    String msg = "Exception in updatePlayer() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
} //end class