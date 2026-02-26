package lists;

import entite.HandicapIndex;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import rowmappers.HandicapIndexRowMapper;
import rowmappers.RowMapper;

@ApplicationScoped
public class ScoreDifferentialList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ScoreDifferentialList() { }

    public List<HandicapIndex> list(final Player player, final String type) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for player = " + player.getIdplayer());
        LOG.debug("for type = " + type);

        try (Connection conn = dataSource.getConnection()) {
            return listWithConnection(player, type, conn, methodName);
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    private List<HandicapIndex> listWithConnection(final Player player, final String type,
                                                   final Connection conn, final String methodName)
            throws SQLException {
        List<HandicapIndex> liste = new ArrayList<>();
        RowMapper<HandicapIndex> mapper = new HandicapIndexRowMapper();

        if (type.equals("<20")) {
            final String query = """
                SELECT *
                FROM handicap_index
                WHERE handicap_index.HandicapPlayerId = ?
                ORDER BY HandicapDate desc
                LIMIT 20
                """;
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, player.getIdplayer());
                utils.LCUtil.logps(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        liste.add(mapper.map(rs));
                    }
                }
            }
        } else if (type.equals(">20")) {
            LOG.debug(methodName + " - create temporary table with 20 last SD");
            final String queryCreate = """
                CREATE TEMPORARY TABLE top_differentials
                SELECT *
                FROM handicap_index
                WHERE handicap_index.HandicapPlayerId = ?
                ORDER BY HandicapDate desc
                LIMIT 20
                """;
            try (PreparedStatement ps = conn.prepareStatement(queryCreate)) {
                ps.setInt(1, player.getIdplayer());
                utils.LCUtil.logps(ps);
                int rows = ps.executeUpdate();
                LOG.debug(methodName + " - rows created in temp table = " + rows);
            }

            LOG.debug(methodName + " - select 8 lowest SD from temporary table");
            final String querySelect = """
                SELECT *
                FROM top_differentials
                ORDER BY HandicapScoreDifferential asc
                LIMIT 8
                """;
            try (PreparedStatement ps = conn.prepareStatement(querySelect)) {
                utils.LCUtil.logps(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        liste.add(mapper.map(rs));
                    }
                }
            }
        }

        // DROP temporary table — IF EXISTS is safe for <20 case where table was not created
        try (PreparedStatement ps = conn.prepareStatement("DROP TEMPORARY TABLE IF EXISTS top_differentials")) {
            utils.LCUtil.logps(ps);
            int rows = ps.executeUpdate();
            LOG.debug(methodName + " - temporary table 'top_differentials' dropped (0=OK) : " + rows);
        }

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
            liste.forEach(item -> LOG.debug("HandicapIndex list : WHS = " + item.getHandicapWHS()
                    + " date = " + item.getHandicapDate()
                    + " SD = " + item.getHandicapScoreDifferential()));
        }
        return liste;
    } // end method

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new ScoreDifferentialList().list(player, type, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #list(Player, String)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        // List<HandicapIndex> li = list(player, ">20");
        // LOG.debug("nombre items = " + li.size());
        LOG.debug("from main, ScoreDifferentialList = ");
    } // end main
    */

} // end class
