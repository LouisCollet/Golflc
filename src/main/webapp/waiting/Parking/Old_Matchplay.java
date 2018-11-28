
package entite;

import java.io.Serializable;
//import java.math.BigDecimal;
import java.util.Date;
import javax.inject.Named;

@Named
public class Old_Matchplay implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;

    private Integer idclub;
    private String clubName;
    
    private Integer idcourse;
    private String courseName;

    private Integer idround;
    private Date roundDate;
    private String roundGame;
    private String roundCompetition;
    private String roundCompetitionName;
    private String roundCompetitionDay;
    private String roundCompetitionMatch;

    private Integer idplayer;
    private String playerFirstName;
    private String playerLastName;

    private String playerhasroundTeam;
    private String playerhasroundPlayerNumber;
    
 //   private Integer finalResult;

    
    public Old_Matchplay()
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

    public String getRoundCompetition() {
        return roundCompetition;
    }

    public void setRoundCompetition(String roundCompetition) {
        this.roundCompetition = roundCompetition;
    }

    public String getRoundCompetitionName() {
        return roundCompetitionName;
    }

    public void setRoundCompetitionName(String roundCompetitionName) {
        this.roundCompetitionName = roundCompetitionName;
    }

    public String getRoundCompetitionDay() {
        return roundCompetitionDay;
    }

    public void setRoundCompetitionDay(String roundCompetitionDay) {
        this.roundCompetitionDay = roundCompetitionDay;
    }

    public String getRoundCompetitionMatch() {
        return roundCompetitionMatch;
    }

    public void setRoundCompetitionMatch(String roundCompetitionMatch) {
        this.roundCompetitionMatch = roundCompetitionMatch;
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

//public void logMatchplay()
//{
 //   LOG.info("ClubCourseRound : idround     = " + idround);
 //   LOG.info("ClubCourseRound : rounddate   = " + roundDate);
 //   LOG.info("ClubCourseRound : clubname    = " + clubName);
 //   LOG.info("ClubCourseRound : coursename  = " + courseName);
//    LOG.info("ClubCourseRound : scorehole   = " + scoreHole);
//    LOG.info("ClubCourseRound : scorestroke = " + scoreStroke);
//}

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
     + " / competition : "  + this.getRoundCompetition()
     + " / competition name : "  + this.getRoundCompetitionName()
     + " / competition day : "  + this.getRoundCompetitionDay()
     + " / competition match : "  + this.getRoundCompetitionMatch()
     + "  idclub : "   + this.getIdclub()
     + " ,clubName : " + this.getClubName()
     + " / idcourse : " + this.getIdcourse()
     + " ,courseName : " + this.getCourseName()
     + NEW_LINE
  );

}



} // end class
