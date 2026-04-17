package read;

import entite.LoggingUser;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class ReadLoggingUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadLoggingUser() { }

    public LoggingUser read(final LoggingUser logging) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with logging = {}", logging);

        final String query = """
                SELECT *
                FROM logging_user
                WHERE LoggingIdPlayer = ?
                AND LoggingIdRound = ?
                AND LoggingType = ?
                """;

        LoggingUser result = dao.querySingle(query, rs -> LoggingUser.map(rs),
                logging.getLoggingIdPlayer(), logging.getLoggingIdRound(), logging.getLoggingType().toUpperCase());
        if (result == null) {
            LOG.debug("- {}", utils.LCUtil.prepareMessageBean("logging.notfound"));
        } else {
            LOG.debug("- {}", utils.LCUtil.prepareMessageBean("logging.found") + result);
        }
        return result;
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // LoggingUser logging = new LoggingUser();
        // logging.setLoggingIdPlayer(324713);
        // logging.setLoggingIdRound(688);
        // logging.setLoggingType("H");
        // var v = new ReadLoggingUser().read(logging);
        // LOG.debug(" from main : LoggingUser = {}", v);
    } // end main
    */

} // end class
