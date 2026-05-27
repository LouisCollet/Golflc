package lists;

import entite.HandicapIndex;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import rowmappers.HandicapIndexRowMapper;
import rowmappers.RowMapper;

@ApplicationScoped
public class ScoreDifferentialList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ScoreDifferentialList() { }

    public List<HandicapIndex> list(final Player player, final String type) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for player = " + player.getIdplayer());
        LOG.debug("for type = " + type);

            final String query;
        if (type.equals("<20")) {
            query = """
                SELECT *
                FROM handicap_index
                WHERE HandicapPlayerId = ?
                ORDER BY HandicapDate DESC
                LIMIT 20
                """;
        } else if (type.equals(">20")) {
            // 8 lowest score differentials from the 20 most recent — single query, no temp table
            query = """
                SELECT * FROM (
                    SELECT *
                    FROM handicap_index
                    WHERE HandicapPlayerId = ?
                    ORDER BY HandicapDate DESC
                    LIMIT 20
                ) AS recent
                ORDER BY HandicapScoreDifferential ASC
                LIMIT 8
                """;
        } else {
            LOG.warn(methodName + " - unknown type: " + type);
            return Collections.emptyList();
        }

        try {
            RowMapper<HandicapIndex> mapper = new HandicapIndexRowMapper();
            List<HandicapIndex> liste = dao.queryList(query, mapper::map, player.getIdplayer());

            if (liste.isEmpty()) {
                LOG.warn(methodName + " - empty result list");
            } else {
                LOG.debug(methodName + " - list size = " + liste.size());
                liste.forEach(item -> LOG.debug("HandicapIndex list : WHS = " + item.getHandicapWHS()
                        + " date = " + item.getHandicapDate()
                        + " SD = " + item.getHandicapScoreDifferential()));
            }
            return liste;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

} // end class
