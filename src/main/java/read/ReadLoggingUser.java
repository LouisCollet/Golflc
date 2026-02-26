package read;

import entite.LoggingUser;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

@ApplicationScoped
public class ReadLoggingUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ReadLoggingUser() { }

    public LoggingUser read(final LoggingUser logging) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - with logging = " + logging);

        final String query = """
                SELECT *
                FROM logging_user
                WHERE LoggingIdPlayer = ?
                AND LoggingIdRound = ?
                AND LoggingType = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, logging.getLoggingIdPlayer());
            ps.setInt(2, logging.getLoggingIdRound());
            ps.setString(3, logging.getLoggingType().toUpperCase());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                LoggingUser result = logging;
                while (rs.next()) {
                    result = LoggingUser.map(rs);
                }
                if (result == null) {
                    LOG.debug(methodName + " - " + utils.LCUtil.prepareMessageBean("logging.notfound"));
                } else {
                    LOG.debug(methodName + " - " + utils.LCUtil.prepareMessageBean("logging.found") + result);
                }
                return result;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // LoggingUser logging = new LoggingUser();
        // logging.setLoggingIdPlayer(324713);
        // logging.setLoggingIdRound(688);
        // logging.setLoggingType("H");
        // var v = new ReadLoggingUser().read(logging);
        // LOG.debug(" from main : LoggingUser = " + v);
    } // end main
    */

} // end class
