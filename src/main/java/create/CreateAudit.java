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

public void startAuditLogin(Player player, Connection conn) throws SQLException //throws SQLException
{
        LOG.info("starting startAuditLogin for player = " + player.getIdplayer());
    PreparedStatement ps = null;
 //   ResultSet rs = null;
try
{
    String query = "INSERT INTO audit VALUES (?,?,?,?)";
        LOG.info(" -- login query = " + query);
    ps = conn.prepareStatement(query);
    ps.setNull(1,java.sql.Types.INTEGER); // auto-increment
    ps.setInt(2,player.getIdplayer());
    ps.setTimestamp(3,LCUtil.getCurrentTimeStamp()); // AuditStartDate
         //LOG.info(" -- Start new connection at : " + sqlDate);
    ps.setTimestamp(4,java.sql.Timestamp.valueOf("2000-01-01 00:00:00") ); // AuditEndDate : date fictive
            //    String p = ps.toString();
    utils.LCUtil.logps(ps);
    int rows = ps.executeUpdate(); // write into database
      if (rows!=0)
        {LOG.info("-- successful INSERT Audit rows = " + rows);
      }else{
        LOG.info("-- UNsuccessful insert Audit !!! ");}
}catch (Exception ex){
    String msg = "Exception in getLastAuditLogin() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
}finally{
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        DBConnection.closeQuietly(null, null, null, ps);
}
} // end method 
} //end class