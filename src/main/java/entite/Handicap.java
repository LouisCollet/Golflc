
package entite;

//import entite.composite.ECourseList;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.*;
//import org.primefaces.event.SelectEvent;
import utils.LCUtil;

//@Named 14-02-2026
//@RequestScoped
public class Handicap implements Serializable, interfaces.GolfInterface{
    private static final long serialVersionUID = 1L;
    
@Past(message="{handicap.start.past}")
    private Date handicapStart;

@NotNull(message="Bean validation : the Handicap End Date cannot be null")
    private Date handicapEnd;

@NotNull(message="{handicap.player.notnull}")
@Min(value=0,message="{handicap.player.min}")
@Max(value=54,message="{handicap.player.max}")
    private BigDecimal handicapPlayerEGA;
    
    private BigDecimal playingHandicap;
    private Integer playerIdplayer;
    private Integer roundIdround;
    private Date handicapModificationDate;
    private ECourseList selectedHandicap; // mod 11-12-2018
    
 //   private List <?>filteredHandicaps; // new 03/08/2014
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

    public BigDecimal getHandicapPlayerEGA() {
        return handicapPlayerEGA;
    }

    public void setHandicapPlayerEGA(BigDecimal handicapPlayerEGA) {
        this.handicapPlayerEGA = handicapPlayerEGA;
    }


    public BigDecimal getPlayingHandicap() {
      //  LOG.debug(" getPlaying Handicap = " + playingHandicap);
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

    public ECourseList getSelectedHandicap() {
        return selectedHandicap;
    }

    public void setSelectedHandicap(ECourseList selectedHandicap) {
        this.selectedHandicap = selectedHandicap;
    }

 //   public List<?> getFilteredHandicaps() {
 //       return filteredHandicaps;
 //   }

 //   public void setFilteredHandicaps(List<?> filteredHandicaps) {
 //       this.filteredHandicaps = filteredHandicaps;
 //   }
 public static Handicap map(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
        Handicap h = new Handicap();
        h.setHandicapStart(rs.getTimestamp("idhandicap")) ;
        h.setHandicapEnd(rs.getTimestamp("HandicapEnd") );
        h.setHandicapPlayerEGA(rs.getBigDecimal("HandicapPlayerEGA") );
   return h;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map   

 @Override
public String toString(){ 
    LOG.debug("starting toString for Handicap!");
 try{
    LOG.debug("idhandicap : "   + this.getHandicapStart());
  //  LOG.debug("RoundDate no format: "   + this.getRoundDate());
  //  LOG.debug("RoundDate format LocalDateTime: "   + this.getRoundDate().format(ZDF_TIME));
//
    return 
        (NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE 
             + " ,Handicap Start : "   + new java.text.SimpleDateFormat("dd/MM/yyyy").format(getHandicapStart())
             + " ,Player Handicap : " + this.getHandicapPlayerEGA() 
     //        + " ,Playing Handicap : " + this.getPlayingHandicap()
             + " ,Handicap End : " + this.getHandicapEnd()
        );
  }catch(Exception e){
        String msg = " EXCEPTION in Handicap.toString = " + e.getMessage();
        LOG.error(msg);
     //   LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end to String
} //end class