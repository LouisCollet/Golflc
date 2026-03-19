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
import utils.LCUtil;

@ApplicationScoped
public class CreateBlocking implements Serializable, interfaces.Log {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateBlocking() { }

    public boolean create(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for player = " + player);

        try (Connection conn = dao.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "blocking");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, player.getIdplayer());
                ps.setTimestamp(2, Timestamp.from(Instant.now())); // BlockingLastAttempt
                ps.setInt(3, 1);
                ps.setTimestamp(4, Timestamp.from(Instant.now()));
                ps.setTimestamp(5, Timestamp.from(Instant.now())); // ModificationDate
                utils.LCUtil.logps(ps);
                int rows = ps.executeUpdate();
                if (rows != 0) {
                    String msg = "Tentative 1 - Après 3 erreurs successives, vous serez bloqué pendant 15 minutes ";
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    LOG.debug("-- UNsuccessful insert Blocking !!! ");
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
        boolean b = new create.CreateBlocking().create(player);
        LOG.debug("from main, CreateBlocking = " + b);
    } // end main
    */

} // end class
