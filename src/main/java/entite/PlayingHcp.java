package entite;

import java.io.Serializable;
import java.util.Arrays;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Named
@SessionScoped

public class PlayingHcp implements Serializable, interfaces.Log{
    private static final long serialVersionUID = 1L;

@NotNull(message="{handicap.player.notnull}")
@Min(value=5,message="{handicap.player.min}")
@Max(value=36,message="{handicap.player.max}")
    private double handicapPlayer;

@NotNull(message="{tee.slope.notnull}")
@Min(value=95,message="{tee.slope.min}")
@Max(value=138,message="{tee.slope.max}")
    private double teeSlope;

@NotNull(message="{tee.rating.notnull}")
@DecimalMin(value="60.0",message="{tee.rating.min}")
@DecimalMax(value="75.0",message="{tee.rating.max}")
    private double teeRating;

@NotNull(message="{course.par.notnull}")
    private Integer coursePar;

@NotNull(message="Bean validation : the Round Holes must be completed (9 or 18)")
//@Size(min=9, max=18,message="Bean validation : the Round Holes is 9 or 18")
    private Short roundHoles;
@NotNull(message="Bean validation : the Round Start must be completed (1 or 10)")
//@Size(min=1, max=10,message="Bean validation : the Round Start is 1 or 10")
    private Short roundStart;
private int playingHandicap;

// new 09/06/2014
private Double[] HcpScr; // faut Double et pas double !!!

    public PlayingHcp() // connector
    {
       initElem();
        playingHandicap = 0;
      
    }

    
    
    public double getHandicapPlayer() {
        return handicapPlayer;
    }

    public void setHandicapPlayer(double handicapPlayer) {
        this.handicapPlayer = handicapPlayer;
    }

    public double getTeeSlope() {
        return teeSlope;
    }

    public void setTeeSlope(double teeSlope) {
        this.teeSlope = teeSlope;
    }

    public double getTeeRating() {
        return teeRating;
    }

    public void setTeeRating(double teeRating) {
        this.teeRating = teeRating;
    }

    public double getCoursePar() {
        return coursePar;
    }

    public void setCoursePar(Integer coursePar) {
        this.coursePar = coursePar;
    }

    public int getPlayingHandicap() {
        return playingHandicap;
    }

    public void setPlayingHandicap(int playingHandicap) {
        this.playingHandicap = playingHandicap;
    }

    public void setHcpScr(Double[] HcpScr) {
        this.HcpScr = HcpScr;
    }

    

    public Double[] getHcpScr() {
        return HcpScr;
    }

    public Short getRoundHoles() {
        return roundHoles;
    }

    public void setRoundHoles(Short roundHoles) {
        this.roundHoles = roundHoles;
    }

public void initElem()
    {
          HcpScr = new Double[]{0.00, 0.00, 0.00, 0.00};
    //    Arrays.fill(HcpScr, 0.0);
  ////      LOG.info("Array HcpScr filled = " + Arrays.deepToString(HcpScr));

    }

    public Short getRoundStart() {
        return roundStart;
    }

    public void setRoundStart(Short roundStart) {
        this.roundStart = roundStart;
    }
   
 @Override
public String toString()
{ return 
        ("from " + getClass().getSimpleName() + " : "
               + " ,handicapplayer : "   + this.getHandicapPlayer()
               + " ,playingHandicap : " + this.getPlayingHandicap()
               + " ,slope : "   + this.getTeeSlope()
               + " ,rating : " + this.getTeeRating()
               + " ,holes : " + this.getRoundHoles()
               + " ,start : " + this.getRoundStart()
               + " ,Handicaps Scramble : " + Arrays.deepToString(this.getHcpScr())
        );
}  
} // end class