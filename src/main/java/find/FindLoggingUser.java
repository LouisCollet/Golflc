package find;


import entite.LoggingUser;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindLoggingUser{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
   // faire un autre read avec Handicap avec 2e signature 
public int find(LoggingUser logging, Connection conn) throws SQLException{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.debug("entering " + methodName);//
        LOG.debug("with Logging " + logging);//
    final String query = """
        SELECT count(*)
        FROM logging_user
        WHERE LoggingIdPlayer = ?
        AND LoggingIdRound = ?
        AND LoggingType = ?
     """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, logging.getLoggingIdPlayer());
    ps.setInt(2, logging.getLoggingIdRound());
    ps.setString(3, logging.getLoggingType().toUpperCase());
    utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();
    int i = 0;
    while(rs.next()){
         i = rs.getInt(1);
    }  //end while
    LOG.debug("found i = " + i);
    return i;
/*    if(logging == null){
          String msg = LCUtil.prepareMessageBean("logging.notfound");
          LOG.debug(msg);
          return false;
 //         LCUtil.showMessageInfo(msg);
     }else{
          String msg = LCUtil.prepareMessageBean("logging.found")+ logging;
          LOG.debug(msg);
          return true;
 //         LCUtil.showMessageInfo(msg);
     }
*/
//  return logging;
}catch (SQLException e){
    String msg = "SQLException in " + methodName + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
}catch (Exception ex){
    LOG.error("Exception in " + methodName + ex);
    LCUtil.showMessageFatal("Exception in LoadClub = " + ex.toString() );
}
finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
       return 0;
} //end method

void main() throws SQLException, Exception{
   Connection conn = new DBConnection().getConnection();
   LoggingUser logging = new LoggingUser();
   logging.setLoggingIdPlayer(324713);
   logging.setLoggingIdRound(691);
   logging.setLoggingType("R");
   int v = new FindLoggingUser().find(logging, conn);
      LOG.debug(" from main : LoggingUser = " + v);
   DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class