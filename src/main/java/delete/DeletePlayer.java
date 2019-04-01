
package delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeletePlayer implements interfaces.Log, interfaces.GolfInterface{
    
    public boolean deletePlayerAndChilds(final int idplayer,final Connection conn) throws Exception{
    PreparedStatement ps = null;
try{   //encore Ã  faire : delete du record activation s'il existe ...
     LOG.info("starting deletePlayersAnd Childs from Table Player cascading ... = " );
     // on commende par le niveau le plus bas !
  String query = " delete from score where score.player_has_round_player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_score = ps.executeUpdate();
        LOG.info("deleted score = " + row_score);
    
    query = " delete from player_has_round where player_has_round.player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_phr = ps.executeUpdate();
        LOG.info("deleted inscription = " + row_score);
    
    query = " delete from handicap where handicap.player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_hcp = ps.executeUpdate();
        LOG.info("deleted handicap = " + row_hcp);
    
    query = " delete from player where player.idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_player = ps.executeUpdate();
        LOG.info("deleted player = " + row_player);
    
    String msg = "<br/> <h1>Records deleted = " 
                        + " <br/></h1>player = " + idplayer
                        + " <br/>score = " + row_score
                        + " <br/>inscription = " + row_phr
                        + " <br/>handicap = " + row_hcp
                        + " <br/>player = " + row_player;
           LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        return true;

}catch (SQLException e){
    String msg = "SQL Exception in DeletePlayer = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeletePlayer() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
 public static void main(String[] args) throws Exception {
     Connection conn = new DBConnection().getConnection();
  try{
    boolean OK = new DeletePlayer().deletePlayerAndChilds(456896, conn);
        LOG.info("from main, after = " + OK);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class
