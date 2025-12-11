package find;

import entite.Audit;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindLastLogin{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 

 public Audit find(Player player, Connection conn) throws SQLException{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
     PreparedStatement ps = null;
     ResultSet rs = null;
 //   java.sql.Timestamp dbSqlTimestamp = null;
      
try{
    LOG.debug("entering " + methodName + " for player = " + player);
//    String au= utils.DBMeta.listMetaColumnsLoad(conn, "audit");
     final String query = """
              SELECT *
              from audit
              WHERE AuditPlayerId = ?
              ORDER by AuditStartDate
              desc limit 1
             """;
      ps = conn.prepareStatement(query);
      ps.setInt(1, player.getIdplayer());
      rs = ps.executeQuery();
         utils.LCUtil.logps(ps);
      Audit audit = new Audit();
 //   int i = 0;
    	while(rs.next()){ 
    //         i = i++;
           //  dbSqlTimestamp = rs.getTimestamp("AuditStartDate");
             audit = entite.Audit.mapAudit(rs);
	}
 //       LOG.debug("last connection = " + i);
  //      LOG.debug("entité audit = " + a);
  //   if(i == 1){
  //       LOG.debug("this is a returning connection for : " + player.getIdplayer());
  //   }else{
   //      String msg = "this is the first connection for : " + player.getIdplayer();
   //      LOG.info(msg);
   //      LCUtil.showMessageInfo(msg);
   //  }
 //    LOG.debug("this is the first connection for : " + player.getIdplayer());
 ///   a.setAuditEndDate(dbSqlTimestamp.toLocalDateTime());
    return audit;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps);
}
} // end method 

    void main() throws Exception , Exception{

    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324793);
    Audit i = new FindLastLogin().find(player, conn);
        LOG.debug("last audit login = " + i);
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class