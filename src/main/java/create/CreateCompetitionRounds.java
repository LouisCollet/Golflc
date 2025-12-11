
package create;
import entite.CompetitionDescription;
import entite.Course;
import entite.composite.ECompetition;
import entite.Round;
import entite.UnavailablePeriod;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDateTime;

public class CreateCompetitionRounds implements interfaces.Log, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static int GENERATED_KEY = 0;

 public boolean create(final CompetitionDescription cd, final Connection conn) throws SQLException {
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
 try{
     LOG.debug("entering CreateCompetitionRounds");
      LOG.debug("competition Status = " + cd.getCompetitionStatus());
  // validation 
     if(! cd.getCompetitionStatus().equals("1")){
         LOG.debug("wrong Status, must be = 1 but is =  " + cd.getCompetitionStatus());
         
         return false;
     }
         // chercher liste triée sur FlightNumber 
     List<ECompetition> li = new lists.CompetitionRoundsList().list(cd, conn);
        LOG.debug("there are rounds = " + li.size());
 // print contenu pour vérification
     for(int i=0; i < li.size() ; i++){
         LOG.debug("flightnumber =  " + li.get(i).getCompetitionData().getCmpDataFlightNumber()+
                  " - StartTime =  "    + li.get(i).getCompetitionData().getCmpDataFlightStart());
     }
 // loop 
     int save = 0;
     for(int i=0; i < li.size() ; i++){
         var competition = li.get(i);
         var description = competition.getCompetitionDescription();
         var data = li.get(i).getCompetitionData();
            LOG.debug("flightnumber =  " + data.getCmpDataFlightNumber());
            LOG.debug("start time =  " + data.getCmpDataFlightStart());
            LOG.debug("save = " + save);
         if(data.getCmpDataFlightNumber() != save){
             LOG.debug("we do both !"); // both  = 1. create round   // 2. modify cmpdata
             // create round
             competition.setCompetitionDescription(description);
          // create a round
             if( ! new CreateCompetitionRounds().createOneRound(competition, conn)){
                 String msg = "createOne Round NOT created ! !";
                 LOG.debug(msg);
                 LCUtil.showMessageFatal(msg);
                 return false;
             }
         }else{
             LOG.debug("we do only modify Data !");
         }
         LOG.debug("GENERATED_KEY used " + GENERATED_KEY);
   /// update data
      data.setCmpDataRoundId(GENERATED_KEY);
         LOG.debug("generated-key inserted = " + data.getCmpDataRoundId());
      // faire un calc in = competitiondescr + data
      String TeeStart = new calc.CalcCompetitionInscriptionTeeStart().calc(competition, conn);
      data.setCmpDataTeeStart(TeeStart);
         LOG.debug("TeeStart inserted = " + data.getCmpDataTeeStart());
      if( ! new update.UpdateCompetitionData().update(data, conn)){
          String msg = "NOT modify CompetitionData !! " + data.getCmpDataRoundId();
          LOG.debug(msg);
          LCUtil.showMessageFatal(msg);
          return false;
      }else{
          String msg = "OK modify CompetitionData for RoundId = " + data.getCmpDataRoundId() + " for player = " + data.getCmpDataPlayerId();
          LOG.info(msg);
          LCUtil.showMessageInfo(msg);
      }
      save = data.getCmpDataFlightNumber(); // ligne importante pour la logique
     }  //end for
 /* update Status of table competition_description transfered to CourseController
     cd.setCompetitionStatus("2");
     if( ! new update.UpdateCompetitionDescription().update(cd, conn)){
         String msg = "NOT modifiy Competition Description Status !! ";
         LOG.debug(msg);
         LCUtil.showMessageFatal(msg);
         return false;
     }
     */
  return true;
 }catch (Exception e) {
            String msg = "Â£Â£Â£ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
 }
  } // end method

 public boolean createOneRound(final ECompetition ec, final Connection conn) throws SQLException {
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    CompetitionDescription de = null;
 try{
          LOG.debug(" ... starting createOneRound" + methodName);
   //    var da = ec.getCompetitionData();
       de = ec.getCompetitionDescription();
       final String query = LCUtil.generateInsertQuery(conn, "round");
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER);
      // concaténation de date de la compétition et heure de départ du flight !!
            LocalDateTime ldt = de.getCompetitionDate().toLocalDate().atTime(ec.getCompetitionData().getCmpDataFlightStart());

            ps.setTimestamp(2,Timestamp.valueOf(ldt));
            ps.setString(3, de.getCompetitionGame());
            ps.setInt(4, 0); // unused
            ps.setString(5, de.getCompetitionName());
            ps.setString(6, de.getCompetitionQualifying());
            ps.setInt(7, 18);
            ps.setInt(8, de.getCompetitionStartHole());
                String e = "0";
                byte[] b = e.getBytes();
            ps.setBytes(9, b); // field MySql = VARBINARY = MatchplayStringCompressed
            ps.setString(10, "no MP score"); //MatchplayResult
       // incrémenté dans inscription.java
            ps.setInt(11,0); // field RoundPlayers - nombre de joueurs de la partie
            ps.setString(12, de.getCompetitionName());
            ps.setInt(13, de.getCompetitionCourseId());
            ps.setTimestamp(14, Timestamp.from(Instant.now()));

            utils.LCUtil.logps(ps);
            int x = ps.executeUpdate(); // write into database
            if (x != 0) {
                GENERATED_KEY = LCUtil.generatedKey(conn);
                String msg = "One Round Created = "  // LCUtil.prepareMessageBean("round.created")
                        + de.getCompetitionId() 
                        + " / round = " + GENERATED_KEY
                        + " <br/>genre = " + de.getCompetitionGame()
                        + " <br/>competition = " + de.getCompetitionName()
                        + " <br/>date = " + de.getCompetitionDate().format(ZDF_TIME_HHmm);
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/>NOT NOT Successful " + methodName + de.getCompetitionId()
                       + " <br/>genre = " + de.getCompetitionGame()
                       + " <br/>competition = " + de.getCompetitionName()
                       + " <br/>date = " + de.getCompetitionDate().format(ZDF_TIME_HHmm);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
 }catch(SQLException sqle) {
            String msg = "£££ exception in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LOG.error("--  " + sqle.toString());
            LCUtil.showMessageFatal(msg);
            return false;
} catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage() + " for competition = " + de.getCompetitionId();
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
  
        //   LocalDateTime cb = DatetoLocalDateTime(course.getCourseBeginDate());
        LocalDateTime cb = course.getCourseBeginDate(); // mod 03-12-2025
           LOG.debug("LocalDateTime courseBegin = " + cb);
        if(round.getRoundDate().isBefore(cb) ){ 
                String msgerr =  LCUtil.prepareMessageBean("round.notopened");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
        }
   //      LOG.debug("line 02");
       //  LocalDateTime ce = DatetoLocalDateTime(course.getCourseEndDate());
        LocalDateTime ce = course.getCourseEndDate(); // mod 03-12-2025
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

  void main() throws SQLException, Exception{ //enlevé static
      Connection conn = new DBConnection().getConnection();
  try{
   String msgerr =  LCUtil.prepareMessageBean("round.closed");
        LOG.error(msgerr); 
    LCUtil.showMessageFatal(msgerr);
   CompetitionDescription cde = new CompetitionDescription();
   cde.setCompetitionId(999);  // fake number
   cde = new read.LoadCompetitionDescription().load(cde, conn);
    boolean b = new CreateCompetitionRounds().create(cde, conn);
        LOG.debug("from main, after lp = " + b);
 
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
} //end class