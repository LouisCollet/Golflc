package update;

import entite.Club;
import entite.Player;
import entite.Round;
//import entite.Score;
import entite.ScoreStableford;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import read.ReadClub;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateScoreStableford implements Serializable, interfaces.Log, interfaces.GolfInterface{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public boolean update(final ScoreStableford score, final Round round, final Player player, final Connection conn) throws SQLException{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
   PreparedStatement ps = null;
   ResultSet rs = null;
   boolean b = false;
 try{
       LOG.debug(" ... entering " + methodName);
       LOG.debug(" with ScoreStableford = " + score);
       LOG.debug(" with Round  = " + round);
       LOG.debug(" with Player  = " + player);
  //     LOG.debug("number of Holes  = " + round.getRoundHoles());
   //    LOG.debug("Starting Hole = " + round.getRoundStart());
  //      LOG.debug("size scoreList = " + score.getScoreList().size());
  //   LOG.debug("TODO alternative : holes to be inserted from scoreList = " + score.getScoreList().toString());
   final String query =  """
          UPDATE score
          SET ScoreStroke=?, ScorePoints=?, ScoreExtraStroke=?
          WHERE ScoreHole=?
          AND player_has_round_player_idplayer=?
          AND player_has_round_round_idround=?
       """;
  //      LOG.debug(" input for modification (points array) = " + Arrays.toString(score.getPointsArray()));
  //      LOG.debug(" input for modification (stroke array) = " + Arrays.toString(score.getStrokeArray()));
      ps = conn.prepareStatement(query);
      for(ScoreStableford.Score sco : score.getScoreList()){
//         LOG.debug("This is the score " + sco);
         ps.setInt(1,sco.getStrokes());  //ici scoreStroke
         ps.setInt(2,sco.getPoints()); // ScorePoints
         ps.setInt(3,sco.getExtra()); // ScoreExtraStroke
         ps.setInt(4,sco.getHole());
         ps.setInt(5, player.getIdplayer());
         ps.setInt(6, round.getIdround());
/*     
  for(int i=0; i<round.getRoundHoles(); i++){  // à vérifier si 9 holes !!
   //      LOG.debug(" i = " + (i));
         int hole = i + round.getRoundStart();
         LOG.debug(" treating hole = " + hole);
   // updated field
         ps.setInt(1, score.getStrokeArray()[hole-1]);
         ps.setInt(2, score.getPointsArray()[hole-1]);
         ps.setInt(3, score.getExtraArray() [hole-1]);
         ps.setInt(4, hole);
         ps.setInt(5, player.getIdplayer());
         ps.setInt(6, round.getIdround());
*/
     utils.LCUtil.logps(ps);
     int row = ps.executeUpdate();
       if(row != 0) {
          String msg = "Successful update ! Strokes = " + sco.getHole(); //re.getStrokeArray()sco;
          LOG.debug(msg);
             //  LCUtil.showMessageInfo(msg);
       }else{
           String msg = "NOT NOT Successful update, hole  = " + sco.getHole();
  //            + " , Strokes = " + score.getStrokeArray()[i];
           LOG.error(msg);
           LCUtil.showMessageFatal(msg);
           return false;
       }
     } //end for (loop sur 9 ou 18 trous)
      
// new 18/07/2022
       LOG.debug("just before SUM totalPoints");
      if(new update.UpdateInscriptionFinalResult().update(player, round, conn)){  // complete InscriptionFinalResult
           LOG.debug("update InscriptionFinalResult OK !!");
      }
      
   String msg = "<br/>Successful update scores <br/> for player = "
       + "id = " + player.getIdplayer()
       + " name " + player.getPlayerLastName()
       + "<br/> , round id = " + round.getIdround()
       + " , round name = " + round.getRoundName();
      LOG.debug(msg);
   LCUtil.showMessageInfo(msg);

 return true;

}catch (SQLException sqle) {
    String msg = "£££ SQLException in " + methodName + " / " + sqle.getMessage() + " ,SQLState = "
          + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
 }catch (Exception ex){
    String msg = "Exception in " + methodName + " / " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
 } finally {
          DBConnection.closeQuietly(null, null, null, ps);
   //         DbUtils.close(ps); // new 24-12-2020
 }
} //end method modify

  void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
     //   Player player = new Player();
     //   player.setIdplayer(324713);
     //   Round round = new Round(); 
     //   round.setIdround(300);
     Club club = new Club();
     club.setIdclub(1104);
  //   load.LoadClub(club,conn);
     Club c = new ReadClub().read(club, conn);
     c.setClubName(club.getIdclub() + "modified");
     // à modifier
  //   boolean b = new ModifyCourse().modify(c,conn);
   //      LOG.debug("from main, resultat = " + b);
         
 }catch (Exception e){
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
      //   DBConnection.closeQuietly(conn, null, null , null); 
          DBConnection.closeQuietly(conn, null, null, null);
   }
   } // end main//
} //end Class