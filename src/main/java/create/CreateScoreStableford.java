package create;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;

public class CreateScoreStableford implements Serializable{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public boolean create(final ScoreStableford score, final Round round, final Player player,
            final Connection conn) throws SQLException{
  final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
try{
       LOG.debug("starting " + methodName);
       LOG.debug(" with ScoreStableford = " + score);
       LOG.debug(" for Round  = " + round);
       LOG.debug(" for Player  = " + player);
     final String query = LCUtil.generateInsertQuery(conn, "score");
     ps = conn.prepareStatement(query);
     for(ScoreStableford.Score sco : score.getScoreList()){
  //       LOG.debug("This is the score " + sco);
         ps.setNull(1, java.sql.Types.INTEGER);// auto-increment
         ps.setInt(2,sco.getHole());
         ps.setInt(3,sco.getStrokes());  //ici scoreStroke
         ps.setInt(4,sco.getExtra()); // ScoreExtraStroke
         ps.setInt(5,sco.getPoints()); // ScorePoints
         ps.setInt(6,sco.getPar()); // ScorePar, 
         ps.setInt(7,sco.getIndex()); // ScoreStrokeIndex
         ps.setInt(8, 0); // ScoreFairway, introduit à 0
         ps.setInt(9, 0); // ScoreGreen, introduit à zéro
         ps.setInt(10, 0); // ScorePutts, introduit à zéro
         ps.setInt(11, 0); // ScoreBunker, introduit à zéro
         ps.setInt(12, 0); // ScorePenalty, introduit à zéro
         ps.setInt(13, player.getIdplayer());
         ps.setInt(14, round.getIdround());
         ps.setTimestamp(15, Timestamp.from(Instant.now()));
         utils.LCUtil.logps(ps);
         int row = ps.executeUpdate();
         if(row != 0){
             score.setIdscore(LCUtil.generatedKey(conn));
         }else{
              String msg = "<br/>NOT NOT insert for hole = " + sco.getHole();
    //             + " , points = " + sco;
  //               + " , round = " + round.getIdround();
                 LOG.debug(msg);
                 LCUtil.showMessageFatal(msg);
                 return false;
         }

    String msg = "<br/>Successful insert scores for score = " + sco;
 //      + player.getIdplayer() + " /" + player.getPlayerLastName()
 //      + " , round = " + round.getIdround();
  LOG.debug(msg);
    //                    LCUtil.showMessageInfo(msg);
  } // end for : loop per hole
     
     // new 18/07/2022
       LOG.debug("just before SUM totalPoints");
      if(new update.UpdateInscriptionFinalResult().update(player, round, conn)){  // complete InscriptionFinalResult
           LOG.debug("update InscriptionFinalResult OK !!");
      }
     
        String msg = "<br/>Successful insert scores = " 
            + " , round id = " + round.getIdround()
            + " , round name = " + round.getRoundName()
            + " , <br/>player = " + player.getPlayerLastName()
            + " , player id= " + player.getIdplayer()
  //          + " , <br/>first score inserted = " + firstId
            + " , last score inserted = " + LCUtil.generatedKey(conn);
           LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
return true;

} catch (SQLException sqle) {
    String msg = "£££ SQLException in " + methodName + " /" + sqle.getMessage() + " , SQLState = "
            + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode();
   LOG.error(msg);
   LCUtil.showMessageFatal(msg);
   return false;
}catch (Exception ex){
    String msg = "Exception in CreateScoreStableford = " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
    DBConnection.closeQuietly(null, null, null, ps);
}
} // end method 

 void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
 try{
     Player player = new Player();
     player.setIdplayer(324713);
 ///           boolean b = new create.CreateScoreStableford().create(player, conn);
     LOG.debug("from main, CreateScoreStableford = "); // + b);
 }catch (Exception e){
            String msg = "££ Exception in main CreateScoreStableford = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
 }finally{
            DBConnection.closeQuietly(conn, null, null, null);
 }
} // end main//
} //end class
  //   }
 /*    for(int i=0; i<round.getRoundHoles();i++){
         ps.setNull(1, java.sql.Types.INTEGER);// auto-increment
             LOG.debug("i = " + i);
             int j = i + round.getRoundStart(); // mod 15/04/2022
             LOG.debug("j= " + i);
   //      ps.setInt(2, i + round.getRoundStart());  // holeNumber
         ps.setInt(2, j);  // holeNumber
    //     LOG.debug
         ps.setInt(3,score.getStrokeArray()[j-1]);  //ici scoreStroke
         ps.setInt(4,score.getExtraArray()[j-1]); // ScoreExtraStroke
         ps.setInt(5,score.getPointsArray()[j-1]); // ScorePoints
         ps.setInt(6,score.getParArray()[j-1]); // ScorePar, 
         ps.setInt(7,score.getIndexArray()[j-1]); // ScoreStrokeIndex
*/