package sql.preparedstatement;

import entite.Subscription;
import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class psUpdateSubscription implements Serializable, interfaces.Log, interfaces.GolfInterface {

    /**
     * @param endDate computed by caller (TRIAL: endDate+1day, else: endDate)
     */
    public static PreparedStatement psMapUpdate(
            PreparedStatement ps,
            final Subscription subscription,
            final Timestamp endDate) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setTimestamp(1, endDate);
            ps.setInt      (2, subscription.getTrialCount());
            ps.setString   (3, subscription.getPaymentReference());
            ps.setString   (4, subscription.getCommunication());
            ps.setDouble   (5, subscription.getSubscriptionAmount());
            ps.setInt      (6, subscription.getIdplayer());
            sql.PrintWarnings.print(ps.getWarnings(), methodName);// TarifModificationDate
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
