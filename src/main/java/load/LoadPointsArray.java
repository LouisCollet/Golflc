
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

public class LoadPointsArray implements interfaces.Log
{
  //  private static int[] par = new int[18]; //va contenir les par des 18 trous

public int [][] LoadPointsArray(Connection conn, int [][] points, final Player player, final Round round) throws SQLException
 
        // pour un joueur particulier et un course !!! new 27/01/2013
        // en entrée : points array emptyConnection conn, int [][] points, Integer player.getIdplayer(), Integer in_round
{
    LOG.info("starting LoadPointsArray with par = " + Arrays.deepToString(points)); //.toString(points) );
 //   int par[] = null; // new int[18]; //va contenir les par des 18 trous
 //   int par2[18] = null; //va contenir les par des 18 trous
    PreparedStatement ps = null;
    ResultSet rs = null;
   // Integer player.getIdplayer() = player.getIdplayer();
   // Integer in_round  = round.getIdround();
try
{
     LOG.info("starting getPointsArray with player = " + player.getIdplayer() + " , round = " + round.getIdround() );
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
          "SELECT HoleNumber, HolePar ,HoleStrokeIndex, idtee, idround, player_has_round.InscriptionTeeStart"
          + "    FROM course"
          + "    JOIN player"
          + "       ON player.idplayer = ?"
          + "    JOIN round"
          + "       ON round.idround = ?"
          +  "   JOIN player_has_round" +   // new 27/05/2017
"                   ON player_has_round.round_idround = round.idround" // new 27/05/2017
          + "       AND round.course_idcourse = course.idcourse"
          + "    JOIN tee"
          + "       ON course.idcourse = tee.course_idcourse"
          + "       AND tee.TeeGender = player.PlayerGender"   //activated 16/07/2016
 //         + "       AND SUBSTRING(Tee.TeeStart,1,1) = 'F' "  // à modifier depuis que YELLOW, BLUE etc..
          + "       AND tee.TeeStart = player_has_round.InscriptionTeeStart"  // new 27/05/2017
          + "    JOIN hole"
          + "       ON hole.tee_idtee = tee.idtee"
          + "       AND hole.tee_course_idcourse = course.idcourse"
          + "       AND hole.HoleNumber"
          + "           BETWEEN round.RoundStart and round.RoundStart + round.RoundHoles - 1 "
          + "    GROUP by hole.HoleNumber   "   // new 12/06/2017 pour scramble
          + "    ORDER by hole.HoleNumber"
          ;

        LOG.info("player = " + player.getIdplayer() ); 
        LOG.info("round  = " + round.getIdround() ); 
        LOG.info("holes  = " + points.length);
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());

          //    String p = ps.toString();
          utils.LCUtil.logps(ps); 
		//get round data from database
    rs =  ps.executeQuery();
    //// control 
    int j = 0;
    while(rs.next())
    {
     j++;
    }
  //  LOG.debug("nombre réponses 1 from ResultSet = " + j); value == null || value.length() == 0
    if(j==9 || j==18)
    {
      //  LOG.info("");
    }else{
        String ms = "Number of holes not Correct (9 or 18) for this round = " + j;
	LOG.error(ms);
        LCUtil.showMessageFatal(ms);
        return null;
    }
//    double d = ((ResultSetImpl)rs).getUpdateCount();
// LOG.debug("nombre réponses 2 from ResultSet = " + d);
    rs.beforeFirst(); //  Initially the cursor is positionned before the first row
      int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
      while (rs.next())
        {rowNum = rs.getRow() - 1;
            points [rowNum][0]= rs.getInt("HoleNumber");   //  hole #
            points [rowNum][1]= rs.getInt("HolePar");   //  hole par
            points [rowNum][2]= rs.getInt("HoleStrokeIndex");   //  hole index
            points [rowNum][3]= 0;  //  hole strokes
            points [rowNum][4]= 0;  //  extra
            points [rowNum][5]= 0;  //  points
        } // end while
     // LOG.info(" -- array points [][]= " + Arrays.deepToString(points) );

      LOG.info("Hole" + TAB + "Par" + TAB + "Index" + TAB +
              "Stroke" + TAB + "Extra" + TAB + "Points");
      LOG.info(NEW_LINE + Arrays.deepToString(points));
      int stop = points.length;
        LOG.info("points length = " + stop);
       for (int i=0; i<stop; i++)
         {
            LOG.info(" -- ending : hole = " + points [i][0]
                    + " , Par = "     + points [i][1]
                    + " , Index = "   + points [i][2] );
               //     + " , strokes = " + points [i][3]);
         } 
return points;
}catch (SQLException e){
    String msg = "SQL Exception in LoadPointsArray() = " + e + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    LOG.error("NullPointerException in LoadPointsArray() " + npe);
    LCUtil.showMessageFatal("Exception = " + npe.toString() );
     return null;
}catch (Exception ex){
    LOG.error("Exception in LoadPointsArray =  " + ex);
    LCUtil.showMessageFatal("Exception in LoadPointsArray = " + ex.toString() );
     return null;
}
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps);
}

} //end method

} // end class
