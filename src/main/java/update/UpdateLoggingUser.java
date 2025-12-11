package update;

import entite.LoggingUser;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateLoggingUser implements interfaces.Log, interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
 public boolean update(final LoggingUser logging, final Connection conn) throws SQLException{
  final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
try{
        LOG.debug("...starting " + methodName);
        LOG.debug("...for logging " + logging);
    final String query = """
            UPDATE logging_user
            SET LoggingCalculations = ?
            WHERE LoggingIdPlayer = ?
            AND LoggingIdRound = ?
        """ ;
    ps = conn.prepareStatement(query);
    ps.setString(1, logging.getLoggingCalculations());
    ps.setInt(2, logging.getLoggingIdPlayer());
    ps.setInt(3, logging.getLoggingIdRound());

    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate();
    if(row != 0){
          String msg = "successful UPDATE LoggingUser = " + NEW_LINE + logging;
          LOG.info(msg);
          LCUtil.showMessageInfo(msg);
          return true;
     }else{
          String msg = "-- UNsuccessful UPDATE LoggingUser = " + methodName + NEW_LINE + logging;
          LOG.error(msg);
          LCUtil.showMessageFatal(msg);
          return false;
        }
}catch(SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch(Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
 
 void main() throws Exception, SQLException{
    Connection conn = new DBConnection().getConnection();
  


//     LOG.debug(" Voici le résultat : = " + b);
     DBConnection.closeQuietly(conn, null, null, null); 
}// end main
} //end class