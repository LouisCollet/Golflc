
package entite;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import javax.inject.Named;

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
        ("from entite : " + this.getClass().getSimpleName()
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

} // end class