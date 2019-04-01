package create;

//import entite.Handicap;
import entite.Player;
import entite.Round;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import utils.LCUtil;
/**
 *
 * @author collet new 24/06/2014
 */
public class CreateHandicap implements interfaces.Log{

 public String[] create(Connection conn, Player player, 
            Round round, double in_newHcp) throws SQLException
    {
       String []array_return_error = new String [3];
    LOG.info(" -- Start of createHandicap : ");
    LOG.info(" -- player = " + player.getIdplayer() );
    LOG.info(" -- Round Date = " + round.getIdround() );
    LOG.info(" -- Round Qualifying = " + round.getRoundQualifying() );
 //   LOG.info(" -- Old (playing) Handicap = " + handicap.getPlayingHandicap() );
    LOG.info(" -- New Handicap = " + in_newHcp);
    LCUtil.showMessageInfo(" -- New Handicap = " + in_newHcp);        
    CallableStatement cs = null;

try{
    final String stored_name = "set_handicap(?,?,?,?,?)";   // nom de la stored pro, 5 parameters
        LOG.info(" -- Before CALL Start with : " + stored_name);
    cs = conn.prepareCall("{CALL " + stored_name + "}");
    String p = cs.toString();
        LOG.debug("Callable statement " + p.substring(p.indexOf(":"), p.length() ));
    //Bind IN parameter first : player and round date
        LOG.info(" -- After CALL : " + stored_name);
    cs.setInt(1, player.getIdplayer());
        LOG.info(" Player = " + player.getIdplayer());
    cs.setInt(2,round.getIdround());
        LOG.info(" Round = " + round.getIdround());
    cs.setDouble(3, in_newHcp);
        LOG.info(" New Handicap = " + in_newHcp);
    // Register OUT parameters
      cs.registerOutParameter(4, Types.VARCHAR); // indic
      cs.registerOutParameter(5, Types.DATE); // new 07/04/2012
        LOG.debug(" -- CallableStatement completed = " + cs.toString());
      boolean hasResults = false;
      hasResults = cs.execute();    // execute statement
        LOG.info(" -- Executed stored procedure = " + " / " + hasResults);
        LOG.info(" -- Date new handicap = " + " / " + cs.getDate("date_new_handicap") );
      String indic = cs.getString(4);
      if(indic.equals("NO"))
      {
          LOG.info(" -- New HCP = old HCP : no new handicap inserted " + indic);
      } 
        LOG.info(" -- Execution indicator = (must be OK) " + indic);
        String msg = "handicap really created = " + in_newHcp;
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
      array_return_error[0]= "NO ERROR";
 
      array_return_error[1]= array_return_error[2]= "";
      return array_return_error;
}catch(SQLException sqle) {
       LOG.error(" -- SQL Exception by LC = " + sqle.getMessage());
       LOG.error(" -- ErrorCode = " +  sqle.getErrorCode() );
       LOG.error(" -- SQLSTATE =  " + sqle.getSQLState());
       array_return_error[0]= "__ERROR";   // mod provisoirement 24/02/2012
       array_return_error[1]= sqle.getMessage();
       array_return_error[2]= "ErrorCode = " + sqle.getErrorCode() + " / SQLState = " + sqle.getSQLState();
       return array_return_error;
    
}catch(Exception e) {
       LOG.error(" -- Exception by LC = " + e.getMessage());
       array_return_error[0]= "__ERROR";   // mod provisoirement 24/02/2012
       array_return_error[1]= e.getMessage();
       return array_return_error;
    } // end catch

finally
{
  cs.close();
  LOG.info(" -- finally - end of method\n");
  //return array_return_error;
}
} //end setStoredNewHandicap
} // end Class