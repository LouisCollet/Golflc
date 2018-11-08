package entite;

import static interfaces.GolfInterface.NEWLINE;
import java.io.Serializable;
import javax.inject.Named;

@Named
public class Classment implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;

   
    private Integer totalPoints;
    private Integer last9;
    private Integer last6;
    private Integer last3;
    private Integer last1;
 
    private Integer totalExtraStrokes;
    public Classment()
    {
       
    }

    public Integer getTotalExtraStrokes() {
        return totalExtraStrokes;
    }

    public void setTotalExtraStrokes(Integer totalExtraStrokes) {
        this.totalExtraStrokes = totalExtraStrokes;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getLast9() {
        return last9;
    }

    public void setLast9(Integer last9) {
        this.last9 = last9;
    }

    public Integer getLast6() {
        return last6;
    }

    public void setLast6(Integer last6) {
        this.last6 = last6;
    }

    public Integer getLast3() {
        return last3;
    }

    public void setLast3(Integer last3) {
        this.last3 = last3;
    }

    public Integer getLast1() {
        return last1;
    }

    public void setLast1(Integer last1) {
        this.last1 = last1;
    }

 @Override
public String toString()
{ return 
        (NEWLINE +"from entite : " + this.getClass().getSimpleName().toUpperCase()+ NEWLINE 
               + " ,TotalExtraStrokes : "   + this.totalExtraStrokes
               + " ,TotalPoints : " + this.totalPoints
               + " ,Last 9 : " + this.last9
               + " ,Last 6 : " + this.last6
               + " ,Last 3 : " + this.last3
               + " ,Last 1 : " + this.last1
        );
}
    
} // end class