
package entite;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import org.primefaces.event.CellEditEvent;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

@Named //enlevé 10/07/2022
@ViewScoped //@RequestScoped mod 09-04-2023 change rien ! totaux pas corrects
public class ScoreStableford implements Serializable{
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private Integer idscore;
 // pour download db pour liste scorecard   
    private Short scoreHole;
    private Short scoreStroke;
    private Short scoreExtraStroke;
    private Short scorePoints;
    private Short scorePar;
    private Short scoreStrokeIndex;
 // pour statistics   
    private Short scoreFairway;
    private Short scoreGreen;
    private Short scorePutts;
    private Short scoreBunker;
    private Short scorePenalty;
    private ArrayList<Statistics> statisticsList = new ArrayList<>(); // used in test include_statistics avec cell update table
    private ArrayList<Score> scoreList = new ArrayList<>(); // used in test include_score_stableford avec cell update table
    private boolean ScoreCardOK;
    private int[] holeArray; // (0)
    private int[] parArray; // (1)
    private int[] indexArray; // (2)
    private int[] strokeArray; //(3)
    private int[] extraArray; // (4)
    private int[] distanceArray; // new 11-08-2023
    private int[] pointsArray; // (5)
    private int playingHandicap;
    private double playerHandicapEGA;
    private double playerHandicapWHS;
    private boolean showCreate = false;  // show button dans score_stableford.xhtml
    private boolean showCalculate = true;  // show button dans score_stableford.xhtml
    private boolean showCreateHandicapIndex = false;  // show button dans score_stableford.xhtml
    private boolean showLineDifferential = false;
    private boolean showButtonStatistics = false;
    private int stablefordResult;
// 14-06-2020 new fields for WHS
    private int courseHandicap;
    private int adjustedGrossScore;
    private String handicapType; // EGA or WHS
    private int totalStrokes;
    private Double scoreDifferential;
// 15-04-2025 new fields for WHS revision 10/2024
    private double expectedSD9Holes;
    private int holesNotPlayed;

    private Short scoreParCourse; // 72, 71, 36 ...
    private Double courseRating;
    private Short slopeRating;
    private int start = 0;
    private int holes = 0;
    
public ScoreStableford(){ // constructor
  //     LOG.debug(" entering constructor scorestableford");
       strokeArray = new int[18]; // new 16-11-2020
       holeArray = new int[18]; // new 26-06-2022  ou List ??
       parArray = new int[18]; // new 13-10-2021
       indexArray = new int[18]; // new 13-10-2021
       extraArray = new int[18]; // new 13-10-2021
       distanceArray = new int[18]; // new 11-08-2023
       pointsArray = new int[18]; // new 13-10-2021
       scoreDifferential = 0.0;
       expectedSD9Holes = 0.0;
  //     showCreate = false;
       //       statistics = new int[18][5]; // 18 trous, 5 statistics fairway, green, putts, bunker, pénalité);
//        for(int[] subarray : statistics){
 //           Arrays.fill(subarray, 0);
//        }

 //           LOG.debug(" array statistics initialized" + Arrays.deepToString(statistics) );
    }
@PostConstruct
public void init(){
      LOG.debug(" from init in ScoreStableford");
    strokeArray = new int[18]; // new 16-11-2020
}

public class Statistics{
  private Integer hole;
  private Integer par;
  private Integer stroke;  
  private Integer fairway;
  private Integer green;
  private Integer putt;
  private Integer bunker;
  private Integer penalty;

    public Integer getHole() {
        return hole;
    }

    public void setHole(Integer hole) {
        this.hole = hole;
    }

    public Integer getPar() {
        return par;
    }

    public void setPar(Integer par) {
        this.par = par;
    }

    public Integer getStroke() {
        return stroke;
    }

    public void setStroke(Integer stroke) {
        this.stroke = stroke;
    }

    public Integer getFairway() {
        return fairway;
    }

    public void setFairway(Integer fairway) {
        this.fairway = fairway;
    }

    public Integer getGreen() {
        return green;
    }

    public void setGreen(Integer green) {
        this.green = green;
    }

    public Integer getPutt() {
        return putt;
    }

    public void setPutt(Integer putt) {
        this.putt = putt;
    }

    public Integer getBunker() {
        return bunker;
    }

