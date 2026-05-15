package sql.preparedstatement;

import entite.Subscription;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

public class psCreateUpdatePaymentSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final Subscription subscription) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setNull     (1, Types.INTEGER);                         // payments_subscription PK — auto-increment
            ps.setInt      (2, subscription.getIdplayer());
            ps.setTimestamp(3, Timestamp.valueOf(subscription.getStartDate()));
            ps.setTimestamp(4, Timestamp.valueOf(subscription.getEndDate()));
            ps.setInt      (5, subscription.getTrialCount());
            ps.setString   (6, subscription.getPaymentReference());
            ps.setString   (7, subscription.getCommunication());
            ps.setDouble   (8, subscription.getSubscriptionAmount());
            ps.setTimestamp(9, Timestamp.from(Instant.now()));         // ModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * @param endDate computed by caller (TRIAL: endDate+1day, else: endDate)
     */
    public static PreparedStatement psMapUpdate(
            PreparedStatement ps,
            final Subscription subscription,
            final Timestamp endDate) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setTimestamp(1, endDate);
            ps.setInt      (2, subscription.getTrialCount());
            ps.setString   (3, subscription.getPaymentReference());
            ps.setString   (4, subscription.getCommunication());
            ps.setDouble   (5, subscription.getSubscriptionAmount());
            ps.setInt      (6, subscription.getIdplayer());            // WHERE
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
