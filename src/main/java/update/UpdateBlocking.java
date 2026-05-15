package update;

import entite.Blocking;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

/**
 * Service de mise à jour des tentatives de connexion bloquées
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdateBlocking implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public UpdateBlocking() { }

    public boolean update(Blocking blocking) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(" with blocking = {}", blocking);

        final String query = """
                UPDATE blocking
                SET BlockingLastAttempt = ?,
                    BlockingAttempts = ?,
                    BlockingRetryTime = ?
                WHERE BlockingPlayerId=?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            LOG.debug("there where attempts = {}", blocking.getBlockingAttempts());
            sql.preparedstatement.psCreateUpdateBlocking.psMapUpdate(ps, blocking);

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
        LOG.debug("entering {}", methodName);
        Blocking blocking = new Blocking();
        blocking.setBlockingPlayerId(324713);
        blocking.setBlockingAttempts((short) 3);
        boolean b = new update.UpdateBlocking().update(blocking);
        LOG.debug("from main, result = {}", b);
    } // end main
    */

} // end class
