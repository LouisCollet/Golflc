
package entite;

import static interfaces.GolfInterface.NEWLINE;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import javax.inject.Named;
import utils.LCUtil;

@Named
public class ScoreStableford implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;
    private Integer idscore;
    private Short scoreHole;
    private Short scoreStroke;
    private Short scoreExtraStroke;
    private Short scorePoints;
    private Short scorePar;
    private Short scoreStrokeIndex;
//    private Short scoreNet;
    private Short scoreFairway;
    private Short scoreGreen;
    private Short scorePutts;
    private Short scoreBunker;
    private Short scorePenalty;
    
    
    
    private Integer player_has_round_player_idplayer;
    private Integer player_has_round_round_idround;
    private Date scoreModificationDate;
    private String[] holes; // = new String[18] dans constructor;
    private String[][] statistics; // = new String[18][5] ; //dans constructor;
 //   private Integer[] scramblePlayers;  // tient la liste des joueurs d'une partie scramble, on enregistre le résultat stb pour chacun d'eux !
    private boolean ScoreCardOK; // 17/07/2017
public ScoreStableford() // constructor
    {
 //           LOG.info(" entering constructor scorestableford");
        holes = new String[18];
        Arrays.fill(holes, "0");
 //           LOG.info(" array holes initialized" + Arrays.deepToString(holes) );
        statistics = new String[18][5]; // 18 trous, 5 statistics scorebunker
        for(String[] subarray : statistics)
        {
            Arrays.fill(subarray, "0");
        }
 //           LOG.info(" array statistics initialized" + Arrays.deepToString(statistics) );
    }
//@PostConstruct
//    public void init() // attention !! ne peut absolument pas avoir : throws SQLException
//    { 
//    }
    
    public Integer getIdscore() {
        return idscore;
    }

    public void setIdscore(Integer idscore) {
        this.idscore = idscore;
    }

    public Short getScoreHole() {
        return scoreHole;
    }

    public void setScoreHole(Short scoreHole) {
        this.scoreHole = scoreHole;
    }

    public Short getScoreStroke() {
        return scoreStroke;
    }

    public void setScoreStroke(Short scoreStroke) {
        this.scoreStroke = scoreStroke;
    }

    public Short getScoreExtraStroke() {
        return scoreExtraStroke;
    }

    public void setScoreExtraStroke(Short scoreExtraStroke) {
        this.scoreExtraStroke = scoreExtraStroke;
    }

    public Short getScorePoints() {
        return scorePoints;
    }

    public void setScorePoints(Short scorePoints) {
        this.scorePoints = scorePoints;
    }

    public Short getScorePar() {
        return scorePar;
    }

    public void setScorePar(Short scorePar) {
        this.scorePar = scorePar;
    }

    public Short getScoreStrokeIndex() {
        return scoreStrokeIndex;
    }

    public Short getScoreFairway() {
        return scoreFairway;
    }

    public void setScoreFairway(Short scoreFairway) {
        this.scoreFairway = scoreFairway;
    }

    public Short getScoreGreen() {
        return scoreGreen;
    }

    public void setScoreGreen(Short scoreGreen) {
        this.scoreGreen = scoreGreen;
    }

    public Short getScorePutts() {
        return scorePutts;
    }

    public void setScorePutts(Short scorePutts) {
        this.scorePutts = scorePutts;
    }

    public Short getScoreBunker() {
        return scoreBunker;
    }

    public void setScoreBunker(Short scoreBunker) {
        this.scoreBunker = scoreBunker;
    }

    public Short getScorePenalty() {
        return scorePenalty;
    }

    public void setScorePenalty(Short scorePenalty) {
        this.scorePenalty = scorePenalty;
    }

    public void setScoreStrokeIndex(Short scoreStrokeIndex) {
        this.scoreStrokeIndex = scoreStrokeIndex;
    }

    public Integer getPlayer_has_round_player_idplayer() {
        return player_has_round_player_idplayer;
    }

    public void setPlayer_has_round_player_idplayer(Integer player_has_round_player_idplayer) {
        this.player_has_round_player_idplayer = player_has_round_player_idplayer;
    }

    public Integer getPlayer_has_round_round_idround() {
        return player_has_round_round_idround;
    }

    public void setPlayer_has_round_round_idround(Integer player_has_round_round_idround) {
        this.player_has_round_round_idround = player_has_round_round_idround;
    }

    public Date getScoreModificationDate() {
        return scoreModificationDate;
    }

    public void setScoreModificationDate(Date scoreModificationDate) {
        this.scoreModificationDate = scoreModificationDate;
    }

//  @ScoreArray() // new 10/05/2013 // new 10/05/2013
    public String[] getHoles()
    {   
      //  LOG.debug(" getHoles (score) " + Arrays.deepToString(holes) );
        return holes;
    }


    public void setHoles(String[] holes)
    {   
        this.holes = holes;
        LOG.debug(" setHoles (score) " + Arrays.deepToString(holes) );
    }

    public String[][] getStatistics() {
        return statistics;
    }

    public void setStatistics(String[][] statistics) {
        this.statistics = statistics;
    }

    public boolean isScoreCardOK() {
        return ScoreCardOK;
    }

    public void setScoreCardOK(boolean ScoreCardOK) {
        this.ScoreCardOK = ScoreCardOK;
    }
    
    
    @Override
    public String toString()
{ return 
        (NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEWLINE 
               + " ,scoreHole : "   + this.getScoreHole()
           //    + " ,Round Date: "   + Round.SDF.format(getRoundDate() )
               + " ,scoreStroke : "   + this.getScoreStroke()
               + " ,scoreExtraStroke : "   + this.getScoreStrokeIndex()
           //    + " ,Round Date/Time: "   + Round.SDF_TIME.format(getRoundDate() )
               + " ,scorePoints : " + this.getScorePoints()
               + " ,scorePar : " + this.getScorePar()
               + " ,scoreStrokeIndex : " + this.getScoreStrokeIndex()
               + " ,holes : " + Arrays.deepToString(getHoles())
        );
}
//    public Integer[] getScramblePlayers() {
  //      return scramblePlayers;
    //}

//    public void setScramblePlayers(Integer[] scramblePlayers) {
  //      this.scramblePlayers = scramblePlayers;
    //}
 public static ScoreStableford mapScoreStableford(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        ScoreStableford s = new ScoreStableford();
            s.setScoreHole(rs.getShort("ScoreHole") );
            s.setScoreStroke(rs.getShort("ScoreStroke") );
            s.setScoreExtraStroke(rs.getShort("ScoreExtraStroke") );
            s.setScorePoints(rs.getShort("ScorePoints") );
            s.setScorePar(rs.getShort("ScorePar") );
            s.setScoreStrokeIndex(rs.getShort("ScoreStrokeIndex"));
            s.setScoreFairway(rs.getShort("ScoreFairway") );
            s.setScoreGreen(rs.getShort("ScoreGreen") );
            s.setScorePutts(rs.getShort("ScorePutts") );
            s.setScoreBunker(rs.getShort("ScoreBunker") );
            s.setScorePenalty(rs.getShort("ScorePenalty") );
                
   return s;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map
} // end class