
package entite;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.New;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped

public class ClubCourseRound implements Serializable, interfaces.Log, interfaces.GolfInterface
{
    private static final long serialVersionUID = 1L;
 @Inject @New private Club club;
 
 //à essayer !!
 // @Inject private Club club;
   private Integer idclub;
    private String clubName;
    private String clubWebsite;
    private BigDecimal clubLatitude;
    private BigDecimal clubLongitude;
    private String clubCity;
    private String clubCountry;
    private String clubAddress;

    private Integer idcourse;
    private String courseName;
    private short courseHoles;
    private short coursePar;
    private Date courseBeginDate;
    private Date courseEndDate;

    private Integer idround;
    private LocalDateTime roundDate;
    private String roundGame;
    private Short roundCBA;
    private Short roundHoles;
    private Short roundPlayers;
    private Short roundStart;
    private String roundCompetition;
    private String roundQualifying;
    private String roundTeam;

 //   private Date idhandicap;
    private Date handicapStart;
    private BigDecimal handicapPlayer;
    
    
    private Short playerhasroundFinalResult;
    private Short playerhasroundZwanzeursResult;
    private Short playerhasroundZwanzeursGreenshirt;
    private Short playerhasroundMatchPlayResult;
    
    private Short scoreHole;
    private Short scoreStroke;

    private Integer idplayer;
    private String playerFirstName;
    private String playerLastName;
    private String playerCity;
    private String playerPhotoLocation;
    private String playerLanguage;
    private String playerEmail;
    
    private Integer idtee;
    private Short teeSlope;
    private BigDecimal teeRating;
    private String teeGender;
    private String teeStart;
    
    private int totalExtraStrokes;
    private Short teeClubHandicap;
    
