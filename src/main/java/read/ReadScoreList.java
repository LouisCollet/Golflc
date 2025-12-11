package read;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import utils.DBConnection;
import utils.LCUtil;

public class ReadScoreList {
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public  ArrayList<ScoreStableford.Score> read(final Player player, final Round round, final Tee tee,final Connection conn) throws SQLException{
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
     ArrayList<ScoreStableford.Score> scoreList = new ArrayList<>();
     int i = 0;
     while(rs.next()){
         scoreList.add(entite.ScoreStableford.Score.map(rs));
         i++;
     }
      LOG.debug(" -- ending ReadScoreList : = " + i + " / " + scoreList.toString() );
 // ici ajouter les distances 20-08-2023
     if(!scoreList.isEmpty()){
        var distance = new find.FindDistances().find(tee, conn);
        for(i=0;i<scoreList.size();i++){
            if(tee.getTeeHolesPlayed().equals("10-18")){
                scoreList.get(i).setDistances(distance.getDistanceArray()[i+9]);   // note the +9
            }else{
                scoreList.get(i).setDistances(distance.getDistanceArray()[i]);
            }
        } //end for
     } // end if
      
return scoreList;
}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = " £££ Exception in ! " + methodName + ex.getMessage();
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
    round.setIdround(689);
    Tee tee = new Tee();
    // tee à compléter
    var v = new read.ReadScoreList().read(player, round,tee, conn);//
     LOG.debug("ScoreList = " + v.toString());
  }catch (Exception e){
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
  }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
  }
}// end main
} // end class