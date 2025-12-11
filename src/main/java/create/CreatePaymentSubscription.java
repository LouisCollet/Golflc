package create;

import entite.Subscription;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

public class CreatePaymentSubscription implements interfaces.Log, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

  public boolean create(final Subscription subscription, final Connection conn) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        PreparedStatement ps = null;
  try {
                LOG.debug("...entering " + methodName);
                LOG.debug(" for subscription  = " + subscription);
            final String query = LCUtil.generateInsertQuery(conn, "payments_subscription"); 
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setInt(2, subscription.getIdplayer());
            ps.setTimestamp(3, Timestamp.valueOf(subscription.getStartDate()));
            ps.setTimestamp(4, Timestamp.valueOf(subscription.getEndDate()));
            ps.setInt(5,subscription.getTrialCount()); 
            ps.setString(6,subscription.getPaymentReference());
            ps.setString(7,subscription.getCommunication());
            ps.setDouble(8, subscription.getSubscriptionAmount()); 
            ps.setTimestamp(9, Timestamp.from(Instant.now()));
            LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0){
                String msg = "Subscription created = " + subscription;
                LOG.debug(msg);
            //    LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/><br/>ERROR in Create for subscription : " + subscription;
                LOG.debug(msg);
                showMessageFatal(msg);
                return false;
            }
  }catch (SQLException sqle) {
            String msg = "SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
 } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
 } finally {
            DBConnection.closeQuietly(null, null, null, ps);
        }
    } //end method
} //end Class