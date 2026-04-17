
package entite;

//import entite.composite.ECourseList;
import com.fasterxml.jackson.databind.ObjectMapper;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import jakarta.faces.view.ViewScoped;
// import jakarta.inject.Inject;  // migrated 2026-02-24
// import jakarta.inject.Named;  // migrated 2026-02-24
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utils.LCUtil;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.ValueChangeEvent;

//@Named // enlevé 20/06/2022
//@ViewScoped // new 07-03-2021 for date range filter
public class Round implements Serializable, interfaces.Log, interfaces.GolfInterface{
    private static final long serialVersionUID = 1L;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 //   final DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

@NotNull(message="{round.id.notnull}")
    private Integer idround;

@NotNull(message="{round.date.notnull}")
    private LocalDateTime roundDate;  // change also ScoreCard.java and StablefordResult !!!!!!
    private ZonedDateTime roundDateZoned;
    private LocalDateTime roundDateTrf;
    
@NotNull(message="{round.game.notnull}")
@Size(min=3,max=20,message="Bean validation : the Round Game is min 3, max 20 characters")
    private String roundGame;

@PositiveOrZero
@NotNull(message="{round.cba.notnull}")
    private Short roundCBA;

@NotNull(message="{round.name.notnull}")
@Size(min=1, max=45, message="{round.name.size}")
    private String RoundName;

@NotNull(message="Bean validation : the Round Qualifying must be completed")
@Size(min=1, max=1,message="Bean validation : the Round Qualifying is max 1 character")
    private String roundQualifying;

@NotNull(message="Bean validation : the Round Holes must be completed (9 or 18)")
//@Size(min=9, max=18,message="Bean validation : the Round Holes is 9 or 18")
    private Short roundHoles;

@NotNull(message="Bean validation : the Round Start must be completed (1 or 10)")
//@Size(min=1, max=10,message="Bean validation : the Round Start is 1 or 10")
    private Short roundStart;

private String RoundScoreString;

@NotNull(message="Bean validation : the Course ID must be completed")
    private Integer courseIdcourse;
    private String roundTeam;
    private Date roundModificationDate;
    private List<Player> playersList ; //added 01/04/2013 renamed 30-11-2018
    private String playersString;
    private String roundCompetition;
    private boolean showQualifying = false;  // afficher scrolling dans inscription.xhtml
// @Inject  // migrated 2026-02-24
ScoreMatchplay scoreMatchplay;
   private String calculations;   
    public Round(){
       this.playersList = new ArrayList<>();
       roundQualifying = "N"; //set default value to radiobutton
       roundCBA = 0;
       roundHoles = 18;
       roundStart = 1;
    }

    public enum GameType {STABLEFORD,SCRAMBLE,CHAPMAN,STROKEPLAY,MP_FOURBALL,MP_FOURSOME,MP_SINGLE}
    public GameType[] GameType() {
 //     LOG.debug("array as list = " + Arrays.asList(Round.GameType.values()));
        return GameType.values();
  }
    /* moved to Tee
    public enum StartType {YELLOW,WHITE,BLACK,BLUE,RED,ORANGE}
    public StartType[] GameType() {
        return StartType.values();
    }
    */

    public boolean isShowQualifying() {
        return showQualifying;
    }

    public void setShowQualifying(boolean showQualifying) {
        this.showQualifying = showQualifying;
    }
    
    
    
    public String getCalculations() {
        return calculations;
    }

    public void setCalculations(String calculations) {
        this.calculations = calculations;
    }

    public LocalDate getRoundDate_LocalDate() {
        return roundDate.toLocalDate();
    }

    public Integer getIdround() {
        return idround;
    }

    public void setIdround(Integer idround)
    { // LOG.debug("from setIdround, new value = " + idround);
        this.idround = idround;
    }

    public List<Player> getPlayers() {
        return playersList;
    }

    public void setPlayers(List<Player> players) {
        this.playersList = players;
    }

    public String getPlayersString() {
        return playersString;
    }

    public void setPlayersString(String playersString) {
        this.playersString = playersString;
    }

    public LocalDateTime getRoundDate() {
 //       LOG.debug("from getRoundDate");
 //       LOG.debug(" from getRoundDate 2 - roundDate = " + roundDate.format(ZDF_TIME));
 //       LOG.debug(" from getRoundDate 3 - roundDate = " + roundDate.format(ZDF_TIME_HHmm));
       return roundDate;
    //    return roundDate.format(ZDF_TIME_HHmm)) ;
    }

    public void setRoundDate(LocalDateTime roundDate) {
 //       LOG.debug("from setRoundDate 1");
 //       LOG.debug("setRoundDate formatted = " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(roundDate));
 //       LOG.debug(" from setRoundDate 2 - roundDate = " + roundDate.format(ZDF_TIME));
 //       LOG.debug(" from setRoundDate 3 - roundDate = " + roundDate.format(ZDF_TIME_HHmm));
 //       
        this.roundDate = roundDate;
        
    }

