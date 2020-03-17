package create;

import entite.Greenfee;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;

// encore à affiner !!
public class CreateGreenfee implements interfaces.Log, interfaces.GolfInterface{
    
    // faire la loop ici ?
    
    
    
 public boolean create(final Player player, final Greenfee greenfee, final Connection conn) throws SQLException{
      PreparedStatement ps = null;
 try {
                LOG.info("...entering createGreenfee");
                LOG.info("Greenfee  = " + greenfee.toString());
                LOG.info("Player  = " + player.toString());
            final String query = LCUtil.generateInsertQuery(conn,"payments_greenfee"); 
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER); // AUTO-INCREMENT
            ps.setInt(2, greenfee.getIdclub());
       //     ps.setInt(3, greenfee.getIdplayer());
            ps.setInt(3, player.getIdplayer());
            ps.setInt(4, greenfee.getIdround());
   //             Timestamp ts = Timestamp.valueOf(greenfee.getRoundDate());
            ps.setTimestamp(5,Timestamp.valueOf(greenfee.getRoundDate()));
            ps.setString(6, greenfee.getPaymentReference()); 
            ps.setString(7, greenfee.getCommunication()); 
            ps.setString(8, greenfee.getItems());
            ps.setString(9, greenfee.getStatus());
            ps.setDouble(10, greenfee.getPrice());  // new 19/02/2019
            ps.setTimestamp(11, Timestamp.from(Instant.now()));
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0){
                String msg = "Cotisation created = " + greenfee.getPrice(); 
                LOG.info(msg);
         //       LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/>ERROR insert for Greenfee : " ;
                LOG.info(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
        }catch (SQLException sqle) {
            String msg = "";
            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 ){
                 msg = LCUtil.prepareMessageBean("create.greenfee.duplicate")
                 + "player = " + player.getIdplayer() + " club = " + greenfee.getIdclub();
            }else{
                  msg = "SQLException in createGreenfee = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            }
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in createGreenfee = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
        }
   //     return true;
    } //end method
} //end Class