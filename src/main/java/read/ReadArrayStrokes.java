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


public class ReadArrayStrokes {
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public int [] read(Connection conn, final Player player, final Round round) throws SQLException{
// complete l'array strokes des strokes bruts joués 
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.debug("starting " + methodName) ;
     LOG.debug(" for player = " + player.getIdplayer());
     LOG.debug(" for round = " + round.getIdround());

  final String query = """
              SELECT *
              FROM score
              WHERE score.player_has_round_round_idround = ?
                 AND score.player_has_round_player_idplayer = ?;
     """ ;
     
     ps = conn.prepareStatement(query);
     ps.setInt(1, round.getIdround());
     ps.setInt(2, player.getIdplayer());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     int[] arrayStrokes = new int[18];
     int i = 0;
     int plus = 0;
     while(rs.next()){
  //      LOG.debug("i = " + i);
        if(i == 0){
            plus = rs.getInt("ScoreHole")-1;  // starthole = 10 on complete de 10à 18
            LOG.debug("plus = " + plus);
        }
         int j = i + plus;
         arrayStrokes[j]= rs.getInt("ScoreStroke");
         i++;
  //           LOG.debug(" ArrayStrokes = " + Arrays.toString(arrayStrokes));
       } // end while
      LOG.debug(" -- ending LoadArrayStrokes : = " + Arrays.toString(arrayStrokes) );
return arrayStrokes;
}catch (SQLException e){
    String msg = "SQL Exception in LoadstrokesEurrray = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = " £££ Exception ! " + ex.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method

void main() throws SQLException, Exception{
    Connection conn = new DBConnection().getConnection();
  try{
    Player player = new Player();
    player.setIdplayer(324713);
  //  player = new load.LoadPlayer().load(player, conn);
    Round round = new Round();
    round.setIdround(630);
    int [] strokes = new read.ReadArrayStrokes().read(conn, player, round);//
     LOG.debug("array strokes = " + Arrays.toString(strokes));
  }catch (Exception e){
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
  }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
  }
}// end main
} // end class