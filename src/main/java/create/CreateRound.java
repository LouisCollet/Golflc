
package create;
import entite.Course;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDateTime;

public class CreateRound implements interfaces.Log, interfaces.GolfInterface
{
    public boolean createRound(final Round round, final Course course, final Connection conn) throws SQLException {
 
        PreparedStatement ps = null;
        try {
            LOG.info(" ... starting createRound()");
       //     LOG.info("round competition = " + round.getRoundCompetition());
            LOG.info("round to be created = = " + round.toString());
            LOG.info("entite course = " + course.toString());          
      //      LOG.info("idcourse    = " + course.getIdcourse());
      //     LOG.info("Begin course = " + course.getCourseBegin() );
      //     LOG.info("End course = " + course.getCourseEnd());
    // à faire : utiliser RoundValidation
    //    if(round.getRoundDate().before(course.getCourseBegin()) )
 //       LOG.info("line 000");
      //   LocalDateTime cb = course.getCourseBegin().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
     //      LOG.info("line 00");
       LocalDateTime cb = DatetoLocalDateTime(course.getCourseBegin());
          LOG.info("LocalDateTime courseBegin = " + cb);
         if(round.getRoundDate().isBefore(cb) ){ 
                String msgerr =  LCUtil.prepareMessageBean("round.notopened");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
           }
    //    if(round.getRoundDate().after(course.getCourseEnd()) )
   //      LOG.info("line 02");
         LocalDateTime ce = DatetoLocalDateTime(course.getCourseEnd());
           LOG.info("LocalDateTime courseEnd = " + ce);
         if(round.getRoundDate().isAfter(ce) ){ 
                String msgerr =  LCUtil.prepareMessageBean("round.closed");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
           }
  //     LOG.info("line 03");
            final String query = LCUtil.generateInsertQuery(conn, "round"); // new 15/11/2012
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER);
       //     ps.setTimestamp(2, LCUtil.getSqlTimestamp(round.getRoundDate())); // fixed 09/05/2013 bug : perdait les minutes !!!
     //           LOG.info("line 2 = ok ");java.sql.Timestamp.valueOf(date.atStartOfDay());
            java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
            ps.setTimestamp(2,ts);
            ps.setString(3, round.getRoundGame());
            if(Round.GameType.STABLEFORD.toString().equals(round.getRoundGame()) )
                {LOG.info("gameType is STABLEFORD");}
            ps.setInt(4, round.getRoundCBA());
            ps.setString(5, round.getRoundCompetition());
            ps.setString(6, round.getRoundQualifying());
            ps.setInt(7, round.getRoundHoles());
            ps.setInt(8, round.getRoundStart());
            // new 31/01/2015
                String e = "0";
                byte[] b = e.getBytes();
            ps.setBytes(9, b); // field MySql = VARBINARY = MatchplayStringCompressed
            ps.setString(10, "no MP score"); //MatchplayResult
       // incrémenté dans inscription.java
            ps.setInt(11, 0); // new 20/06/2017 - field RoundPlayers - nombre de joueurs de la partie
            ps.setString(12, round.getRoundTeam() );  // new 26/06/2017
            ps.setInt(13, course.getIdcourse());
            ps.setTimestamp(14, LCUtil.getCurrentTimeStamp());
            
            utils.LCUtil.logps(ps);
            int x = ps.executeUpdate(); // write into database
            if (x != 0) {
                int key = LCUtil.generatedKey(conn);
                round.setIdround(key);
           //     LOG.info("Round created = " + round.getIdround());
//                setNextInscription(true); // affiche le bouton next(Inscription) bas ecran Ã  droite
                String msg =  LCUtil.prepareMessageBean("round.created"); 
                msg = msg
                        + "<h1>" + round.getIdround() + "</h1>"
                        + " <br/>genre = " + round.getRoundGame()
                        + " <br/>competition = " + round.getRoundCompetition()
                        + " <br/>CBA = " + round.getRoundCBA()
                        + " <br/>qualifying = " + round.getRoundQualifying()
                        + " <br/>holes = " + round.getRoundHoles()
                        + " <br/>start = " + round.getRoundStart()
                //        + " <br/>date = " + SDF.format(round.getRoundDate() );
                        + " <br/>date = " + round.getRoundDate().format(ZDF_TIME_HHmm);
        //          LOG.info("RoundDate = " + round.getRoundDate().format(ZDF_TIME));
                
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/>NOT NOT Successful insert Round ! = " + round.getIdround()
                        + " <br/>genre = " + round.getRoundGame()
                        + " <br/>competition = " + round.getRoundCompetition()
                        + " <br/>CBA = " + round.getRoundCBA()
                        + " <br/>qualifying = " + round.getRoundQualifying()
                        + " <br/>holes = " + round.getRoundHoles()
                        + " <br/>start = " + round.getRoundStart()
                   //     + " <br/>date = " + SDF.format(round.getRoundDate() );
                        + " <br/>date = " + round.getRoundDate().format(ZDF_TIME);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
//      }catch (LCCustomException e){
  //         return false;        
        } catch (SQLException sqle) {
            String msg = "Â£Â£Â£ exception in Insert Round = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LOG.error("--  " + sqle.toString());
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (NumberFormatException nfe) {
            String msg = "-- Â£Â£Â£ NumberFormatException in Insert Round " + nfe.toString();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (NullPointerException nfe) {
            String msg = "-- Â£Â£Â£ NullPointerException in Insert Round " + nfe.toString();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "Â£Â£Â£ Exception in Create round = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
           // DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
    } //end method
}