    public ClubCourseRound()
    {
            // empty constructor
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public int getTotalExtraStrokes() {
        return totalExtraStrokes;
    }

    public void setTotalExtraStrokes(int totalExtraStrokes) {
        this.totalExtraStrokes = totalExtraStrokes;
    }

    public Integer getIdclub() {
        return idclub;
    }

    public void setIdclub(Integer idclub) {
        this.idclub = idclub;
    }



    public Short getPlayerhasroundFinalResult() {
        return playerhasroundFinalResult;
    }

    public void setPlayerhasroundFinalResult(Short playerhasroundFinalResult) {
        this.playerhasroundFinalResult = playerhasroundFinalResult;
    }

    public Date getHandicapStart() {
        return handicapStart;
    }

    public void setHandicapStart(Date handicapStart) {
        this.handicapStart = handicapStart;
    }

    
    
    /*
    public Date getIdhandicap() {
        return idhandicap;
    }

    public void setIdhandicap(Date idhandicap) {
        this.idhandicap = idhandicap;
    }
*/


    public BigDecimal getHandicapPlayer() {
        return handicapPlayer;
    }

    public void setHandicapPlayer(BigDecimal handicapPlayer) {
        this.handicapPlayer = handicapPlayer;
    }

    public Integer getIdcourse() {
        return idcourse;
    }

    public void setIdcourse(Integer idcourse)
    {   //LOG.info("setIdcourse for = " + idcourse);
        this.idcourse = idcourse;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName)
    {
        this.courseName = courseName;
    }

    public short getCourseHoles() {
        return courseHoles;
    }

    public void setCourseHoles(short courseHoles) {
        this.courseHoles = courseHoles;
    }

    public short getCoursePar() {
        return coursePar;
    }

    public void setCoursePar(short coursePar) {
        this.coursePar = coursePar;
    }

//    public Integer getIdclub() {
//        return idclub;
//    }

//    public void setIdclub(Integer idclub)
//    {  //LOG.info("setIdclub for = " + idclub);
//        this.idclub = idclub;
//    }

    public String getClubName()
    {
        return clubName;
    }

    public void setClubName(String clubName)
    {
        this.clubName = clubName;
    }

    public String getClubCity() {
        return clubCity;
    }

    public void setClubCity(String clubCity) {
        this.clubCity = clubCity;
    }

    public String getClubCountry() {
        return clubCountry;
    }

    public void setClubCountry(String clubCountry) {
        this.clubCountry = clubCountry;
    }

    public Integer getIdround()
    {
        return idround;
    }

    public void setIdround(Integer idround)
    {
        this.idround = idround;
    }

    
    
    public LocalDateTime getRoundDate() {
 //       LOG.info(" from getRoundDate 1");
 //       LOG.info(" from getRoundDate 2 - roundDate = " + roundDate.format(ZDF_TIME));
        return roundDate;
    }

    public void setRoundDate(LocalDateTime roundDate) {
 //       LOG.info(" from setRoundDate");
        this.roundDate = roundDate;
    }
    
    public String getRoundGame() {
        return roundGame;
    }

    public void setRoundGame(String roundGame) {
        this.roundGame = roundGame;
    }

    public Short getRoundCBA() {
        return roundCBA;
    }

    public void setRoundCBA(Short roundCBA) {
        this.roundCBA = roundCBA;
    }

    public Short getRoundHoles() {
        return roundHoles;
    }

    public void setRoundHoles(Short roundHoles) {
        this.roundHoles = roundHoles;
    }

    public Short getRoundPlayers() {
        return roundPlayers;
    }

    public void setRoundPlayers(Short roundPlayers) {
        this.roundPlayers = roundPlayers;
    }

    public Short getRoundStart() {
        return roundStart;
    }

    public void setRoundStart(Short roundStart) {
        this.roundStart = roundStart;
    }

    public String getRoundCompetition() {
        return roundCompetition;
    }
    public void setRoundCompetition(String roundCompetition) {
        this.roundCompetition = roundCompetition;
    }

    public String getRoundTeam() {
        return roundTeam;
    }

    public void setRoundTeam(String roundTeam) {
        this.roundTeam = roundTeam;
    }



    public String getRoundQualifying() {
        return roundQualifying;
    }

    public void setRoundQualifying(String roundQualifying) {
        this.roundQualifying = roundQualifying;
    }

    public String getClubWebsite()
    {
        return clubWebsite;
    }

    public void setClubWebsite(String clubWebsite) {
        this.clubWebsite = clubWebsite;
    }

    public BigDecimal getClubLatitude() {
        return clubLatitude;
    }

    public void setClubLatitude(BigDecimal clubLatitude) {
        this.clubLatitude = clubLatitude;
    }

    public BigDecimal getClubLongitude() {
        return clubLongitude;
    }

    public void setClubLongitude(BigDecimal clubLongitude) {
        this.clubLongitude = clubLongitude;
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

    public String getPlayerCity() {
        return playerCity;
    }

    public void setPlayerCity(String playerCity) {
        this.playerCity = playerCity;
    }

    public String getTeeStart() {
        return teeStart;
    }

//    public Integer getFinalResult() {
//        return finalResult;
//    }
//    public void setFinalResult(Integer finalResult) {
//        this.finalResult = finalResult;
//    }
    public void setTeeStart(String teeStart) {
        this.teeStart = teeStart;
    }

    public Short getTeeSlope() {
        return teeSlope;
    }

    public void setTeeSlope(Short teeSlope) {
        this.teeSlope = teeSlope;
    }

    public BigDecimal getTeeRating() {
        return teeRating;
    }

    public void setTeeRating(BigDecimal teeRating) {
        this.teeRating = teeRating;
    }

    public Date getCourseBeginDate() {
        return courseBeginDate;
    }

    public void setCourseBeginDate(Date courseBeginDate) {
        this.courseBeginDate = courseBeginDate;
    }

    public Date getCourseEndDate() {
        return courseEndDate;
    }

    public void setCourseEndDate(Date courseEndDate) {
        this.courseEndDate = courseEndDate;
    }

    public String getTeeGender() {
        return teeGender;
    }

    public void setTeeGender(String teeGender) {
        this.teeGender = teeGender;
    }

    public Integer getIdtee() {
        return idtee;
    }

    public void setIdtee(Integer idtee) {
        this.idtee = idtee;
    }

    public String getClubAddress() {
        return clubAddress;
    }

    public void setClubAddress(String clubAddress) {
        this.clubAddress = clubAddress;
    }

    public Short getPlayerhasroundZwanzeursResult() {
        return playerhasroundZwanzeursResult;
    }

    public void setPlayerhasroundZwanzeursResult(Short playerhasroundZwanzeursResult) {
        this.playerhasroundZwanzeursResult = playerhasroundZwanzeursResult;
    }

    public Short getPlayerhasroundZwanzeursGreenshirt() {
        return playerhasroundZwanzeursGreenshirt;
    }

    public void setPlayerhasroundZwanzeursGreenshirt(Short playerhasroundZwanzeursGreenshirt) {
        this.playerhasroundZwanzeursGreenshirt = playerhasroundZwanzeursGreenshirt;
    }

    public Short getPlayerhasroundMatchPlayResult() {
        return playerhasroundMatchPlayResult;
    }

    public void setPlayerhasroundMatchPlayResult(Short playerhasroundMatchPlayResult) {
        this.playerhasroundMatchPlayResult = playerhasroundMatchPlayResult;
    }


    public String getPlayerPhotoLocation() {
        return playerPhotoLocation;
    }

    public void setPlayerPhotoLocation(String playerPhotoLocation) {
        this.playerPhotoLocation = playerPhotoLocation;
    }

    public String getPlayerLanguage() {
        return playerLanguage;
    }

    public void setPlayerLanguage(String playerLanguage) {
        this.playerLanguage = playerLanguage;
    }

    public String getPlayerEmail() {
        return playerEmail;
    }

    public void setPlayerEmail(String playerEmail) {
        this.playerEmail = playerEmail;
    }

    public Short getTeeClubHandicap() {
        return teeClubHandicap;
    }

    public void setTeeClubHandicap(Short teeClubHandicap) {
        this.teeClubHandicap = teeClubHandicap;
    }
/*
public void logClubCourseRound()
{
    LOG.info("ClubCourseRound : idround     = " + idround);
    LOG.info("ClubCourseRound : rounddate   = " + roundDate);
    LOG.info("ClubCourseRound : clubname    = " + clubName);
    LOG.info("ClubCourseRound : coursename  = " + courseName);
    LOG.info("ClubCourseRound : scorehole   = " + scoreHole);
    LOG.info("ClubCourseRound : scorestroke = " + scoreStroke);
}
*/
@Override
public String toString()
{ return 
        ("from ClubCourseRound : "
               + "  idclub : "   + this.getIdclub()
               + " , clubName : " + this.getClubName()
               + " , idcourse : " + this.getIdcourse()
               + " , courseName : " + this.getCourseName()
               + " , roundQualifying : " + this.getRoundQualifying()
               + " , idplayer : " + this.getIdplayer()
               + " , player First Name : " + this.getPlayerFirstName()
               + " , idround : "  + this.getIdround()
               + " , Round Date HHmm : "   + this.getRoundDate().format(ZDF_TIME_HHmm)
               + " , Round players : " + this.getRoundPlayers()
       //        + " , IdHandicap : " + this.getIdhandicap()
         + " , IdHandicap : " + this.getHandicapStart()

        );

}

//    public Object setClub() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

} // end class
