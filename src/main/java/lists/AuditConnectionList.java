package lists;

import entite.Audit;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

/**
 * List of audit connections for the last 30 days, sorted by duration descending.
 * No cache — always fresh data.
 * Player names are resolved by the controller via PlayerManager.
 */
@ApplicationScoped
public class AuditConnectionList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public AuditConnectionList() { } // end constructor

    public List<Audit> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        // GROUP BY tronqué à la minute : les doublons créés par double-clic ont des secondes
        // différentes mais la même minute — DATE_FORMAT('%Y-%m-%d %H:%i') les regroupe.
        // MIN(auditStartDate) / MIN(auditEndDate) : on garde la première occurrence.
        final String query = """
            SELECT MIN(a.AuditId) AS AuditId,
                   a.AuditPlayerId,
                   MIN(a.auditStartDate) AS auditStartDate,
                   MIN(a.auditEndDate)   AS auditEndDate,
                   (SELECT CONCAT(p.PlayerLastName, ', ', p.PlayerFirstName)
                    FROM player p WHERE p.idplayer = a.AuditPlayerId LIMIT 1) AS playerName
            FROM audit a
            WHERE a.auditStartDate >= ?
            GROUP BY a.AuditPlayerId,
                     DATE_FORMAT(a.auditStartDate, '%Y-%m-%d %H:%i'),
                     DATE_FORMAT(a.auditEndDate,   '%Y-%m-%d %H:%i')
            ORDER BY a.AuditPlayerId, MIN(a.auditStartDate) DESC
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setTimestamp(1, Timestamp.valueOf(
                    java.time.LocalDateTime.now().minusDays(30)));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<Audit> liste = new ArrayList<>();
                while (rs.next()) {
                    Audit a = Audit.mapAudit(rs);
                    a.setPlayerName(rs.getString("playerName")); // colonne extra de cette requête uniquement
                    liste.add(a);
                }
                LOG.debug(methodName + " - found " + liste.size() + " audit entries");
                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * Returns true if the player already has an open session (auditEndDate IS NULL).
     * Used at login to prevent double connections from different browsers.
     */
    public boolean isPlayerOnline(int playerId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " for playerId=" + playerId);

        final String query = "SELECT COUNT(*) FROM audit WHERE AuditPlayerId = ? AND AuditEndDate IS NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean online = rs.getInt(1) > 0;
                    LOG.debug(methodName + " - playerId=" + playerId + " online=" + online);
                    return online;
                }
            }
            return false;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /**
     * Returns true if the given auditId is still open (AuditEndDate IS NULL).
     * Used to distinguish "same session reconnecting" from "stale session with closed audit".
     */
    public boolean isAuditOpen(int auditId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " for auditId=" + auditId);

        final String query = "SELECT COUNT(*) FROM audit WHERE AuditId = ? AND AuditEndDate IS NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, auditId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean open = rs.getInt(1) > 0;
                    LOG.debug(methodName + " - auditId=" + auditId + " open=" + open);
                    return open;
                }
            }
            return false;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /**
     * Count of users currently online (auditEndDate IS NULL).
     */
    public int countOnline() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = "SELECT COUNT(*) FROM audit WHERE auditEndDate IS NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt(1);
                LOG.debug(methodName + " - online users = " + count);
                return count;
            }
            return 0;
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
        // tests locaux
    } // end main
    */

} // end class
