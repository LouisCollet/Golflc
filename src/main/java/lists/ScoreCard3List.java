package lists;

import entite.ECourseList;
import entite.Hole;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class ScoreCard3List implements interfaces.Log
{
    private static List<ECourseList> liste = null; 
    
public List<ECourseList> getScoreCardList3(final Player player, final Round round ,
     //    final PlayerHasRound phr, final Connection conn) throws SQLException{ 
     final Inscription phr, final Connection conn) throws SQLException{ 
if(liste == null)
{    
    LOG.debug("starting getScoreCardList3... = ");
    LOG.info("player = ... = " + player.toString());
    LOG.info("round = ... = " + round.toString());
    LOG.info("playerHasRound = ... = " + phr.toString());
    
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
    String sc = utils.DBMeta.listMetaColumnsLoad(conn, "score");
    String ho = utils.DBMeta.listMetaColumnsLoad(conn, "hole");
  // String cl = utils.DBMeta.listMetaColumnsLoad(conn, "Club");
  //   String co = utils.DBMeta.listMetaColumnsLoad(conn, "Course");
    String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
  
    String query =
         "SELECT"
         + sc + "," + ho + "," + ro + "," + ph
    //      " ScoreHole, ScorePar ,HoleStrokeIndex, ScoreExtraStroke, HoleDistance,"
   //     + "  ScoreStroke, ScorePoints, ScoreFairway, ScoreGreen, ScorePutts, ScoreBunker, ScorePenalty,"
   //     + " idround, RoundCSA, RoundHoles, RoundCompetition, RoundGame,RoundQualifying, "
 //   + " Player_has_roundZwanzeursResult, Player_has_roundZwanzeursGreenshirt"

        + " FROM course"
        + " JOIN player"
        + "     ON player.idplayer = ?"
        + " JOIN round"
        + "     ON round.idround = ?"
        + "     AND round.course_idcourse = course.idcourse"
        + "  JOIN player_has_round" // new
        + "    ON  player_has_round.player_idplayer = player.idplayer" //new
        + "    AND player_has_round.round_idround = round.idround"    //new
            
        + " JOIN tee"
        + "     ON course.idcourse = tee.course_idcourse"
        + "     AND tee.TeeGender = player.PlayerGender"
        + "     AND tee.TeeStart = ? "
        + " JOIN hole"
        + "     ON hole.tee_idtee = tee.idtee"
        + "     AND hole.tee_course_idcourse = course.idcourse"
        + "     AND Hole.HoleNumber between roundstart and roundstart + roundholes - 1"
        + " JOIN score"
        + "     ON score.player_has_round_player_idplayer = player.idplayer"
        + "     AND score.player_has_round_round_idround = round.idround"
        + "     AND hole.HoleNumber = score.ScoreHole"
        + " ORDER by hole.HoleNumber"
         ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());
     ps.setString(3, phr.getInscriptionTeeStart() );

         utils.LCUtil.logps(ps);
		//get scoreCard data from database
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet ScoreCardList3 has " + rs.getRow() + " lines.");
    if(rs.getRow() == 0)    
    {String msg = "-- Empty Result Table for ScoreCardList3 !! ";
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
             throw new Exception(msg);
    }   
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>(); // new 02/06/2013
  //  ECourseList cc = new ECourseList(); mod

while(rs.next())
{
          ECourseList ecl = new ECourseList(); // est réi, donc total = 0

          Hole h = new Hole();
          h = entite.Hole.mapHole(rs);
          ecl.setHole(h);

          Round r = new Round();
          r = entite.Round.mapRound(rs);
          ecl.setRound(r);
          
          Inscription i = new Inscription();
          i = entite.Inscription.mapInscription(rs);  
          ecl.setInscriptionNew(i);

          ScoreStableford s = new ScoreStableford();
          s = entite.ScoreStableford.mapScoreStableford(rs);  
          ecl.setScoreStableford(s);
/*
            cc.setHoleStrokeIndex(rs.getShort("HoleStrokeIndex") );
            cc.setHoleDistance(rs.getShort("HoleDistance") );
            
            cc.setScoreHole(rs.getShort("ScoreHole") );
            cc.setScorePar(rs.getShort("ScorePar") );
            cc.setScoreExtraStroke(rs.getShort("ScoreExtraStroke") );
            cc.setScoreStroke(rs.getShort("ScoreStroke") );
            cc.setScorePoints(rs.getShort("ScorePoints") );
            cc.setScoreFairway(rs.getShort("ScoreFairway") );
            cc.setScoreGreen(rs.getShort("ScoreGreen") );
            cc.setScorePutts(rs.getShort("ScorePutts") );
            cc.setScoreBunker(rs.getShort("ScoreBunker") );
            cc.setScorePenalty(rs.getShort("ScorePenalty") );
            
            cc.setIdround(rs.getInt("idround"));
            cc.setRoundCBA(rs.getShort("RoundCSA") );
            cc.setRoundHoles(rs.getShort("RoundHoles") );
            cc.setRoundCompetition(rs.getString("RoundCompetition") );
            cc.setRoundGame(rs.getString("RoundGame") );
            cc.setRoundQualifying(rs.getString("RoundQualifying") );
            
            cc.setPlayerhasroundZwanzeursResult(rs.getShort("Player_has_roundZwanzeursResult") );
            cc.setPlayerhasroundZwanzeursGreenshirt(rs.getShort("Player_has_roundZwanzeursGreenshirt") );
*/
            liste.add(ecl );//store all data into a List
            //    LOG.info("just after add to listsc3");
} //end while

 ///  LOG.info("exiting ScoreCard3List with " + liste.toString());
    return liste;
}catch (SQLException e){
        String msg = "SQL Exception in getScoreCardList3 " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in getScoreCardList3() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
     //   DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
        LOG.debug("escaped to ScoreCard3 repetition thanks to lazy loading");
return liste;
}
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        ScoreCard3List.liste = liste;
    }



} //end Class