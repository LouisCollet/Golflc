package delete;

import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteBlocking implements interfaces.GolfInterface{

  public boolean delete(final Player player, final Connection conn) throws Exception {
    PreparedStatement ps = null;
try{ 
       LOG.debug("starting DeleteBlocking ... = " );
       LOG.debug("Delete Blocking for player "  + player);
    final String query = 
       " DELETE from blocking" +
       " WHERE BlockingPlayerId = ?" 
        ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_delete = ps.executeUpdate();
        LOG.debug("deleted Blocking = " + row_delete);
 //   String msg = "There are " + row_delete + " Blocking deleted for = " + player.getIdplayer();
  if(row_delete > 0){
        String msg = "End of Blocking for 15 min for = " + player.getIdplayer();
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        return true;
  }else{
      String msg = "NOT success for unblocking 15 min for = " + player.getIdplayer();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
  }
        
}catch (SQLException e){
    String msg = "SQL Exception in DeleteBlocking = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteBlocking() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
   
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
 //   int idclub = 113;
  //  boolean b = new DeleteClub().delete(idclub, conn);
//    LOG.debug("from main - resultat deleteBlocking = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class