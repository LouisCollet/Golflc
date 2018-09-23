
package entite;

//import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;

@Named
public class ScoreScramble implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;
    private Integer idclub;
    private Integer idcourse;

    private String clubName;
    private String courseName;
    
 //   private Date roundDate;
    private String roundCompetition;
    private LocalDateTime roundDate; // mod 01/06/2017
    private String roundGame;
    private Integer idround;
    
    private Integer idplayer;
    private String playerFirstName;
    private String playerLastName;
    
    private Integer idscore;
 //   private Integer idround;
    private Short scoreHole;
    private Short scoreStroke;
    private Short scoreExtraStroke;
    private Short scorePoints;
    private Short scorePar;
    private Short scoreStrokeIndex;
    
    private Integer player_has_round_player_idplayer;
    private Integer player_has_round_round_idround;
        private String inscriptionTeam;
        
    private Date scoreModificationDate;
    private Integer[] players;
    private String[] holes; // = new String[18] dans constructor;
    private String[][] statistics; // = new String[18][5] ; //dans constructor;

public ScoreScramble() // constructor
    {
        players = new Integer[4];
        Arrays.fill(players, 0);
   //         LOG.info(" array players initialized" + Arrays.deepToString(players) );
        holes = new String[18];
        Arrays.fill(holes, "0");
    //        LOG.info(" array holes initialized" + Arrays.deepToString(holes) );
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

    public Integer getIdround() {
        return idround;
    }

    public void setIdround(Integer idround) {
        this.idround = idround;
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

    public String getInscriptionTeam() {
        return inscriptionTeam;
    }

    public void setInscriptionTeam(String inscriptionTeam) {
        this.inscriptionTeam = inscriptionTeam;
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
        LOG.debug(" getHoles (score) " + Arrays.deepToString(holes) );
        return holes;
    }

    public void setHoles(String[] holes)
    {   
        this.holes = holes;
        LOG.debug(" setHoles (score) " + Arrays.deepToString(holes) );
    }

    public Integer[] getPlayers() {
        return players;
    }

    public void setPlayers(Integer[] players) {
        this.players = players;
    }

    public String[][] getStatistics() {
        return statistics;
    }

    public void setStatistics(String[][] statistics) {
        this.statistics = statistics;
    }

    public String getRoundCompetition() {
        return roundCompetition;
    }

    public void setRoundCompetition(String roundCompetition) {
        this.roundCompetition = roundCompetition;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public LocalDateTime getRoundDate() {
        return roundDate;
    }

    public void setRoundDate(LocalDateTime roundDate) {
        this.roundDate = roundDate;
    }

    public String getRoundGame() {
        return roundGame;
    }

    public void setRoundGame(String roundGame) {
        this.roundGame = roundGame;
    }

    public Integer getIdclub() {
        return idclub;
    }

    public void setIdclub(Integer idclub) {
        this.idclub = idclub;
    }

    public Integer getIdcourse() {
        return idcourse;
    }

    public void setIdcourse(Integer idcourse) {
        this.idcourse = idcourse;
    }

    public Integer getIdplayer() {
        return idplayer;
    }

    public void setIdplayer(Integer idplayer) {
        this.idplayer = idplayer;
    }

    public String getPlayerFirstName() {
        return playerFirstName;
    }

    public void setPlayerFirstName(String playerFirstName) {
        this.playerFirstName = playerFirstName;
    }

    public String getPlayerLastName() {
        return playerLastName;
    }

    public void setPlayerLastName(String playerLastName) {
        this.playerLastName = playerLastName;
    }
@Override
public String toString()
{       try {
    return
            ("from entite." + this.getClass().getSimpleName()
            + NEW_LINE + " ,players : "   + Arrays.deepToString(getPlayers() )
   //         + NEW_LINE + " ,Score Array  : " + Arrays.deepToString(getScoreMP4() )
       //     + NEW_LINE + " ,Score String : " + getScoreString()
            );
        } catch (Exception ex) {
            Logger.getLogger(ScoreMatchplay.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
}
} // end class