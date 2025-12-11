package create;

import entite.Cotisation;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;

// encore à faire !! dans le futur, l'appeler à partir de create player
public class CreatePaymentCotisation implements interfaces.GolfInterface{
    
 public boolean create(final Cotisation cotisation, final Connection conn) throws SQLException, InstantiationException{
      PreparedStatement ps = null;
 try {
                LOG.debug("...entering createPaymentCotisation with cotisation = " + cotisation);
            final String query = LCUtil.generateInsertQuery(conn, "payments_cotisation"); 
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER); // AUTO-INCREMENT
            ps.setInt(2, cotisation.getIdclub());
            ps.setInt(3, cotisation.getIdplayer());
            ps.setTimestamp(4,Timestamp.valueOf(cotisation.getCotisationStartDate()));
            ps.setTimestamp(5,Timestamp.valueOf(cotisation.getCotisationEndDate()));
            ps.setString(6, cotisation.getPaymentReference()); 
            ps.setString(7, cotisation.getCommunication()); 
            ps.setString(8, cotisation.getItems());
            ps.setString(9, cotisation.getStatus());
            ps.setDouble(10, cotisation.getPrice());  // new 14/04/2021
            ps.setTimestamp(11, Timestamp.from(Instant.now()));
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if(row != 0){
                String msg = "Cotisation payement created = </h1>" + cotisation.getPrice(); 
                LOG.debug(msg);
         //       LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/><br/>ERROR payment for Cotisation : " ;
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
    }catch (SQLException sqle) {
            String msg = "";
            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 ){
                 msg = LCUtil.prepareMessageBean("create.cotisation.duplicate") + NEW_LINE + cotisation
                 + " player = " + cotisation.getIdplayer() + " club = " + cotisation.getIdclub();
            }else{
                 msg = "SQLException in createPaymentCotisation = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            }
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
    } catch (Exception e) {
            String msg = "£££ Exception in createPaymentCotisation = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
    } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
        }
} //end method
} //end Class