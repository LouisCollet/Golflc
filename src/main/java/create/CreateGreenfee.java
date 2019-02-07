package create;

import entite.Greenfee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.DBConnection;
import utils.LCUtil;

// encore à affiner !!
public class CreateGreenfee implements interfaces.Log, interfaces.GolfInterface{
    
 public boolean create(final Greenfee greenfee, final Connection conn) throws SQLException{
      PreparedStatement ps = null;
 try {
                LOG.info("...entering createGreenfee");
                LOG.info("Greenfee  = " + greenfee.toString());
            final String query = LCUtil.generateInsertQuery(conn,"greenfee"); 
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER); // AUTO-INCREMENT
            ps.setInt(2, greenfee.getIdclub());
            ps.setInt(3, greenfee.getIdplayer());
     //      followings are java LocalDate and MySQL DATE
        //    ps.setDate(4, java.sql.Date.valueOf(cotisation.getStartDate()));
        //    ps.setDate(5, java.sql.Date.valueOf(cotisation.getEndDate()));
                Timestamp ts = Timestamp.valueOf(greenfee.getRoundDate());
           ps.setTimestamp(4,ts);
 ///               ts = Timestamp.valueOf(greenfee.getEndDate());
 ///           ps.setTimestamp(5,ts);
            ps.setString(5, greenfee.getPaymentReference()); 
            ps.setString(6, greenfee.getCommunication()); 
            ps.setString(7, greenfee.getItems());
            ps.setString(8, greenfee.getStatus());
            ps.setTimestamp(9, LCUtil.getCurrentTimeStamp());
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0){
     //           int key = LCUtil.generatedKey(conn);
     //               LOG.info("Course created = " + key);
     //           course.setIdcourse(key);
//                tee.setNextTee(true); // affiche le bouton next(Tee) bas ecran à droite
                String msg = "<br/><h1>Cotisation created = </h1>" + greenfee.getPrice(); 
                LOG.info(msg);
         //       LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/><br/>ERROR insert for subscription : " ;
                LOG.info(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
        }catch (SQLException sqle) {
            String msg = "";
            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 )
            {
                 msg = LCUtil.prepareMessageBean("create.cotisation.duplicate")
                 + "player = " + greenfee.getIdplayer() + " club = " + greenfee.getIdclub();
            }else{
                  msg = "SQLException in createCotisation = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            }
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in createCotisation = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
        }
   //     return true;
    } //end method
} //end Class