package exceptions;

import static interfaces.Log.LOG;
import java.sql.SQLException;
import static utils.LCUtil.showMessageFatal;

public class LCException extends Exception{
   // private ErrorCode code = null;

  /* Constructor that accepts a message
    public LCException(String msg){
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
   } 
*/
 //   public LCException(String msg, Throwable err){
 //       super(msg, err);
 //   LOG.error(msg + " / error " + err);
 //   LCUtil.showMessageFatal(msg + " / error " + err);
  // } 
    
    
    
    public static void handleSQLException(SQLException e, String methodName) throws SQLException {
     //   String msg = "£££ exception in Insert Club = " + sqle.getMessage() + " ,SQLState = "
     //               + e.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
        String msg = String.format(
                "SQL Exception in %s: %s, SQLState=%s, ErrorCode=%d",
                methodName, e.getMessage(), e.getSQLState(), e.getErrorCode()
        );
        LOG.error(msg, e);
        showMessageFatal(msg);
        throw new SQLException(msg,e);
    }

    public static void handleGenericException(Exception e, String methodName) {
        String msg = String.format("Unexpected exception in %s: %s", methodName, e.getMessage());
        LOG.error(msg, e);
        showMessageFatal(msg);
      //  throw new Exception(msg);
        throw new AppException(msg, e); // mod 28-12-2025
    }
    
  }