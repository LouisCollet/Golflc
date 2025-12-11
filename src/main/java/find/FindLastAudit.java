package find;

import entite.Audit;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindLastAudit{

private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 

  public Audit find(Audit audit, Connection conn) throws SQLException, Exception{
    PreparedStatement ps = null;
    ResultSet rs = null;
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
try{
    LOG.debug("entering " + methodName + " for audit = " + audit);
//    String au= utils.DBMeta.listMetaColumnsLoad(conn, "audit");
 /*   String query =
              "SELECT " + au +
              " from audit"
              + " where AuditPlayerId = ?"
              + " ORDE R BY AuditStartDate"
              + " desc limit 1 ";
*/
        final String query = """
              SELECT *
              FROM audit
              WHERE AuditPlayerId = ?
              ORDER by AuditStartDate
              DESC limit 1
          """;
    
      ps = conn.prepareStatement(query);
      ps.setInt(1, audit.getAuditPlayerId());
        utils.LCUtil.logps(ps); 
      rs = ps.executeQuery();
      Audit a = null;
      int i = 0;
	while(rs.next()){ 
           i = i + 1;
           a = entite.Audit.mapAudit(rs);
	}
     if(i == 0){
         String msg = "££ Empty Result Table in " + methodName + " for player = " + audit.getAuditPlayerId();
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
         a = null;
   //      return null;
     }else{
         LOG.debug("ResultSet FindSubscription has " + i + " lines.");
     }  
   return a;
}catch(Exception ex){
    String err = "-- Exception in " + methodName + " " + ex.toString() + "/";
    LOG.error(err);
    return null;
} // end catch
finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
} // end method find
    
 void main() throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();
    Audit audit = new Audit();
    audit.setAuditPlayerId(324713);
    audit = new FindLastAudit().find(audit, conn);
        LOG.debug("last audit found = " + audit);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
 
} // end class