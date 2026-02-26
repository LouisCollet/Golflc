package create;

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
import java.sql.Timestamp;
import java.time.Instant;
import javax.sql.DataSource;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

@ApplicationScoped
public class CreatePaymentSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public CreatePaymentSubscription() { }

    public boolean create(final Subscription subscription) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for subscription  = " + subscription);

        try (Connection conn = dataSource.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "payments_subscription");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setNull(1, java.sql.Types.INTEGER);
                ps.setInt(2, subscription.getIdplayer());
                ps.setTimestamp(3, Timestamp.valueOf(subscription.getStartDate()));
                ps.setTimestamp(4, Timestamp.valueOf(subscription.getEndDate()));
                ps.setInt(5, subscription.getTrialCount());
                ps.setString(6, subscription.getPaymentReference());
                ps.setString(7, subscription.getCommunication());
                ps.setDouble(8, subscription.getSubscriptionAmount());
                ps.setTimestamp(9, Timestamp.from(Instant.now()));
                LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "Subscription created = " + subscription;
                    LOG.debug(msg);
                    return true;
                } else {
                    String msg = "<br/><br/>ERROR in Create for subscription : " + subscription;
                    LOG.debug(msg);
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
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class
