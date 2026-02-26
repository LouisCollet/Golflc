package delete;

import entite.Subscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class DeleteSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public DeleteSubscription() { }

    public boolean delete(final Subscription subscription) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String query = """
                DELETE from payments_subscription
                WHERE SubscriptionIdPlayer = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, subscription.getIdplayer());
            LCUtil.logps(ps);
            int rowDeleted = ps.executeUpdate();
            LOG.debug(methodName + " - deleted Subscription = " + rowDeleted);
            String msg = "There are " + rowDeleted + " Subscription deleted = " + subscription;
            LOG.debug(msg);
            return true;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return false;
        }
    } // end method

    /** @deprecated Use {@link #delete(Subscription)} instead — migrated 2026-02-24 */
    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Subscription subscription = new Subscription();
        subscription.setIdplayer(125896);
        boolean b = new DeleteSubscription().delete(subscription);
        LOG.debug("from main - resultat deleteSubscription = " + b);
    } // end main
    */

} // end class
