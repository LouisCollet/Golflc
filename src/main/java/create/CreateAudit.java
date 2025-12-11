package create;

import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import utils.DBConnection;
import utils.LCUtil;

public class CreateAudit implements Serializable,interfaces.Log{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public boolean create(final Player player, final Connection conn) throws SQLException {
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        LOG.debug("starting " + methodName + " for player = " + player);
    PreparedStatement ps = null;
 //   ResultSet rs = null;
try{
    final String query = LCUtil.generateInsertQuery(conn, "audit"); // new 26/05/2019
    ps = conn.prepareStatement(query);
    ps.setNull(1,java.sql.Types.INTEGER); // auto-increment
    ps.setInt(2,player.getIdplayer());
    ps.setTimestamp(3,Timestamp.from(Instant.now())); // AuditStartDate
    ps.setTimestamp(4,Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS))); // AuditEndDate = now plus one hour
    ps.setTimestamp(5,Timestamp.from(Instant.now())); // ModificationDate
    utils.LCUtil.logps(ps);
    int rows = ps.executeUpdate(); // write into database
      if (rows!=0){
            LOG.debug("-- successful INSERT Audit rows = " + rows);
          return true;
      }else{
          LOG.debug("-- UNsuccessful insert Audit !!! ");
          return false;
      }
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
    DBConnection.closeQuietly(null, null, null, ps);
}
} // end method 

 void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try {
            Player player = new Player();
            player.setIdplayer(324713);
            boolean b = new create.CreateAudit().create(player, conn);
            LOG.debug("from main, CreateAudit = " + b);
        } catch (Exception e) {
            String msg = "££ Exception in main CreateAudit = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//

} //end class