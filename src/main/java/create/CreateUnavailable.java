package create;

import entite.Unavailable;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.inject.Named;
import utils.DBConnection;
import utils.LCUtil;

@Named
public class CreateUnavailable {

    public boolean create(final Unavailable unavailable, final Connection conn) throws SQLException {
        PreparedStatement ps = null;
        try{
            LOG.info("starting create Unavailable "); 
            LOG.info("with unavailable = " + unavailable.toString());
    //        LOG.info("for club = " + club.toString());
   //         if(club.getIdclub() == null){
                // for testing purposes
   //             LOG.info("clubid was null, forced to La Tournette");
    //            club.setIdclub(101); // la tournette
   //         }
 // validations           
            if(unavailable.getEndDate().isBefore(unavailable.getStartDate()) ){ 
                String msgerr =  LCUtil.prepareMessageBean("tarif.member.endbeforestart");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
           }
 
   final String query = LCUtil.generateInsertQuery(conn, "unavailable");
      //      LOG.info("generated query = " + query);
            ps = conn.prepareStatement(query);
            ps.setNull(1,java.sql.Types.INTEGER);  //autoincrement
        //    ps.setDate(2,LCUtil.getSqlDate(tarifMember.getMemberStartDate()));
        //    ps.setDate(3,LCUtil.getSqlDate(tarifMember.getMemberEndDate()));
            ps.setInt(2,unavailable.getIdcourse()); 
            java.sql.Timestamp ts = Timestamp.valueOf(unavailable.getStartDate());
            ps.setTimestamp(3,ts);
            ts = Timestamp.valueOf(unavailable.getEndDate());
            ps.setTimestamp(4,ts);
            ps.setString(5,unavailable.getCause());
            ps.setTimestamp(6,LCUtil.getCurrentTimeStamp());

            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
             LOG.info("row  = " + row);
               String msg = "<h1>Unavailable Created for"
                        + "<br/>Course = " + unavailable.getIdcourse();
                 //       + " / " + club.getClubName();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
             return true;
   } catch (Exception e) {
            String msg = "£££ Exception in CreateUnavailable = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }finally{
         DBConnection.closeQuietly(null, null, null, ps); 
          }
   } // end main//
} // en class