    public ZonedDateTime getRoundDateZoned() {
        return roundDateZoned;
    }

    public void setRoundDateZoned(ZonedDateTime roundDateZoned) {
        this.roundDateZoned = roundDateZoned;
    }

    public String getRoundTeam() {
        return roundTeam;
    }

    public void setRoundTeam(String roundTeam) {
        this.roundTeam = roundTeam;
    }

    public String getRoundGame() {
        return roundGame;
    }

    public void setRoundGame(String roundGame) {
        this.roundGame = roundGame;
    }

    public Short getRoundCBA() {
        return roundCBA;
    }
    public void setRoundCBA(Short roundCBA) {
        this.roundCBA = roundCBA;
    }

    public String getRoundName() {
        return RoundName;
    }
    public void setRoundName(String RoundName) {
        this.RoundName = RoundName;
    }

    public String getRoundQualifying() {
        return roundQualifying;
    }
    public void setRoundQualifying(String roundQualifying) {
        this.roundQualifying = roundQualifying;
    }

    public Short getRoundHoles() {
        return roundHoles;
    }
    public void setRoundHoles(Short roundHoles) {
        this.roundHoles = roundHoles;
    }

    public Short getRoundStart() {
        return roundStart;
    }

    public void setRoundStart(Short roundStart) {
        this.roundStart = roundStart;
    }

    public String getRoundScoreString() {
        return RoundScoreString;
    }

    public void setRoundScoreString(String RoundScoreString) {
        this.RoundScoreString = RoundScoreString;
    }

    public Integer getCourseIdcourse() {
        return courseIdcourse;
    }

    public void setCourseIdcourse(Integer courseIdcourse) {
        this.courseIdcourse = courseIdcourse;
    }

    public Date getRoundModificationDate() {
        return roundModificationDate;
    }

    public void setRoundModificationDate(Date roundModificationDate) {
        this.roundModificationDate = roundModificationDate;
    }

    public List<Player> getPlayersList() {
        return playersList;
    }

    public void setPlayersList(List<Player> playersList) {
        this.playersList = playersList;
    }

    public LocalDateTime getRoundDateTrf() {
        return roundDateTrf;
    }

    public void setRoundDateTrf(LocalDateTime roundDateTrf) {
        this.roundDateTrf = roundDateTrf;
    }

    public ScoreMatchplay getScoreMatchplay() {
        return scoreMatchplay;
    }

    public void setScoreMatchplay(ScoreMatchplay scoreMatchplay) {
        this.scoreMatchplay = scoreMatchplay;
    }

    public String getRoundCompetition() {
        return roundCompetition;
    }

