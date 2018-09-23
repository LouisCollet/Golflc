/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modify;

//import static interfaces.Log.LOG;
//import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

/**
 *
 * @author collet
 */
public class ModifyRoundPlayers implements interfaces.Log, interfaces.GolfInterface
{
    public  String updateRecordFromRound(Round round, Connection conn) throws SQLException
{
    // Connection conn = null;
    PreparedStatement ps = null;
try
{
  //  conn = DBConnection.getConnection();
        LOG.info("starting updateRecordFromRound ... = " );
    String query = "UPDATE round" +
                   " SET round.RoundPlayers = round.RoundPlayers + 1" +
                   " WHERE round.idround = ?";
    ps = conn.prepareStatement(query);
    ps.setInt(1,round.getIdround() );
         //    String p = ps.toString();
         utils.LCUtil.logps(ps);
   int row = ps.executeUpdate();
      if (row!=0)
        { LOG.info("-- successful UPDATE round # of players ");
          return "updated" + row ;
        }else{
             String msg = "-- UNsuccessful result in UPDATE for player : " + round.getIdround();
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
          return null;
        }
}catch (SQLException e){
    String msg = "SQL Exception in update round = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in updateRound() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
finally
{
        DBConnection.closeQuietly(null, null, null, ps);
}

} //end method

} //end class
