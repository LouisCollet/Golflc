package create;
import entite.CompetitionDescription;
import entite.CompetitionData;
import entite.Course;
import entite.composite.ECompetition;
import entite.Round;
import entite.UnavailablePeriod;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
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

public class CreateCompetitionInscriptions {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static int GENERATED_KEY = 0;

 public boolean create(final CompetitionDescription cd, final Connection conn) throws SQLException {
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
 try{
     // chercher liste triée sur FlightNumber
     LOG.debug("entering CreateCompetitionInscriptions with description =  = " + cd);
     LOG.debug("competition Status = " + cd.getCompetitionStatus());
     if(! cd.getCompetitionStatus().equals("2")){
         LOG.debug("wrong Status, must be = 2 but is =  " + cd.getCompetitionStatus());
         return false;
     }
     List<ECompetition> li = new lists.CompetitionRoundsList().list(cd, conn);
 // print contenu pour vérification
     for(int i=0; i < li.size() ; i++){
         LOG.debug("flightnumber =  " + li.get(i).getCompetitionData().getCmpDataFlightNumber());
         LOG.debug(" - StartTime =  "    + li.get(i).getCompetitionData().getCmpDataFlightStart());
         LOG.debug(" - roundId =  "    + li.get(i).getCompetitionData().getCmpDataRoundId());
         LOG.debug(" - TeeStart =  "    + li.get(i).getCompetitionData().getCmpDataTeeStart());
     }
     for(int i=0; i < li.size() ; i++){
         var competition = li.get(i);
         var data = li.get(i).getCompetitionData();
            LOG.debug("round ID =  " + data.getCmpDataRoundId());
            LOG.debug("start time =  " + data.getCmpDataFlightStart());
          if( ! new CreateCompetitionInscriptions().createOneInscription(competition, conn)){
              String msg = "createOne Inscription NOT created ! !";
              LOG.debug(msg);
              LCUtil.showMessageFatal(msg);
              return false;
          }
//      data.setCmpDataRoundId(GENERATED_KEY);
   //      LOG.debug("generated-key inserted = " + data.getCmpDataRoundId());
      if( ! new update.UpdateCompetitionData().update(data, conn)){
          String msg = "NOT modify CompetitionData !! ";
          LOG.debug(msg);
          LCUtil.showMessageFatal(msg);
          return false;
      }
  //    save = data.getCmpDataFlightNumber(); // ligne importante pour la logique
     }  //end for
 /* update description
     cd.setCompetitionStatus("3");
     if( ! new update.UpdateCompetitionDescription().update(cd, conn)){
         String msg = "NOT modifiy Competition Description !! ";
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

 public boolean createOneInscription(final ECompetition ec, final Connection conn) throws SQLException {
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    CompetitionDescription cde = null;
    CompetitionData cda = null;
 try{
            LOG.debug(" ... starting " + methodName);
       cda = ec.getCompetitionData();
       cde = ec.getCompetitionDescription();
       final String query = LCUtil.generateInsertQuery(conn, "player_has_round");
       ps = conn.prepareStatement(query);
       ps.setNull(1, java.sql.Types.INTEGER);
       ps.setInt(2, cda.getCmpDataRoundId());
  //           LOG.debug("line 02");
       ps.setInt(3, cda.getCmpDataPlayerId());
  //           LOG.debug("line 03");
       ps.setInt(4, 0);  // Final Results : initial value at zero
       ps.setInt(5, 0);  // NotUsed1 initial value at zero
       ps.setInt(6, 0);  // NotUsed2 initial value at zero
       ps.setString(7, cda.getCmpDataTeeStart());  //exemple YELLOW / M / 01-18 / 102
      //          LOG.debug("line 05");
       String TeeStart = cda.getCmpDataTeeStart();//    String tee = s.substring(s.lastIndexOf("/")+2,s.length() ); // 2 pos après dernier / jusque fin de string
       int tee = Integer.valueOf(TeeStart
               .substring(TeeStart
               .lastIndexOf("/")+2,TeeStart
               .length()
            ));
          LOG.debug("tee extracted from inscriptionTeeStart = " + tee);
       ps.setInt(8, tee);  // new 31/03/2019
       ps.setInt(9, cda.getCmpDataPlayerId());
       // manque parameter 10
       ps.setTimestamp(10, Timestamp.from(Instant.now()));

       utils.LCUtil.logps(ps);
            int x = ps.executeUpdate(); // write into database
            if (x != 0) {
        //       GENERATED_KEY = LCUtil.generatedKey(conn);
                String msg = // LCUtil.prepareMessageBean("round.created")
                        + cde.getCompetitionId() 
                        + " <br/>genre = " + cde.getCompetitionGame()
                        + " <br/>competition = " + cde.getCompetitionName()
            //            + " <br/>GENERATED_KEY = " + GENERATED_KEY
                        + " <br/>date = " + cde.getCompetitionDate().format(ZDF_TIME_HHmm);
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/>NOT NOT Successful " + methodName + cde.getCompetitionId()
                       + " <br/>genre = " + cde.getCompetitionGame()
                       + " <br/>competition = " + cde.getCompetitionName()
                       + " <br/>date = " + cde.getCompetitionDate().format(ZDF_TIME_HHmm);
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
            String msg = "£££ Exception in " + methodName + e.getMessage() + " for competition = " + cde.getCompetitionId();
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
      //  LocalDateTime cb = DatetoLocalDateTime(course.getCourseBeginDate());
        LocalDateTime cb = course.getCourseBeginDate(); // mod 03-12-2025
          LOG.debug("LocalDateTime courseBegin = " + cb);
        if(round.getRoundDate().isBefore(cb) ){ 
                String msgerr =  LCUtil.prepareMessageBean("round.notopened");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
        }
      //   LocalDateTime ce = DatetoLocalDateTime(course.getCourseEndDate());
        LocalDateTime ce = course.getCourseEndDate(); // mod 03-12-2025
           LOG.debug("LocalDateTime courseEnd = " + ce);
        if(round.getRoundDate().isAfter(ce) ){ 
                String msgerr =  LCUtil.prepareMessageBean("round.closed");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
        }
      return true;  // no errors   
   }catch (Exception e){
            String msg = "£££ Exception in validate Round = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
    } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
            DBConnection.closeQuietly(null, null, null, null); // new 14/08/2014
    }
} // end method validate
    
  void main() throws SQLException, Exception{ //enlevé static
      Connection conn = new DBConnection().getConnection();
  try{
   CompetitionDescription cd = new CompetitionDescription();
   cd.setCompetitionId(27);
   cd = new read.LoadCompetitionDescription().load(cd, conn);
   boolean b = new CreateCompetitionInscriptions().create(cd, conn);
        LOG.debug("from main, after lp = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
} //end class