    public void setRoundCompetition(String roundCompetition) {
        this.roundCompetition = roundCompetition;
    }
    
public static String fillRoundPlayersString(List<Player> players) { // from
 if(players.isEmpty()){  // was size == 0
      LOG.debug(" exiting fillRoundPlayersString with no player");
     return "";
 }
     StringBuilder sb = new StringBuilder();
     for(int i=0; i < players.size() ; i++){
        sb.append(players.get(i).getPlayerLastName()).append(" (");
        sb.append(players.get(i).getIdplayer()).append("), ");
     } // end for 
      sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
 //    LOG.debug(" exiting fillRoundPlayersString with = " + sb.toString());
 return sb.toString();
}

public static String fillRoundPlayersStringEcl(java.util.List<ECourseList> players) {
    ArrayList<Player> p = new ArrayList<>();   // transform player2 in list<player<    
    for(int i=0; i < players.size() ; i++){
//    LOG.debug("elem = " + players.get(i).Eplayer.getPlayerLastName());
       p.add(players.get(i).player());
     //       LOG.debug(" -- item in for idplayer # = " + dlPlayers.getTarget().get(i).getIdplayer() );
    }   
    return fillRoundPlayersString(p); // next method
}
/* new 21-09-2024*/
public void ajaxListener(AjaxBehaviorEvent event) {
  try{
     LOG.debug("Round - ajaxListener called !");
//    LOG.debug("Round - event = " + event.toString());
//     LOG.debug("AjaxListener :: "+ event.getBehavior()+ " : " + event.getSource() + " : "+ event.getComponent());
  }catch(Exception e){
        String msg = " EXCEPTION in ajaxListener = " + e.getMessage();
        LOG.error(msg);
     //   LCUtil.showMessageFatal(msg);
     //   return msg;
  }
}
// vers courseC
  public void qualifyingListener(ValueChangeEvent e) {
        LOG.debug("qualifyingListener OldValue = " + e.getOldValue());
        LOG.debug("qualifying NewValue = " + e.getNewValue());
      this.setShowQualifying(true);
        LOG.debug("showQualifying is true = "); // + this.getShowQualifying());
   // address.setCity(e.getNewValue().toString() );
}
 @Override
public String toString(){ 
   // LOG.debug("starting toString for Round!");
 try{
   if(this.getClass() == null){
      return (CLASSNAME + "is null, no print : ");
   } 
     return 
        (NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE 
               + " ,idround : "   + this.getIdround()
      //         + " ,Round Players : "   + this.getRoundPlayers()
     //          + " ,Work Date format Date : "   + this.getWorkDate()
     //          + " ,Work Hours : "   + this.getWorkHour()
               + " ,RoundDate format LocalDateTime: "   + this.getRoundDate() //.format(ZDF_TIME_HHmm)
               + " ,RoundDateTRF - LocalDateTime: "   + this.getRoundDateTrf() //.format(ZDF_TIME_HHmm)
               + " ,RoundDate format ZonedDateTime: "   + this.getRoundDateZoned()//.format(ZDF_TIME_HHmm) //.getRoundDate().format(ZDF_TIME)
          + NEW_LINE + TAB
               + " ,Round Competition : " + this.getRoundName()
               + " ,Round Game : " + this.getRoundGame()
               + " ,Round Qualifying : " + this.getRoundQualifying()
               + " ,Team : "   + this.getRoundTeam()
               + " ,Holes : "   + this.getRoundHoles()
          + NEW_LINE + TAB+
               " ,Start : "   + this.getRoundStart()
    //           + " ,Nombre Players : "   + this.getRoundPlayers()
               + " ,Name Players : "   + this.getPlayersString()
               + " ,getPlayers List: "   + this.getPlayers().toString()
               + " ,idCourse : "   + this.getCourseIdcourse()
               + " ,competition : "   + this.getRoundCompetition()
        );
  }catch(Exception e){
        String msg = " EXCEPTION in Round.toString = " + e.getMessage();
        LOG.error(msg);
     //   LCUtil.showMessageFatal(msg);
        return msg;
  }
}
// provoque faute !!
/*
public Round dtoMapper(ResultSet rs) throws SQLException{
 //     final String methodName = utils.LCUtil.getCurrentMethodName(); 
 // LOG.debug("entering Round map  without club");
  try{
    //  Club club = null;
   //   Round r = map(rs, club);
      Round r = dtoMapper(rs, null);
      return r;
}catch(Exception e){
   String msg = "£££ Exception in rs without club = " + " /" + e.getMessage(); 
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;

}
}
*/
/*
   public Round dtoMapper(ResultSet rs, Club club) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
        Round round = new Round();
 //        LOG.debug("entering mapRound with club = " + club);
        round.setIdround(rs.getInt("idround"));
//              LOG.debug("Idround = " + round.getIdround());
        if(club == null){
          LOG.debug("club == null");
          club= new Club();
          club.getAddress().setZoneId("Europe/Brussels");
        }
        LocalDateTime ldt = rs.getTimestamp("roundDate").toLocalDateTime();
 //          LOG.debug("ldt roundDate from DB = " + ldt);
        ZoneOffset zoneOffSet = ZoneId.of(club.getAddress().getZoneId()).getRules().getOffset(ldt);
 //          LOG.debug("zoneOffset = " + zoneOffSet); 
// here is the magic !!   08-05-2024   current --> destination, contrepartie = CreateRound on stocke en DB sous format UTC !!!
// ici on restore à l'heure du club où se joue la partie !!
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of(club.getAddress().getZoneId()));
 //           LOG.debug("zdt = " + zdt + " ,offset = " + zdt.getOffset());
        round.setRoundDate(zdt.toLocalDateTime());
  //          LOG.debug("ldt final = " + round.getRoundDate());
        round.setRoundDateZoned(zdt);
  //            LOG.debug("RoundDateZoned = " + round.getRoundDateZoned());
        round.setRoundGame(rs.getString("roundgame"));
        round.setRoundCBA(rs.getShort("RoundCSA"));
        round.setRoundName(rs.getString("RoundName"));
        round.setRoundQualifying(rs.getString("RoundQualifying"));
        round.setRoundHoles(rs.getShort("RoundHoles"));
        round.setRoundStart(rs.getShort("RoundStart"));
     if(!rs.getString("RoundMatchplayResult").equals("no MP score")){ // il y a des infos matchplay
          ObjectMapper om = new ObjectMapper();
          ScoreMatchplay mp = om.readValue(rs.getString("RoundMatchplayResult"),ScoreMatchplay.class);
          round.setScoreMatchplay(mp);
//          LOG.debug("r.setScoreMatchplay = " + r.getScoreMatchplay());
     }
        round.setRoundTeam(rs.getString("roundTeam"));
        round.setCourseIdcourse(rs.getInt("course_idcourse"));
        round.setRoundCompetition(rs.getString("RoundCompetition"));
   return round;
}catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map (rs, club)
   
   */
} //end class