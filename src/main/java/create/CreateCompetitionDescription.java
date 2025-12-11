package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import entite.CompetitionDescription;
import entite.ValidationsLC;
import entite.ValidationsLC.ValidationStatus;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.LocalDateTimeToDate;

public class CreateCompetitionDescription {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
   ValidationsLC vlc = new ValidationsLC();
 public boolean create(final CompetitionDescription competition, final Connection conn) throws SQLException{
     
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        LOG.debug("... entering in " + methodName); 
        LOG.debug("for competition = " + competition);
    PreparedStatement ps = null;
    ObjectMapper om = new ObjectMapper();
try{
       // validations ..new 22-11-2020.
        vlc = new create.CreateCompetitionDescription().validate(competition);
   //     LOG.debug("line 03");
        LOG.debug("line 04 v = "+ vlc.toString());
       if(vlc.getStatus0().equals(ValidationStatus.REJECTED.toString())){
           LOG.error(vlc.getStatus1());
           LCUtil.showMessageFatal(vlc.getStatus1());
           return false;
       }

//    ObjectMapper om = new ObjectMapper();
 //   om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
 
//       om.registerModule(new JavaTimeModule());  // new 18/01/2019 traiter LocalDateTime format aussi dans finTarifMembersData
 //   	om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
 //       om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);  // fonctionne ??
// om.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    final String query = LCUtil.generateInsertQuery(conn, "competition_description"); 
    int index = 0;
 //   var competition = ec.getCompetitionDescription();
// for (int i=0; i<holesGlobal.getDataHoles().length; i++){
        ps = conn.prepareStatement(query);
   // updated fields
   
    ps.setNull(++index, java.sql.Types.INTEGER); // CompetitionId
    ps.setTimestamp(2,Timestamp.valueOf(competition.getCompetitionDate()));
    ps.setString(3, competition.getCompetitionName());
    ps.setTimestamp(4,Timestamp.valueOf(competition.getStartInscriptionDate()));
    ps.setTimestamp(5,Timestamp.valueOf(competition.getEndInscriptionDate()));
    ps.setInt(6,competition.getCompetitionClubId());
    ps.setString(7,competition.getCompetitionCourseIdName());
    ps.setString(8, competition.getCompetitionGender());
    ps.setString(9, competition.getCompetitionGame());
 //   ps.setShort(10, (short) competition.getStartHole());
    ps.setShort(10, competition.getCompetitionStartHole());
    ps.setShort(11, competition.getFlightNumberPlayers());
    ps.setString(12, competition.getTimeSlots()); // c'est quoi ??
    String json = om.writeValueAsString(competition); // sur class et pas sur field attention ici erreur cherché longtemps !!
       LOG.debug("seriesHandicap converted in json format = " + NEW_LINE + json);
    ps.setString(13, json);
    ps.setString(14, competition.getCompetitionQualifying());
    ps.setTime(15,Time.valueOf(competition.getPriceGivingTime()));
    ps.setTimestamp(16,Timestamp.valueOf(competition.getStartingListDate()));
    ps.setTimestamp(17,Timestamp.valueOf(competition.getClassmentDate()));
 /* ici chercher le par dans Course encore bien utile ?? à remplacer par teePar ??
        Course c = new Course();
        c.setIdcourse(competition.getCompetitionCourseId());
        c = new load.LoadCourse().load(conn, c);
    ps.setShort(18, c.getCoursePar());  // CompetitionPar// à remplacer par le PAR du tee ??
 */
    ps.setShort(18,(short) 72);  // CompetitionPar// à remplacer par le PAR du tee ??
  //      LOG.debug("par in Competition coming from course = " + c.getCoursePar());
    ps.setString(19, "0") ; //competition.getCompetitionStatus());
    ps.setShort(20, competition.getCompetitionAgeLadies());
    ps.setShort(21, competition.getCompetitionAgeMens());
    ps.setShort(22, competition.getCompetitionMaximumPlayers());
  //        c.setCompetitionAgeLadies(rs.getShort("CompetitionAgeLadies"));
  //      c.setCompetitionAgeMens(rs.getShort("CompetitionAgeMens"));
  //      c.setCompetitionMaximumPlayers(rs.getShort("CompetitionMaximumPlayers"));
    ps.setTimestamp(23, Timestamp.from(Instant.now()));
    
    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate(); // write into database
   
    if(row!=0){
        competition.setCompetitionId(LCUtil.generatedKey(conn));
        String msg =  LCUtil.prepareMessageBean("competition.create") + competition;
        LOG.debug(msg); 
        LCUtil.showMessageInfo(msg);
        return true;
     }else{
        String msg = "-- ERROR update competitionDescrition : " + competition; 
        LOG.debug(msg); 
        LCUtil.showMessageFatal(msg);
        return false;
     }
//  } // end for
  
//return true;
} catch(SQLException sqle) {
       String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
       return false;
    } catch(Exception e) {
       LOG.error(" -- Exception in " + methodName + e.getMessage());
       return false;
    }finally{
        DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
 //       return false;
    }
} //end create
 
 public ValidationsLC validate(final CompetitionDescription competition) throws SQLException{
   try{
       LOG.debug("entering validation before create CompetitionDescription");
    vlc.setStatus0(ValidationsLC.ValidationStatus.APPROVED.toString());
 //   LOG.debug("line 01");
    if(competition.getEndInscriptionDate().isBefore(competition.getStartInscriptionDate()) ){ 
  //      LOG.debug("ici l'erreur nouveau style");
       vlc.setStatus0(ValidationsLC.ValidationStatus.REJECTED.toString());
  // attention parameter substitution in resource bundles !!
  
       Object[] data = new Object[2]; 
       data[0] = competition.getEndInscriptionDate();
       data[1] = LocalDateTimeToDate(competition.getStartInscriptionDate());
       String msgerr = LCUtil.prepareMessageBean("competition.endbefore.start",data);
            
           //    competition.getEndInscriptionDate(),  // limitation de Class MessageFormat : ne connait pas LocalDateTime !!
           //    LocalDateTimeToDate(competition.getStartInscriptionDate())
   //    );
  //        LOG.info(msgerr);
  //        showMessageInfo(msgerr);
          vlc.setStatus1(msgerr);
          return vlc;
           }

    if(competition.getCompetitionDate().isBefore(competition.getStartInscriptionDate()) ){ 
       vlc.setStatus0(ValidationsLC.ValidationStatus.REJECTED.toString());
  // attention parameter substitution in resource bundles !!
  
       Object[] data = new Object[2]; 
       data[0] = LocalDateTimeToDate(competition.getCompetitionDate());
       data[1] = LocalDateTimeToDate(competition.getStartInscriptionDate());

 // String msg = 
       String msgerr = LCUtil.prepareMessageBean("competition.datebefore.start",data);
    //           LocalDateTimeToDate(competition.getCompetitionDate()),
    //           LocalDateTimeToDate(competition.getStartInscriptionDate())
    //   );
          vlc.setStatus1(msgerr);
          return vlc;
           }
    
    
    
  //    LOG.debug("line 02");
     return vlc; // is approved
        } catch (Exception e) {
            String msg = "Â£Â£ Exception in  validate of CreateCompetitionDescription " + e.getMessage();
            LOG.error(msg);
            return null;
   }finally{   }
  } //end validate
  

  void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try{
  //          Player player = new Player();
    //        player.setIdplayer(324713);

    }catch (Exception e){
            String msg = "££ Exception in main CreateBlocking = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
    }
    } // end main//
 } // end class