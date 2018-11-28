package entite;

//import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import javax.inject.Named;
import utils.LCUtil;

@Named
public class ScoreCard implements Serializable, interfaces.Log, interfaces.GolfInterface
{
    private static final long serialVersionUID = 1L;
    //private static final Logger log = Logger.getLogger("golflc");

    private Integer idclub;
    private String clubName;
    private String clubCity;

    private Integer idcourse;
    private String courseName;
    private short courseHoles;
    private short coursePar;

   private Integer idplayer;
   private String playerFirstName;
   private String playerLastName;
   private String playerGender;
    private Date  playerBirthDate;

   private Date handicapStart;
   private BigDecimal handicapPlayer;

   private Integer idround;
   private LocalDateTime roundDate;
   private String roundCompetition;
   private Short roundCBA;
   private Short roundHoles;
   private Short roundStart;
   private String roundGame;
   private String roundQualifying;

   private Short playerhasroundFinalResult;
   private Short playerhasroundZwanzeursResult;
   private Short playerhasroundZwanzeursGreenshirt;
 private String inscriptionTeeStart;
 
    private Integer idtee;
    private String teeStart;
    private Short teeSlope;
    private BigDecimal teeRating;

    private Short scoreHole;
    private Short scorePar;
    private Short scoreStroke;
    private Short scoreNet;
    private Short scoreExtraStroke;
    private Short scorePoints;
    private Short scoreFairway;
    private Short scoreGreen;
    private Short scorePutts;
    private Short scoreBunker;
    private Short scorePenalty;

    private Short holeDistance;
    private Short holeStrokeIndex;
   

    public ScoreCard() // constructor
    {
       
//        totalDistance=0;
//        totalPoints=0;
//        totalPar=0;
//        totalExtraStroke=0;
//        totalStroke=0;
    }

    public Integer getIdround() {
        return idround;
    }

    public void setIdround(Integer idround) {
        this.idround = idround;
    }

    public Integer getIdcourse() {
        return idcourse;
    }

