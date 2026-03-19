package update;

import entite.Audit;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import utils.LCUtil;

/**
 * Service de clôture d'audit (enregistrement de la date de fin)
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdateAudit implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public UpdateAudit() { }

    public boolean stop(Audit audit) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(" with audit = " + audit);

        final String query = """
                UPDATE audit
                SET AuditEndDate=?
                WHERE AuditId=?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setInt(2, audit.getIdaudit());
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            if (row != 0) {
                String msg = "Successful result in UpdateAudit.stop at " + LocalDateTime.now().format(ZDF_TIME);
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "UNsuccessful result in UPDATE audit !!!";
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
        Audit audit = new Audit();
        audit.setAuditPlayerId(324713);
        audit.setIdaudit(8538);
        boolean b = new update.UpdateAudit().stop(audit);
        LOG.debug("from main, result = " + b);
    } // end main
    */

} // end class
