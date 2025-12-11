
package entite;

import java.io.Serializable;
import java.util.Date;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Matchplay implements Serializable, interfaces.Log{
    private static final long serialVersionUID = 1L;

    private Integer idclub;
    private String clubName;
    
    private Integer idcourse;
    private String courseName;

    private Integer idround;
    private Date roundDate;
    private String roundGame;
    private String RoundName;
    private String RoundNameName;
    private String RoundNameDay;
    private String RoundNameMatch;

    private Integer idplayer;
    private String playerFirstName;
    private String playerLastName;

    private String playerhasroundTeam;
    private String playerhasroundPlayerNumber;
    
 //   private Integer finalResult;

    
    public Matchplay()
    {
            // empty constructor
    }

    public Integer getIdclub() {
        return idclub;
    }

    public void setIdclub(Integer idclub) {
        this.idclub = idclub;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public Integer getIdcourse() {
        return idcourse;
    }

    public void setIdcourse(Integer idcourse) {
        this.idcourse = idcourse;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getIdround() {
        return idround;
    }

    public void setIdround(Integer idround) {
        this.idround = idround;
    }

    public Date getRoundDate() {
        return roundDate;
    }

    public void setRoundDate(Date roundDate) {
        this.roundDate = roundDate;
    }

    public String getRoundName() {
        return RoundName;
    }

    public void setRoundName(String RoundName) {
        this.RoundName = RoundName;
    }

    public String getRoundNameName() {
        return RoundNameName;
    }

    public void setRoundNameName(String RoundNameName) {
        this.RoundNameName = RoundNameName;
    }

    public String getRoundNameDay() {
        return RoundNameDay;
    }

    public void setRoundNameDay(String RoundNameDay) {
        this.RoundNameDay = RoundNameDay;
    }

    public String getRoundNameMatch() {
        return RoundNameMatch;
    }

    public void setRoundNameMatch(String RoundNameMatch) {
        this.RoundNameMatch = RoundNameMatch;
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

    public String getPlayerhasroundTeam() {
        return playerhasroundTeam;
    }

    public void setPlayerhasroundTeam(String playerhasroundTeam) {
        this.playerhasroundTeam = playerhasroundTeam;
    }

    public String getPlayerhasroundPlayerNumber() {
        return playerhasroundPlayerNumber;
    }

    public void setPlayerhasroundPlayerNumber(String playerhasroundPlayerNumber) {
        this.playerhasroundPlayerNumber = playerhasroundPlayerNumber;
    }

    public String getRoundGame() {
        return roundGame;
    }

    public void setRoundGame(String roundGame) {
        this.roundGame = roundGame;
    }

@Override
public String toString()
{ return 
    ("from Matchplay : "
     + NEW_LINE
     + " / idplayer : " + this.getIdplayer()
     + " / player name : " + this.getPlayerLastName()
     + " / player team : " + this.getPlayerhasroundTeam()
     + " / player number : " + this.getPlayerhasroundPlayerNumber()
     + " / idround : "  + this.getIdround()
     + " / game : "  + this.getRoundGame()
     + " / competition : "  + this.getRoundName()
     + " / competition name : "  + this.getRoundNameName()
     + " / competition day : "  + this.getRoundNameDay()
     + " / competition match : "  + this.getRoundNameMatch()
     + "  idclub : "   + this.getIdclub()
     + " ,clubName : " + this.getClubName()
     + " / idcourse : " + this.getIdcourse()
     + " ,courseName : " + this.getCourseName()
     + NEW_LINE
  );

}



} // end class
