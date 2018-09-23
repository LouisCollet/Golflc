
package utils;

import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author collet
 */
public class __Score_Insert_Update implements interfaces.GolfInterface, interfaces.Log 
{
    public int getCountScore(Connection conn, Player player, Round round, String operation)
        throws SQLException
{
    PreparedStatement ps = null;
    ResultSet rs = null;
    String query = null;
try
{
   //     LOG.info("player  = " + idplayer);
   //     LOG.info("round  =  " + idround);
   //     LOG.info("Connection =  " + conn);
if (operation.equalsIgnoreCase("rows") )
    {query =
            "SELECT count(*)"
          + " FROM score"
          + " WHERE score.player_has_round_player_idplayer=?"
          + "   and player_has_round_round_idround=?"
          ;
    }else{
    query =
            "SELECT sum(scorestroke)"
          + " FROM score"
          + " WHERE score.player_has_round_player_idplayer=?"
          + " and player_has_round_round_idround=?"
          ;
    }

    ps = conn.prepareStatement(query);
    ps.setInt(1,player.getIdplayer());
    ps.setInt(2,round.getIdround());
          //    String p = ps.toString();
  //        utils.LCUtil.logps(ps);
    rs = ps.executeQuery(); // attention !! il ne faut rien mettre entre les ()
        //LOG.debug(" -- resultset = " + rs.toString());
    if (rs.next())
    {  // LOG.debug("resultat : getCountScore = " + rs.getInt(1) );
      return rs.getInt(1);
    }else{
      //  LOG.debug("no next : getCountScore = " + rs.getInt(1) );
        return 99;  //error code
    }
} catch(SQLException sqle){
    String msg = "Â£Â£Â£ SQLException in getCountScore = " + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
        LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 99;
}catch(NumberFormatException nfe){
    String msg = "Â£Â£Â£ NumberFormatException in getCountScore = " + nfe.getMessage();
        LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 99;
}
finally
{
      DBConnection.closeQuietly(null, null, rs, ps);
}

} //end method
}
