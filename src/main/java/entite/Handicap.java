
package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.inject.Named;
import javax.validation.constraints.*;
import utils.LCUtil;
/**
 *
 * @author collet
 */
@Named
public class Handicap implements Serializable, interfaces.GolfInterface
{
    private static final long serialVersionUID = 1L;
    
//@NotNull(message="{handicap.start.notnull}")
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
        (NEW_LINE + "FROM ENTITE = " + this.getClass().getSimpleName()
             + " ,Handicap Start : "   + Handicap.SDF.format(getHandicapStart() )
             + " ,Player Handicap : " + this.getHandicapPlayer() 
     //        + " ,Playing Handicap : " + this.getPlayingHandicap()
             + " ,Handicap End : " + this.getHandicapEnd()
        );
}
  public static Handicap mapHandicap(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Handicap h = new Handicap();
        h.setHandicapStart(rs.getDate("idhandicap") );
        h.setHandicapEnd(rs.getDate("HandicapEnd") );
        h.setHandicapPlayer(rs.getBigDecimal("HandicapPlayer") );
    
   return h;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map

} //end class