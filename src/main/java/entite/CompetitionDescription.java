package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
// import jakarta.annotation.PostConstruct;  // migrated 2026-02-26 — POJO, not CDI-managed
// import jakarta.enterprise.context.RequestScoped;  // migrated 2026-02-24
// import jakarta.enterprise.context.SessionScoped;  // migrated 2026-02-24
// import jakarta.inject.Named;  // migrated 2026-02-24
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.*;
import utils.LCUtil;
import validator.ClubValidation;
import validator.FirstUpperConstraint;


@ClubValidation
@GroupSequence({CompetitionDescription.class, FirstUpperConstraint.class})
// @Named("competitionDescription")  // migrated 2026-02-24
// @RequestScoped  // migrated 2026-02-24
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // private or public !
// https://www.baeldung.com/jackson-jsonmappingexception 
// By default, Jackson 2 will only work with with fields that are either public, or have a public getter methods
// – serializing an entity that has all fields private or package private will fail:

public class CompetitionDescription implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private StatusExecution status = StatusExecution.PROVISIONAL; // Default priority
@JsonIgnore private Integer competitionId;

@Pattern(regexp="[a-zA-Z0-9éèàê'!â& ç-]*",message="{club.name.characters}")
@NotEmpty(message="{club.name.notnull}")
@Size(min=3, max=45,message="{club.name.size}")
//@FirstUpperConstraint(max=7) // new 10/05/2013 custom validation !!! mod 1/11/2016  param max non utilisé
@FirstUpperConstraint() // new 10/05/2013 custom validation !!! mod 1/11/2016  param max non utilisé
@JsonIgnore   private String competitionName;

 @NotNull(message="{competition.startdate.notnull}")
 @FutureOrPresent(message="Competition date future or present !")
 @JsonIgnore   private LocalDateTime competitionDate;

 @NotNull(message="{competition.inscription.start.notnull}")
 @FutureOrPresent(message="Start Inscription date future or present !")
 @JsonIgnore   private LocalDateTime startInscriptionDate;

@NotNull(message="{competition.inscription.end.notnull}")
@FutureOrPresent(message="End Inscription date future or present !")   
@JsonIgnore   private LocalDateTime endInscriptionDate;
 
@JsonIgnore   private boolean CreateModify = true;

@JsonIgnore private Integer competitionClubId;

@JsonIgnore private Integer competitionCourseId;
@JsonIgnore private String competitionCourseIdName;


@NotNull(message="{round.game.notnull}")
@Size(min=3,max=20,message="Bean validation : the Round Game is min 3, max 20 characters")
@JsonIgnore private String competitionGame;

@NotNull(message="Bean validation : the Competition Qualifying must be completed")
@Size(min=1, max=1,message="Bean validation : the Round Qualifying is max 1 character")
@JsonIgnore private String competitionQualifying;

@JsonIgnore private String competitionGender;
@NotNull(message="{competition.starthole.notnull}")
@Min(value=1, message="{competition.starthole.min}")
@Max(value=10, message="{competition.starthole.max}")
@JsonIgnore private Short competitionStartHole;

@NotNull(message="Bean validation : the Number of players must be completed")
@Min(value=2, message="Bean validation : the Number of Players is  min 2") 
@Max(value=4, message="Bean validation : the Number of Players is  MAX 4") 
// ne fonctionne pas pour Short !!@Size(min=1, max=4,message="Bean validation : the Number of Players is  min 1, max 4")
@JsonIgnore  private Short flightNumberPlayers;

@JsonIgnore private String timeSlots;

// private String [][]handicapLimits;

@JsonIgnore private LocalTime priceGivingTime;

@FutureOrPresent(message="Starting list Date future or present !")
@JsonIgnore private LocalDateTime startingListDate;

@FutureOrPresent(message="Classment date future or present !")
@JsonIgnore private LocalDateTime classmentDate;

//@JsonIgnore private LocalTime minTime;
//@JsonIgnore private LocalTime maxTime;
//@JsonIgnore private LocalDateTime minDate; // bug : ne fonctionne pas !
//@JsonIgnore private LocalDate maxDate;

//@JsonIgnore private Double[][] seriesHandicap; // mod 29-09-2020 was String !!
 private Double[][] seriesHandicap; // mod 29-09-2020 was JsonIgnore

@JsonIgnore private String competitionStatus; // 0, 1 , 2
@JsonIgnore private String competitionExecution; // 0, 1 , 2
@JsonIgnore private Short competitionPar;

@NotNull(message="Bean validation : the Age Ladies must be completed")
@Min(value=5, message="Bean validation : the age of the ladies is  min 5") 
@JsonIgnore private Short competitionAgeLadies;

