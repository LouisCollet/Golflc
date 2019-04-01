package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import entite.TarifMember;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import javax.inject.Named;
import utils.DBConnection;
import utils.LCUtil;

@Named
public class CreateTarifMember {

    public boolean create(final TarifMember tarifMember, final Club club, final Connection conn) throws SQLException {
        PreparedStatement ps = null;
        try{
            LOG.info("starting create TarifMember "); 
            LOG.info("with tarif = " + tarifMember.toString());
            LOG.info("for club = " + club.toString());
            if(club.getIdclub() == null){
                // for testing purposes
                LOG.info("clubid was null, forced to La Tournette");
                club.setIdclub(101); // la tournette
            }
 // validations           
            if(tarifMember.getMemberEndDate().isBefore(tarifMember.getMemberStartDate()) ){ 
                String msgerr =  LCUtil.prepareMessageBean("tarif.member.endbeforestart");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
           }
          YearMonth yearmonth = YearMonth.from(tarifMember.getMemberStartDate());
         LOG.info("yearmonth = " + yearmonth);
           Month month = Month.from(tarifMember.getMemberStartDate());
          LOG.info("month = " + month);
     //       LocalDateTime start = tarifMember.getMemberStartDate().withDayOfMonth(1);
     //        LOG.info("start = " +start);
     //       LocalDateTime end = tarifMember.getMemberEndDate().withDayOfMonth(31);with(TemporalAdjusters.lastDayOfMonth());
     //       LOG.info("end = " + end);
            LocalDateTime lastOfMonth = tarifMember.getMemberEndDate().with(TemporalAdjusters.lastDayOfMonth());
            LOG.info("lastOfMonth = " + lastOfMonth);
            
            LOG.info("getDayOfMonth = Start " + tarifMember.getMemberStartDate().getDayOfMonth());
            LOG.info("getMonthValue Start = " + tarifMember.getMemberStartDate().getMonthValue());
            LOG.info("getMonthValue End = " + tarifMember.getMemberEndDate().getMonthValue());
            
            LocalDate lastDayofMonthYear = YearMonth.of(2019,12).atEndOfMonth();
             LOG.info("lastDayofMonthYear = " + lastDayofMonthYear);
            
// ce qui suit est ok
             LOG.info("getYear End = " + tarifMember.getMemberEndDate().getYear());
             LOG.info("getMonth End = " + tarifMember.getMemberEndDate().getMonthValue());
             LOG.info("getDayMonth End = " + tarifMember.getMemberEndDate().getDayOfMonth());
             
             LOG.info("getYear Start = " + tarifMember.getMemberStartDate().getYear());
             LOG.info("getMonth Start = " + tarifMember.getMemberStartDate().getMonthValue());
             LOG.info("getDayMonth Start = " + tarifMember.getMemberStartDate().getDayOfMonth());
             
           
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());  // new 18/01/2019 traiter LocalDateTime format aussi dans finTarifMembersData
    	om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
        om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);  // fonctionne ??
        tarifMember.RemoveNull(); // remove null from arrays
        String json = om.writeValueAsString(tarifMember);
            LOG.info("Tarif Member converted in json format = " + json);

   final String query = LCUtil.generateInsertQuery(conn, "tarif_members");
      //      LOG.info("generated query = " + query);
            ps = conn.prepareStatement(query);
            ps.setNull(1,java.sql.Types.INTEGER);  //autoincrement
        //    ps.setDate(2,LCUtil.getSqlDate(tarifMember.getMemberStartDate()));
        //    ps.setDate(3,LCUtil.getSqlDate(tarifMember.getMemberEndDate()));
            java.sql.Timestamp ts = Timestamp.valueOf(tarifMember.getMemberStartDate());
            ps.setTimestamp(2,ts);
            ts = Timestamp.valueOf(tarifMember.getMemberEndDate());
            ps.setTimestamp(3,ts);
            ps.setInt(4,club.getIdclub()); 
            ps.setString(5,json);
            ps.setTimestamp(6,LCUtil.getCurrentTimeStamp());

            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
             LOG.info("row  = " + row);
               String msg = "Tarif Member Created for"
                        + "<br/>Club = " + club.getIdclub()
                        + " / " + club.getClubName();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
             return true;
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