    public void setIdcourse(Integer idcourse)
    {   LOG.info("setIdcourse for = " + idcourse);
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

    public String getRoundQualifying() {
        return roundQualifying;
    }

    public void setRoundQualifying(String roundQualifying) {
        this.roundQualifying = roundQualifying;
    }

    public short getCoursePar() {
        return coursePar;
    }

    public void setCoursePar(short coursePar) {
        this.coursePar = coursePar;
    }

    public Integer getIdclub() {
        return idclub;
    }

    public void setIdclub(Integer idclub)
    {LOG.info("setIdclub for = " + idclub);
        this.idclub = idclub;
    }

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

    public String getPlayerGender() {
        return playerGender;
    }

    public void setPlayerGender(String playerGender) {
        this.playerGender = playerGender;
    }

    public Date getHandicapStart() {
        return handicapStart;
    }

    public void setHandicapStart(Date handicapStart) {
        this.handicapStart = handicapStart;
    }

    public BigDecimal getHandicapPlayer() {
        return handicapPlayer;
    }

    public void setHandicapPlayer(BigDecimal handicapPlayer) {
        this.handicapPlayer = handicapPlayer;
    }

    public LocalDateTime getRoundDate() {
        return roundDate;
    }

    public void setRoundDate(LocalDateTime roundDate) {
        this.roundDate = roundDate;
    }

    public String getRoundCompetition() {
        return roundCompetition;
    }

    public void setRoundCompetition(String roundCompetition) {
        this.roundCompetition = roundCompetition;
    }

    public Short getRoundCBA() {
        return roundCBA;
    }

    public void setRoundCBA(Short roundCBA) {
        this.roundCBA = roundCBA;
    }

    public String getRoundGame() {
        return roundGame;
    }

    public void setRoundGame(String roundGame) {
        this.roundGame = roundGame;
    }

    public Short getPlayerhasroundFinalResult() {
        return playerhasroundFinalResult;
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

    public void setPlayerhasroundFinalResult(Short playerhasroundFinalResult) {
        this.playerhasroundFinalResult = playerhasroundFinalResult;
    }

    public Integer getIdtee() {
        return idtee;
    }

    public void setIdtee(Integer idtee) {
        this.idtee = idtee;
    }

    public String getTeeStart() {
        return teeStart;
    }

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

    public Short getScoreHole() {
        return scoreHole;
    }

    public Short getScorePar() {
        return scorePar;
    }

    public void setScorePar(Short scorePar) {
        this.scorePar = scorePar;
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

    public Short getScoreNet() {
        return scoreNet;
    }

    public void setScoreNet(Short scoreNet) {
        this.scoreNet = scoreNet;
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

    public void setScorePoints(Short scorePoints)
    {
        this.scorePoints = scorePoints;
    }

    public Short getHoleDistance() {
        return holeDistance;
    }

    public void setHoleDistance(Short holeDistance)
    {   //log.debug("hole distance setted = " + holeDistance);
        this.holeDistance = holeDistance;
    }

    public Short getHoleStrokeIndex() {
        return holeStrokeIndex;
    }

    public void setHoleStrokeIndex(Short holeStrokeIndex) {
        this.holeStrokeIndex = holeStrokeIndex;
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

    public Short getPlayerhasroundZwanzeursResult() {
        return playerhasroundZwanzeursResult;
    }

    public void setPlayerhasroundZwanzeursResult(Short playerhasroundZwanzeursResult) {
        this.playerhasroundZwanzeursResult = playerhasroundZwanzeursResult;
    }

    public Short getPlayerhasroundZwanzeursGreenshirt() {
        return playerhasroundZwanzeursGreenshirt;
    }

    public Date getPlayerBirthDate() {
        return playerBirthDate;
    }

    public void setPlayerBirthDate(Date playerBirthDate) {
        this.playerBirthDate = playerBirthDate;
    }

    public void setPlayerhasroundZwanzeursGreenshirt(Short playerhasroundZwanzeursGreenshirt) {
        this.playerhasroundZwanzeursGreenshirt = playerhasroundZwanzeursGreenshirt;
    }

    public String getInscriptionTeeStart() {
        return inscriptionTeeStart;
    }

    public void setInscriptionTeeStart(String inscriptionTeeStart) {
        this.inscriptionTeeStart = inscriptionTeeStart;
    }

    

@Override
public String toString()
{ return 
        ("from entite " + this.getClass().getSimpleName()
               + " = idclub : "   + this.getIdclub()
               + " ,roundgame : " + this.getRoundGame()
               + " ,clubName : " + this.getClubName()
               + " ,idcourse : " + this.getIdcourse()
               + " ,idround : "  + this.getIdround()
               + " ,roundQualifying : "  + this.getRoundQualifying()
               + " ,idplayer : " + this.getIdplayer()
               + " ,points   : " + this.getScorePoints()
               + " ,handicap start  : " + this.getHandicapStart()
               + " ,handicap player  : " + this.getHandicapPlayer()
        );

} // end method

  public static EScoreCardList mapScoreCard(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        EScoreCardList s = new EScoreCardList();
   //         s.setHoleStrokeIndex(rs.getShort("HoleStrokeIndex") );
   //         s.setHoleDistance(rs.getShort("HoleDistance") );
            s.setScoreHole(rs.getShort("ScoreHole") );
            s.setScoreStroke(rs.getShort("ScoreStroke") );
            s.setScoreExtraStroke(rs.getShort("ScoreExtraStroke") );
            s.setScorePoints(rs.getShort("ScorePoints") );
            s.setScorePar(rs.getShort("ScorePar") );
            s.setHoleStrokeIndex(rs.getShort("ScoreStrokeIndex"));
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


//Whenever you print any instance of your class, the default toString implementation
//of Object class is called, which returns the representation that you are getting.
//It contains two parts: - Type and Hashcode
//http://stackoverflow.com/questions/13001427/printing-out-all-the-objects-in-array-list
//default : Object's toString returns getClass().getName() + '@' + Integer.toHexString(hashCode()). 
//So, in student.Student@82701e that you get as output -> 
//•student.Student is the Type, and
//•82701e is the HashCode
//So, you need to override a toString method in your Student class to get required 
} // end class