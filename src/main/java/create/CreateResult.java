package create;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class CreateResult implements interfaces.Log {
    final private static String []array_return_error = new String [3];
    
  public String [] create(final Connection conn, final int in_player,
        final int in_round, final int in_result_stableford, final int in_result_zwanzeurs,
        final int in_result_greenshirt) throws SQLException{
    
  LOG.info(" -- Start of setStoredResult Procedure");
  LOG.info(" -- Result Stableford to be inserted = " + in_result_stableford);
  LOG.info(" -- Result Zwanzeurs  to be inserted = " + in_result_zwanzeurs);
  CallableStatement cs = null;
try{
    final String stored_name = "set_result(?,?,?,?,?,?)";   // nom de la stored pro, 6 parameters
    LOG.info(" -- Start with : " + stored_name);
    cs = conn.prepareCall("{CALL " + stored_name + "}");
    String p = cs.toString();
 ////       LOG.debug("Callable statement " + p.substring(p.indexOf(":"), p.length() ));
    //Bind IN parameter first : player and round date
      cs.setInt(1, in_player);
        LOG.info("Param1 - Player = " + in_player);
      cs.setInt(2,in_round);
        LOG.info("Param2 - Round = " + in_round);
      cs.setInt(3, in_result_stableford);
        LOG.info("Param 3 - Result Stableford = " + in_result_stableford);
      cs.setInt(4, in_result_zwanzeurs);
        LOG.info("Param 4 - Result Zwanzeurs = " + in_result_zwanzeurs);
      cs.setInt(5, in_result_greenshirt);
        LOG.info("Param 5 - Result Greenshirt = " + in_result_greenshirt);
    // Register OUT parameters
      cs.registerOutParameter(6,Types.VARCHAR); // indic
      boolean hasResults = cs.execute(); // execute statement
        LOG.info("Executed setStoredResult (stored procedure) = " + " / " + hasResults);
      String indic = cs.getString(6);
        LOG.info(" -- Execution indicator = " + indic);
      array_return_error[0]= "NO ERROR";
      array_return_error[1]= array_return_error[2]= "";
      return array_return_error;
}catch(SQLException sqle){
       if (sqle.getSQLState().equals("LC001") ) // warning new result = old result
            {LOG.error(" -- NEW Result = OLD result = " + sqle.getErrorCode());
            array_return_error[0]= "WARNING";
            array_return_error[1]= "simply a warning";
            array_return_error[2]= "";
            return array_return_error;
       }else{
            LOG.error(" -- SQL Exception by LC = " + sqle.getMessage());
            LOG.error(" -- ErrorCode = " +  sqle.getErrorCode() );
            LOG.error(" -- SQLSTATE =  " + sqle.getSQLState());
            array_return_error[0]= "ERROR";
            array_return_error[1]= sqle.getMessage();
            array_return_error[2]= "ErrorCode = " + sqle.getErrorCode() + " / SQLState = " + sqle.getSQLState();
        //throw new Exception("errrrrrrrrr");
            return array_return_error;
       }
} // end catch
finally
{
  cs.close();
  LOG.info(" -- end of method\n");
  //return array_return_error;
} // end finally

} //end setStoredResult
} // end class