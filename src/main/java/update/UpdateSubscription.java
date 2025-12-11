package update;

import entite.Subscription;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateSubscription{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public boolean modify(final Subscription subscription, final Connection conn) throws SQLException {
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
   PreparedStatement ps = null;
  try {
            LOG.debug(" ... entering " + methodName);
            LOG.debug("with Subscription =  " + subscription);
          final String query = """
              UPDATE payments_subscription
                  SET SubscriptionEndDate = ? ,
                      SubscriptionTrialCount = ?,
                      SubscriptionPaymentReference = ?,
                      SubscriptionCommunication = ?,
                      SubscriptionAmount = ?
                  WHERE
                      SubscriptionIdPlayer=?
             """;
            ps = conn.prepareStatement(query);
            if(subscription.getSubCode().equals("TRIAL")){    // new endDate
                ps.setTimestamp(1, Timestamp.valueOf(subscription.getEndDate().plusDays(1)));
            }else{
                ps.setTimestamp(1, Timestamp.valueOf(subscription.getEndDate())); 
            }
    //          LOG.debug(" new trial count = " + subscription.getTrialCount());
    
            if(subscription.getSubCode().equals("TRIAL")){
                Short s = subscription.getTrialCount();
     //           LOG.debug("String s trialcount = " + s);
                subscription.setTrialCount(++s);  // attention ++ doit se trouver avant !!!
                   LOG.debug("This is a TRIAL, new count = " + subscription.getTrialCount());
  //                 subscription.setCommunication(utils.LCUtil.prepareMessageBean("subscription.trial"));
             }else{               // monthly or yearly
                subscription.setTrialCount((short)0);
                   LOG.debug("This is a MONTH/YEAR, donc TRIAL = " + subscription.getTrialCount());
            }
            
          if(subscription.getTrialCount() > 5 && LocalDateTime.now().isAfter(subscription.getEndDate())){  // new 22-02-2019 
             String msg = LCUtil.prepareMessageBean("subscription.create.toomuchtrials")
        //    String msg = "subscription Trial > 5 - Use Subscription Month of Year instead !!! "
                  + " player = " + subscription.getIdplayer()
                  + " , trial  = <h1>" + subscription.getTrialCount() + "</h1>"
                  ;
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
             LOG.debug("returned to subscription.xhtml");
             throw new Exception(msg);
      //       return false;
          }
            ps.setInt(2, subscription.getTrialCount()); // trial count
            ps.setString(3, subscription.getPaymentReference());
            ps.setString(4, subscription.getCommunication());
            ps.setDouble(5, subscription.getSubscriptionAmount()); // new 22-02-2024
            ps.setInt(6, subscription.getIdplayer());
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate();
 //               LOG.debug("rows = " + row);
            if (row != 0) {
 //                 LOG.debug("before subscription success msg");
                 String msg =  LCUtil.prepareMessageBean("subscription.success") + subscription.getEndDate().format(ZDF_DAY);
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
             }else{
                   String msg = "NOT NOT Successful update, row = 0 "
                           + " player = " + subscription.getIdplayer();
                   LOG.debug(msg);
                   LCUtil.showMessageFatal(msg);
                   throw new Exception(msg);
             //      return false;
                 } //end if
  }catch (SQLException sqle) {
            String msg = "££££ SQLException in " + methodName + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode()
                    + " player = " + subscription.getIdplayer();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps);
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
 } //end method
} //end class