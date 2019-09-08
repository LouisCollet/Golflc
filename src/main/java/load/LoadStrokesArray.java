
package load;

import calc.CalcWorkHcpStb;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;

public class LoadStrokesArray implements interfaces.Log{

public int [][] load(Connection conn, int [][] points, final Player player, final Round round) throws SQLException{

  //  LOG.info("starting LoadStrokesArray with par = " + Arrays.toString(points) ); // nonArrays.deepToString(table));
    LOG.info("starting LoadStrokesArray with par = " + Arrays.deepToString(points));
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.info("starting LoadStrokesArray = " + " player = " + player.getIdplayer() + " round = " + round.getIdround());
     String sc = utils.DBMeta.listMetaColumnsLoad(conn, "score");
     String query =     // attention faut un espace en fin de ligne avant le " !!!!
          "SELECT  " + sc + "," + "round.idround"
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
         utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     // a faire : controle des résultats
     
     
     
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
    String msg = "SQL Exception in LoadStrokesArray = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in LoadStrokesArray = " + ex.toString() );
     return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method

public static void main(String[] args) throws SQLException, Exception{
  
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
 //   LOG.info("line 010");
    Round round = new Round();
    round.setIdround(437);
 //    LOG.info("line 011");
   // LocalDateTime ldt = LocalDateTime.of(2017,Month.AUGUST,26,0,0);
   // round.setRoundDate(ldt);
    int [][] points = new CalcWorkHcpStb().createArrayPoints(18);
  //        LOG.info("line 012");
    int[][] a = new LoadStrokesArray().load(conn, points, player, round);
    LOG.info(" array points filled = " + Arrays.deepToString(a));

DBConnection.closeQuietly(conn, null, null, null);

}// end main

} // end class