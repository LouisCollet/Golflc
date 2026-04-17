
package create;

import entite.Club;
import entite.Course;
import entite.Round;
import entite.UnavailablePeriod;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Création d'un round en base de données
 * ✅ Migré vers CDI (@ApplicationScoped)
 * ✅ Connection supprimée — gérée via DataSource injecté
 * ✅ try-with-resources (plus de finally/closeQuietly)
 * ✅ handleGenericException / handleSQLException
 * ✅ main() conservée commentée
 */
@ApplicationScoped
public class CreateRound implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ========================================
    // CREATE
    // ========================================

    /**
     * Crée un round en base après validation.
     * Convertit la date locale en UTC avant insertion.
     *
     * @param round       le round à créer
     * @param course      le course associé
     * @param club        le club associé (pour la timezone)
     * @param unavailable les périodes d'indisponibilité
     * @return true si succès, false sinon
     */
    public boolean create(final Round round, final Course course,
                          final Club club, final UnavailablePeriod unavailable) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("round to be created = {}", round);
        LOG.debug("course = {}", course);
        LOG.debug("club   = {}", club);

        // Validation avant insertion
        if (!validate(round, course, unavailable)) {
            LOG.debug("validation error, aborting");
            return false;
        }

        // ✅ try-with-resources : Connection et PreparedStatement fermés automatiquement
        try (Connection conn        = dao.getConnection();
             PreparedStatement ps   = conn.prepareStatement(utils.LCUtil.generateInsertQuery(conn, "round"))) {

            LOG.debug("ZoneId = {}", club.getAddress().getZoneId());

            // Conversion date locale → UTC pour stockage en base
            ZonedDateTime zdt = round.getRoundDate()
                    .atZone(ZoneId.of(club.getAddress().getZoneId()))
                    .withZoneSameInstant(ZoneId.of("UTC"));
            LocalDateTime ldt = zdt.toLocalDateTime();

            LOG.debug("ZonedDateTime UTC for DB = {} offset = {}", zdt, zdt.getOffset());
            LOG.debug("LocalDateTime UTC for DB = {}", ldt);

            ps.setNull(1, java.sql.Types.INTEGER);              // auto-increment
            ps.setObject(2, ldt, JDBCType.TIMESTAMP);
            ps.setString(3, round.getRoundGame());
            ps.setInt(4, round.getRoundCBA());
            ps.setString(5, round.getRoundName());
            ps.setString(6, round.getRoundQualifying());
            ps.setInt(7, round.getRoundHoles());
            ps.setInt(8, round.getRoundStart());
            ps.setString(9, round.getRoundCompetition());
            ps.setString(10, "no MP score");                    // MatchplayResult
            ps.setInt(11, 0);                                   // RoundPlayers — not used since 16-09-2021
            ps.setString(12, round.getRoundTeam());
            ps.setInt(13, course.getIdcourse());
            ps.setTimestamp(14, Timestamp.from(Instant.now()));

            utils.LCUtil.logps(ps);
            int x = ps.executeUpdate();

            if (x != 0) {
                round.setIdround(utils.LCUtil.generatedKey(conn));
                String msg = utils.LCUtil.prepareMessageBean("round.created")
                        + round.getIdround()
                        + " <br/>genre = "       + round.getRoundGame()
                        + " <br/>competition = " + round.getRoundName()
                        + " <br/>qualifying = "  + round.getRoundQualifying()
                        + " <br/>holes = "       + round.getRoundHoles()
                        + " <br/>start = "       + round.getRoundStart()
                        + " <br/>date = "        + round.getRoundDate().format(ZDF_TIME_HHmm)
                        + " <br/>UTC ZonedDateTime DB inserted = " + zdt.format(ZDF_TIME_HHmm);
                LOG.debug(msg);
                showMessageInfo(msg);
                return true;
            } else {
                String msg = "NOT Successful " + methodName
                        + " <br/>id = "          + round.getIdround()
                        + " <br/>genre = "       + round.getRoundGame()
                        + " <br/>competition = " + round.getRoundName()
                        + " <br/>qualifying = "  + round.getRoundQualifying()
                        + " <br/>holes = "       + round.getRoundHoles()
                        + " <br/>start = "       + round.getRoundStart()
                        + " <br/>date = "        + round.getRoundDate().format(ZDF_TIME);
                LOG.error(msg);
                showMessageFatal(msg);
                return false;
            }

        } catch (SQLException sqle) {
            handleSQLException(sqle, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method create

    // ========================================
    // VALIDATE
    // ========================================

    /**
     * Vérifie que la date du round est dans la période d'ouverture du course.
     *
     * @return true si valide, false sinon
     */
    public boolean validate(final Round round, final Course course,
                            final UnavailablePeriod unavailable) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try {
            if (round.getRoundDate() == null) {
                String msg = utils.LCUtil.prepareMessageBean("round.date.required");
                LOG.error("roundDate is null");
                showMessageFatal(msg);
                return false;
            } // end guard clause

            LOG.debug("course begin date = {}", course.getCourseBeginDate()); // format localdatetime
            LocalDateTime cb = course.getCourseBeginDate();
            LOG.debug("courseBegin = {}", cb);
            if (round.getRoundDate().isBefore(cb)) {
                String msg = utils.LCUtil.prepareMessageBean("round.notopened");
                LOG.error(msg);
                showMessageFatal(msg);
                return false;
            }
            LOG.debug("course end date = {}", course.getCourseEndDate()); // format localdatetime
            LocalDateTime ce = course.getCourseEndDate();
            LOG.debug("courseEnd = {}", ce);
            if (round.getRoundDate().isAfter(ce)) {
                String msg = utils.LCUtil.prepareMessageBean("round.closed");
                LOG.error(msg);
                showMessageFatal(msg);
                return false;
            }

            return true;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
        // ✅ finally supprimé — closeQuietly(null,null,null,null) ne faisait rien
    } // end method validate

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

    /*
    public void main(String[] args) throws Exception {
        Connection conn = new DBConnection().getConnection();
        try {
            Round round = new Round();
            round.setRoundDate(LocalDateTime.parse("2018-11-03T12:45:30"));
            Course course = new Course();
            course.setIdcourse(135);
            course.setCourseBeginDate(LocalDateTime.parse("31/12/2019")); // à corriger format
            course.setCourseEndDate(LocalDateTime.parse("31/12/2021"));
            UnavailablePeriod unavailable = new UnavailablePeriod();
            Club club = null; // fake — à corriger
            boolean lp = new CreateRound().create(round, course, club, unavailable, conn);
            LOG.debug("from main, after lp = {}", lp);
        } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main
    */

} // end class

/*
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
import connection_package.DBConnection;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDateTime;

public class CreateRound {
    
    public boolean create(final Round round, final Course course, final Club club, UnavailablePeriod unavailable, final Connection conn) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName(); 
        PreparedStatement ps = null;
        try {
            // lors d'une prochaine modfication, séparer create de validate comme dans createGreenfee
            LOG.debug("starting");
       //     LOG.debug("round competition = {}", round.getRoundName());
            LOG.debug("round to be created = {}", round);
            LOG.debug("entite course = {}", course);
            LOG.debug("entite club = {}", club);
            
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

       LOG.debug("ZoneId = {}", club.getAddress().getZoneId());
   //   ZoneOffset zoneOffSet = ZoneId.of(club.getAddress().getZoneId()).getRules().getOffset(ldt);
   //        LOG.debug("zoneOffset = {}", zoneOffSet);   
// here is the magic ! contrepartie = Round.map // new 08-05-2024      
    ZonedDateTime zdt = round.getRoundDate().atZone(ZoneId.of(club.getAddress().getZoneId())) // origine
                        .withZoneSameInstant(ZoneId.of("UTC")); // destination
       LOG.debug("ZonedDateTime zdt in UTC format for DB insert = {} ,offset = {}", zdt, zdt.getOffset());
    LocalDateTime ldt = zdt.toLocalDateTime();
          LOG.debug("LocalDateTime ldt in UTC format for DB insert = {}", ldt);
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
           //     LOG.debug("Round created = {}", round.getIdround());
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
            LOG.error("--  {}", sqle.toString());
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
           LOG.debug("LocalDateTime courseBegin = {}", cb);
        if(round.getRoundDate().isBefore(cb) ){ 
                String msgerr =  LCUtil.prepareMessageBean("round.notopened");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
        }
   //      LOG.debug("line 02");
        // LocalDateTime ce = DatetoLocalDateTime(course.getCourseEndDate()); // mod 03-12-2025
        LocalDateTime ce = course.getCourseEndDate(); 
           LOG.debug("LocalDateTime courseEnd = {}", ce);
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
        LOG.debug("from main, after lp = {}", lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main

} //end class
*/