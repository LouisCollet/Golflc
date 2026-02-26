package update;

import entite.Blocking;
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
import java.time.LocalDateTime;
import javax.sql.DataSource;
import utils.LCUtil;

/**
 * Service de mise à jour des tentatives de connexion bloquées
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdateBlocking implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public UpdateBlocking() { }

    public boolean update(Blocking blocking) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(" with blocking = " + blocking);

        final String query = """
                UPDATE blocking
                SET BlockingLastAttempt = ?,
                    BlockingAttempts = ?,
                    BlockingRetryTime = ?
                WHERE BlockingPlayerId=?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            LOG.debug("there where attempts = " + blocking.getBlockingAttempts());
            ps.setShort(2, blocking.getBlockingAttempts());
            if (blocking.getBlockingAttempts() > 2) {
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
            } else {
                ps.setTimestamp(3, Timestamp.from(Instant.now()));
                LOG.debug("There are attempts now = " + blocking.getBlockingAttempts());
            }
            ps.setInt(4, blocking.getBlockingPlayerId());
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            if (row != 0) {
                String msg = "Erreur " + blocking.getBlockingAttempts() + " - Après 3 erreurs successives, vous serez bloqué pendant 15 minutes";
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "UNsuccessful result in UPDATE blocking !!!";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
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
        Blocking blocking = new Blocking();
        blocking.setBlockingPlayerId(324713);
        blocking.setBlockingAttempts((short) 3);
        boolean b = new update.UpdateBlocking().update(blocking);
        LOG.debug("from main, result = " + b);
    } // end main
    */

} // end class
