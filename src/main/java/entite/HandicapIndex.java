package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import utils.LCUtil;

//@Named //enlevé 14-02-2026
//@RequestScoped  
public class HandicapIndex implements Serializable {
    
    private int handicapId;
    private int handicapPlayerId;
    private int handicapRoundId;
    private BigDecimal handicapScoreDifferential;
    private double handicapExpectedSD9Holes; // new 15-4-2025
    private short handicapHolesNotPlayed; // new 15-4-2025
@NotNull(message="{handicap.start.notnull}")
@Past(message="{handicap.start.past}")
    private LocalDateTime handicapDate;

@NotNull(message="{handicap.player.notnull}")
@Min(value=0,message="{handicap.player.min}")
@Max(value=54,message="{handicap.player.max}")
    private BigDecimal handicapWHS;
    private Double lowHandicapIndex;
    private Short handicapExceptionalScoreReduction;
    private String handicapSoftHardCap;  // col 8
    private String handicapComment; // col 9
    private Short handicapPlayedStrokes;
 //   private Short handicapTotalStrokes;
// working areas
  ///  private ECourseList2 selectedHandicap; // enlevé 23-02-2026 moved to PlayerController
    private List <?>filteredHandicaps;
    private String calculations;
    
    public int getHandicapId() {
        return handicapId;
    }

    public void setHandicapId(int handicapId) {
        this.handicapId = handicapId;
    }

    public int getHandicapPlayerId() {
        return handicapPlayerId;
    }

    public void setHandicapPlayerId(int handicapPlayerId) {
        this.handicapPlayerId = handicapPlayerId;
    }

    public int getHandicapRoundId() {
        return handicapRoundId;
    }

    public void setHandicapRoundId(int handicapRoundId) {
        this.handicapRoundId = handicapRoundId;
    }

    public BigDecimal getHandicapScoreDifferential() {
        return handicapScoreDifferential;
    }

    public void setHandicapScoreDifferential(BigDecimal handicapScoreDifferential) {
        this.handicapScoreDifferential = handicapScoreDifferential;
    }

    public LocalDateTime getHandicapDate() {
        return handicapDate;
    }

    public void setHandicapDate(LocalDateTime handicapDate) {
        this.handicapDate = handicapDate;
    }


    public BigDecimal getHandicapWHS() {
        return handicapWHS;
    }

    public void setHandicapWHS(BigDecimal handicapWHS) {
        this.handicapWHS = handicapWHS;
    }

    public Short getHandicapExceptionalScoreReduction() {
        return handicapExceptionalScoreReduction;
    }

    public void setHandicapExceptionalScoreReduction(Short handicapExceptionalScoreReduction) {
        this.handicapExceptionalScoreReduction = handicapExceptionalScoreReduction;
    }

    public String getHandicapSoftHardCap() {
        return handicapSoftHardCap;
    }

    public void setHandicapSoftHardCap(String handicapSoftHardCap) {
        this.handicapSoftHardCap = handicapSoftHardCap;
    }

    public String getHandicapComment() {
 //       LOG.debug("get handicap comment = " + handicapComment);
        return handicapComment;
    }

    public void setHandicapComment(String handicapComment) {
        this.handicapComment = handicapComment;
 //       LOG.debug("set handicap comment = " + this.handicapComment);
    }

 //   public ECourseList2 getSelectedHandicap() {
 //       return selectedHandicap;
  //  }

  //  public void setSelectedHandicap(ECourseList2 selectedHandicap) {
  //      this.selectedHandicap = selectedHandicap;
  //  }

    public List<?> getFilteredHandicaps() {
        return filteredHandicaps;
    }

    public void setFilteredHandicaps(List<?> filteredHandicaps) {
        this.filteredHandicaps = filteredHandicaps;
    }

    public Short getHandicapPlayedStrokes() {
        return handicapPlayedStrokes;
    }

    public void setHandicapPlayedStrokes(Short handicapPlayedStrokes) {
        this.handicapPlayedStrokes = handicapPlayedStrokes;
    }

