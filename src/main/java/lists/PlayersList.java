package lists;

import entite.composite.EPlayerPassword;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.PlayerRowMapper;
import rowmappers.RowMapper;
import utils.LCUtil;

/**
 * Liste tous les joueurs actifs avec leur mot de passe
 * ✅ Migré vers GenericDAO
 * ✅ main() conservée commentée
 */
@ApplicationScoped
public class PlayersList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<EPlayerPassword> liste = null;

    private static final String QUERY = """
            /* lists.PlayersList.list */
            SELECT *
            FROM Player
            WHERE PlayerActivation='1'
            ORDER BY idplayer
            """;

    /**
     * Liste tous les joueurs actifs avec leur mot de passe.
     *
     * @return liste des EPlayerPassword, jamais null
     */
    public List<EPlayerPassword> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list (" + liste.size() + " entries)");
            return liste;
        }

        RowMapper<EPlayerPassword> compositeMapper = rs -> {
            var player   = new PlayerRowMapper().map(rs);
            var password = entite.Password.map(rs);
            return new EPlayerPassword(player, password);
        };

        liste = dao.queryList(QUERY, compositeMapper);

        if (liste.isEmpty()) {
            String msg = "Empty Result Table in " + methodName;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } else {
            LOG.debug(methodName + " - ResultSet has " + liste.size() + " lines");
        }

        return liste;
    } // end method

    // ✅ Getters/setters d'instance
    public List<EPlayerPassword> getListe()               { return liste; }
    public void setListe(List<EPlayerPassword> liste)     { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException, Exception {
        LOG.debug("starting main");
        Connection conn = new connection_package.DBConnection().getConnection();
        LOG.debug("after connexion");
        if (conn == null) {
            LOG.debug("conn is null");
        }
        List<EPlayerPassword> p1 = new PlayersList().list(conn);
        LOG.debug("Players list = " + p1.size());
        connection_package.DBConnection.closeQuietly(conn, null, null, null);
    } // end main
    */

} // end class
