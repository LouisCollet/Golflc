package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
//import jakarta.inject.Named;
import utils.LCUtil;

// @Named  enlevé 07/08/2022
public class Average implements Serializable, interfaces.Log{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static final long serialVersionUID = 1L;
    private Short avgHole;
    private Short avgPar;
    private Short avgStrokeIndex;
    private Short avgExtraStroke;
    private Double avgStroke;  // mod 18-08-2020 pour tenir compte des décimales !
    private Double avgPoints;  // moyenne des points
    private Short countRounds;  // tours joués sur le parcours

public Average(){
// constructor
    }

    public Short getAvgHole() {
        return avgHole;
    }

    public void setAvgHole(Short avgHole) {
        this.avgHole = avgHole;
    }

    public Short getAvgPar() {
        return avgPar;
    }

    public void setAvgPar(Short avgPar) {
        this.avgPar = avgPar;
    }

    public Short getAvgStrokeIndex() {
        return avgStrokeIndex;
    }

    public void setAvgStrokeIndex(Short avgStrokeIndex) {
        this.avgStrokeIndex = avgStrokeIndex;
    }

    public Short getAvgExtraStroke() {
        return avgExtraStroke;
    }

    public void setAvgExtraStroke(Short avgExtraStroke) {
        this.avgExtraStroke = avgExtraStroke;
    }

    public Double getAvgStroke() {
        return avgStroke;
    }

    public void setAvgStroke(Double avgStroke) {
        this.avgStroke = avgStroke;
    }

    public Double getAvgPoints() {
        return avgPoints;
    }

    public void setAvgPoints(Double avgPoints) {
        this.avgPoints = avgPoints;
    }

    public Short getCountRounds() {
        return countRounds;
    }

    public void setCountRounds(Short countRounds) {
        this.countRounds = countRounds;
    }
public static Average map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        Average a = new Average();
            a.setAvgHole(rs.getShort("ScoreHole") );
            a.setAvgPar(rs.getShort("ScorePar") );
            a.setAvgStrokeIndex(rs.getShort("ScoreStrokeIndex") );
            a.setAvgExtraStroke(rs.getShort("ScoreExtraStroke") );
            a.setAvgStroke(rs.getDouble("averageStroke") );  // was Short
            a.setAvgPoints(rs.getDouble("averagePoints") );
            a.setCountRounds(rs.getShort("countround") );
   return a;
  }catch(Exception e){
   String msg = "£££ Exception in mapAverage = " + methodName + " / "+ e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} // end map

@Override
public String toString(){ 
    return 
        (NEW_LINE + "FROM ENTITE = "+ this.getClass().getSimpleName().toUpperCase()+ NEW_LINE
               + " hole : "    + this.getAvgHole()
               + " par : "    + this.getAvgPar()
               + " strokeIndex : "    + this.getAvgStrokeIndex()
               + " ,average strokes : " + this.getAvgStroke()
               + " ,average points  : " + this.getAvgPoints()
               + " ,countRounds  : " + this.getCountRounds()
        );

}
} // end class