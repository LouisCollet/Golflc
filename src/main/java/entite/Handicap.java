
package entite;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.inject.Named;
import javax.validation.constraints.*;
/**
 *
 * @author collet
 */
@Named
public class Handicap implements Serializable, interfaces.Log, interfaces.GolfInterface
{
    private static final long serialVersionUID = 1L;
    
@NotNull(message="{handicap.start.notnull}")
@Past(message="{handicap.start.past}")
    private Date handicapStart;

@NotNull(message="Bean validation : the Handicap End Date cannot be null")
    private Date handicapEnd;

@NotNull(message="{handicap.player.notnull}")
@Min(value=0,message="{handicap.player.min}")
@Max(value=54,message="{handicap.player.max}")
    private BigDecimal handicapPlayer;

    private BigDecimal playingHandicap;

    private Integer playerIdplayer;

    private Integer roundIdround;

    private Date handicapModificationDate;

 //   public Handicap()
 //   {
 //   }

    public Date getHandicapStart() {
        return handicapStart;
    }

    public void setHandicapStart(Date handicapStart) {
        this.handicapStart = handicapStart;
    }

    public Date getHandicapEnd() {
        return handicapEnd;
    }

    public void setHandicapEnd(Date handicapEnd) {
        this.handicapEnd = handicapEnd;
    }

    public BigDecimal getHandicapPlayer() {
        return handicapPlayer;
    }

    public void setHandicapPlayer(BigDecimal handicapPlayer) {
        this.handicapPlayer = handicapPlayer;
    }

    public BigDecimal getPlayingHandicap() {
      //  LOG.info(" getPlaying Handicap = " + playingHandicap);
        return playingHandicap;
    }

    public void setPlayingHandicap(BigDecimal playingHandicap) {
        this.playingHandicap = playingHandicap;
    }



    public Integer getPlayerIdplayer() {
        return playerIdplayer;
    }

    public void setPlayerIdplayer(Integer playerIdplayer) {
        this.playerIdplayer = playerIdplayer;
    }

    public Integer getRoundIdround() {
        return roundIdround;
    }

    public void setRoundIdround(Integer roundIdround) {
        this.roundIdround = roundIdround;
    }

    public Date getHandicapModificationDate() {
        return handicapModificationDate;
    }

    public void setHandicapModificationDate(Date handicapModificationDate) {
        this.handicapModificationDate = handicapModificationDate;
    }
 @Override
public String toString()
{ return 
        ("from entite = " + this.getClass().getSimpleName()
             + " ,Handicap Start : "   + this.SDF.format(getHandicapStart() )
             + " ,Player Handicap : " + this.getHandicapPlayer() 
     //        + " ,Playing Handicap : " + this.getPlayingHandicap()
             + " ,Handicap End : " + this.getHandicapEnd()
        );
}
} //end class
