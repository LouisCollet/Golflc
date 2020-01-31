package create;

import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class CreateBlocking implements Serializable,interfaces.Log{

public boolean create(Player player, Connection conn) throws SQLException {
        LOG.info("starting CreateBlocking.create for player = " + player);
    PreparedStatement ps = null;
try{
    final String query = LCUtil.generateInsertQuery(conn, "blocking"); // new 26/05/2019
    ps = conn.prepareStatement(query);
    ps.setInt(1,player.getIdplayer());
    ps.setTimestamp(2,LCUtil.getCurrentTimeStamp()); // BlockingLastAttempt
 //   ps.setTimestamp(4,java.sql.Timestamp.valueOf("2019-06-01 00:00:00") ); // AuditEndDate : date fictive
    ps.setInt(3, 1); // AuditAttempts init = 1 à la création
    ps.setTimestamp(4,LCUtil.getCurrentTimeStamp()); // AuditRetryTimeDate : date fictive);
    ps.setTimestamp(5,LCUtil.getCurrentTimeStamp()); // ModificationDate
    utils.LCUtil.logps(ps);
    
    int rows = ps.executeUpdate(); // write into database
      if (rows!=0){
            String msg = "-- successful INSERT in CreateBlocking.create";//, rows = " + rows;
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
          return true;
      }else{
          LOG.info("-- UNsuccessful insert Blocking !!! ");
          // lancer une erreur ??
          return false;
      }
}catch (Exception ex){
    String msg = "Exception in CreateBlocking = " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
    DBConnection.closeQuietly(null, null, null, ps);
}
} // end method 

 public static void main(String[] args) throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try{
            Player player = new Player();
            player.setIdplayer(324713);
            boolean b = new create.CreateBlocking().create(player, conn);
            LOG.info("from main, CreateBlocking = " + b);
    }catch (Exception e){
            String msg = "££ Exception in main CreateBlocking = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
    }
    } // end main//

} //end class