package read;

import entite.Blocking;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class LoadBlocking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public LoadBlocking() { }

    public Blocking load(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for player = {}", player.getIdplayer());

        final String query = """
                SELECT *
                FROM blocking
                WHERE BlockingPlayerId = ?
                """;

        return dao.querySingle(query, rs -> entite.Blocking.mapBlocking(rs), player.getIdplayer());
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Player player = new Player(); player.setIdplayer(206658);
        // Blocking blocking = new LoadBlocking().load(player);
        // LOG.debug("Blocking found = {}", blocking);
    } // end main
    */

} // end class
