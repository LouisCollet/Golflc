package create;

import entite.LoggingUser;
import entite.Professional;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import read.ReadLoggingUser;
import utils.DBConnection;
import static utils.LCUtil.generatedKey;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
public class CreateLoggingUser implements interfaces.Log, interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
public boolean create(final LoggingUser logging, final Connection conn) throws SQLException{
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        PreparedStatement ps = null;
  try {
            LOG.debug("...entering " + methodName);
            LOG.debug(" with LoggingUser  = " + logging);

            final String query = utils.LCUtil.generateInsertQuery(conn, "logging_user");
            ps = conn.prepareStatement(query);
            ps.setInt(1, logging.getLoggingIdPlayer());
            ps.setInt(2, logging.getLoggingIdRound());
            ps.setString(3, logging.getLoggingType());
            ps.setString(4, logging.getLoggingCalculations());
            ps.setTimestamp(5, Timestamp.from(Instant.now()));

            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate();
            if (row != 0){
        //        pro.setProId(generatedKey(conn));
                String msg = "LoggingUser Created = " + logging;
                LOG.info(msg);
                showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/>ERROR insert LoggingUser : " + logging;
                LOG.debug(msg);
                showMessageFatal(msg);
                return false;
            }
   }catch (SQLException sqle) {
            String msg = "SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
    } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
    } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
    }
    } //end method
void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
try{
   LoggingUser logging = new LoggingUser();
   logging.setLoggingIdPlayer(324713);
   logging.setLoggingIdRound(688);
   logging.setLoggingType("H");
   logging.setLoggingCalculations("these are the calculations for 324713, 388, Handicap");
   var v = new CreateLoggingUser().create(logging, conn);
      LOG.debug(" from main : LoggingUser = " + v);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
} //end 