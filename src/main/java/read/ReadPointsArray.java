
package read;

import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;

public class ReadPointsArray implements interfaces.Log{
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 
public int[][] load(Connection conn, int [][] points, final Player player, final Round round) throws SQLException{
        // pour un joueur particulier et un course !!! new 27/01/2013
        // en enstarting LoadPointsArraytrée : points array emptyConnection conn, int [][] points, Integer player.getIdplayer(), Integer in_round
  //  LOG.debug(" with par = " + Arrays.deepToString(points)); //.toString(points) );
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.debug("starting " + methodName);
     LOG.debug("for round = " + round);
     LOG.debug("player = " + player.getIdplayer() ); 
     LOG.debug("points  = " + Arrays.deepToString(points));
     
  final String query =     // attention faut un espace en fin de ligne avant le " !!!!
          "SELECT * "
          + " FROM course"
          + " JOIN player"
          + "   ON player.idplayer = ?"
          + " JOIN round"
          + "   ON round.idround = ?"
          + " JOIN player_has_round" 
          + "   ON InscriptionIdRound = round.idround"
          + "   AND round.course_idcourse = course.idcourse"
          + " JOIN tee"
          + "  ON course.idcourse = tee.course_idcourse"
          + "  AND tee.TeeGender = player.PlayerGender"
          +"   AND tee.idtee = player_has_round.InscriptionIdTee" // new 05-04-2018
          + " JOIN hole"
          + "   ON hole.tee_idtee = tee.TeeMasterTee" //- inséré 07-04-2019  
          + "   AND hole.tee_course_idcourse = course.idcourse"
          + "   AND hole.HoleNumber"
          + "      BETWEEN round.RoundStart and round.RoundStart + round.RoundHoles - 1 "
          + "   GROUP by hole.HoleNumber"   // new 12/06/2017 pour scramble
          + "   ORDER by hole.HoleNumber"
          ;

     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
      while(rs.next()){
            rowNum = rs.getRow() - 1;
            points [rowNum][0]= rs.getInt("HoleNumber");   //  hole #
            points [rowNum][1]= rs.getInt("HolePar");   //  hole par
            points [rowNum][2]= rs.getInt("HoleStrokeIndex");   //  hole index
            points [rowNum][3]= 0;  //  hole strokes
            points [rowNum][4]= 0;  //  extra
            points [rowNum][5]= 0;  //  points
        } // end while
     // LOG.debug(" -- array points [][]= " + Arrays.deepToString(points) );

      LOG.debug("Hole" + TAB + "Par" + TAB + "Index" + TAB +
              "Stroke" + TAB + "Extra" + TAB + "Points");
         LOG.debug(NEW_LINE + "Array completed = " + Arrays.deepToString(points));
/*      int stop = points.length;
         LOG.debug("points length = " + stop);
       for (int i=0; i<stop; i++){
            LOG.debug(" -- hole = " + points [i][0]
                    + " , Par = "     + points [i][1]
                    + " , Index = "   + points [i][2] );
        } 
   */    
return points;
}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + " / " + e + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception in LoadPointsArray =  " + ex);
    LCUtil.showMessageFatal("Exception in LoadPointsArray = " + ex.toString() );
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method

void main() throws SQLException, Exception{
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    player = new read.ReadPlayer().read(player, conn);
    Round round = new Round();
    round.setIdround(484);
    round = new read.ReadRound().read(round, conn);
//    int[][] points = new calc.CalcNewHandicapEGA().createArrayPoints(9);
  //      LOG.debug("empty array points = " + Arrays.deepToString(points));
  //  int[][] a = new LoadPointsArray().load(conn, points, player, round);
  //      LOG.debug(" array points filled = " + Arrays.deepToString(a));
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class