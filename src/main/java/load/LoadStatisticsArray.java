package load;

import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;

/** à modifier !!
 *
 * @author collet games = new String[7];
 */
public class LoadStatisticsArray implements interfaces.Log{
    private final static String[][] statistics = new String[18][5]; // scorebunker
 //   private static int[] par = new int[18]; //va contenir les par des 18 trous

public String[][] LoadStatisticsArray(Connection conn, final Player player, final Round round) throws SQLException
{ 
        // pour un joueur particulier et un course !!! new 27/01/2013
        // en entrée : points array empty
        ResultSet rs = null;
        PreparedStatement ps = null;
try
{    
    LOG.info("starting LoadStatisticsArray with player= = " + player.getIdplayer() 
            + " round = " + round.getIdround() + " connection = " + conn);
     String sc = utils.DBMeta.listMetaColumnsLoad(conn, "score");
  //   String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String query =     // attention faut un espace en fin de ligne avant le " !!!!
          "SELECT "
          + sc + "," + "round.idround"
     //     + "scorehole, scorepar, scorestrokeindex, ScoreStroke,idround, ScorePoints, scoreextrastroke,"
      //    + " ScoreFairway, ScoreGreen, ScorePutts, ScoreBunker, ScorePenalty "
          + "		from score, round"
          + "		where score.player_has_round_player_idplayer = ?"
          + "		and round.idround = ?"
          + "		and score.player_has_round_round_idround = round.idround"
          + "		and round.idround = score.player_has_round_round_idround"
  ;

     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());
         utils.LCUtil.logps(ps);
		//get round data from database
    rs =  ps.executeQuery();
    rs.beforeFirst(); //  Initially the cursor is positionned before the first row
      int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
      while (rs.next())
        {rowNum = rs.getRow() - 1;
           statistics[rowNum][0]= Integer.toString(rs.getInt("ScoreFairway") );
           statistics[rowNum][1]= Integer.toString(rs.getInt("ScoreGreen") );
           statistics[rowNum][2]= Integer.toString(rs.getInt("ScorePutts") );
           statistics[rowNum][3]= Integer.toString(rs.getInt("ScoreBunker") );
           statistics[rowNum][4]= Integer.toString(rs.getInt("ScorePenalty") );
        } // end while
      LOG.info(" -- statistics [][] = " + Arrays.deepToString(statistics) );
return statistics;
}
catch (SQLException e)
{
    String msg = "SQLException in getStatisticsArray() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}
catch (NullPointerException npe)
{
    LOG.error("NullPointerException in getStatisticsArray() " + npe);
    LCUtil.showMessageFatal("Exception = " + npe.toString() );
     return null;
}
catch (Exception ex)
{
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in getStatisticssArray = " + ex.toString() );
     return null;
}
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); //mod 14/08/2014
}

} //end method
private static void main(String[] args) throws SQLException, Exception // testing purposes
{
    DBConnection dbc = new DBConnection();
Connection conn = dbc.getConnection();
    Player player = null;
    Round round = null; 
player.setIdplayer(324713);
round.setIdround(206);

//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class
