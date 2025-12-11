package update;

import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class UpdatePlayerActivation implements interfaces.Log, interfaces.GolfInterface{
    
 public boolean update(Player player, Connection conn) throws SQLException{
    PreparedStatement ps = null;
try{
        LOG.debug("starting update activation table Player.. = " + player);
    final String query = """
            UPDATE player
            SET PlayerActivation = 1
            WHERE idplayer = ?
         """;
    ps = conn.prepareStatement(query);
    ps.setInt(1,player.getIdplayer() );
    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate();
      if (row!=0){
          LOG.debug("-- successful UPDATE player - PlayerActivation is now = 1");
          return true;
         // return "updated" + row ;
        }else{
             String msg = "-- UNsuccessful result in UPDATE for player : " + player.getIdplayer();
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
          return false;
        }
}catch(SQLException e){
    String msg = "SQL Exception in update player = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch(Exception ex){
    String msg = "Exception in updatePlayer() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
} //end class