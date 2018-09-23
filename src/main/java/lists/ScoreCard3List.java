package lists;

import entite.Player;
import entite.PlayerHasRound;
import entite.Round;
import entite.ScoreCard;
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
    private static List<ScoreCard> liste = null; 
    
public List<ScoreCard> getScoreCardList3(final Player player, final Round round ,
         final PlayerHasRound phr, final Connection conn) throws SQLException
{   
if(liste == null)
{    
    LOG.debug("starting getScoreCardList3... = ");
    LOG.info("player = ... = " + player.toString());
    LOG.info("round = ... = " + round.toString());
    LOG.info("playerHasRound = ... = " + phr.toString());
    
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{

    String query =
          "SELECT ScoreHole, ScorePar ,HoleStrokeIndex, ScoreExtraStroke, HoleDistance,"
        + "  ScoreStroke, ScorePoints, ScoreFairway, ScoreGreen, ScorePutts, ScoreBunker, ScorePenalty,"
        + " idround, RoundCSA, RoundHoles, RoundCompetition, RoundGame,RoundQualifying, "
        + " Player_has_roundZwanzeursResult, Player_has_roundZwanzeursGreenshirt"
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
   //int TotalDistance = 0;
   //int TotalPar = 0;

    ScoreCard cc = new ScoreCard();

while(rs.next())
{
            cc = new ScoreCard(); // est réi, donc total = 0
		//cc.setIdclub(rs.getInt("idclub") ); // was idscoreCard : not case sensitive ??
            cc.setIdround(rs.getInt("idround"));
            // LOG.debug("idround = " + cc.getIdround());
       //     cc.setIdclub(rs.getInt("idclub"));
            cc.setScoreHole(rs.getShort("ScoreHole") );
            cc.setScorePar(rs.getShort("ScorePar") );
            cc.setHoleStrokeIndex(rs.getShort("HoleStrokeIndex") );
            cc.setScoreExtraStroke(rs.getShort("ScoreExtraStroke") );
            cc.setHoleDistance(rs.getShort("HoleDistance") );
            cc.setScoreStroke(rs.getShort("ScoreStroke") );
            cc.setScorePoints(rs.getShort("ScorePoints") );
            cc.setScoreFairway(rs.getShort("ScoreFairway") );
            cc.setScoreGreen(rs.getShort("ScoreGreen") );
            cc.setScorePutts(rs.getShort("ScorePutts") );
            cc.setScoreBunker(rs.getShort("ScoreBunker") );
            cc.setScorePenalty(rs.getShort("ScorePenalty") );
            cc.setRoundCBA(rs.getShort("RoundCSA") );
            cc.setRoundHoles(rs.getShort("RoundHoles") );
            cc.setRoundCompetition(rs.getString("RoundCompetition") );
            cc.setRoundGame(rs.getString("RoundGame") );
            cc.setRoundQualifying(rs.getString("RoundQualifying") );
            cc.setPlayerhasroundZwanzeursResult(rs.getShort("Player_has_roundZwanzeursResult") );
            cc.setPlayerhasroundZwanzeursGreenshirt(rs.getShort("Player_has_roundZwanzeursGreenshirt") );

            liste.add(cc);			//store all data into a List
            //    LOG.info("just after add to listsc3");
} //end while
      LOG.info("listsc3 after while = " + liste.toString() );
      // ici classement zawanzeurs
  //    LOG.info("classement zwanzeurs roundholes = " + cc.getRoundHoles() );
//      LOG.info("classement zwanzeurs totalstroke = " + getTotalStroke() );
   //   LOG.info("classement zwanzeurs totalPar = " + getTotalPar() );
      
  //    LOG.info("classement zwanzeurs handicap = " + HandicapPlayer );  // positionné dans getScoreCardList1
        // aller le chercher dans scorecard1list !!!

    return liste;
}catch (SQLException e){       String msg = "SQL Exception in getScoreCardList3 " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){   String msg = "NullPointerException in getScoreCardList3() " + npe;
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
         //    LOG.debug("escaped to ScoreCard3 repetition thanks to lazy loading");
return liste;  //plusieurs fois ??
}

} //end method

    public static List<ScoreCard> getListe() {
        return liste;
    }

    public static void setListe(List<ScoreCard> liste) {
        ScoreCard3List.liste = liste;
    }



} //end Class