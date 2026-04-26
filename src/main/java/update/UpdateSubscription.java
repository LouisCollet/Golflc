package update;

import entite.Subscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import utils.LCUtil;

@ApplicationScoped
public class UpdateSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public UpdateSubscription() { }

    public boolean modify(final Subscription subscription) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with Subscription = {}", subscription);

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

        if (subscription.getSubCode().equals("TRIAL")) {
            Short s = subscription.getTrialCount();
            subscription.setTrialCount(++s);
            LOG.debug("TRIAL — new count = {}", subscription.getTrialCount());
        } else {
            subscription.setTrialCount((short) 0);
            LOG.debug("MONTH/YEAR — trial count reset to {}", subscription.getTrialCount());
        }

        if (subscription.getTrialCount() > 5 && LocalDateTime.now().isAfter(subscription.getEndDate())) {
            String msg = LCUtil.prepareMessageBean("subscription.create.toomuchtrials")
                    + " player = " + subscription.getIdplayer()
                    + " trial = " + subscription.getTrialCount();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw new Exception(msg);
        }

        Timestamp endDate = subscription.getSubCode().equals("TRIAL")
                ? Timestamp.valueOf(subscription.getEndDate().plusDays(1))
                : Timestamp.valueOf(subscription.getEndDate());

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            sql.preparedstatement.psUpdateSubscription.psMapUpdate(ps, subscription, endDate);
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            if (row != 0) {
                String msg = LCUtil.prepareMessageBean("subscription.success") + subscription.getEndDate().format(ZDF_DAY);
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                LOG.error("no row updated for player = {}", subscription.getIdplayer());
                LCUtil.showMessageFatal("Update subscription failed for player = " + subscription.getIdplayer());
                throw new Exception("no row updated for player = " + subscription.getIdplayer());
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
