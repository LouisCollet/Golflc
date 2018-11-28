package entite;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import javax.inject.Named;
/**
 *
 * @author collet
 */
//  enlevé 04/05/2014  @javax.enterprise.context.SessionScoped   // added 05/10/2013 change quelque chose ???

@Named
public class StablefordResult implements Serializable, interfaces.Log, interfaces.GolfInterface
{
    private static final long serialVersionUID = 1L;

    private Integer idclub;
    private String clubName;
        
    private Integer idcourse;
    private String courseName;
    private short courseHoles;
    private short coursePar;
    private Date courseBeginDate;
    private Date courseEndDate;

    private Integer idround;
  //  private Date roundDate;
    private LocalDateTime roundDate;
    
    
    private String roundGame;
    private Short roundCBA;
    private Short roundHoles;
    private Short roundStart;
    private String roundCompetition;
    private String roundQualifying;

    private Date idhandicap;
    private BigDecimal handicapPlayer;

    private Integer idplayer;
    private String playerGender;

 //   private Integer finalResult;
    
    private Integer idtee;
    private Short teeSlope;
    private BigDecimal teeRating;
    private String teeGender;
    private String teeStart;
    private String inscriptionTeam; 
    private String inscriptionTeeStart;
    private Short teeClubHandicap;

    public StablefordResult()
    {
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

    public Integer getIdround() {
        return idround;
    }

    public void setIdround(Integer idround) {
        this.idround = idround;
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

    public String getRoundQualifying() {
        return roundQualifying;
    }

    public void setRoundQualifying(String roundQualifying) {
        this.roundQualifying = roundQualifying;
    }

    public Date getIdhandicap() {
        return idhandicap;
    }

    public void setIdhandicap(Date idhandicap) {
        this.idhandicap = idhandicap;
    }

    public BigDecimal getHandicapPlayer() {
        return handicapPlayer;
    }

    public void setHandicapPlayer(BigDecimal handicapPlayer) {
        this.handicapPlayer = handicapPlayer;
    }

    public Integer getIdplayer() {
        return idplayer;
    }

    public void setIdplayer(Integer idplayer) {
        this.idplayer = idplayer;
    }

    public String getPlayerGender() {
        return playerGender;
    }

    public void setPlayerGender(String playerGender) {
        this.playerGender = playerGender;
    }

    public Integer getIdtee() {
        return idtee;
    }

    public void setIdtee(Integer idtee) {
        this.idtee = idtee;
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

    public String getTeeGender() {
        return teeGender;
    }

    public void setTeeGender(String teeGender) {
        this.teeGender = teeGender;
    }

    public String getTeeStart() {
        return teeStart;
    }

    public void setTeeStart(String teeStart) {
        this.teeStart = teeStart;
    }

    public String getInscriptionTeam() {
        return inscriptionTeam;
    }

    public void setInscriptionTeam(String inscriptionTeam) {
        this.inscriptionTeam = inscriptionTeam;
    }

    public String getInscriptionTeeStart() {
        return inscriptionTeeStart;
    }

    public void setInscriptionTeeStart(String inscriptionTeeStart) {
        this.inscriptionTeeStart = inscriptionTeeStart;
    }

    public Short getTeeClubHandicap() {
        return teeClubHandicap;
    }

    public void setTeeClubHandicap(Short teeClubHandicap) {
        this.teeClubHandicap = teeClubHandicap;
    }
    
    
@Override
public String toString()
{ return 
        (NEW_LINE + "FRROM ENTITE " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE
               + " = slope : "   + this.getTeeSlope()
               + " , rating : " + this.getTeeRating()
               + " , coursepar : " + this.getCoursePar()
               + " , courseName : " + this.getCourseName()
               + " , playergender : " + this.getPlayerGender()
               + " , rounddate : "  + getRoundDate().format(ZDF_TIME_HHmm)
               + " , teeStart : "  + this.getTeeStart()
        );
}
} // end class