    public void setBunker(Integer bunker) {
        this.bunker = bunker;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public void setPenalty(Integer penalty) {
        this.penalty = penalty;
    }


 
  public String toString(){
 try {
//      LOG.debug("starting toString TarifGreenfee !");
    return
          ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + " hole='" + hole
            + ", par=" + par
            + ", stroke=" + stroke
            + ", fairway='" + fairway
            + ", green=" + green
            + ", putts=" + putt
            + ", bunker=" + bunker
            + ", penalty=" + penalty
          );
 }catch(Exception e){
    String msg = "£££ Exception in Statistics.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
        }
} //end method
} // end inner class Statistics

static public class ExtraClass{
//public class ExtraClass {
        private int hole;
        private int index;
        private int extra;

        public ExtraClass(int hole, int index) {
            this.hole = hole;
            this.index = index;
        }

        public int getHole() {
            return hole;
        }

        public void setHole(int hole) {
            this.hole = hole;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getExtra() {
            return extra;
        }

        public void setExtra(int extra) {
            this.extra = extra;
        }

        @Override
        public String toString() {
            return  NEW_LINE + "hole = " + hole +
                    TAB + "index = " + index +
                    TAB + "extra = " + extra;

        }
    }
// end inner class ExtraClass

public class Score{
  private Integer hole;
  private Integer par;
  private Integer index;
  private Integer extra;
  private int points;
  private int strokes;
  private int distances;

    public Integer getHole() {
        return hole;
    }

    public void setHole(Integer hole) {
        this.hole = hole;
    }

    public Integer getPar() {
        return par;
    }

