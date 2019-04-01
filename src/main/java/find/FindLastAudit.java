package find;

import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;

public class FindLastAudit{
 // public class Score_Insert_Update implements interfaces.GolfInterface, interfaces.Log 
final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 

    public int getLastAuditId(Player player, Connection conn) throws SQLException, Exception
{   PreparedStatement ps = null;
    ResultSet rs = null;
try{
     String query =
             "select AuditId from audit"
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
}catch(SQLException ex){
    LOG.error("-- setLastAuditId exception ! " + ex.toString() + "/" );
    throw ex;
} // end catch
finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
} // end method getLastAuditId
    
    public static void main(String[] args) throws Exception , Exception{

    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    int i = new FindLastAudit().getLastAuditId(player, conn);
        LOG.info("last audit id = " + i);
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class