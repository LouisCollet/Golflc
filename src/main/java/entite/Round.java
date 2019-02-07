
package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDateTime;

@Named
public class Round implements Serializable, interfaces.Log, interfaces.GolfInterface
{
    private static final long serialVersionUID = 1L;

//@NotNull(message="Bean validation : the Round ID must be completed")
    private Integer idround;

@NotNull(message="{round.date.notnull}")
  //  private Date roundDate;
    private LocalDateTime roundDate;  // change also ScoreCard.java and StablefordResult !!!!!!
    private Date workDate;
    private String workHour;

@NotNull(message="{round.game.notnull}")
@Size(min=3,max=20,message="Bean validation : the Round Game is min 3, max 20 characters")
    private String roundGame;

//private String gametype; // new 1/11/2016
public enum GameType {STABLEFORD,SCRAMBLE,CHAPMAN,STROKEPLAY,ZWANZEURS,MP_FOURBALL,MP_FOURSOME,MP_SINGLE}

@NotNull(message="{round.cba.notnull}")
    private Short roundCBA;

@NotNull(message="{round.competition.notnull}")
@Size(max=45, message="{round.competition.size}")
    private String roundCompetition;


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
    
// new !! ajouter à l'objet round la liste des player ??
    // lignes suivantes : ne fonctionne pas !!!
// @NotEmpty(message="At least one player is required in this round !")
// @Valid enlevé 29-11-218
    private List<Player> playersList ; //added 01/04/2013 renamed 30-11-2018
    private String playersString;
    private Short roundPlayers;
    
    public Round() // constructor
    {
       this.playersList = new ArrayList<>();
       roundQualifying = "N"; //set default value to radiobutton
       roundCBA = 0;
       roundHoles = 18;
       roundStart = 1;
       roundPlayers = 0;
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
 //       LOG.info(" from getRoundDate 2 - roundDate = " + roundDate.format(ZDF_TIME));
 //       LOG.info(" from getRoundDate 3 - roundDate = " + roundDate.format(ZDF_TIME_HHmm));
       return roundDate;
    //    return roundDate.format(ZDF_TIME_HHmm)) ;
    }

    public void setRoundDate(LocalDateTime roundDate) {
 //       LOG.debug("from setRoundDate 1");
 //       LOG.info("setRoundDate formatted = " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(roundDate));
 //       LOG.info(" from setRoundDate 2 - roundDate = " + roundDate.format(ZDF_TIME));
 //       LOG.info(" from setRoundDate 3 - roundDate = " + roundDate.format(ZDF_TIME_HHmm));
 //       
        this.roundDate = roundDate;
    }

    public Date getWorkDate() {
////        LOG.debug("from getWorkDate 1");
   //     LOG.info(" from getWorkDate 2 - roundDate = " + SDF_TIME.format(getWorkDate()) );
            
        return workDate;
    }
    
    public void setWorkDate(Date workDate) {
        LOG.debug("from setWorkDate 1");
  //      LOG.info(" from setWorkDate 2 - roundDate = " + SDF_TIME.format(getWorkDate()));
        this.workDate = workDate;
    }
    
    public String getWorkHour() {
        return workHour;
    }

    public void setWorkHour(String workHour) {
        this.workHour = workHour;
    }

    public Short getRoundPlayers() {
        return roundPlayers;
    }

    public void setRoundPlayers(Short roundPlayers) {
        this.roundPlayers = roundPlayers;
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

    public String getRoundCompetition() {
        return roundCompetition;
    }
    public void setRoundCompetition(String roundCompetition) {
        this.roundCompetition = roundCompetition;
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
    
    
 @Override
public String toString()
{ 
    LOG.info("starting toString for Round = " + this.getIdround());
    if(this.getRoundDate() != null || this.getIdround() != null){
       return 
        (NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEWLINE 
               + " ,idround : "   + this.getIdround()
               + " ,Round Players : "   + this.getRoundPlayers()
               + " ,Round Date: "   + this.getRoundDate().format(ZDF_TIME)
               + " ,Round Date HHmm : "   + this.getRoundDate().format(ZDF_TIME_HHmm)
           //    + " ,Round Date/Time: "   + Round.SDF_TIME.format(getRoundDate() )
               + " ,Round Competition : " + this.getRoundCompetition()
               + " ,Round Game : " + this.getRoundGame()
               + " ,Round Qualifying : " + this.getRoundQualifying()
               + " ,Team : "   + this.getRoundTeam()
               + " ,Holes : "   + this.getRoundHoles()
               + " ,Start : "   + this.getRoundStart()
               + " ,Nombre Players : "   + this.getPlayers()
               + " ,Name Players : "   + this.getPlayersString()
        );
   }else{
        return
        (NEW_LINE + "from entite : " + this.getClass().getSimpleName() + NEWLINE 
          + " idRound = null !!");
    }
}
public static Round mapRound(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Round r = new Round();
            r.setIdround(rs.getInt("idround") );
            
            
                java.util.Date d = rs.getTimestamp("roundDate");
            r.setRoundDate(DatetoLocalDateTime(d));
     // new solution 21/01/2019       
            r.setRoundDate(rs.getTimestamp("roundDate").toLocalDateTime());
            
            r.setRoundGame(rs.getString("roundgame") );
            r.setRoundCBA(rs.getShort("RoundCSA") );
            r.setRoundCompetition(rs.getString("RoundCompetition") );
            r.setRoundQualifying(rs.getString("RoundQualifying") );
            r.setRoundHoles(rs.getShort("RoundHoles") );
            r.setRoundStart(rs.getShort("RoundStart") );// start
            r.setRoundPlayers(rs.getShort("RoundPlayers") ); // new 20/06/2017
            r.setRoundTeam(rs.getString("roundTeam"));
            
      //      r.setCourse_idcourse(rs.getInt("course_idcourse"));
           
   return r;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map

} //end class