package find;

import entite.Course;
import entite.Player;
import entite.Tarif;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;

public class FindLastAudit{
 // public class Score_Insert_Update implements interfaces.GolfInterface, interfaces.Log 
final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 

    public int getLastAuditId(Player player) throws SQLException, Exception
{   PreparedStatement ps = null;
    ResultSet rs = null;
    Connection conn = null;
try{
    DBConnection dbc = new DBConnection();
     conn = dbc.getConnection();
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
    DBConnection.closeQuietly(conn, null, rs, ps);
}

} // end method getLastAuditId
    
    public static void main(String[] args) throws Exception , Exception{

    Connection conn = new DBConnection().getConnection();
    Course course = new Course();
    course.setIdcourse(102);
  //  FindTarifData ftd = new FindTarifData();
    Tarif t1 = new FindTarifData().findCourseTarif(course, conn);
     LOG.info("Tarif extracted from database = "  + t1.toString());
//findPlayerHandicap(player,round, conn);
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
    
    
    
    
} // end class