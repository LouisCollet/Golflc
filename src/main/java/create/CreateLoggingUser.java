package create;

import entite.LoggingUser;
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
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class CreateLoggingUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public CreateLoggingUser() { }

    public boolean create(final LoggingUser logging) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with LoggingUser  = " + logging);

        try (Connection conn = dataSource.getConnection()) {
            final String query = utils.LCUtil.generateInsertQuery(conn, "logging_user");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, logging.getLoggingIdPlayer());
                ps.setInt(2, logging.getLoggingIdRound());
                ps.setString(3, logging.getLoggingType());
                ps.setString(4, logging.getLoggingCalculations());
                ps.setTimestamp(5, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "LoggingUser Created = " + logging;
                    LOG.info(msg);
                    showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "<br/>ERROR insert LoggingUser : " + logging;
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
        LoggingUser logging = new LoggingUser();
        logging.setLoggingIdPlayer(324713);
        logging.setLoggingIdRound(688);
        logging.setLoggingType("H");
        logging.setLoggingCalculations("these are the calculations for 324713, 388, Handicap");
        var v = new CreateLoggingUser().create(logging);
        LOG.debug(" from main : LoggingUser = " + v);
    } // end main
    */

} // end class
