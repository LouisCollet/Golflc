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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class CreateAudit implements Serializable, interfaces.Log {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateAudit() { }

    public boolean create(final Player player) throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for player = " + player);

        try (Connection conn = dao.getConnection()) {
            final String query = sql.SqlFactory.generateInsertQuery(conn, "audit");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setNull(1, java.sql.Types.INTEGER); // auto-increment
                ps.setInt(2, player.getIdplayer());
                ps.setTimestamp(3, Timestamp.from(Instant.now())); // AuditStartDate
                ps.setTimestamp(4, Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS))); // AuditEndDate
                ps.setTimestamp(5, Timestamp.from(Instant.now())); // ModificationDate
                utils.LCUtil.logps(ps);
                int rows = ps.executeUpdate();
                if (rows != 0) {
                    LOG.debug("-- successful INSERT Audit rows = " + rows);
                    return true;
                } else {
                    LOG.debug("-- UNsuccessful insert Audit !!! ");
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
        Player player = new Player();
        player.setIdplayer(324713);
        boolean b = new create.CreateAudit().create(player);
        LOG.debug("from main, CreateAudit = " + b);
    } // end main
    */

} // end class
