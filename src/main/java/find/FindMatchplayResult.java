package find;

import entite.Club;
import entite.MatchplayPlayerResult;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class FindMatchplayResult {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static List<MatchplayPlayerResult> liste = null;
    
 public List<MatchplayPlayerResult> find(final Player player,final Round round,final Connection conn) throws SQLException{   
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug("entering : " + methodName); 
        LOG.debug("starting FindMatchplayResult for player = " + player);
        LOG.debug("starting FindMatchplayResult for round = " + round);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{ 
    final String query = """
    SELECT player.PlayerFirstName, player.PlayerLastName, player.idplayer, round.idround, score.ScoreHole, score.ScoreStroke
    FROM round
    JOIN player
        ON player.idplayer = ?
        AND round.idround = ?
    JOIN score
        ON score.player_has_round_player_idplayer = player.idplayer
        AND score.player_has_round_round_idround = round.idround
""";
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    ps.setInt(2, round.getIdround());
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
	while(rs.next()){
             MatchplayPlayerResult result = entite.MatchplayPlayerResult.map(rs);
             liste.add(result);
	}
    if(liste.isEmpty()){
         String msg = "££ Empty Result " + methodName + " for player = " + player.getIdplayer();
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
     }else{
         LOG.debug("ResultSet FindMatchplayResult is " + liste.size());
     }    
        return liste;
}catch (SQLException e){
    String msg = "SQL Exception in = " + methodName + " / " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

void main(String [] args) throws SQLException, Exception{ // testing purposes
    
 //   LOG.debug("arguments passed = " + args[0] + " / " + args[1]);
    for(String item : args){
        LOG.debug(" argument => " + item);
    }
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Round round = new Round();
    round.setIdround(694);
    var p1 = new FindMatchplayResult().find(player, round, conn);
       LOG.debug("result found = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class