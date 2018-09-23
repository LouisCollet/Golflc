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

/**    à modifier !!!
 *
 * @author collet games = new String[7];
 */
public class LoadScoreArray implements interfaces.Log
{
    private static String[] strokes = new String[18];
    private static int j = 0;
    //LOG.info("strokes = null constructor");
public LoadScoreArray() // constructor
    {
     //   Arrays.fill(strokes, null);
        strokes = null;
                LOG.info("strokes = null constructor");
              j++;
              LOG.info("j = " + j);
    }
    
public String [] LoadScoreArray(Connection conn, final Player player, final Round round) throws SQLException
{
if (strokes == null) // ce test ne fonctonne pas !
{   LOG.info("strokes = null YES");
}
// if (strokes == null) // ce test ne fonctonne pas !
//{   
    LOG.info("starting LoadScoreArray with strokes = " + Arrays.toString(strokes) );
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
        LOG.info("player = " + player);
        LOG.info("course = " + round);
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
      while (rs.next())
         // LOG.info(" -- RoundStart1 = " + rs.getInt("RoundStart") );
        {
            rowNum = rs.getRow() - 1;
            int start = rs.getInt("RoundStart");
  ///            LOG.info(" -- RoundStart = " + start );
            if(start == 1)
            {
                strokes[rowNum]= Integer.toString(rs.getInt("ScoreStroke") );
            }else{ //roundstart = 10
                strokes[rowNum+9]= Integer.toString(rs.getInt("ScoreStroke") ); // mod 14/11/2013
            }
        } // end while
      LOG.info(" -- array strokes [] = " + Arrays.deepToString(strokes) );
return strokes;
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
//    return strokes;  //plusieurs fois ??
//}
} //end method
private static void main(String[] args) throws SQLException, Exception // testing purposes
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
