package lists;

import entite.Player;
import entite.composite.EPlayerPassword;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rowmappers.PlayerRowMapper;
import rowmappers.RowMapper;
import utils.LCUtil;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;

/**
 * Liste tous les joueurs actifs avec leur mot de passe
 * ✅ Migré vers CDI (@ApplicationScoped)
 * ✅ Connection supprimée — gérée via DataSource injecté
 * ✅ try-with-resources (plus de finally/closeQuietly)
 * ✅ return null remplacé par Collections.emptyList()
 * ✅ main() conservée commentée
 */
@ApplicationScoped
public class PlayersList implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Injection DataSource WildFly
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<EPlayerPassword> liste = null;

    private static final String QUERY = """
            /* lists.PlayersList.list */
            SELECT *
            FROM Player
            WHERE PlayerActivation='1'
            ORDER BY idplayer
            """;

    // ========================================
    // MÉTHODE PRINCIPALE
    // ========================================

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

        // ✅ try-with-resources : Connection, PreparedStatement, ResultSet fermés automatiquement
        try (Connection conn        = dataSource.getConnection();
             PreparedStatement ps   = conn.prepareStatement(QUERY);
             ResultSet rs           = ps.executeQuery()) {

            LCUtil.logps(ps);

            RowMapper<Player> playerMapper = new PlayerRowMapper();
            List<EPlayerPassword> result   = new ArrayList<>();

            while (rs.next()) {
                var player   = playerMapper.map(rs);
                var password = entite.Password.map(rs);
                result.add(new EPlayerPassword(player, password));
            }

            if (result.isEmpty()) {
                String msg = "Empty Result Table in " + methodName;
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
            } else {
                LOG.debug(methodName + " - ResultSet has " + result.size() + " lines");
            }

            liste = result;                                         // ✅ mise en cache
            return liste;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();                         // ✅ jamais null
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();                         // ✅ jamais null
        }
    } // end method

    // ========================================
    // CACHE - Getters / Setters statiques
    // ========================================

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

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

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