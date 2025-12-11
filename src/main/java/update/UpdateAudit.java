package update;

import entite.Audit;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateAudit implements Serializable, interfaces.Log, interfaces.GolfInterface{

public boolean stop(Audit audit, Connection conn) throws SQLException{
        LOG.debug("starting ModifyAudit.stop - audit = " + audit);
    PreparedStatement ps = null;
try{
     final String query = """
              UPDATE audit
              SET AuditEndDate=?
              WHERE AuditId=?
            """;
    ps = conn.prepareStatement(query);
    ps.setTimestamp(1, Timestamp.from(Instant.now())); // AuditEndDate
    ps.setInt(2,audit.getIdaudit());  // mod 29-12-2019
        utils.LCUtil.logps(ps); 
    int row = ps.executeUpdate(); // write into database
      if(row!=0){
          String msg = "Successful result in Modify.stop audit at !!!" + LocalDateTime.now().format(ZDF_TIME);
          LOG.debug(msg);
          LCUtil.showMessageInfo(msg);
        return true;
      }else{
          String msg = "UNsuccessful result in UPDATE audit !!!";
          LOG.error(msg);
          LCUtil.showMessageFatal(msg);
      }
      return false;
}catch (Exception ex){
    String msg = "Exception in stopAuditLogin() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps);
  //      return false;
}
} // end method 
 void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try {
            Audit audit = new Audit();
            audit.setAuditPlayerId(324713);
       //     audit.setAuditStartDate(LocalDateTime.parse("2019-02-03T12:30:30"));
        //    audit.setAuditEndDate(LocalDateTime.parse("2019-02-03T12:31:31"));
            audit.setAuditEndDate(LocalDateTime.now()); // non modifiable !!
  //          audit.setAuditAttempts((short)3);
        //    audit.setAuditRetryTime(LocalDateTime.parse("2019-06-13T19:31:31"));
            audit.setIdaudit(8538); //setAuditRetryTime(LocalDateTime.now().minusMinutes(1));
   
            boolean b = new update.UpdateAudit().stop(audit, conn);
            LOG.debug("from main, ModifyAudit = " + b);
        } catch (Exception e) {
            String msg = "££ Exception in main ModifyAudit = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//
} //end Class