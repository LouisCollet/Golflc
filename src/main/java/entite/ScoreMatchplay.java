
package entite;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
@Named
public class ScoreMatchplay implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;

private Integer[] players;

private String[][] scoreMP4; 
private String matchplayResult;
private String scoreString;
private String roundCompetition;
private String clubName;
private String courseName;
private Date roundDate;
private String roundGame;
private Integer idround;
private Integer idplayer;
private String playerFirstName;
private String playerLastName;

public ScoreMatchplay() // constructor
{
        players = new Integer[4];
        scoreMP4 = new String[6][18]; // , 4 resultats joueurs, 2 Match Progress sur 18 trous
      for(String[] subarray : scoreMP4)
        {
            Arrays.fill(subarray, " ");
        }
} //end constructor

    public Integer[] getPlayers() {
        return players;
    }

    public void setPlayers(Integer[] players) {
        this.players = players;
    }

    public String[][] getScoreMP4() {
  //       LOG.info("getMP4 - MP4 = " + Arrays.deepToString(scoreMP4) );
        return scoreMP4;
    }

    public void setScoreMP4(String[][] scoreMP4) {
         LOG.info("setMP4 with : " + Arrays.deepToString(scoreMP4));
        this.scoreMP4 = scoreMP4;
    }


    public String getScoreString() throws IOException {
 //       LOG.info("from getScoreString");
      return scoreString;
///         return utils.LCUtil.uncompress(scoreString); // mod 30/01/2015 le compresse se fera au niveau MySql
    }

    public void setScoreString(String scoreString) throws IOException {
///        if(scoreString != null)
 ///       {
            this.scoreString = scoreString;
 ///           this.scoreString = utils.LCUtil.compress(scoreString);
 ///           System.out.println("setScoreString = " + this.scoreString);
            LOG.info("setScoreString = " + this.scoreString);
///        }else{
 ///           LOG.info("setScoreString = null, skipped !");     }
    }

    public String getMatchplayResult() {
        return matchplayResult;
    }

    public void setMatchplayResult(String matchplayResult) {
        this.matchplayResult = matchplayResult;
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

    public Date getRoundDate() {
        return roundDate;
    }

    public void setRoundDate(Date roundDate) {
        this.roundDate = roundDate;
    }

    public String getRoundGame() {
        return roundGame;
    }

    public void setRoundGame(String roundGame) {
        this.roundGame = roundGame;
    }

    public Integer getIdround() {
        return idround;
    }

    public void setIdround(Integer idround) {
        this.idround = idround;
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
            + NEW_LINE + " ,Score Array  : " + Arrays.deepToString(getScoreMP4() )
            + NEW_LINE + " ,Score String : " + getScoreString()
            );
        } catch (IOException ex) {
            Logger.getLogger(ScoreMatchplay.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
}
} // end class
