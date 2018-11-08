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

public class LoadScoreArray implements interfaces.Log
{
  //  private static String[] strokes = new String[18];
    private final static int[] STROKES = new int[18];
 //   private static int j = 0;
    //LOG.info("strokes = null constructor");
public LoadScoreArray() // constructor
    {
     //   Arrays.fill(strokes, null);
    //    STROKES = null;
        //        LOG.info("strokes = null constructor");
      //        j++;
      //        LOG.info("j = " + j);
    }
public int[] LoadScoreArray(Connection conn, final Player player, final Round round) throws SQLException{
//if (STROKES == null) // ce test ne fonctonne pas !
//{  // LOG.info("strokes = null YES");

// if (strokes == null) // ce test ne fonctonne pas !
//{   
  //  Arrays.fill(strokes, 0); // new 25-10-2018 dumpt !!
    
        LOG.info("starting LoadScoreArray with strokes = " + Arrays.toString(STROKES) );
        LOG.info("player = " + player.toString());
        LOG.info("round = " + round.toString());
        LOG.info("after player and round");
        
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{    
  //  String[] strokes = new String[18];  // mod 31/7/2016
     LOG.info("starting LoadScoreArray= " );
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
          "SELECT scorehole, scorepar, scorestrokeindex, ScoreStroke,idround, ScorePoints,"
          + " scoreextrastroke, RoundStart "
          + "		from score, round"
          + "		where score.player_has_round_player_idplayer = ?"
          + "		and round.idround = ?"
          + "		and score.player_has_round_round_idround = round.idround"
          + "		and round.idround = score.player_has_round_round_idround"
  ;

		//get round data from database

     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());
         //    String p = ps.toString();
         utils.LCUtil.logps(ps);
		//get round data from database
    rs =  ps.executeQuery();
    rs.beforeFirst(); //  Initially the cursor is positionned before the first row
      int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
        while(rs.next()){
     //     LOG.info("rs.getRow = " + rs.getRow());
            rowNum = rs.getRow() - 1;
     //         LOG.info(" -- rowNum = " + rowNum);
        //       int start = rs.getInt("RoundStart");
                 //  LOG.info(" -- start = " + start);
            if(rs.getInt("RoundStart") == 1)
            {  // LOG.info("score = " + rs.getInt("ScoreStroke"));
             //   strokes[rowNum]= Integer.toString(rs.getInt("ScoreStroke") );
                STROKES[rs.getRow()-1]= rs.getInt("ScoreStroke") ;
      //          LOG.info(" -- inserting LoadScoreArray = " + Arrays.toString(STROKES) );
            }else{ //roundstart = 10
                STROKES[rowNum+9]= rs.getInt("ScoreStroke") ; 
            }
       } // end while
      LOG.info(" -- exiting LoadScoreArray with strokes [] = " + Arrays.toString(STROKES) );
return STROKES;
}catch (SQLException e){
    String msg = "SQLException in LoasScoreArray() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    LOG.error("NullPointerException in LoadScoreArray() " + npe);
    LCUtil.showMessageFatal("Exception = " + npe.toString() );
     return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in LoadScoreArray = " + ex.toString() );
     return null;
}
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // mod 14/08/2014
}
//}else{
//         LOG.debug("escaped to ScoreArray repetition with lazy loading");
//  return STROKES;  //plusieurs fois ??
//}
} //end method
private static void main(String args) throws SQLException, Exception // testing purposes
{
    DBConnection dbc = new DBConnection();
Connection conn = dbc.getConnection();
    Player player = null;
    Round round = null; 
player.setIdplayer(324713);
round.setIdround(206);
//int [] t = LoadScoreArray(conn, player, round );
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class
