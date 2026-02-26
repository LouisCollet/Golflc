package delete;

import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class DeleteBlocking implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public DeleteBlocking() { }

    public boolean delete(final Player player) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("Delete Blocking for player " + player);

        final String query = """
                DELETE from blocking
                WHERE BlockingPlayerId = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            LCUtil.logps(ps);
            int rowDeleted = ps.executeUpdate();
            LOG.debug(methodName + " - deleted Blocking = " + rowDeleted);
            if (rowDeleted > 0) {
                String msg = "End of Blocking for 15 min for = " + player.getIdplayer();
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "NOT success for unblocking 15 min for = " + player.getIdplayer();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return false;
        }
    } // end method

    /** @deprecated Use {@link #delete(Player)} instead — migrated 2026-02-24 */
    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        boolean b = new DeleteBlocking().delete(player);
        LOG.debug("from main - resultat deleteBlocking = " + b);
    } // end main
    */

} // end class