    public short getHandicapHolesNotPlayed() {
        return handicapHolesNotPlayed;
    }

    public void setHandicapHolesNotPlayed(short handicapHolesNotPlayed) {
        this.handicapHolesNotPlayed = handicapHolesNotPlayed;
    }



    public String getCalculations() {
        return calculations;
    }

    public void setCalculations(String calculations) {
        this.calculations = calculations;
    }

    public Double getLowHandicapIndex() {
        return lowHandicapIndex;
    }

    public void setLowHandicapIndex(Double lowHandicapIndex) {
        this.lowHandicapIndex = lowHandicapIndex;
    }

    public double getHandicapExpectedSD9Holes() {
        return handicapExpectedSD9Holes;
    }

    public void setHandicapExpectedSD9Holes(double handicapExpectedSD9Holes) {
        this.handicapExpectedSD9Holes = handicapExpectedSD9Holes;
    }
/*
public static HandicapIndex map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
        HandicapIndex h = new HandicapIndex();
        h.setHandicapId(rs.getInt("HandicapId") );
        h.setHandicapPlayerId(rs.getInt("HandicapPlayerId"));
        h.setHandicapRoundId(rs.getInt("HandicapRoundId"));
        h.setHandicapScoreDifferential(rs.getBigDecimal("HandicapScoreDifferential") );
        h.setHandicapExpectedSD9Holes(rs.getDouble("HandicapExpectedSD9Holes") );  // new 15-04-2025
        h.setHandicapHolesNotPlayed(rs.getShort("HandicapHolesNotPlayed") );  // new 15-04-2025
        h.setHandicapDate(rs.getTimestamp("HandicapDate").toLocalDateTime());
        h.setHandicapWHS(rs.getBigDecimal("HandicapWHS"));
        h.setHandicapExceptionalScoreReduction(rs.getShort("HandicapExceptionalScoreReduction"));
        h.setHandicapSoftHardCap(rs.getString("HandicapSoftHardCap"));
        h.setHandicapComment(rs.getString("HandicapComment"));
        h.setHandicapPlayedStrokes(rs.getShort("HandicapPlayedStrokes"));
        h.setLowHandicapIndex(rs.getDouble("HandicapPreviousLowHandicap"));
        
//) );
   return h;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method    
*/
@Override
    public String toString(){
 //   final String methodName = utils.LCUtil.getCurrentMethodName(); 
 try{ 
//     LOG.debug("to String - HandicapId : "   + this.getHandicapId());
 return 
        ( NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE 
               + " HandicapId : "   + this.getHandicapId()
               + " ,player id : " + this.getHandicapPlayerId()
               + " ,round id : " + this.getHandicapRoundId()
               + " ,Handicap Score Differential : " + this.getHandicapScoreDifferential()
               + " ,Handicap ExpectedSD9Holes : " + this.getHandicapExpectedSD9Holes()
               + " ,Handicap Date : " + this.getHandicapDate()
               + " ,HandicapWHS : " + this.getHandicapWHS()
           + NEW_LINE + TAB
               + " ,Handicap Exceptional Reduction = " + this.getHandicapExceptionalScoreReduction()
               + " ,Handicap soft or hard cap : " + this.getHandicapSoftHardCap()
               + " ,Handicap Comment : " + this.getHandicapComment()
               + " ,HandicapPlayedStrokes : " + this.getHandicapPlayedStrokes()
               + " ,LowHandicapIndex : " + this.lowHandicapIndex
    //       + NEW_LINE + TAB
                );
    }catch(Exception e){
        String msg = "£££ Exception in toString HandicapIndex " + " / " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method
    

 public static class sortByScoreDifferential implements Comparator<HandicapIndex> { // this is a static inner class
    // Used for sorting in ascending order of scoreDifferential
    
    public int compare(HandicapIndex a, HandicapIndex b) { 
     //   return (int) (a.scoreDifferential - b.scoreDifferential); 
     return Double.compare(a.getHandicapScoreDifferential().doubleValue(), b.getHandicapScoreDifferential().doubleValue());
    } 
} // end inner class 
} // end class