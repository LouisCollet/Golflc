package modify;

import entite.Subscription;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.DBConnection;
import utils.LCUtil;

public class ModifySubscription implements interfaces.Log
{
public boolean modifySubscription(final Subscription subscription, final Connection conn) throws SQLException
 {
        PreparedStatement ps = null;
  try {
            LOG.info("starting modifySubscription");
        //    LOG.info("Player ID = " + player.getIdplayer());
            LOG.info("Subscription =  " + subscription.toString());
          //  LOG.info("Subscription endDate =  " + subscription.getEndDate());
     //       LOG.info("Player ID = " + player.getIdplayer() );
     
            final String query // à modifier
              = "  UPDATE subscription" +
            "      SET subscription.SubscriptionEndDate = ? ," +
            "          subscription.SubscriptionTrialCount = ?," +
            "          subscription.SubscriptionPaymentReference = ?" +
            "      WHERE" +
            "          subscription.subscription_player_id = ?";
            
    //          LOG.info(" new end date = " + d);
              LOG.info(" new trial count = " + subscription.getTrialCount());
            ps = conn.prepareStatement(query);
            java.sql.Timestamp ts = Timestamp.valueOf(subscription.getEndDate().atStartOfDay());
              LOG.info("new endDate inserted in DB = " + ts);
            ps.setTimestamp(1, ts); // new endDate
            ps.setInt(2, subscription.getTrialCount()); // trial count
            ps.setString(3, subscription.getPaymentReference()); // new 14-10-22018
            ps.setInt(4, subscription.getIdplayer());
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate();
            if (row != 0) {
         //          LOG.info("before subscription success msg");
                 String msg =  "<h1> " + LCUtil.prepareMessageBean("subscription.success") + subscription.getEndDate()
                //         + " , new end date = " + d.format(ZDF_DAY) + "</h1>"
                           ;
                    LOG.info(msg);
                 LCUtil.showMessageInfo(msg);
                    return true;
                 }else{
                   String msg = "NOT NOT Successful update, row = 0 "
    //                            + " hole  = " + (i + 1)
                           + " player = " + subscription.getIdplayer();
                   LOG.info(msg);
                   LCUtil.showMessageFatal(msg);
                   return false;
                 } //end if
        }catch (SQLException sqle) {
            String msg = "££££ SQLException in ModifySubscription = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode()
                    + " player = " + subscription.getIdplayer();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in ModifySubscription = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps);
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }

 } //end method
 
} //end class