    public void setPar(Integer par) {
        this.par = par;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getExtra() {
        return extra;
    }

    public void setExtra(Integer extra) {
        this.extra = extra;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getStrokes() {
        return strokes;
    }

    public void setStrokes(int strokes) {
        this.strokes = strokes;
    }

        public int getDistances() {
            return distances;
        }

        public void setDistances(int distances) {
            this.distances = distances;
        }
        
  public static ScoreStableford.Score map(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
         ScoreStableford.Score score = new ScoreStableford().new Score();
         score.setHole(rs.getInt("ScoreHole"));
         score.setPar(rs.getInt("ScorePar"));
         score.setIndex(rs.getInt("ScoreStrokeIndex"));
         score.setExtra(rs.getInt("ScoreExtraStroke"));
         score.setStrokes(rs.getInt("ScoreStroke"));
         score.setPoints(rs.getInt("ScorePoints"));
   return score;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map   
 public String toString(){
 try {
//      LOG.debug("starting toString TarifGreenfee !");
    return
          ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + "  hole =" + TAB + hole
            + ", par =" + TAB + par
            + ", index =" + TAB + index
            + ", extra =" + TAB + extra
            + ", distance =" + TAB + distances
            + ", strokes =" + TAB + strokes
            + ", points =" + TAB + points
          );
 }catch(Exception e){
    String msg = "£££ Exception in Score.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
 }
} //end method
    } // end inner class Score
    public ArrayList<Score> getScoreList() {
        return scoreList;
    }

    public void setScoreList(ArrayList<Score> scoreList) {
        this.scoreList = scoreList;
    }

    public boolean isShowCreateHandicapIndex() {
        return showCreateHandicapIndex;
    }

    public void setShowCreateHandicapIndex(boolean showCreateHandicapIndex) {
        this.showCreateHandicapIndex = showCreateHandicapIndex;
    }




    public int[] getHoleArray() {
        return holeArray;
    }

    public void setHoleArray(int[] holeArray) {
        this.holeArray = holeArray;
    }

    public Short getScorePar() {
        return scorePar;
    }

    public void setScorePar(Short scorePar) {
        this.scorePar = scorePar;
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

    public Short getScoreStrokeIndex() {
        return scoreStrokeIndex;
    }

    public void setScoreStrokeIndex(Short scoreStrokeIndex) {
        this.scoreStrokeIndex = scoreStrokeIndex;
    }

    public boolean isShowLineDifferential() {
        return showLineDifferential;
    }

    public void setShowLineDifferential(boolean showLineDifferential) {
        this.showLineDifferential = showLineDifferential;
    }

    public Integer getIdscore() {
        return idscore;
    }

    public void setIdscore(Integer idscore) {
        this.idscore = idscore;
    }

    public boolean isShowCalculate() {
        return showCalculate;
    }

    public void setShowCalculate(boolean showCalculate) {
        this.showCalculate = showCalculate;
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

  //  public int[][] getStatistics() {
  //      return statistics;
   // }

  //  public void setStatistics(int[][] statistics) {
  //      this.statistics = statistics;
  //  }

    public boolean isScoreCardOK() {
        return ScoreCardOK;
    }

    public void setScoreCardOK(boolean ScoreCardOK) {
        this.ScoreCardOK = ScoreCardOK;
    }

    public int[] getParArray() {
        return parArray;
    }

    public void setParArray(int[] parArray) {
        this.parArray = parArray;
    }

    public int[] getIndexArray() {
        return indexArray;
    }

    public void setIndexArray(int[] indexArray) {
        this.indexArray = indexArray;
    }

    public int[] getStrokeArray() {
        return strokeArray;
    }

    public void setStrokeArray(int[] strokeArray) {
        this.strokeArray = strokeArray;
    }

    public int[] getExtraArray() {
        return extraArray;
    }

    public int[] getDistanceArray() {
        return distanceArray;
    }

    public void setDistanceArray(int[] distanceArray) {
        this.distanceArray = distanceArray;
    }

    public int[] getPointsArray() {
        return pointsArray;
    }

    public void setExtraArray(int[] extraArray) {
        this.extraArray = extraArray;
    }
    public void setPointsArray(int[] pointsArray) {
        this.pointsArray = pointsArray;
    }

    public int getPlayingHandicap() {
        return playingHandicap;
    }

    public void setPlayingHandicap(int playingHandicap) {
        this.playingHandicap = playingHandicap;
    }

    public double getPlayerHandicapEGA() {
        return playerHandicapEGA;
    }

    public void setPlayerHandicapEGA(double playerHandicapEGA) {
        this.playerHandicapEGA = playerHandicapEGA;
    }

    public boolean isShowCreate() {
        return showCreate;
    }

    public void setShowCreate(boolean showCreate) {
        this.showCreate = showCreate;
    }



    public boolean isShowButtonStatistics() {
        return showButtonStatistics;
    }

    public void setShowButtonStatistics(boolean showButtonStatistics) {
        this.showButtonStatistics = showButtonStatistics;
    }

    public int getStablefordResult() {
        return stablefordResult;
    }

    public void setStablefordResult(int stablefordResult) {
        this.stablefordResult = stablefordResult;
    }

    public Double getScoreDifferential() {
        return scoreDifferential;
    }

    public void setScoreDifferential(Double scoreDifferential) {
        this.scoreDifferential = scoreDifferential;
    }

    public int getCourseHandicap() {
        return courseHandicap;
    }

    public void setCourseHandicap(int courseHandicap) {
        this.courseHandicap = courseHandicap;
    }

    public int getAdjustedGrossScore() {
        return adjustedGrossScore;
    }

    public void setAdjustedGrossScore(int adjustedGrossScore) {
        this.adjustedGrossScore = adjustedGrossScore;
    }



    public String getHandicapType() {
        return handicapType;
    }

    public void setHandicapType(String stablefordType) {
        this.handicapType = stablefordType;
    }

    public int getTotalStrokes() {
        return totalStrokes;
    }

    public double getPlayerHandicapWHS() {
        return playerHandicapWHS;
    }

    public void setPlayerHandicapWHS(double playerHandicapWHS) {
        this.playerHandicapWHS = playerHandicapWHS;
    }

    public void setTotalStrokes(int totalStrokes) {
        this.totalStrokes = totalStrokes;
    }

    public Short getScoreParCourse() {
        return scoreParCourse;
    }

    public void setScoreParCourse(Short scoreParCourse) {
        this.scoreParCourse = scoreParCourse;
    }

    public Double getCourseRating() {
        return courseRating;
    }

    public void setCourseRating(Double courseRating) {
        this.courseRating = courseRating;
    }




    public Short getSlopeRating() {
        return slopeRating;
    }

    public void setSlopeRating(Short slopeRating) {
        this.slopeRating = slopeRating;
    }



    public ArrayList<Statistics> getStatisticsList() {
        return statisticsList;
    }

    public void setStatisticsList(ArrayList<Statistics> statisticsList) {
        this.statisticsList = statisticsList;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getHoles() {
        return holes;
    }

    public void setHoles(int holes) {
        this.holes = holes;
    }

    public Double getExpectedSD9Holes() {
        return expectedSD9Holes;
    }

    public void setExpectedSD9Holes(Double expectedSD9Holes) {
        this.expectedSD9Holes = expectedSD9Holes;
    }

    public int getHolesNotPlayed() {
        return holesNotPlayed;
    }

    public void setHolesNotPlayed(int holesNotPlayed) {
        this.holesNotPlayed = holesNotPlayed;
    }

public void onCellEdit(CellEditEvent<Object> event) { // used in include_statistics
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        setShowCalculate(true); // donner la possibilité de recalculer  not used
        if (newValue != null && !newValue.equals(oldValue)) {
            String msg = "Calculate = " + isShowCalculate() +" - Cell Changed Old: " + oldValue + ", New:" + newValue;
            LCUtil.showMessageInfo(msg);
    //        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Calculate = true - Cell Changed", "Old: " + oldValue + ", New:" + newValue);
    //        FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
 @Override
    public String toString(){
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
 try{
     if(this.getClass() == null){
        return (CLASSNAME + " is null, no print !! ");
     } 
  return
        (NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE 
                + " ,adjustedGrossScore : " + this.getAdjustedGrossScore()
               + " ,HandicapType : " + this.getHandicapType()
         + NEW_LINE
               + " ,<br/>ScoreHole : " + getScoreHole()
               + " ,ScorePar : " + getScorePar()
               + " ,ScoreStroke : " + getScoreStroke()
               + " ,ScoreExtraStroke : " + getScoreExtraStroke()
               + " ,ScorePoints : " + getScorePoints()
               + " ,ScoreStrokeIndex : " + getScoreStrokeIndex()
            + NEW_LINE
               + " ,statisticsList : " + statisticsList.toString()
            + NEW_LINE
               + " ,scoreList : " + scoreList.toString()
           + NEW_LINE
               + " ,<br/>ScoreParCourse : " + getScoreParCourse()
               + " ,CourseRating : " + getCourseRating()
               + " ,SlopeRating : " + this.getSlopeRating())
          + " ,courseHandicap : " + this.getCourseHandicap()
               + " ,playingHandicap : " + this.getPlayingHandicap()
               + " ,player Handicap WHS: " + this.playerHandicapWHS
               + " ,start " + start
               + " ,holes " + holes
        + NEW_LINE
               + " ,<br/>stablefordResult : " + this.getStablefordResult()
               + " ,Total Strokes : " + this.getTotalStrokes()
               + " ,Score Differential : " + this.getScoreDifferential()
               + " ,ExpectedSD9Holes : " + this.getExpectedSD9Holes()
               + " ,Round12-17Holes = " + this.getHolesNotPlayed()
        + NEW_LINE
               + " ,<br/>parArray :    " + Arrays.toString(getParArray())
        + NEW_LINE
               + " ,<br/>indexArray :  " + Arrays.toString(getIndexArray())
        + NEW_LINE
               + " ,<br/>extraArray  : " + Arrays.toString(getExtraArray())
        + NEW_LINE
               + " ,<br/>distanceArray  : " + Arrays.toString(getDistanceArray()) 
        + NEW_LINE
               + " ,<br/>strokeArray : " + Arrays.toString(getStrokeArray())
         + NEW_LINE
               + " ,<br/>pointsArray : " + Arrays.toString(getPointsArray()
               );
 
    }catch(Exception e){
        String msg = "£££ Exception in " + methodName + e.getMessage();
//        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} // end toString

 public static ScoreStableford map(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
  try{
        ScoreStableford s = new ScoreStableford();
            s.setScoreHole(rs.getShort("ScoreHole") );
            s.setScoreStroke(rs.getShort("ScoreStroke") );
            s.setScoreExtraStroke(rs.getShort("ScoreExtraStroke") );
            s.setScorePoints(rs.getShort("ScorePoints") );
            s.setScorePar(rs.getShort("ScorePar") );
            s.setScoreStrokeIndex(rs.getShort("ScoreStrokeIndex"));
            s.setScoreFairway(rs.getShort("ScoreFairway") );
            s.setScoreGreen(rs.getShort("ScoreGreen") );
            s.setScorePutts(rs.getShort("ScorePutts") );
            s.setScoreBunker(rs.getShort("ScoreBunker") );
            s.setScorePenalty(rs.getShort("ScorePenalty") );
   return s;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + e.getMessage();
//   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map
} // end class