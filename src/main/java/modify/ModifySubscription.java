package modify;

import entite.Subscription;
import static interfaces.GolfInterface.ZDF_DAY;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
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
            "          subscription.SubscriptionTrialCount = ?" +
            "      WHERE" +
            "          subscription.subscription_player_id = ?";
            int count = 0;
            LocalDate d = null; //LocalDate.now(); // mod 02-08-2018
            
            switch(subscription.getSubCode())
                {
                    case "T":  // trial one day
                            LOG.info("getSubCode(): T");
                        d = LocalDate.now().plusDays(1);
                        count = subscription.getTrialCount() + 1;
                        break;
                    case "M":
                            LOG.info("getSubCode(): M");
                        d = subscription.getEndDate().plusMonths(1); 
                        count = 0;
                        break;
                    case "Y":
                            LOG.info("getSubCode(): Y");
                        d = subscription.getEndDate().plusYears(1); 
                        count = 0;
                        break;
                    default:
                            LOG.info(": getSubCode() UNKNOWN" + subscription.getSubCode() );
                } //end switch
            
              LOG.info(" new end date = " + d);
              LOG.info(" new trial count = " + count);
            ps = conn.prepareStatement(query);
            java.sql.Timestamp ts = Timestamp.valueOf(d.atStartOfDay());
              LOG.info("new endDate inserted in DB = " + ts);
            ps.setTimestamp(1, ts); // new endDate
            ps.setInt(2, count); // trial count
            ps.setInt(3, subscription.getIdplayer());
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate();
               if (row != 0) {
         //          LOG.info("before subscription success msg");
                 String msg =  "<h1> "+ LCUtil.prepareMessageBean("subscription.success")
                           + subscription.getIdplayer()
                           + " , new end date = " + d.format(ZDF_DAY) + "</h1>"
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
       } //end try
        catch (NullPointerException npe) {
            String msg = "£££ NullPointerException in ModifySubscription "  + npe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (IndexOutOfBoundsException iobe) {
            String msg = "£££ IndexOutOfBoundsException in Modify Subscrition = " + iobe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }catch (SQLException sqle) {
            String msg = "££££ SQLException in ModifySubscription = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode()
                    + " player = " + subscription.getIdplayer();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in ModifySubscription= " + nfe.getMessage();
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