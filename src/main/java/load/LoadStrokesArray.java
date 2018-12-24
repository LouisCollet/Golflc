
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

public class LoadStrokesArray implements interfaces.Log{
   // private static int[] par = new int[18]; //va contenir les par des 18 trous

public int [][] LoadStrokesArray(Connection conn, int [][] points, final Player player, final Round round) throws SQLException
         // pour un joueur particulier et un course !!! new 27/01/2013
        // en entrée : points array emptyConnection conn, int [][] points, Integer player.getIdplayer(), Integer in_round
{
    LOG.info("starting LoadStrokesArray with par = " + Arrays.toString(points) );
 //   int par[] = null; // new int[18]; //va contenir les par des 18 trous
 //   int par2[18] = null; //va contenir les par des 18 trous
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.info("starting LoadStrokesArray = " + " player = " + player.getIdplayer() + " round = " + round.getIdround());
     String sc = utils.DBMeta.listMetaColumnsLoad(conn, "score");
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
          "SELECT  " 
          + sc + "," + "round.idround"
      //    scorehole, scorepar, scorestrokeindex, scorestroke,, scorepoints, scoreextrastroke "
          + "		from score, round"
          + "		where score.player_has_round_player_idplayer = ?"
          + "		and round.idround = ?"
          + "		and score.player_has_round_round_idround = round.idround"
          + "		and round.idround = score.player_has_round_round_idround"
          + "           and scorestroke > 0"   // new 2/11/2013 utile si partie 9 holes !!
  ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());
     //ps.setInt(3, in_holenumber);
         utils.LCUtil.logps(ps);
		//get round data from database
    rs =  ps.executeQuery();
    rs.beforeFirst(); //  Initially the cursor is positionned before the first row
      int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
      while (rs.next())
        {rowNum = rs.getRow() - 1;
           points[rowNum][3]= rs.getInt("ScoreStroke");   //  hole Par
           LOG.info(" strokes from ScoreStroke for hole " + (rowNum+1)  + " = " + points[rowNum][3] );
        } // end while
     // LOG.info(" -- array points [][]= " + Arrays.deepToString(points) );

      LOG.info("Row" + TAB + "Hole" + TAB + "Par" + TAB + "Index" + TAB +
              "Stroke" + TAB + "Extra" + TAB + "Points");

//       for (int i=0; i<points.length; i++)
//    {
        LOG.info(" -- ending LoadStrokesArray : = " + Arrays.deepToString(points) );
//    } 
return points;
}catch (SQLException e){
    String msg = "SQL Exception in getHoleStrokes = " + e.toString() + ", SQLState = " + e.getSQLState().toString()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in getHoleStrokes = " + ex.toString() );
     return null;
}
finally
{
        DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method
} // end class