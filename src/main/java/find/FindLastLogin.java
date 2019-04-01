package find;

import entite.Player;
import static interfaces.GolfInterface.SDF;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.DBConnection;
import utils.LCUtil;

public class FindLastLogin{
 // public class Score_Insert_Update implements interfaces.GolfInterface, interfaces.Log 
final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 

    public Timestamp lastAuditLogin(Player player, Connection conn) throws SQLException{
    LOG.info("starting getLastAuditLogin - player = " + player.getIdplayer());
    PreparedStatement ps = null;
    ResultSet rs = null;
    java.sql.Timestamp dbSqlTimestamp = null;
try{
     String query = "SELECT AuditStartDate, AuditPlayerId from audit"
                  + " where AuditPlayerId = ?"
                  + " order by AuditStartDate desc limit 1 ";
      ps = conn.prepareStatement(query);
      ps.setInt(1, player.getIdplayer());      // Assign value to input parameter
      rs = ps.executeQuery();       // Get the result table from the query  3
         //    String p = ps.toString();
         utils.LCUtil.logps(ps);
        if(rs.first())
            {LOG.info("this is a returning connection for : " + player.getIdplayer());
            dbSqlTimestamp = rs.getTimestamp("AuditStartDate");
         //   String s = ;
            LOG.info("last connection string = " + SDF.format(dbSqlTimestamp));
            
        }else{
             LOG.info("this is the first connection for : " + player.getIdplayer());
       //        String text = ;
       //     dbSqlTimestamp = null;   
            dbSqlTimestamp = Timestamp.valueOf("2000-01-01 00:00:00.123456");  // fake date
        }
    return dbSqlTimestamp;
}catch (Exception ex){
    String msg = "Exception in getLastAuditLogin() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
        DBConnection.closeQuietly(null, null, rs, ps);
}

} // end method 

    public static void main(String[] args) throws Exception , Exception{

    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Timestamp i = new FindLastLogin().lastAuditLogin(player, conn);
        LOG.info("last audit login = " + i);
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
    
    
    
    
} // end class