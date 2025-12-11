package create;

import entite.Club;
import entite.Course;
import entite.Round;
import entite.UnavailablePeriod;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDateTime;

public class CreateRound implements interfaces.Log, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    public boolean create(final Round round, final Course course, final Club club, UnavailablePeriod unavailable, final Connection conn) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        PreparedStatement ps = null;
        try {
            // lors d'une prochaine modfication, séparer create de validate comme dans createGreenfee
            LOG.debug(" ... starting " + methodName);
       //     LOG.debug("round competition = " + round.getRoundName());
            LOG.debug("round to be created = " + round);
            LOG.debug("entite course = " + course);
            LOG.debug("entite club = " + club);
            
      //      boolean b = new CreateRound().validate(round, course, unavailable);
       if( ! new CreateRound().validate(round, course, unavailable)){
           LOG.debug("Create Round : there is a validation error");
           return false;
       }else{
           // no errors found : we continue ...
       }
       
    final String query = LCUtil.generateInsertQuery(conn, "round");
    ps = conn.prepareStatement(query);
    ps.setNull(1, java.sql.Types.INTEGER);
     //       java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
 //  old solution      //   ps.setTimestamp(2,Timestamp.valueOf(round.getRoundDate())); // roundDate format LocalDateTime
   //https://stackoverflow.com/questions/29773390/getting-the-date-from-a-resultset-for-use-with-java-time-classes

       LOG.debug("ZoneId = " + club.getAddress().getZoneId());
   //   ZoneOffset zoneOffSet = ZoneId.of(club.getAddress().getZoneId()).getRules().getOffset(ldt);
   //        LOG.debug("zoneOffset = " + zoneOffSet);   
// here is the magic ! contrepartie = Round.map // new 08-05-2024      
    ZonedDateTime zdt = round.getRoundDate().atZone(ZoneId.of(club.getAddress().getZoneId())) // origine
                        .withZoneSameInstant(ZoneId.of("UTC")); // destination
       LOG.debug("ZonedDateTime zdt in UTC format for DB insert = " + zdt + " ,offset = " + zdt.getOffset());
    LocalDateTime ldt = zdt.toLocalDateTime();
          LOG.debug("LocalDateTime ldt in UTC format for DB insert = " + ldt);
    ps.setObject(2, ldt, JDBCType.TIMESTAMP);
 //  // old  ps.setObject(2, round.getRoundDate(), JDBCType.TIMESTAMP); // new 18-02-2020
    ps.setString(3, round.getRoundGame());
    if(Round.GameType.STABLEFORD.toString().equals(round.getRoundGame()) ){
        LOG.debug("gameType is STABLEFORD");
    }
    
    
    ps.setInt(4, round.getRoundCBA());
    ps.setString(5, round.getRoundName());
    ps.setString(6, round.getRoundQualifying());
    ps.setInt(7, round.getRoundHoles());
    ps.setInt(8, round.getRoundStart());
    ps.setString(9, round.getRoundCompetition());
    ps.setString(10, "no MP score"); //MatchplayResult
       // incrémenté dans inscription.java
    ps.setInt(11, 0); // not used anymore 16-09-2021 field RoundPlayers - nombre de joueurs de la partie
    ps.setString(12, round.getRoundTeam() );  // new 26/06/2017
    ps.setInt(13, course.getIdcourse());
    ps.setTimestamp(14, Timestamp.from(Instant.now()));

            utils.LCUtil.logps(ps);
            int x = ps.executeUpdate(); // write into database
            if (x != 0) {
                round.setIdround(LCUtil.generatedKey(conn));
           //     LOG.debug("Round created = " + round.getIdround());
//                setNextInscription(true); // affiche le bouton next(Inscription) bas ecran Ã  droite
                String msg =  LCUtil.prepareMessageBean("round.created")
             //   msg = msg
                        + round.getIdround() 
                        + " <br/>genre = " + round.getRoundGame()
                        + " <br/>competition = " + round.getRoundName()
                        + " <br/>qualifying = " + round.getRoundQualifying()
                        + " <br/>holes = " + round.getRoundHoles()
                        + " <br/>start = " + round.getRoundStart()
                        + " <br/>date = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                        + " <br/>UTC ZonedDateTime DB inserted = " + zdt.format(ZDF_TIME_HHmm);
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/>NOT NOT Successful " + methodName + round.getIdround()
                        + " <br/>genre = " + round.getRoundGame()
                        + " <br/>competition = " + round.getRoundName()
                        + " <br/>qualifying = " + round.getRoundQualifying()
                        + " <br/>holes = " + round.getRoundHoles()
                        + " <br/>start = " + round.getRoundStart()
                   //     + " <br/>date = " + SDF.format(round.getRoundDate() );
                        + " <br/>date = " + round.getRoundDate().format(ZDF_TIME);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
 }catch(SQLException sqle) {
            String msg = "Â£Â£Â£ exception in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LOG.error("--  " + sqle.toString());
            LCUtil.showMessageFatal(msg);
            return false;
} catch (Exception e) {
            String msg = "Â£Â£Â£ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
 } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
    } //end method
    
  public boolean validate(final Round round, final Course course,final UnavailablePeriod unavailable) throws SQLException{
  //  PreparedStatement ps = null;
   try{
           LOG.debug("entering validation before create round");
      //  LocalDateTime cb = DatetoLocalDateTime(course.getCourseBeginDate()); mod 03-12-2025
        LocalDateTime cb = course.getCourseBeginDate();
           LOG.debug("LocalDateTime courseBegin = " + cb);
        if(round.getRoundDate().isBefore(cb) ){ 
                String msgerr =  LCUtil.prepareMessageBean("round.notopened");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
        }
   //      LOG.debug("line 02");
        // LocalDateTime ce = DatetoLocalDateTime(course.getCourseEndDate()); // mod 03-12-2025
        LocalDateTime ce = course.getCourseEndDate(); 
           LOG.debug("LocalDateTime courseEnd = " + ce);
        if(round.getRoundDate().isAfter(ce) ){ 
                String msgerr =  LCUtil.prepareMessageBean("round.closed");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
        }
      return true;  // no errors   
         } catch (Exception e) {
            String msg = "£££ Exception in validate Round = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
            DBConnection.closeQuietly(null, null, null, null); // new 14/08/2014
        }     
// return false;
} // end method validate
    
 public void main(String[] args) throws Exception {
      Connection conn = new DBConnection().getConnection();
  try{
   Round round = new Round(); 
  //  LocalDateTime dt1 = LocalDateTime.parse("2018-11-03T12:45:30"); 
   round.setRoundDate(LocalDateTime.parse("2018-11-03T12:45:30"));
   Course course = new Course();
   course.setIdcourse(135);
   course.setCourseBeginDate(LocalDateTime.parse("31/12/2019")); // à modifier !!
   course.setCourseEndDate(LocalDateTime.parse("31/12/2021"));
   UnavailablePeriod unavailable = new UnavailablePeriod();
   Club club = null; // fake à modifier !!
    boolean lp = new CreateRound().create(round, course, club, unavailable, conn);
        LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main

} //end class