package update;

import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class UpdatePlayerPhotoLocation implements interfaces.Log, interfaces.GolfInterface{
    
  public boolean updateRecordFromPlayer(Player player, Connection conn) throws SQLException{
    PreparedStatement ps = null;
try{
        LOG.debug("starting update photolocation for. = " + player.getIdplayer() + " photolocation = " 
                                            + player.getPlayerPhotoLocation() );
    final String query = "UPDATE player "
            + " SET PlayerPhotoLocation = ? "
            + " WHERE idplayer = ?";
    ps = conn.prepareStatement(query);
    ps.setString(1,player.getPlayerPhotoLocation());
    ps.setInt(2,player.getIdplayer());
         utils.LCUtil.logps(ps);
   int row = ps.executeUpdate();
      if (row!=0){ 
//LOG.debug("-- successful UPDATE player " + upload.getIdplayer());
        String msg = "<br/>Successful UPDATE file = " + player.getPlayerPhotoLocation()
                + "<br/> for player = " + player.getIdplayer();
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
       //   return "updated" + row ;
          return true;
        }else{
             String msg = "-- UNsuccessful result in UPDATE for player : " + player.getIdplayer();
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
   //          throw new Exception(msg);
             return false;
        }
}catch (SQLException e){
    String msg = "SQL Exception in update player = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch (Exception ex){
    String msg = "Exception in updatePlayer() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
} //end class