
package create;

import entite.HandicapIndex;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import manager.PlayerManager;
import utils.LCUtil;

@ApplicationScoped
public class CreateHandicapIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject
    private PlayerManager playerManager;

    /**
     * Crée un nouvel handicap index
     * @param handicapIndex à créer
     * @return HandicapIndex créé avec son ID généré
     */
    public HandicapIndex create(final HandicapIndex handicapIndex) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        LOG.debug("entering {}", methodName);
        LOG.debug("with HandicapIndex = {}", handicapIndex);

        try (Connection conn = dao.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "handicap_index");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateUpdateHandicapIndex.psMapCreate(ps, handicapIndex);
                int row = ps.executeUpdate();
                if (row != 0) {
                    handicapIndex.setHandicapId(LCUtil.generatedKey(conn));
                    String msg = "HandicapIndex created = " + handicapIndex;
                    LOG.debug(msg);
                    return handicapIndex;
                } else {
                    String msg = "Not successful insert for HandicapIndex = " + handicapIndex;
                    LOG.error(msg);
                    throw new SQLException(msg);
                }
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
        try {
            Player player = new Player();
            player.setIdplayer(456782);

            // ✅ PlayerManager déjà migré
            player = playerManager.readPlayer(player.getIdplayer());
            Round round = new Round();
            round.setIdround(717); // test WHS le 11/11/2020
            HandicapIndex index = new HandicapIndex();
            index.setHandicapPlayerId(player.getIdplayer());
            index.setHandicapRoundId(round.getIdround());
            index.setHandicapPlayedStrokes((short) 0);
            index.setHandicapDate(round.getRoundDate().minusDays(1)); // créé à la veille du round
            index.setHandicapScoreDifferential(new BigDecimal("36.0").setScale(3, RoundingMode.HALF_UP));
            index.setHandicapWHS(new BigDecimal("36.0").setScale(3, RoundingMode.HALF_UP));

            HandicapIndex hi = new create.CreateHandicapIndex().create(index);
            LOG.debug("from main, CreateHandicapIndex = {}", hi);

        } catch (Exception e) {
            String msg = "Exception in main CreateHandicapIndex: " + e.getMessage();
            LOG.error(msg, e);
        }
    } // end main
    */

} // end class