package create;

import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class CreateAudit implements Serializable,interfaces.Log{

public boolean create(Player player, Connection conn) throws SQLException {
        LOG.info("starting CreateAudit.create for player = " + player.getIdplayer());
    PreparedStatement ps = null;
 //   ResultSet rs = null;
try{
    final String query = LCUtil.generateInsertQuery(conn, "audit"); // new 26/05/2019
 //       LOG.info(" -- login query = " + query);
    ps = conn.prepareStatement(query);
    ps.setNull(1,java.sql.Types.INTEGER); // auto-increment
    ps.setInt(2,player.getIdplayer());
    ps.setTimestamp(3,LCUtil.getCurrentTimeStamp()); // AuditStartDate
    ps.setTimestamp(4,java.sql.Timestamp.valueOf("2019-06-01 00:00:00") ); // AuditEndDate : date fictive
    ps.setInt(5, 0); // AuditAttempts init = 0
  //  java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
    ps.setTimestamp(6,LCUtil.getCurrentTimeStamp()); // AuditRetryTimeDate : date fictive);
    ps.setTimestamp(7,LCUtil.getCurrentTimeStamp()); // ModificationDate
    utils.LCUtil.logps(ps);
    int rows = ps.executeUpdate(); // write into database
      if (rows!=0){
          LOG.info("-- successful INSERT Audit rows = " + rows);
          return true;
      }else{
          LOG.info("-- UNsuccessful insert Audit !!! ");
          return false;
      }
}catch (Exception ex){
    String msg = "Exception in CreateAudit = " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        DBConnection.closeQuietly(null, null, null, ps);
}
} // end method 

 public static void main(String[] args) throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try {
            Player player = new Player();
            player.setIdplayer(324713);
            boolean b = new create.CreateAudit().create(player, conn);
            LOG.info("from main, CreateAudit = " + b);
        } catch (Exception e) {
            String msg = "££ Exception in main CreateAudit = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//

} //end class