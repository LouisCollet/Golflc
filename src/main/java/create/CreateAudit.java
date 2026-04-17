package create;

import entite.Player;
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
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class CreateAudit implements Serializable, interfaces.Log {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateAudit() { }

    /**
     * Insère une ligne dans audit et retourne l'AuditId généré (auto-increment).
     * Retourne 0 en cas d'échec — ne jamais stocker 0 en session.
     */
    public int create(final Player player) throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for player = {}", player);

        try (Connection conn = dao.getConnection()) {
            final String query = sql.SqlFactory.generateInsertQuery(conn, "audit");
            try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setNull(1, java.sql.Types.INTEGER); // auto-increment
                ps.setInt(2, player.getIdplayer());
                ps.setTimestamp(3, Timestamp.from(Instant.now())); // AuditStartDate
                ps.setNull(4, java.sql.Types.TIMESTAMP); // AuditEndDate — NULL until logout
                ps.setTimestamp(5, Timestamp.from(Instant.now())); // ModificationDate
                utils.LCUtil.logps(ps);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int auditId = keys.getInt(1);
                        LOG.debug("-- successful INSERT Audit auditId = {}", auditId);
                        return auditId;
                    }
                }
                LOG.warn("-- INSERT Audit executed but no generated key returned");
                return 0;
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
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        boolean b = new create.CreateAudit().create(player);
        LOG.debug("from main, CreateAudit = {}", b);
    } // end main
    */

} // end class
