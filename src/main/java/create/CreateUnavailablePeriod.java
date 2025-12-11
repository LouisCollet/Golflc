package create;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Round;
import entite.UnavailablePeriod;
import entite.ValidationsLC;
import entite.ValidationsLC.ValidationStatus;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import jakarta.inject.Named;
import utils.DBConnection;
import utils.LCUtil;

// @Named enlevé 17/06/2022
public class CreateUnavailablePeriod{
  ValidationsLC vlc = new ValidationsLC();

  public boolean create(final UnavailablePeriod unavailable, final Connection conn) throws SQLException {
        PreparedStatement ps = null;
    try{
            LOG.debug("starting createUnavailablePeriod "); 
            LOG.debug("with unavailable = " + unavailable);
    // validations ...
        vlc = new create.CreateUnavailablePeriod().validate(unavailable);
   //     LOG.debug("line 03");
        LOG.debug("line 04 v = "+ vlc.toString());
       if(vlc.getStatus0().equals(ValidationStatus.REJECTED.toString())){
           LOG.error(vlc.getStatus1());
           LCUtil.showMessageFatal(vlc.getStatus1());
           return false;
       }
 //  LOG.debug("line 05");
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
    	om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
        om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);  // fonctionne ??
 // enlevé 14-09-2025 deprecated   om.setSerializationInclusion(JsonInclude.Include.NON_NULL);  // fait également dans entite
        
        
   //     LOG.debug("line 05");
        unavailable.setItemPeriod(utils.LCUtil.removeNull1DBoolean(unavailable.getItemPeriod())); // mod 17/06/2022
        String json = om.writeValueAsString(unavailable);
            LOG.debug("Unavailable Period converted in json format = " + NEW_LINE + json);
// à faire  psUnavailablePeriodCreate(?? comme dans clubModify ??

   final String query = LCUtil.generateInsertQuery(conn, "unavailable_periods");
            ps = conn.prepareStatement(query);
            ps.setNull(1,java.sql.Types.INTEGER);  //autoincrement
            ps.setInt(2,unavailable.getIdclub()); 
            ps.setObject(3, unavailable.getStartDate(), JDBCType.TIMESTAMP); // new 18-02-2020
            ps.setTimestamp(4,Timestamp.valueOf(unavailable.getEndDate()));  // 2 solutions équivalentes !!
            ps.setString(5,json);
            ps.setTimestamp(6,Timestamp.from(Instant.now()));
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
               LOG.debug("row created = " + row);
            if(row != 0){
               String msg = "Unavailable Created for"
                        + "<br/>Course = " + unavailable.getIdclub();
                 //       + " / " + club.getClubName();
                LOG.debug(msg);
 //// a remettre               LCUtil.showMessageInfo(msg);
 /*
   //  vérification si des rounds sont réservés pour la période d'indisponibilité
                List<ECourseList> ecl = new find.FindCancellation().find(unavailable,round, conn);
         // les joueurs sont prévenus par mail
         LOG.debug("ecl = " + ecl);
                boolean b = new mail.CancellationMail().dispatch(ecl);
                LOG.debug("boolean back from mail.cancellation = " + b);
        //  a faire : stocker les infos dans une table
        // qui sera accessible par l'admnistrateur local du club
             return true;
  */           
            }else{
                String msg = "<br/><br/>NOT NOT Successful insert for unavailable = " ;
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
       return true;
   } catch (Exception e) {
            String msg = "£££ Exception in CreateUnavailablePeriod = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }finally{
         DBConnection.closeQuietly(null, null, null, ps); 
          }
   } // end create
  
  public ValidationsLC validate(final UnavailablePeriod unavailable) throws SQLException{
   try{
       LOG.debug("entering validation before createUnavailablePeriod");
  //  v = new ValidationsLC();
    vlc.setStatus0(ValidationStatus.APPROVED.toString());
 //   LOG.debug("line 01");
            if(unavailable.getEndDate().isBefore(unavailable.getStartDate()) ){ 
                LOG.debug("ici l'erreur nouveau style");
                vlc.setStatus0(ValidationStatus.REJECTED.toString());
                String msgerr =  LCUtil.prepareMessageBean("tarif.member.endbeforestart");
                vlc.setStatus1(msgerr);
           //     LOG.error(msgerr); 
           //     LCUtil.showMessageFatal(msgerr);
       //         return v;
           }
  //    LOG.debug("line 02");
     return vlc; // is approved
        } catch (Exception e) {
            String msg = "Â£Â£ Exception in  validate of CreateUnavailablePeriod " + e.getMessage();
            LOG.error(msg);
            return null;
   }finally{   }
  } //end validate

void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
  try{
   UnavailablePeriod unavailable = new UnavailablePeriod();
////   unavailable.setCause("01");
   unavailable.setIdclub(1006);
   unavailable.setStartDate(LocalDateTime.parse("2020-11-03T12:45:30"));  // ISO 8601 default
   unavailable.setEndDate(LocalDateTime.parse("2020-10-04T12:45:30")); //, ));
    //  unavailable.setEndDate(LocalDateTime.of(2020, Month.MARCH, 23, 9,57,30));
   Round round = new Round();
   round.setIdround(106);  // n'importe quoi
   boolean lp = new CreateUnavailablePeriod().create(unavailable, conn);
        LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
} // end class