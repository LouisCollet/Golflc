package update;

import entite.HandicapIndex;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateInscriptionFinalResult implements interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
 public boolean update(final Player player, Round round, final Connection conn) throws SQLException{
  final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
    ResultSet rs = null;
    int TotalPoints = 0;
try{
        LOG.debug("...starting " + methodName);
    //    LOG.debug("...for handicapIndex " + handicapIndex);
 //       LOG.debug("...for playerId " + handicapIndex.getHandicapPlayerId());
 
      String query2 = """
SELECT SUM(score.ScorePoints) AS totalPoints
FROM score
WHERE score.player_has_round_player_idplayer = ?
AND score.player_has_round_round_idround = ?
""";
        ps = conn.prepareStatement(query2);
        ps.setInt(1, player.getIdplayer());
        ps.setInt(2, round.getIdround());
        utils.LCUtil.logps(ps);
        rs =  ps.executeQuery();
      	while(rs.next()){
               TotalPoints = rs.getInt("totalPoints");
	} //end while
 
    LOG.debug("totalPoints to update = " + TotalPoints);
    final String query = """
            UPDATE player_has_round
            SET InscriptionFinalResult = ?
            WHERE InscriptionIdPlayer = ?
            AND InscriptionIdRound = ?
            """ ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, TotalPoints);
    ps.setInt(2, player.getIdplayer());
    ps.setInt(3, round.getIdround());
   
    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate();
    if(row != 0){
          String msg = "successful UPDATE InscriptionFinalResult for TotalPoints = " + TotalPoints;
          LOG.info(msg);
       //   LCUtil.showMessageInfo(msg);
          return true;
     }else{
          String msg = "-- UNsuccessful result in " + methodName + " for player : " + player;
          LOG.error(msg);
          LCUtil.showMessageFatal(msg);
          return false;
        }
}catch(SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch(Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
 
 void main() throws Exception, SQLException{
    Connection conn = new DBConnection().getConnection();
     HandicapIndex index = new HandicapIndex();
 /*
    Player player = new Player();
    player.setIdplayer(324713);
    player = new load.LoadPlayer().load(player, conn);
    Round round = new Round();
    round.setIdround(487);
   
   index.setHandicapScoreDifferential(new BigDecimal("28.6"));
   index.setHandicapPlayerId(player.getIdplayer());
   index.setHandicapRoundId(round.getIdround());
   index.setHandicapDate(round.getRoundDate());
 */
    index.setHandicapId(26); // changer si modification !!
    index.setHandicapWHS(BigDecimal.valueOf(2.3));
    index.setHandicapComment("no comment for this handicap");
    Short s1 = -3;
    index.setHandicapExceptionalScoreReduction(s1);
    index.setHandicapSoftHardCap("capM"); // 4 pos
    LOG.debug("still in main - index = " + index);
     DBConnection.closeQuietly(conn, null, null, null); 
}// end main
} //end class