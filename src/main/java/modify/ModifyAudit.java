package modify;

import entite.Audit;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyAudit implements Serializable, interfaces.Log, interfaces.GolfInterface{

public boolean stop(Audit audit, Connection conn) throws SQLException{
        LOG.info("starting ModifyAudit.stop - audit = " + audit);
    PreparedStatement ps = null;
try{
  //    Player p = new Player();
  //    p.setIdplayer(audit.getAuditPlayerId());
    //  Audit a =  new find.FindLastAudit().find(audit, conn);
   //      LOG.info(" -- after FindLastAudit, IdAudit = " + a.getIdaudit());
      
 //   String query = " UPDATE audit"
 //                + " SET AuditEndDate =? "
 //                + " ,AuditModificationDate =? "
 //                + " WHERE AuditId=?";
   // faut retrouver row current - last connection  
   
   String au = utils.DBMeta.listMetaColumnsUpdate(conn, "audit");
    //    LOG.info("String from listMetaColumns = " + au);
    String query = "UPDATE audit SET "
                   + au
                   + "  WHERE AuditId=?"
   ;
    ps = conn.prepareStatement(query);
  //  ps.setInt(1,p.getIdplayer());
    ps.setInt(1,audit.getAuditPlayerId()); // mod 2-12-2019
 //   Timestamp ts = Timestamp.valueOf(audit.getAuditStartDate());
 //   ps.setTimestamp(2,ts);
    Timestamp ts = Timestamp.valueOf(audit.getAuditEndDate());
    ps.setTimestamp(2, ts);
    ps.setShort(3, audit.getAuditAttempts());
      //  LOG.info("there where attempts = " + audit.getAuditAttempts());
    if(audit.getAuditAttempts() == 3){
            LOG.info("there are 3 attempts = ");
        audit.setAuditRetryTime(audit.getAuditRetryTime().plusMinutes(15)); // blocage connection pendant 15 minutes
            LOG.info("connection for this user is blocked until  " + audit.getAuditRetryTime());
            
    }
    ps.setTimestamp(4,Timestamp.valueOf(audit.getAuditRetryTime()) );
  //  ps.setInt(4, 0); // AuditAttempts init = 0
 //   java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
  //  ps.setTimestamp(5,java.sql.Timestamp.valueOf("2019-06-01 00:00:00") ); // AuditRetryTimeDate : date fictive);
    
   // ps.setInt(5,lastAuditId);
//    ps.setInt(5,a.getIdaudit());
      ps.setInt(5,audit.getIdaudit());  // mod 29-12-2019
        utils.LCUtil.logps(ps); 
// call executeUpdate to execute our sql update statement
   int row = ps.executeUpdate(); // write into database
      if (row!=0){
          String msg = "Successful result in Modify.stop audit !!!";
          LOG.info(msg);
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
 public static void main(String[] args) throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try {
            Audit audit = new Audit();
            audit.setAuditPlayerId(324713);
       //     audit.setAuditStartDate(LocalDateTime.parse("2019-02-03T12:30:30"));
        //    audit.setAuditEndDate(LocalDateTime.parse("2019-02-03T12:31:31"));
            audit.setAuditEndDate(LocalDateTime.now()); // non modifiable !!
            audit.setAuditAttempts((short)3);
        //    audit.setAuditRetryTime(LocalDateTime.parse("2019-06-13T19:31:31"));
            audit.setAuditRetryTime(LocalDateTime.now().minusMinutes(1));
            boolean b = new modify.ModifyAudit().stop(audit, conn);
            LOG.info("from main, ModifyAudit = " + b);
        } catch (Exception e) {
            String msg = "££ Exception in main ModifyAudit = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//
} //end Class