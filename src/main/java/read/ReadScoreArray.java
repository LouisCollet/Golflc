package read;

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

// chargement des scores prélablement à l'affichage de score_stableford.xhtml
public class ReadScoreArray {
    private static int[] STROKES = null; //new String[18];
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public ReadScoreArray(){ // constructor
    STROKES = new int[18];
    }

public int[] load(Connection conn, final Player player, final Round round) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        LOG.debug("starting " + methodName + " with strokes = " + Arrays.toString(STROKES) );
        LOG.debug("player = " + player.toString());
        LOG.debug("round = " + round.toString());
        LOG.debug("after player and round");
        
    PreparedStatement ps = null;
    ResultSet rs = null;
try{    
  //   LOG.debug("starting LoadScoreArray= " );
     
 //    String sc = utils.DBMeta.listMetaColumnsLoad(conn, "score");
  //   String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
  final String query = """
          SELECT *
          	from score, round
          	where score.player_has_round_player_idplayer = ?
          	and round.idround = ?
          	and score.player_has_round_round_idround = round.idround
          	and round.idround = score.player_has_round_round_idround
      """   ;

     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
//     rs.beforeFirst(); //  Initially the cursor is positionned before the first row
      int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
        while(rs.next()){
     //     LOG.debug("rs.getRow = " + rs.getRow());
            rowNum = rs.getRow() - 1;
     //         LOG.debug(" -- rowNum = " + rowNum);
        //       int start = rs.getInt("RoundStart");
                 //  LOG.debug(" -- start = " + start);
            if(rs.getInt("RoundStart") == 1){ 
                // LOG.debug("score = " + rs.getInt("ScoreStroke"));
             //   strokes[rowNum]= Integer.toString(rs.getInt("ScoreStroke") );
                STROKES[rs.getRow()-1]= rs.getInt("ScoreStroke") ;
      //          LOG.debug(" -- inserting LoadScoreArray = " + Arrays.toString(STROKES) );
            }else{ //roundstart = 10
                STROKES[rowNum+9]= rs.getInt("ScoreStroke") ; 
            }
       } // end while
      LOG.debug(" -- exiting LoadScoreArray with strokes [] = " + Arrays.toString(STROKES) );
return STROKES;
}catch (SQLException e){
    String msg = "SQLException in LoadScoreArray() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in LoadScoreArray = " + ex.toString() );
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // mod 14/08/2014
}
//}else{
//         LOG.debug("escaped to ScoreArray repetition with lazy loading");
//  return STROKES;  //plusieurs fois ??
//}
} //end method

//public static void main(String args) throws SQLException, Exception{  // erreur cherchée !!
 void main() throws SQLException, Exception{    
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Round round = new Round(); 
    round.setIdround(676);
   int[] i = new read.ReadScoreArray().load(conn, player, round);
       LOG.debug("result main = " + Arrays.toString(i));
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class