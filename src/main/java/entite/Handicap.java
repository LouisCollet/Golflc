
package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.inject.Named;
import javax.validation.constraints.*;
import org.primefaces.event.SelectEvent;
import utils.LCUtil;

@Named
public class Handicap implements Serializable, interfaces.GolfInterface{
    private static final long serialVersionUID = 1L;

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
    private ECourseList selectedHandicap; // mod 11-12-2018
    
    private List <?>filteredHandicaps; // new 03/08/2014
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

    public ECourseList getSelectedHandicap() {
        return selectedHandicap;
    }

    public void setSelectedHandicap(ECourseList selectedHandicap) {
        this.selectedHandicap = selectedHandicap;
    }

    public List<?> getFilteredHandicaps() {
        return filteredHandicaps;
    }

    public void setFilteredHandicaps(List<?> filteredHandicaps) {
        this.filteredHandicaps = filteredHandicaps;
    }
   // public void onRowSelect(SelectEvent event) {
    public void onrowSelect(SelectEvent event) {
        LOG.info("onrowSelect Event fired !");
    }
    public void onrowUnselect(SelectEvent event) {
        LOG.info("onrowUnselect Event fired !");
    }
    
    public void onrowSelectCheckbox(SelectEvent event) {
        LOG.info("onrowSelectCheckbox Event fired !");
    }
    
    public void onrowUnselectCheckbox(SelectEvent event) {
        LOG.info("onrowSelectCheckbox Event fired !" + event.getObject().toString());
    }
    
 public static Handicap mapHandicap(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Handicap h = new Handicap();
        h.setHandicapStart(rs.getTimestamp("idhandicap")) ;
        h.setHandicapEnd(rs.getTimestamp("HandicapEnd") );
        h.setHandicapPlayer(rs.getBigDecimal("HandicapPlayer") );
   return h;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map   

 @Override
public String toString(){ 
    LOG.info("starting toString for Handicap!");
 try{
    LOG.info("idhandicap : "   + this.getHandicapStart());
  //  LOG.info("RoundDate no format: "   + this.getRoundDate());
  //  LOG.info("RoundDate format LocalDateTime: "   + this.getRoundDate().format(ZDF_TIME));
//
    return 
        (NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEWLINE 
             + " ,Handicap Start : "   + Handicap.SDF.format(getHandicapStart() )
             + " ,Player Handicap : " + this.getHandicapPlayer() 
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