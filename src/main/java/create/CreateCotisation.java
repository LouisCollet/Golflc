package create;

import entite.Cotisation;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.DBConnection;
import utils.LCUtil;

// encore à faire !! dans le futur, l'appeler à partir de create player
public class CreateCotisation implements interfaces.Log, interfaces.GolfInterface{
    
 public boolean create(final Cotisation cotisation, final Connection conn) throws SQLException{
      PreparedStatement ps = null;
 try {
                LOG.info("...entering createCotisation");
                LOG.info("cotisation  = " + cotisation.toString());
            final String query = LCUtil.generateInsertQuery(conn, "cotisation"); 
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER); // AUTO-INCREMENT
            ps.setInt(2, cotisation.getIdclub());
            ps.setInt(3, cotisation.getIdplayer());
     //      followings are java LocalDate and MySQL DATE
        //    ps.setDate(4, java.sql.Date.valueOf(cotisation.getStartDate()));
        //    ps.setDate(5, java.sql.Date.valueOf(cotisation.getEndDate()));
                Timestamp ts = Timestamp.valueOf(cotisation.getStartDate());
            ps.setTimestamp(4,ts);
                ts = Timestamp.valueOf(cotisation.getEndDate());
            ps.setTimestamp(5,ts);
            ps.setString(6, cotisation.getPaymentReference()); 
            ps.setString(7, cotisation.getCommunication()); 
            ps.setString(8, cotisation.getItems());
            ps.setString(9, cotisation.getStatus());
            ps.setTimestamp(10, LCUtil.getCurrentTimeStamp());
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0){
     //           int key = LCUtil.generatedKey(conn);
     //               LOG.info("Course created = " + key);
     //           course.setIdcourse(key);
//                tee.setNextTee(true); // affiche le bouton next(Tee) bas ecran à droite
                String msg = "<br/><h1>Cotisation created = </h1>" + cotisation.getPrice(); 
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
                 + "player = " + cotisation.getIdplayer() + " club = " + cotisation.getIdclub();
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