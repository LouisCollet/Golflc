package modify;

//import create.*;
//import entite.Handicap;
import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyAudit implements Serializable, interfaces.Log, interfaces.GolfInterface{

public boolean stopAuditLogin(Player player, Connection conn) throws SQLException{
        LOG.info("starting stopAuditLogin - player = " + player.getIdplayer() );
    PreparedStatement ps = null;
  //  ResultSet rs = null;
 //   Connection conn = null;
try{
      //   LOG.info(" -- getLastAuditLogin - query = " + query);
    String query = " UPDATE audit"
                 + " SET AuditEndDate =? "
                 + " WHERE AuditId=?";
    
    find.FindLastAudit fla = new find.FindLastAudit();
    int lastAuditId = fla.getLastAuditId(player, conn);
          // faut retrouver row current - last connection  
    ps = conn.prepareStatement(query);
        //log.info(" -- Audit Logout : called auditId = " + lc);
    ps.setTimestamp(1, LCUtil.getCurrentTimeStamp());
    ps.setInt(2,lastAuditId);
        utils.LCUtil.logps(ps); 
// call executeUpdate to execute our sql update statement
   int row = ps.executeUpdate(); // write into database
      if (row!=0)
        {LOG.info("-- successful UPDATE audit ");
        return true;
      }else{
          LOG.info("-- UNsuccessful result in UPDATE audit !!! ");}
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

} //end Class