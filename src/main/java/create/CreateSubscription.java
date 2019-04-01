package create;

import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import utils.DBConnection;
import utils.LCUtil;

// encore à faire !! dans le futur, l'appeler à partir de create player n'est pas utilisé !!!
// called from FindSubscriptionStatus
public class CreateSubscription implements interfaces.Log, interfaces.GolfInterface{
    public boolean create(final Player player, final Connection conn) throws SQLException{
        PreparedStatement ps = null;
        try {
            LOG.info("...entering createSubscription");
            LOG.info("player  = " + player.getIdplayer());
            //LOG.info("club City  = " + club.getClubCity() );
       //     LOG.info("subscription code  = " + subscription.getSubCode());

            final String query = LCUtil.generateInsertQuery(conn, "payments_subscription"); 
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setInt(2, player.getIdplayer());
                LOG.info("LocalDate now() = " + LocalDate.now());
            java.sql.Timestamp ts = Timestamp.valueOf(LocalDate.now().minusDays(1).atStartOfDay());  // day before today
                LOG.info("new startdate and endDate inserted in DB = " + ts);
      //          LocalDate localDate = LocalDate.of(2016,8,19);    alternative fixed date
            ps.setTimestamp(3, ts);  // start subscription date
            ps.setTimestamp(4, ts);  // end subscription date
            ps.setInt(5, 0);  // trial count
            ps.setString(6,""); // new 14-10-2018 paymentReference
            ps.setString(7,""); // new 14-10-2018 paymentReference
            ps.setTimestamp(8, LCUtil.getCurrentTimeStamp());
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
            if (row != 0) 
            {
     //           int key = LCUtil.generatedKey(conn);
     //               LOG.info("Course created = " + key);
     //           course.setIdcourse(key);
//                tee.setNextTee(true); // affiche le bouton next(Tee) bas ecran à droite
                String msg = "Initial Subscription created for player = " + player.getIdplayer();
                LOG.info(msg);
       //         LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/><br/>ERROR insert for subscription : "  + player.getIdplayer();
                LOG.info(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
        }catch (SQLException sqle) {
            //LOG.error("-- SQLException in Insert Course " + sqle.toString());
            String msg = "SQLException in createsubscription = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in createsubscription = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
        }
    } //end method

} //end Class