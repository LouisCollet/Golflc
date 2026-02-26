package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
// import jakarta.enterprise.context.RequestScoped;  // migrated 2026-02-24
// import jakarta.inject.Named;  // migrated 2026-02-24
import utils.LCUtil;

// @Named  // migrated 2026-02-24
// @RequestScoped  // migrated 2026-02-24
public class CompetitionData implements Serializable{
    
    private static final long serialVersionUID = 1L;
private Integer cmpDataId;

private Integer cmpDataCompetitionId;
private Integer cmpDataPlayerId;
private Short cmpDataPlayingHandicap;
private Double cmpDataHandicap;
private LocalTime cmpDataFlightStart;
private Short cmpDataFlightNumber;
private Short cmpDataScorePoints;
private String cmpDataLastHoles;
private String cmpDataPlayerFirstLastName;
private String cmpDataAskedStartTime;
private String cmpDataPlayerGender;
private Integer cmpDataRoundId;
private String cmpDataTeeStart;
private Double cmpDataScoreDifferential;
public CompetitionData(){ // constructor

    }

    public Integer getCmpDataId() {
        return cmpDataId;
    }

    public void setCmpDataId(Integer cmpDataId) {
        this.cmpDataId = cmpDataId;
    }

    public Integer getCmpDataCompetitionId() {
        return cmpDataCompetitionId;
    }

    public void setCmpDataCompetitionId(Integer cmpDataCompetitionId) {
        this.cmpDataCompetitionId = cmpDataCompetitionId;
    }

    public Integer getCmpDataPlayerId() {
        return cmpDataPlayerId;
    }

    public void setCmpDataPlayerId(Integer cmpDataPlayerId) {
        this.cmpDataPlayerId = cmpDataPlayerId;
    }

    public Short getCmpDataPlayingHandicap() {
        return cmpDataPlayingHandicap;
    }

    public void setCmpDataPlayingHandicap(Short cmpDataPlayingHandicap) {
        this.cmpDataPlayingHandicap = cmpDataPlayingHandicap;
    }

    public Double getCmpDataHandicap() {
        return cmpDataHandicap;
    }

    public void setCmpDataHandicap(Double cmpDataHandicap) {
        this.cmpDataHandicap = cmpDataHandicap;
    }

    public LocalTime getCmpDataFlightStart() {
        return cmpDataFlightStart;
    }

    public void setCmpDataFlightStart(LocalTime cmpDataFlightStart) {
        this.cmpDataFlightStart = cmpDataFlightStart;
    }

    public Short getCmpDataFlightNumber() {
        return cmpDataFlightNumber;
    }

    public void setCmpDataFlightNumber(Short cmpDataFlightNumber) {
        this.cmpDataFlightNumber = cmpDataFlightNumber;
    }

    public Short getCmpDataScorePoints() {
        return cmpDataScorePoints;
    }

    public void setCmpDataScorePoints(Short cmpDataScorePoints) {
        this.cmpDataScorePoints = cmpDataScorePoints;
    }

    public String getCmpDataLastHoles() {
        return cmpDataLastHoles;
    }

    public void setCmpDataLastHoles(String cmpDataLastHoles) {
        this.cmpDataLastHoles = cmpDataLastHoles;
    }

    public String getCmpDataPlayerFirstLastName() {
        return cmpDataPlayerFirstLastName;
    }

    public void setCmpDataPlayerFirstLastName(String cmpDataPlayerFirstLastName) {
        this.cmpDataPlayerFirstLastName = cmpDataPlayerFirstLastName;
    }

    public String getCmpDataAskedStartTime() {
        return cmpDataAskedStartTime;
    }

    public void setCmpDataAskedStartTime(String cmpDataAskedStartTime) {
        this.cmpDataAskedStartTime = cmpDataAskedStartTime;
    }

    public String getCmpDataPlayerGender() {
        return cmpDataPlayerGender;
    }

    public void setCmpDataPlayerGender(String cmpDataPlayerGender) {
        this.cmpDataPlayerGender = cmpDataPlayerGender;
    }

