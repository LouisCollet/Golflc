package update;

import entite.Blocking;
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

public class UpdateBlocking implements Serializable, interfaces.Log, interfaces.GolfInterface{
    public boolean update(Blocking blocking, Connection conn) throws SQLException{
        LOG.debug("starting ModifyBlocking.modify - with blocking = " + blocking);
    PreparedStatement ps = null;
try{
    final String query = """
            UPDATE blocking
            SET BlockingLastAttempt = ?,
                BlockingAttempts = ?,
                BlockingRetryTime = ?
            WHERE BlockingPlayerId=?
        """;
    ps = conn.prepareStatement(query);
    ps.setTimestamp(1,Timestamp.from(Instant.now())); // BlockingLastAttempt
        LOG.debug("there where attempts = " + blocking.getBlockingAttempts());
    ps.setShort(2, blocking.getBlockingAttempts());
    if(blocking.getBlockingAttempts() > 2){
  //          LOG.debug("there are 3 attempts = " + blocking.getBlockingAttempts());
     //   blocking.setBlockingRetryTime(LocalDateTime.now().plusMinutes(15)); // blocage connection pendant 15 minutes
     //       LOG.debug("Connection for this user is blocked until  " + blocking.getBlockingRetryTime().format(ZDF_TIME));
     //   ps.setTimestamp(3,Timestamp.valueOf(blocking.getBlockingRetryTime()));
         ps.setTimestamp(3,Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
    }else{
        ps.setTimestamp(3,Timestamp.from(Instant.now()));
          LOG.debug("There are attempts now = " + blocking.getBlockingAttempts());
    }
// attention c'est le ? du query
     ps.setInt(4,blocking.getBlockingPlayerId());
        utils.LCUtil.logps(ps); 

   int row = ps.executeUpdate();
      if(row!=0){
          String msg = "Erreur " + blocking.getBlockingAttempts() + " - Après 3 erreurs successives, vous serez bloqué pendant 15 minutes";
          LOG.debug(msg);
          LCUtil.showMessageInfo(msg);
        return true;
      }else{
          String msg = "UNsuccessful result in UPDATE blocking !!!";
          LOG.error(msg);
          LCUtil.showMessageFatal(msg);
      }
      return false;
}catch (Exception ex){
    String msg = "Exception in ModifyBlocking() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps);
  //      return false;
}
} // end method 
 void main() throws SQLException, Exception{
   Connection conn = new DBConnection().getConnection();
   try{
            Blocking blocking = new Blocking();
       //     audit.setAuditStartDate(LocalDateTime.parse("2019-02-03T12:30:30"));
        //    audit.setAuditEndDate(LocalDateTime.parse("2019-02-03T12:31:31"));
 //           audit.setAuditEndDate(LocalDateTime.now()); // non modifiable !!
            blocking.setBlockingPlayerId(324713);
            blocking.setBlockingAttempts((short)3);
        //    audit.setAuditRetryTime(LocalDateTime.parse("2019-06-13T19:31:31"));
   //         audit.setAuditRetryTime(LocalDateTime.now().minusMinutes(1));
            boolean b = new update.UpdateBlocking().update(blocking, conn);
            LOG.debug("from main, ModifyBlocking = " + b);
    }catch (Exception e){
            String msg = "££ Exception in main ModifyBlocking = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//
} //end Class