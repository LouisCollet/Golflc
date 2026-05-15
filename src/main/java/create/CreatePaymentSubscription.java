package create;

import entite.Subscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static interfaces.GolfInterface.ZDF_DAY;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

@ApplicationScoped
public class CreatePaymentSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;
    @Inject private find.FindSubscriptionOverlapping findSubscriptionOverlapping;

    public CreatePaymentSubscription() { }

    public boolean create(final Subscription subscription) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for subscription  = {}", subscription);

        if (findSubscriptionOverlapping.find(subscription)) {
            String period = ZDF_DAY.format(subscription.getStartDate())
                    + " - " + ZDF_DAY.format(subscription.getEndDate());
            String label = LCUtil.prepareMessageBean("tarif.overlapping");
            String msg = "[SUBSCRIPTION] " + (label != null ? label : "Subscription overlap:") + " " + period;
            LOG.warn("overlap rejected player={} period={}", subscription.getIdplayer(), period);
            throw new Exception(msg);
        }

        try (Connection conn = dao.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "payments_subscription");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateUpdatePaymentSubscription.psMapCreate(ps, subscription);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "Subscription created = " + subscription;
                    LOG.debug(msg);
                    return true;
                } else {
                    String msg = "[SUBSCRIPTION] ERROR in Create for subscription : " + subscription;
                    LOG.error(msg);
                    showMessageFatal(msg);
                    return false;
                }
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
