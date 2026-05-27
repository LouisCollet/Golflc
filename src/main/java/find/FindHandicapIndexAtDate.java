package find;

import entite.HandicapIndex;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import rowmappers.HandicapIndexRowMapper;
import rowmappers.RowMapper;

@ApplicationScoped
public class FindHandicapIndexAtDate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject
    private create.CreateHandicapIndex createHandicapIndexService;

    /**
     * Trouve le handicap index à une date donnée (le plus récent avant cette date)
     * Si aucun HI trouvé, en crée un nouveau à compléter manuellement
     * @param handicapIndex avec HandicapPlayerId et HandicapDate définis
     * @return HandicapIndex trouvé ou null si créé
     */
    public HandicapIndex find(HandicapIndex handicapIndex) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering : " + methodName);

        final String query = """
            SELECT *
            FROM handicap_index
            WHERE HandicapPlayerId = ?
            AND HandicapDate < ?
            ORDER BY HandicapDate DESC
            LIMIT 1
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, handicapIndex.getHandicapPlayerId());
            ps.setTimestamp(2, Timestamp.valueOf(handicapIndex.getHandicapDate()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                RowMapper<HandicapIndex> handicapIndexMapper = new HandicapIndexRowMapper();
                int i = 0;

                if (rs.next()) {
                    i++;
                    handicapIndex = handicapIndexMapper.map(rs);
                }

                if (i == 0) {
                    // ✅ Aucun HI trouvé → création d'un nouveau
                    String msg = "No HandicapIndex found in " + methodName;
                    LOG.error(msg);

                    // ✅ Service injecté sans conn
                    var hi = createHandicapIndexService.create(handicapIndex);

                    msg = "Compléter manuellement la situation de départ // created = " + hi;
                    LOG.info(msg);

                    // ⚠️ Retourne null pour indiquer qu'il faut compléter manuellement
                    return null;

                } else {
                    LOG.debug("Handicap Index found = " + handicapIndex.getHandicapWHS());
                }

                return handicapIndex;
            }

        } catch (SQLException e) {
            String msg = "SQLException in " + methodName + ": " + e.getMessage()
                    + ", SQLState = " + e.getSQLState()
                    + ", ErrorCode = " + e.getErrorCode();
            LOG.error(msg, e);
            throw e;

        } catch (Exception e) {
            String msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg, e);
            throw new SQLException(msg, e);
        }
    } // end method

    /*
    void main() throws SQLException {
        HandicapIndex handicapIndex = new HandicapIndex();
        handicapIndex.setHandicapPlayerId(324713);
        Round round = new Round();
        round.setIdround(590);

        handicapIndex.setHandicapDate(round.getRoundDate());
        HandicapIndex hi = new find.FindHandicapIndexAtDate().find(handicapIndex);

        if (hi != null) {
            LOG.debug("FindHandicapIndexAtDate = " + hi.getHandicapWHS());
        } else {
            LOG.debug("HandicapIndex created - needs manual completion");
        }
    } // end main
    */

} // end Class