    public Integer getCmpDataRoundId() {
        return cmpDataRoundId;
    }

    public void setCmpDataRoundId(Integer cmpDataRoundId) {
        this.cmpDataRoundId = cmpDataRoundId;
    }

    public String getCmpDataTeeStart() {
        return cmpDataTeeStart;
    }

    public void setCmpDataTeeStart(String cmpDataTeeStart) {
        this.cmpDataTeeStart = cmpDataTeeStart;
    }

    public Double getCmpDataScoreDifferential() {
        return cmpDataScoreDifferential;
    }

    public void setCmpDataScoreDifferential(Double cmpDataScoreDifferential) {
        this.cmpDataScoreDifferential = cmpDataScoreDifferential;
    }



 @Override
public String toString(){
    final String methodName = utils.LCUtil.getCurrentMethodName();
 try{
   LOG.debug("starting toString " + methodName);
 //    LOG.debug("idclub : "   + this.getIdclub());
 //  if(this.cmpDataCompetitionId == null){
 //      return ("CmpDataCompetitionId is null, no print : "  );
//   }
 return 
        ( NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEW_LINE 
               + " idcompetitionData : "   + this.cmpDataId
               + " ,idcompetition : "   + this.cmpDataCompetitionId
               + " ,player : " + this.cmpDataPlayerId
               + " ,player First Last Name: " + this.cmpDataPlayerFirstLastName
               + " ,playingHandicap : "  + this.cmpDataHandicap
               + " ,handicap : "  + this.cmpDataHandicap
               + " ,score differential : "  + this.cmpDataScoreDifferential
         + NEW_LINE
       //        + " ,flight start time : "  + this.cmpDataFlightStart.format(ZDF_TIME_HHmm)
               + " ,flight start time : "  + this.cmpDataFlightStart //.format(ZDF_TIME_HHmm)
               + " ,flight number : " + this.cmpDataFlightNumber
               + " ,score points : " + this.cmpDataScorePoints
         + NEW_LINE
               + " ,classment last holes : " + this.cmpDataLastHoles
               + " ,asked start time : " + this.cmpDataAskedStartTime
               + " ,player Gender : " + this.cmpDataPlayerGender
               + " ,RoundId : " + this.cmpDataRoundId
               + " ,Teestart: " + this.cmpDataTeeStart
           + NEW_LINE + TAB
              );
    }catch(Exception e){
        String msg = "£££ Exception in " + methodName + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}

public static CompetitionData map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
        CompetitionData c = new CompetitionData();
        c.setCmpDataId(rs.getInt("CmpDataId") );
        c.setCmpDataCompetitionId(rs.getInt("CmpDataCompetitionId"));
        c.setCmpDataPlayerId(rs.getInt("CmpDataPlayerId") );
        c.setCmpDataPlayingHandicap(rs.getShort("CmpDataPlayingHandicap"));
        c.setCmpDataHandicap(rs.getDouble("CmpDataHandicap"));
        c.setCmpDataFlightStart(rs.getTime("CmpDataFlightStart").toLocalTime());
        c.setCmpDataFlightNumber(rs.getShort("CmpDataFlightNumber"));
        c.setCmpDataScorePoints(rs.getShort("CmpDataScorePoints"));
        c.setCmpDataLastHoles(rs.getString("CmpDataLastHoles"));
        c.setCmpDataPlayerFirstLastName(rs.getString("CmpDataPlayerFirstLastName"));
        c.setCmpDataAskedStartTime(rs.getString("CmpDataAskedStartTime"));
        c.setCmpDataPlayerGender(rs.getString("CmpDataPlayerGender"));
        c.setCmpDataRoundId(rs.getInt("CmpDataRoundId"));
        c.setCmpDataTeeStart(rs.getString("CmpDataTeeStart"));
        c.setCmpDataScoreDifferential(rs.getDouble("CmpDataScoreDifferential"));
   return c;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName+ " / "+ e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class