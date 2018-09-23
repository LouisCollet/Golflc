
package utils;

import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author collet
 */
public class Audit implements interfaces.Log
{
    
    public int getLastAuditId(Player player) throws SQLException, Exception
{   PreparedStatement ps = null;
    ResultSet rs = null;
    Connection conn = null;
try
{
    DBConnection dbc = new DBConnection();
     conn = dbc.getConnection();
     String query =
             "select AuditId from audit_in_out"
              + " where AuditPlayerId = ?"
              + " order by AuditStartDate desc limit 1 ";
      ps = conn.prepareStatement(query);
      ps.setInt(1, player.getIdplayer() );
               utils.LCUtil.logps(ps); 
      rs = ps.executeQuery();
      int auditId = 0;
      while (rs.next() )        // ne devrait en avoir qu'un !!!
        {
            auditId = rs.getInt("AuditId");
                LOG.info(" -- AuditId in method = " + auditId);
        } // end while
return auditId;
}
catch(SQLException ex)
{
    LOG.error("-- setLastAuditId exception ! " + ex.toString() + "/" );
    throw ex;
} // end catch
finally
{
    DBConnection.closeQuietly(conn, null, rs, ps);
}

} // end method getLastAuditId

public static void startAuditLogin(Player player, Connection conn) throws SQLException //throws SQLException
{
        LOG.info("starting startAuditLogin for player = " + player.getIdplayer());
    PreparedStatement ps = null;
 //   ResultSet rs = null;
try
{
    String query = "INSERT INTO audit_in_out VALUES (?,?,?,?)";
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

public static void stopAuditLogin(Player player, Connection conn) throws SQLException
{
        LOG.info("starting stopAuditLogin - player = " + player.getIdplayer() );
    PreparedStatement ps = null;
  //  ResultSet rs = null;
 //   Connection conn = null;
try
{
  //  conn = DBConnection.getConnection();
      //   LOG.info(" -- getLastAuditLogin - query = " + query);
    String query = "UPDATE audit_in_out SET AuditEndDate =?  WHERE AuditId=?";
    ps = conn.prepareStatement(query);
    ps.setTimestamp(1,LCUtil.getCurrentTimeStamp()); // AuditEndDate
        // faut retrouver row current - last connection
    Audit au = new Audit();
    int lc = au.getLastAuditId(player);
        //log.info(" -- Audit Logout : called auditId = " + lc);
    ps.setInt(2,lc);
        //    String p = ps.toString();
        utils.LCUtil.logps(ps); 
// call executeUpdate to execute our sql update statement
   int row = ps.executeUpdate(); // write into database
      if (row!=0)
        {LOG.info("-- successful UPDATE audit ");}
      else
        {LOG.info("-- UNsuccessful result in UPDATE audit !!! ");}
}
catch (Exception ex)
{
    String msg = "Exception in stopAuditLogin() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
}
finally
{
        DBConnection.closeQuietly(null, null, null, ps);
}
} // end method 

} //rnd class
