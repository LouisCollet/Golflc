package update;

import entite.LoggingUser;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Service de mise à jour des calculs de handicap (logging_user)
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdateLoggingUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public UpdateLoggingUser() { }

    public boolean update(final LoggingUser logging) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(" for logging = " + logging);

        final String query = """
                UPDATE logging_user
                SET LoggingCalculations = ?
                WHERE LoggingIdPlayer = ?
                AND LoggingIdRound = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, logging.getLoggingCalculations());
            ps.setInt(2, logging.getLoggingIdPlayer());
            ps.setInt(3, logging.getLoggingIdRound());
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            if (row != 0) {
                String msg = "successful UPDATE LoggingUser = " + NEW_LINE + logging;
                LOG.info(msg);
                showMessageInfo(msg);
                return true;
            } else {
                String msg = "UNsuccessful UPDATE LoggingUser = " + methodName + NEW_LINE + logging;
                LOG.error(msg);
                showMessageFatal(msg);
                return false;
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
