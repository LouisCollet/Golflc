package find;

import entite.Subscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class FindSubscriptionOverlapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private find.OverlapChecker overlapChecker;

    public FindSubscriptionOverlapping() { }

    public boolean find(final Subscription subscription) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for subscription = {}", subscription);
        try {
            return overlapChecker.check(
                    "[SUBSCRIPTION]",
                    subscription.getStartDate(),
                    subscription.getEndDate(),
                    """
                    SELECT * FROM payments_subscription
                    WHERE SubscriptionIdPlayer = ?
                    """,
                    ps -> ps.setInt(1, subscription.getIdplayer()),
                    new rowmappers.SubscriptionRowMapper(),
                    Subscription::getStartDate,
                    Subscription::getEndDate);
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
