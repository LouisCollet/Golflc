package find;

import entite.Audit;
import entite.Player;
import static interfaces.GolfInterface.SDF_TIME;
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

  //  public Timestamp find(Player player, Connection conn) throws SQLException{
    public Audit find(Player player, Connection conn) throws SQLException{    
        LOG.info("starting getLastAuditLogin - player = " + player);
    PreparedStatement ps = null;
    ResultSet rs = null;
    java.sql.Timestamp dbSqlTimestamp = null;
try{
    String au= utils.DBMeta.listMetaColumnsLoad(conn, "audit");
     String query = //"SELECT AuditStartDate, AuditPlayerId "
               "SELECT " + au
             + " from audit"
             + " where AuditPlayerId = ?"
             + " order by AuditStartDate"
             + " desc limit 1 ";
      ps = conn.prepareStatement(query);
      ps.setInt(1, player.getIdplayer());      // Assign value to input parameter
      rs = ps.executeQuery();       // Get the result table from the query  3
         utils.LCUtil.logps(ps);
        if(rs.first()){
                LOG.info("this is a returning connection for : " + player.getIdplayer());
            dbSqlTimestamp = rs.getTimestamp("AuditStartDate");
               LOG.info("last connection string = " + SDF_TIME.format(dbSqlTimestamp));
        }else{
             LOG.info("this is the first connection for : " + player.getIdplayer());
            dbSqlTimestamp = Timestamp.valueOf("2000-01-01 00:00:00");  // fake date
        }
   // return dbSqlTimestamp;
    Audit a = new Audit();
    a.setAuditEndDate(dbSqlTimestamp.toLocalDateTime());
    return a;
}catch (Exception ex){
    String msg = "Exception in " + CLASSNAME + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps);
}
} // end method 

    public static void main(String[] args) throws Exception , Exception{

    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    
    Audit i = new FindLastLogin().find(player, conn);
        LOG.info("last audit login = " + i);
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class