
package create;
import entite.Course;
import entite.Round;
import entite.Unavailable;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDateTime;
import static utils.LCUtil.showMessageFatal;

public class CreateRound implements interfaces.Log, interfaces.GolfInterface{
    public boolean create(final Round round, final Course course, Unavailable unavailable, final Connection conn) throws SQLException {
        PreparedStatement ps = null;
        try {
            // lors d'une prochaine modficatin, séparer create de validate comme dans createGreenfee
            LOG.info(" ... starting createRound()");
       //     LOG.info("round competition = " + round.getRoundCompetition());
            LOG.info("round to be created = = " + round.toString());
            LOG.info("entite course = " + course.toString());          
            
      //      boolean b = new CreateRound().validate(round, course, unavailable);
       if( ! new CreateRound().validate(round, course, unavailable)){
           LOG.info("Create Round : there is a validation error");
           // il y a une erreur de validation
           return false;
       }else{
           // no errors found : we continue ...
       }

/*
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
*/
  
            final String query = LCUtil.generateInsertQuery(conn, "round"); // new 15/11/2012
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER);
            
     //       java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
 //  old solution      //   ps.setTimestamp(2,Timestamp.valueOf(round.getRoundDate())); // roundDate format LocalDateTime
                LOG.info("line 03");
   //https://stackoverflow.com/questions/29773390/getting-the-date-from-a-resultset-for-use-with-java-time-classes
            ps.setObject(2, round.getRoundDate(), JDBCType.TIMESTAMP); // new 18-02-2020
             LOG.info("line 04");

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
            ps.setTimestamp(14, Timestamp.from(Instant.now()));
            
            utils.LCUtil.logps(ps);
            int x = ps.executeUpdate(); // write into database
            if (x != 0) {
                int key = LCUtil.generatedKey(conn);
                round.setIdround(key);
           //     LOG.info("Round created = " + round.getIdround());
//                setNextInscription(true); // affiche le bouton next(Inscription) bas ecran Ã  droite
                String msg =  LCUtil.prepareMessageBean("round.created"); 
                msg = msg
                        + round.getIdround() 
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
    
    public boolean validate(final Round round, final Course course,final Unavailable unavailable) throws SQLException{
  //  PreparedStatement ps = null;
   try{
       LOG.info("entering validation before create round");
       // double emploi avec 
           if(unavailable.getCause() != null){ // autre formulation
             LOG.info("after if, unavailable = " + unavailable);
   //          LOG.info("line 01");
      //          String msg = "Il y a une indisponibilité pour : " + unavailable.getCause() + " le " + round.getRoundDate().format(ZDF_DAY);
              String msg = LCUtil.prepareMessageBean("round.unavailable"); 
              msg = msg + unavailable.getCause() + " le " + round.getRoundDate().format(ZDF_DAY);
                LOG.error(msg);
                showMessageFatal(msg);
             //   round.setWorkDate(null);
            //    to_selectCourse_xhtml("CreateRound");  // comme s'il venait du menu
             return false;
          }else{
   //          LOG.info("line 02");
              String msg = "Il y a PAS d'indisponibilité "; // + unavailable.getCause() + round.getRoundDate();
               LOG.info(msg);
          }
         LocalDateTime cb = DatetoLocalDateTime(course.getCourseBegin());
          LOG.info("LocalDateTime courseBegin = " + cb);
         if(round.getRoundDate().isBefore(cb) ){ 
                String msgerr =  LCUtil.prepareMessageBean("round.notopened");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
           }
   //      LOG.info("line 02");
         LocalDateTime ce = DatetoLocalDateTime(course.getCourseEnd());
           LOG.info("LocalDateTime courseEnd = " + ce);
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
    
        public static void main(String[] args) throws SQLException, Exception{ //enlevé static
        LOG.info("line 01");

    LOG.info("line 03, map = "); // + get);
      Connection conn = new DBConnection().getConnection();
  try{
   Round round = new Round(); 
  //  LocalDateTime dt1 = LocalDateTime.parse("2018-11-03T12:45:30"); 
   round.setRoundDate(LocalDateTime.parse("2018-11-03T12:45:30"));
   Course course = new Course();
 //  Date date = SDF.parse("2009/12/31" );
   course.setIdcourse(135);
   course.setCourseBegin(SDF.parse("31/12/2019"));
   course.setCourseEnd(SDF.parse("31/12/2021"));
   Unavailable unavailable = new Unavailable();
   unavailable.setCause(null);
 //  inscription.setInscriptionIdTee(154);
 
    boolean lp = new CreateRound().create(round, course, unavailable, conn);
        LOG.info("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
    
    
    
    
    
} //end class