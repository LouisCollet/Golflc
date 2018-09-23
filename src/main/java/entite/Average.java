package entite;

import java.io.Serializable;
import javax.inject.Named;

@Named
public class Average implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;
    private Short avgHole;
    private Short avgPar;
    private Short avgStrokeIndex;
    private Short avgExtraStroke;
    private Short avgStroke;  // moyenne des strokes
    private Short avgPoints;  // moyenne des points
    private Short countRounds;  // tours joués sur le parcours

public Average()
    {
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

    public Short getAvgStroke() {
        return avgStroke;
    }

    public void setAvgStroke(Short avgStroke) {
        this.avgStroke = avgStroke;
    }

    public Short getAvgPoints() {
        return avgPoints;
    }

    public void setAvgPoints(Short avgPoints) {
        this.avgPoints = avgPoints;
    }

    public Short getCountRounds() {
        return countRounds;
    }

    public void setCountRounds(Short countRounds) {
        this.countRounds = countRounds;
    }

@Override
public String toString()
{ return 
        ("from " + this.getClass().getSimpleName() +" :"
               + " hole : "    + this.getAvgHole()
    //           + " ,clubName : " + this.getClubName()
    //           + " ,idcourse : " + this.getIdcourse()
    //           + " ,idround : "  + this.getIdround()
               + " ,average strokes : " + this.getAvgStroke()
               + " ,average points  : " + this.getAvgPoints()
        );

}    
    
    
} // end class
