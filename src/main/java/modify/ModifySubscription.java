package modify;

import entite.Subscription;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.DBConnection;
import utils.LCUtil;

public class ModifySubscription{
public boolean modify(final Subscription subscription, final Connection conn) throws SQLException {
   PreparedStatement ps = null;
  try {
            LOG.info("starting modifySubscription");
            LOG.info("with Subscription =  " + subscription.toString());
     
            final String query // à modifier
              = "  UPDATE payments_subscription" +
            "      SET SubscriptionEndDate = ? ," +
            "          SubscriptionTrialCount = ?," +
            "          SubscriptionPaymentReference = ?," +
            "          SubscriptionCommunication = ?" +
            "      WHERE" +
            "          SubscriptionIdPlayer=?";
            
    //          LOG.info(" new end date = " + d);
             
            ps = conn.prepareStatement(query);
            //endDate format LocalDate
            java.sql.Timestamp ts = Timestamp.valueOf(subscription.getEndDate().atStartOfDay());
              LOG.info("new endDate inserted in DB = " + ts);
            ps.setTimestamp(1, ts); // new endDate
    //          LOG.info(" new trial count = " + subscription.getTrialCount());
            if(subscription.getSubCode().equals("TRIAL")){
                Short s = subscription.getTrialCount();
     //           LOG.info("String s trialcount = " + s);
                subscription.setTrialCount(++s);  // attention ++ doit se trouver avant !!!
                LOG.info("This is a TRIAL, new count = " + subscription.getTrialCount());
              }
            ps.setInt(2, subscription.getTrialCount()); // trial count
            ps.setString(3, subscription.getPaymentReference()); // new 14-10-2018
            ps.setString(4, subscription.getCommunication()); // new 26-01-2019
            ps.setInt(5, subscription.getIdplayer());
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate();
                LOG.info("rows = " + row);
            if (row != 0) {
                  LOG.info("before subscription success msg");
                 String msg =  LCUtil.prepareMessageBean("subscription.success") + subscription.getEndDate()
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