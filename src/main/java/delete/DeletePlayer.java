
package delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeletePlayer implements interfaces.Log, interfaces.GolfInterface
{
    public boolean deletePlayerAndChilds(final int idplayer,final Connection conn) throws Exception
    {
    PreparedStatement ps = null;
try
{   //encore Ã  faire : delete du record activation s'il existe ...
     LOG.info("starting delete from Table Player cascading ... = " );
  String query = " delete from score where score.player_has_round_player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_score = ps.executeUpdate();
        LOG.info("deleting score = " + row_score);
    
    query = " delete from player_has_round where player_has_round.player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_phr = ps.executeUpdate();
        LOG.info("deleting inscription = " + row_score);
    
    query = " delete from handicap where handicap.player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_hcp = ps.executeUpdate();
        LOG.info("deleting handicap = " + row_hcp);
    
    query = " delete from player where player.idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_player = ps.executeUpdate();
        LOG.info("deleting player = " + row_player);
    
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
     DBConnection dbc = new DBConnection();
     Connection conn = dbc.getConnection();
  try{
 //       Player p = new Player();
 //       p.setIdplayer(121221);
    //    p.setWrkpassword("test123LC");
    //    LOG.info("01");
 //   DeletePlayer dp = new DeletePlayer();
    boolean OK = new DeletePlayer().deletePlayerAndChilds(121221, conn);
        
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
