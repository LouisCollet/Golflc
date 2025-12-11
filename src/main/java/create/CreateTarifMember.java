package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.TarifMember;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import jakarta.inject.Named;
import utils.DBConnection;
import utils.LCUtil;

@Named
public class CreateTarifMember {

 public boolean create(final TarifMember tarif, final Connection conn) throws SQLException {
    PreparedStatement ps = null;
 try{
            LOG.debug("starting create TarifMember with tarif = " + tarif); 
 // validations           
// new 17-05-2021
     if(new find.FindTarifMembersOverlapping().find(tarif, conn)){
         return false; // rejected for dates overlapping
     }
 
 // included in Overlapping ??
 // à déplacer dans TarifController partie input comme dans TarifGreenfee
          //  if(tarif.getEndDate().isBefore(tarif.getStartDate()) ){ 
             if(tarif.getBasicList().get(0).getEndDate().isBefore(tarif.getBasicList().get(0).getStartDate()) ){     
                String msgerr =  LCUtil.prepareMessageBean("tarif.member.endbeforestart");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
           }
 /*  enlevé 09/05/2022      LOG.debug("line 00");
          YearMonth yearmonth = YearMonth.from(tarif.getStartDate());
         LOG.debug("yearmonth = " + yearmonth);
           Month month = Month.from(tarif.getStartDate());
          LOG.debug("month = " + month);
          
   */       
     //       LocalDateTime start = tarifMember.getMemberStartDate().withDayOfMonth(1);
     //        LOG.debug("start = " +start);
     //       LocalDateTime end = tarifMember.getMemberEndDate().withDayOfMonth(31);with(TemporalAdjusters.lastDayOfMonth());
     //       LOG.debug("end = " + end);
     /*
            LocalDateTime lastOfMonth = tarif.getMemberEndDate().with(TemporalAdjusters.lastDayOfMonth());
            LOG.debug("lastOfMonth = " + lastOfMonth);
            
            LOG.debug("getDayOfMonth = Start " + tarif.getMemberStartDate().getDayOfMonth());
            LOG.debug("getMonthValue Start = " + tarif.getMemberStartDate().getMonthValue());
            LOG.debug("getMonthValue End = " + tarif.getMemberEndDate().getMonthValue());
            
            LocalDate lastDayofMonthYear = YearMonth.of(2019,12).atEndOfMonth();
             LOG.debug("lastDayofMonthYear = " + lastDayofMonthYear);
            
// ce qui suit est ok
             LOG.debug("getYear End = " + tarif.getMemberEndDate().getYear());
             LOG.debug("getMonth End = " + tarif.getMemberEndDate().getMonthValue());
             LOG.debug("getDayMonth End = " + tarif.getMemberEndDate().getDayOfMonth());
             
             LOG.debug("getYear Start = " + tarif.getMemberStartDate().getYear());
             LOG.debug("getMonth Start = " + tarif.getMemberStartDate().getMonthValue());
             LOG.debug("getDayMonth Start = " + tarif.getMemberStartDate().getDayOfMonth());
*/
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
    	om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
        om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);  // fonctionne ??
        String tarifJson = om.writeValueAsString(tarif);
            LOG.debug("Tarif Member converted in json format = " + NEW_LINE + tarifJson);

   final String query = LCUtil.generateInsertQuery(conn, "tarif_members");
      //      LOG.debug("generated query = " + query);
            ps = conn.prepareStatement(query);
            ps.setNull(1,java.sql.Types.INTEGER);  //autoincrement
            ps.setTimestamp(2,Timestamp.valueOf(tarif.getStartDate()));
            ps.setTimestamp(3,Timestamp.valueOf(tarif.getEndDate()));
            ps.setInt(4,tarif.getTarifMemberIdClub());
            ps.setString(5,tarifJson);
            ps.setTimestamp(6,Timestamp.from(Instant.now()));

            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
            if(row != 0){
                String msg = "Tarif Member Created  = <br/>" + tarif;
     //                   + "<br/>Club = " + club.getIdclub()
      //                  + " / " + club.getClubName();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            }else{
                String msg = "<br/><br/>ERROR insert for tarif : "  + tarif;
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
   } catch (Exception e) {
            String msg = "£££ Exception in CreateTarifMember = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }finally{
         DBConnection.closeQuietly(null, null, null, ps); 
          }
   } // end main//
} // en class