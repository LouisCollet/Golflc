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

/**
 * Service de modification d'abonnement (renouvellement, essai)
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdateSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public UpdateSubscription() { }

    public boolean modify(final Subscription subscription) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(" with Subscription = " + subscription);

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
            LOG.debug("This is a TRIAL, new count = " + subscription.getTrialCount());
        } else {
            subscription.setTrialCount((short) 0);
            LOG.debug("This is a MONTH/YEAR, donc TRIAL = " + subscription.getTrialCount());
        }

        if (subscription.getTrialCount() > 5 && LocalDateTime.now().isAfter(subscription.getEndDate())) {
            String msg = LCUtil.prepareMessageBean("subscription.create.toomuchtrials")
                    + " player = " + subscription.getIdplayer()
                    + " , trial  = <h1>" + subscription.getTrialCount() + "</h1>";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw new Exception(msg);
        }

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            if (subscription.getSubCode().equals("TRIAL")) {
                ps.setTimestamp(1, Timestamp.valueOf(subscription.getEndDate().plusDays(1)));
            } else {
                ps.setTimestamp(1, Timestamp.valueOf(subscription.getEndDate()));
            }
            ps.setInt(2, subscription.getTrialCount());
            ps.setString(3, subscription.getPaymentReference());
            ps.setString(4, subscription.getCommunication());
            ps.setDouble(5, subscription.getSubscriptionAmount());
            ps.setInt(6, subscription.getIdplayer());
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            if (row != 0) {
                String msg = LCUtil.prepareMessageBean("subscription.success") + subscription.getEndDate().format(ZDF_DAY);
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "NOT NOT Successful update, row = 0 player = " + subscription.getIdplayer();
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                throw new Exception(msg);
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
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class
