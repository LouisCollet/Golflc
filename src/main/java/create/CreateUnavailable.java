package create;

import entite.ECourseList;
import entite.Round;
import entite.Unavailable;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import javax.inject.Named;
import utils.DBConnection;
import utils.LCUtil;

@Named
public class CreateUnavailable {

    public boolean create(final Unavailable unavailable, Round round, final Connection conn) throws SQLException {
        PreparedStatement ps = null;
        try{
            LOG.info("starting create Unavailable "); 
            LOG.info("with unavailable = " + unavailable.toString());
 // validations           
            if(unavailable.getEndDate().isBefore(unavailable.getStartDate()) ){ 
                String msgerr =  LCUtil.prepareMessageBean("tarif.member.endbeforestart");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
           }
 
   final String query = LCUtil.generateInsertQuery(conn, "unavailable");
            ps = conn.prepareStatement(query);
            ps.setNull(1,java.sql.Types.INTEGER);  //autoincrement
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
             if (row != 0){
               String msg = "Unavailable Created for"
                        + "<br/>Course = " + unavailable.getIdcourse();
                 //       + " / " + club.getClubName();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
   //  vérification si des rounds sont réservés pour la période d'indisponibilité
                List<ECourseList> ecl = new find.FindCancellation().find(unavailable,round,conn);
         // les joueurs sont prévenus par mail
                boolean b = new mail.CancellationMail().dispatch(ecl);
        //  a faire : stocker les infos dans une table
        // qui sera accessible par l'admnistrateur local du club
             return true;
            }else{
                String msg = "<br/><br/>NOT NOT Successful insert for unavailable = " ;
         //               + " " + s;
                LOG.info(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
 
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