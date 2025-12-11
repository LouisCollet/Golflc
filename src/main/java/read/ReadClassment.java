package read;

import entite.Classment;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.LCUtil;

public class ReadClassment implements interfaces.Log, interfaces.GolfInterface{
    private List<Classment> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 
    
public Classment read(final Player player, final Round round, final Connection conn) throws SQLException{ 
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
      LOG.debug("... entering " + methodName);
      LOG.debug("for player = " + player.getIdplayer());
      LOG.debug("for round = " + round.getIdround());
      LOG.debug("number of holes = " + round.getRoundHoles());
      LOG.debug("start = " + round.getRoundStart());
    PreparedStatement ps = null;
    ResultSet rs = null;
    //https://joshuaotwell.com/conditional-logic-with-sum-and-the-if-functions-in-mysql/
try{
 /* String query = """
  SELECT
    score.player_has_round_player_idplayer,
    sum(score.ScoreExtraStroke) as TotalExtraStrokes,
    sum(score.ScorePoints) as TotalScore,
    sum( IF (score.ScoreHole > 9, ScorePoints,0)) as Last9,
    sum( IF (score.ScoreHole > 12,ScorePoints,0)) as Last6,
    sum( IF (score.ScoreHole > 15,ScorePoints,0)) as Last3,
    sum( IF (score.ScoreHole > 17,ScorePoints,0)) as Last1
  FROM score
  WHERE score.player_has_round_player_idplayer = ?
    AND score.player_has_round_round_idround = ?
""";
 next was modified 11-10-2024 */
 String query = """
    SELECT
         score.player_has_round_player_idplayer,
         sum(score.ScoreExtraStroke) as TotalExtraStrokes,
         sum(score.ScorePoints) as TotalScore,
         sum(case WHEN score.ScoreHole > 9  THEN ScorePoints ELSE 0 end) as Last9,
         sum(case WHEN score.ScoreHole > 12 THEN ScorePoints ELSE 0 end) as Last6,
         sum(case WHEN score.ScoreHole > 15 THEN ScorePoints ELSE 0 end) as Last3,
         sum(case WHEN score.ScoreHole > 17 THEN ScorePoints ELSE 0 end) as Last1
      FROM score
         WHERE score.player_has_round_player_idplayer = ?
         AND score.player_has_round_round_idround = ?;
 """;
  
  String query9Holes= """
      SELECT
         score.player_has_round_player_idplayer,
         sum(score.ScoreExtraStroke) as TotalExtraStrokes,
         sum(score.ScorePoints) as TotalScore,
         sum( IF (score.ScoreHole > 0,ScorePoints,0)) as Last9,
         sum( IF (score.ScoreHole > 3,ScorePoints,0)) as Last6,
         sum( IF (score.ScoreHole > 6,ScorePoints,0)) as Last3,
         sum( IF (score.ScoreHole > 8,ScorePoints,0)) as Last1
      FROM score
         WHERE score.player_has_round_player_idplayer = ?
         AND score.player_has_round_round_idround = ?;
   """;
 // new 13/04/2022
 if(round.getRoundHoles() == 9 && round.getRoundStart() == 1){
     query = query9Holes;
     LOG.debug("query9Holes choosen !");
 }
 
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    ps.setInt(2, round.getIdround());
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
   // int i = 0;
	while(rs.next()){
       //    i++;
           Classment c = entite.Classment.mapClassment(rs);
         liste.add(c);
	}
    //  if(i == 0){
      if(liste.isEmpty()){
         String msg = "££ liste isEmpty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
         return null;
     }else{
         LOG.debug("liste is notEmpty ResultSet " + methodName + " has " + liste.size() + " element");
     }   
        LOG.debug("exiting with Classment getFirst = "  + liste.getFirst()); // new 11-10-2024
        return liste.getFirst();
 //    }
}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    utils.DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
//}else{
////         LOG.debug("escaped to findClassmentElements repetition thanks to lazy loading");
//    return liste;  //not null, donc pas d'acces
      //   return liste;
////         return null;

//} // 
}//end method

// void main() throws SQLException, Exception{
 public static void main(String args[])throws SQLException, Exception{            
 Connection conn = new utils.DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Round round = new Round(); 
    round.setIdround(750);
    round = new read.ReadRound().read(round, conn);
//   LoadClassment ftes = new LoadClassment();
   Classment cl = new ReadClassment().read(player, round, conn);
       LOG.debug("Classment = " + cl);
utils.DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end Class