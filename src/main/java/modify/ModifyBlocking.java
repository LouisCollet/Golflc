package modify;

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

public class ModifyBlocking implements Serializable, interfaces.Log, interfaces.GolfInterface{
    public boolean modify(Blocking blocking, Connection conn) throws SQLException{
        LOG.info("starting ModifyBlocking.modify - with blocking = " + blocking);
    PreparedStatement ps = null;
try{
   String bl = utils.DBMeta.listMetaColumnsUpdate(conn, "blocking");
    String query = "UPDATE blocking SET " + bl
                   + "  WHERE BlockingPlayerId=?";

    ps = conn.prepareStatement(query);
    ps.setTimestamp(1,Timestamp.from(Instant.now())); // BlockingLastAttempt
        LOG.info("there where attempts = " + blocking.getBlockingAttempts());
    ps.setShort(2, blocking.getBlockingAttempts());
    //
    if(blocking.getBlockingAttempts() > 2){
  //          LOG.info("there are 3 attempts = " + blocking.getBlockingAttempts());
        blocking.setBlockingRetryTime(LocalDateTime.now().plusMinutes(15)); // blocage connection pendant 15 minutes
            LOG.info("Connection for this user is blocked until  " + blocking.getBlockingRetryTime().format(ZDF_TIME));
        ps.setTimestamp(3,Timestamp.valueOf(blocking.getBlockingRetryTime()));
    }else{
 //       blocking.setBlockingRetryTime(LocalDateTime.now());
        ps.setTimestamp(3,Timestamp.from(Instant.now()));
          LOG.info("there are attempts = " + blocking.getBlockingAttempts());
    }
   // ps.setTimestamp(3,Timestamp.valueOf(blocking.getBlockingRetryTime()));

// attention c'est le ? du query
     ps.setInt(4,blocking.getBlockingPlayerId());
        utils.LCUtil.logps(ps); 
// call executeUpdate to execute our sql update statement
   int row = ps.executeUpdate(); // write into database
      if(row!=0){
          String msg = "Successful result in ModifyBlocking.modify !!!" + blocking.getBlockingAttempts();
          LOG.info(msg);
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
 public static void main(String[] args) throws SQLException, Exception{
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
            boolean b = new modify.ModifyBlocking().modify(blocking, conn);
            LOG.info("from main, ModifyBlocking = " + b);
    }catch (Exception e){
            String msg = "££ Exception in main ModifyBlocking = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//
} //end Class