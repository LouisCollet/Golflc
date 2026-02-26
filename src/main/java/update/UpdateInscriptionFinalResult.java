package update;

import entite.Player;
import entite.Round;
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
import utils.LCUtil;

@ApplicationScoped
public class UpdateInscriptionFinalResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public UpdateInscriptionFinalResult() { }

    public boolean update(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for player = " + player);
        LOG.debug("for round = " + round);

        try (Connection conn = dataSource.getConnection()) {

            // 1. SELECT SUM of score points
            int totalPoints = 0;
            try (PreparedStatement ps = conn.prepareStatement("""
                    SELECT SUM(score.ScorePoints) AS totalPoints
                    FROM score
                    WHERE score.player_has_round_player_idplayer = ?
                      AND score.player_has_round_round_idround = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                ps.setInt(2, round.getIdround());
                utils.LCUtil.logps(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        totalPoints = rs.getInt("totalPoints");
                    }
                }
            }
            LOG.debug(methodName + " - totalPoints to update = " + totalPoints);

            // 2. UPDATE player_has_round with totalPoints
            try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE player_has_round
                    SET InscriptionFinalResult = ?
                    WHERE InscriptionIdPlayer = ?
                      AND InscriptionIdRound = ?
                    """)) {
                ps.setInt(1, totalPoints);
                ps.setInt(2, player.getIdplayer());
                ps.setInt(3, round.getIdround());
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "successful UPDATE InscriptionFinalResult for TotalPoints = " + totalPoints;
                    LOG.info(msg);
                    return true;
                } else {
                    String msg = "-- UNsuccessful result in " + methodName + " for player : " + player;
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
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

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new UpdateInscriptionFinalResult().update(player, round, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #update(Player, Round)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(487);
        boolean b = update(player, round);
        LOG.debug("update result = " + b);
    } // end main
    */

} // end class
