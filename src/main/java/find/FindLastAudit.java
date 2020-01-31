package find;

import entite.Audit;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;

public class FindLastAudit{

final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 

  public Audit find(Audit audit, Connection conn) throws SQLException, Exception{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    LOG.info("entering FindLastAudit for audit = " + audit); //.toString());
    String au= utils.DBMeta.listMetaColumnsLoad(conn, "audit");
     String query =
              "SELECT " + au +
              " from audit"
              + " where AuditPlayerId = ?"
              + " order by AuditStartDate"
              + " desc limit 1 ";
      ps = conn.prepareStatement(query);
    //  ps.setInt(1, player.getIdplayer() );
      ps.setInt(1, audit.getAuditPlayerId());
        utils.LCUtil.logps(ps); 
      rs = ps.executeQuery();
      rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindLastAudit has " + rs.getRow() + " lines.");
        if(rs.getRow() > 1)
            {   throw new Exception(" -- More than one LastAudit = " + rs.getRow() );  }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
          //LOG.info("just before while ! ");
        Audit a = null;
	while(rs.next()){
             a = entite.Audit.mapAudit(rs);
	}
   return a;
}catch(Exception ex){
    String err = "-- Exception in ! " + CLASSNAME + " / " + ex.toString() + "/";
    LOG.error(err);
    return null;
} // end catch
finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
} // end method find
    
 public static void main(String[] args) throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();
    Audit audit = new Audit();
    audit.setAuditPlayerId(324713);
    audit = new FindLastAudit().find(audit, conn);
        LOG.info("last audit found = " + audit);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
 
} // end class