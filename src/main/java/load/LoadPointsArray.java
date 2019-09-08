
package load;

import calc.CalcWorkHcpStb;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;

public class LoadPointsArray implements interfaces.Log{
  // va contenir les par des 18 trous

public int[][] load(Connection conn, int [][] points, final Player player, final Round round) throws SQLException{

        // pour un joueur particulier et un course !!! new 27/01/2013
        // en entrée : points array emptyConnection conn, int [][] points, Integer player.getIdplayer(), Integer in_round
    LOG.info("starting LoadPointsArray with par = " + Arrays.deepToString(points)); //.toString(points) );
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.info("starting getPointsArray with player = " + player.getIdplayer() + " , round = " + round.getIdround() );
     String ho = utils.DBMeta.listMetaColumnsLoad(conn, "hole");
     String te = utils.DBMeta.listMetaColumnsLoad(conn, "tee");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
          "SELECT " + ho + "," + ph + "," + ro + "," + pl + "," + te
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
          + "  AND tee.TeeGender = player.PlayerGender"   //activated 16/07/2016
      //    + "  AND tee.TeeStart = player_has_round.InscriptionTeeStart"  // enlevé 05-04-2018
          +"   AND tee.idtee = player_has_round.InscriptionIdTee" // new 05-04-2018
          + " JOIN hole"
     //     + "   ON hole.tee_idtee = tee.idtee" // enlevé 07-04-2019
          + "   ON hole.tee_idtee = tee.TeeMasterTee" //- inséré 07-04-2019  
          + "   AND hole.tee_course_idcourse = course.idcourse"
          + "   AND hole.HoleNumber"
          + "      BETWEEN round.RoundStart and round.RoundStart + round.RoundHoles - 1 "
          + "   GROUP by hole.HoleNumber"   // new 12/06/2017 pour scramble
          + "   ORDER by hole.HoleNumber"
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
    while(rs.next()) {
     j++;
    }
  //  LOG.debug("nombre réponses 1 from ResultSet = " + j); value == null || value.length() == 0
    if(j==9 || j==18) {
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
      while (rs.next()){
            rowNum = rs.getRow() - 1;
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
       for (int i=0; i<stop; i++){
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
}catch (Exception ex){
    LOG.error("Exception in LoadPointsArray =  " + ex);
    LCUtil.showMessageFatal("Exception in LoadPointsArray = " + ex.toString() );
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method

public static void main(String[] args) throws SQLException, Exception{
  
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713); // l = 324714
  //  player.setPlayerGender("L");
 //   LOG.info("line 010");
    Round round = new Round();
    round.setIdround(437);

   // LocalDateTime ldt = LocalDateTime.of(2017,Month.AUGUST,26,0,0);

    int[][] points = new CalcWorkHcpStb().createArrayPoints(18);
        LOG.info("empty array points = " + Arrays.deepToString(points));
    int[][] a = new LoadPointsArray().load(conn, points, player, round);
    LOG.info(" array points filled = " + Arrays.deepToString(a));

DBConnection.closeQuietly(conn, null, null, null);

}// end main



} // end class