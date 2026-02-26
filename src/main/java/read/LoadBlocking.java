package read;

import entite.Blocking;
import entite.Player;
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
import javax.sql.DataSource;

@ApplicationScoped
public class LoadBlocking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public LoadBlocking() { }

    public Blocking load(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for player = " + player.getIdplayer());

        final String query = """
                SELECT *
                FROM blocking
                WHERE BlockingPlayerId = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int i = 0;
                Blocking a = null;
                while (rs.next()) {
                    i++;
                    a = entite.Blocking.mapBlocking(rs);
                }
                if (i == 0) {
                    LOG.debug(methodName + " - no blocking found for player = " + player.getIdplayer());
                    return null;
                } else {
                    LOG.debug(methodName + " - blocking found, lines = " + i);
                }
                return a;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Player player = new Player(); player.setIdplayer(206658);
        // Blocking blocking = new LoadBlocking().load(player);
        // LOG.debug("Blocking found = " + blocking);
    } // end main
    */

} // end class