@NotNull(message="Bean validation : the Age Mens must be completed")
@Min(value=10, message="{competition.age.mens}")
@JsonIgnore private Short competitionAgeMens;

@NotNull(message="Bean validation : the Maximum Number of players must be completed")
@Min(value=15, message="Bean validation : the maximum players is min 15")
@JsonIgnore private Short competitionMaximumPlayers;

public enum StatusExecution {
        PROVISIONAL{
             @Override   // new 14-12-2020 for test
             public String toString() {
                 return "PROVISIONAL";
             }
        }
       , FINAL{
             @Override
             public String toString() {
                return "FINAL";
             }
       }
    }

 public CompetitionDescription(){
   // LOG.debug("before init series handicap");
  seriesHandicap = new Double[3][2]; // 3 series de deux limites handicap attention ne pas inverser l'ordre !!!! 
 // for(String[] row : seriesHandicap){
 //           Arrays.fill(row, " ");  // default
 //      }
 //  LOG.debug("after init, series handicap = " + Arrays.deepToString(seriesHandicap));
   
    } // end c
// @PostConstruct  // migrated 2026-02-26 — POJO, not CDI-managed
    public void init() {

    }

    public StatusExecution getStatus() {
        return status;
    }

    public void setStatus(StatusExecution status) {
        this.status = status;
    }
    
    public Integer getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Integer competitionId) {
        this.competitionId = competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public LocalDateTime getCompetitionDate() {
        return competitionDate;
    }

    public void setCompetitionDate(LocalDateTime competitionDate) {
        this.competitionDate = competitionDate;
    }

    public LocalDateTime getStartInscriptionDate() {
        return startInscriptionDate;
    }

    public void setStartInscriptionDate(LocalDateTime startInscriptionDate) {
        this.startInscriptionDate = startInscriptionDate;
    }

    public LocalDateTime getEndInscriptionDate() {
        return endInscriptionDate;
    }

    public void setEndInscriptionDate(LocalDateTime endInscriptionDate) {
        this.endInscriptionDate = endInscriptionDate;
    }

    public Integer getCompetitionClubId() {
//    LOG.debug("competitionClubId getted = " + competitionClubId);
        return competitionClubId;
    }

    public void setCompetitionClubId(Integer competitionClubId) {
//      LOG.debug("competitionClubId setted to = " + competitionClubId);
        this.competitionClubId = competitionClubId;
    }

    public Integer getCompetitionCourseId() {
 //       LOG.debug("competitionCourseId getted = " + competitionCourseId);
        return competitionCourseId;
    }

    public void setCompetitionCourseId(Integer competitionCourseId) {
 //          LOG.debug("competitionCourseId setted to = " + competitionCourseId);
        this.competitionCourseId = competitionCourseId;
    }

    public String getCompetitionGame() {
        return competitionGame;
    }

    public void setCompetitionGame(String roundGame) {
        this.competitionGame = roundGame;
    }

    public String getCompetitionQualifying() {
        return competitionQualifying;
    }

    public void setCompetitionQualifying(String competitionQualifying) {
        this.competitionQualifying = competitionQualifying;
    }

    public Short getCompetitionStartHole() {
        return competitionStartHole;
    }

    public void setCompetitionStartHole(Short competitionStartHole) {
        this.competitionStartHole = competitionStartHole;
    }

    public Short getFlightNumberPlayers() {
        return flightNumberPlayers;
    }

    public void setFlightNumberPlayers(Short flightNumberPlayers) {
        this.flightNumberPlayers = flightNumberPlayers;
    }

    public String getCompetitionGender() {
        return competitionGender;
    }

    public void setCompetitionGender(String competitionGender) {
        this.competitionGender = competitionGender;
    }

    public String getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(String timeSlots) {
        this.timeSlots = timeSlots;
    }
/*
    public String[][] getHandicapLimits() {
        return utils.LCUtil.array2DoubleToString(seriesHandicap);  // attention !!
     //  return seriesHandicap;
    }

    public void setHandicapLimits(String [][]handicapLimits) {
        this.handicapLimits = handicapLimits;
    }
*/
    public LocalTime getPriceGivingTime() {
        return priceGivingTime;
    }

    public void setPriceGivingTime(LocalTime priceGivingTime) {
        this.priceGivingTime = priceGivingTime;
    }

    public LocalDateTime getStartingListDate() {
        return startingListDate;
    }

    public void setStartingListDate(LocalDateTime startingListDate) {
        this.startingListDate = startingListDate;
    }

    public LocalDateTime getClassmentDate() {
        return classmentDate;
    }

    public void setClassmentDate(LocalDateTime classmentDate) {
        this.classmentDate = classmentDate;
    }

    public Double[][] getSeriesHandicap() {
        return seriesHandicap;
    }

    public void setSeriesHandicap(Double[][] series) {
        this.seriesHandicap = series;
    }

    public String getCompetitionStatus() {
        return competitionStatus;
    }

    public void setCompetitionStatus(String competitionStatus) {
        this.competitionStatus = competitionStatus;
    }

    public Short getCompetitionPar() {
        return competitionPar;
    }

    public void setCompetitionPar(Short competitionPar) {
        this.competitionPar = competitionPar;
    }

    public Short getCompetitionAgeLadies() {
        return competitionAgeLadies;
    }

    public void setCompetitionAgeLadies(Short competitionAgeLadies) {
        this.competitionAgeLadies = competitionAgeLadies;
    }

    public Short getCompetitionAgeMens() {
        return competitionAgeMens;
    }

    public void setCompetitionAgeMens(Short competitionAgeMens) {
        this.competitionAgeMens = competitionAgeMens;
    }

    public Short getCompetitionMaximumPlayers() {
        return competitionMaximumPlayers;
    }

    public void setCompetitionMaximumPlayers(Short competitionMaximumPlayers) {
        this.competitionMaximumPlayers = competitionMaximumPlayers;
    }

    public String getCompetitionExecution() {
        return competitionExecution;
    }

    public void setCompetitionExecution(String competitionExecution) {
        this.competitionExecution = competitionExecution;
    }

    public String getCompetitionCourseIdName() {
        return competitionCourseIdName;
    }

    public void setCompetitionCourseIdName(String competitionCourseIdName) {
        this.competitionCourseIdName = competitionCourseIdName;
    }

    public static PreparedStatement psCompetitionDescriptionModify(PreparedStatement ps, CompetitionDescription cd){
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
      LOG.debug("entering psCompetitionDescription");
      // voir aussi http://www.javased.com/index.php?source_dir=archaius/archaius-core/src/main/java/com/netflix/config/sources/JDBCConfigurationSource.java
 //     int index = 0;
    ps.setTimestamp(1,Timestamp.valueOf(cd.getCompetitionDate()));
    ps.setString(2, cd.getCompetitionName());
    ps.setTimestamp(3,Timestamp.valueOf(cd.getStartInscriptionDate()));
    ps.setTimestamp(4,Timestamp.valueOf(cd.getEndInscriptionDate()));
    ps.setInt(5,cd.getCompetitionClubId());
    ps.setString(6,cd.getCompetitionCourseIdName()); // mod 19-11-2020
    ps.setString(7,cd.getCompetitionGender());
    ps.setString(8, cd.getCompetitionGame());
    ps.setShort(9, cd.getCompetitionStartHole());
    ps.setShort(10, cd.getFlightNumberPlayers());
    ps.setString(11, cd.getTimeSlots()); // c'est quoi ??
    String json = OBJECT_MAPPER.writeValueAsString(cd); // sur class et pas sur field attention ici erreur cherché longtemps !!
            LOG.debug("seriesHandicap converted in json format = " + NEW_LINE + json);
    ps.setString(12, json);
    ps.setString(13, cd.getCompetitionQualifying());
    ps.setTime(14,Time.valueOf(cd.getPriceGivingTime()));
    ps.setTimestamp(15,Timestamp.valueOf(cd.getStartingListDate()));
    ps.setTimestamp(16,Timestamp.valueOf(cd.getClassmentDate()));
    ps.setShort(17, cd.getCompetitionPar());  // CompetitionPar// à remplacer par le PAR du tee ??
 //       LOG.debug("par in Competition coming from course = " + c.getCoursePar());
    ps.setString(18,cd.getCompetitionStatus());
    ps.setShort(19,cd.getCompetitionAgeLadies());
    ps.setShort(20,cd.getCompetitionAgeMens());
    ps.setShort(21,cd.getCompetitionMaximumPlayers());
    ps.setTimestamp(22, Timestamp.from(Instant.now()));
   // key
    ps.setInt(23, cd.getCompetitionId());  // ne pas oublier !!
    
return ps;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
    
    
    
    
    
 @Override
public String toString(){
    final String methodName = utils.LCUtil.getCurrentMethodName();
 try{ 
//   LOG.debug("starting toString Competition");
 
    if(this.getCompetitionId() == null){
         return ("CompetitionDescription is null, no print : "  );
     }
 return 
        ( NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEW_LINE 
               + " competition Id : "   + this.getCompetitionId()
               + " ,competition Name : " + this.getCompetitionName()
            + NEW_LINE
               + " ,competition Date : "  + this.getCompetitionDate() //.format(ZDF_TIME_HHmm)
               + " ,Inscription startDate : "  + this.getStartInscriptionDate() //.format(ZDF_TIME_HHmm)
               + " ,Inscription endDate : "  + this.getEndInscriptionDate() //.format(ZDF_TIME_HHmm)
               + " ,club Id : " + this.getCompetitionClubId()
               + " ,course Id : " + this.getCompetitionCourseId()
               + " ,course Id Name : " + this.getCompetitionCourseIdName()
               + " ,gender : " + this.getCompetitionGender()
               + " ,game : " + this.getCompetitionGame()
         + NEW_LINE
               + " ,start hole : " + this.getCompetitionStartHole()
               + " ,Number Players : " + this.getFlightNumberPlayers()
               + " ,Time Slots : " + this.getTimeSlots()
    //           + " ,HandicapLimits : " + Arrays.deepToString(handicapLimits)
               + " ,qualifying : " + this.getCompetitionQualifying()
          + NEW_LINE
               + " ,price Giving Time : " + this.getPriceGivingTime()
               + " ,starting list publication : " + this.getStartingListDate()
               + " ,classment publication : " + this.getClassmentDate()
               + " ,séries handicaps : " + Arrays.deepToString(this.getSeriesHandicap())
               + " ,par : " + this.getCompetitionPar()
          + NEW_LINE 
               + " ,statut : " + this.getCompetitionStatus()
               + " ,execution: " + this.getCompetitionExecution()
               + " ,Age Ladies : " + this.getCompetitionAgeLadies()
               + " ,Age Mens : " + this.getCompetitionAgeMens()
               + " ,Maximum Players: " + this.getCompetitionMaximumPlayers()
              );
    }catch(Exception e){
        String msg = "£££ Exception in " + methodName + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}

public static CompetitionDescription map(ResultSet rs) throws Exception, SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName();
  try{
        CompetitionDescription c = new CompetitionDescription();
        c.setCompetitionId(rs.getInt("CompetitionId") );
        c.setCompetitionDate(rs.getTimestamp("CompetitionDate").toLocalDateTime());
        c.setCompetitionName(rs.getString("CompetitionName") );
        c.setStartInscriptionDate(rs.getTimestamp("CompetitionStartInscription").toLocalDateTime());
        c.setEndInscriptionDate(rs.getTimestamp("CompetitionEndInscription").toLocalDateTime());
        c.setCompetitionClubId(rs.getInt("CompetitionClubId"));
        c.setCompetitionCourseIdName(rs.getString("CompetitionCourseIdName")); // attention is a string id - name
          String s = c.getCompetitionCourseIdName();
          s = s.substring(0,s.lastIndexOf("-")-1); // example String s = "1 - English Course";
 //            LOG.debug("courseId extracted = " + s);
        c.setCompetitionCourseId(Integer.parseInt(s));
        c.setCompetitionGender(rs.getString("CompetitionGender"));
        c.setCompetitionGame(rs.getString("CompetitionGame"));
        c.setCompetitionStartHole(rs.getShort("CompetitionStartHole"));
        c.setFlightNumberPlayers(rs.getShort("CompetitionFlightNumberPlayers"));
        c.setTimeSlots(rs.getString("CompetitionTimeSlots"));
        CompetitionDescription cd = OBJECT_MAPPER.readValue(rs.getString("CompetitionHandicapLimitsJson"),CompetitionDescription.class);
//           LOG.debug("cd handicap series from om = " + Arrays.deepToString(cd.getSeriesHandicap()));
        c.setSeriesHandicap(cd.getSeriesHandicap());
//           LOG.debug("cd handicap setted to c = " + Arrays.deepToString(c.getSeriesHandicap()));
        c.setCompetitionQualifying(rs.getString("CompetitionQualifying"));
        c.setPriceGivingTime(rs.getTime("CompetitionPrizeGivingTime").toLocalTime());
        c.setStartingListDate(rs.getTimestamp("CompetitionStartingListDate").toLocalDateTime());
        c.setClassmentDate(rs.getTimestamp("CompetitionClassmentDate").toLocalDateTime());
        c.setCompetitionStatus(rs.getString("CompetitionStatus"));
        c.setCompetitionPar(rs.getShort("CompetitionPar"));
        c.setCompetitionAgeLadies(rs.getShort("CompetitionAgeLadies"));
        c.setCompetitionAgeMens(rs.getShort("CompetitionAgeMens"));
        c.setCompetitionMaximumPlayers(rs.getShort("CompetitionMaximumPlayers"));
   return c;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class
