package find;

import entite.LoggingUser;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
public class FindLoggingUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindLoggingUser() { }

    public int find(LoggingUser logging) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with Logging " + logging);

        final String query = """
            SELECT count(*)
            FROM logging_user
            WHERE LoggingIdPlayer = ?
            AND LoggingIdRound = ?
            AND LoggingType = ?
         """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, logging.getLoggingIdPlayer());
            ps.setInt(2, logging.getLoggingIdRound());
            ps.setString(3, logging.getLoggingType().toUpperCase());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int i = 0;
                while (rs.next()) {
                    i = rs.getInt(1);
                }
                LOG.debug("found i = " + i);
                return i;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return 0;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return 0;
        }
    } // end method

/*
void main() throws SQLException, Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    // tests locaux
} // end main
*/

} // end class
