package update;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class UpdateScoreStableford implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private update.UpdateInscriptionFinalResult updateInscriptionFinalResult;

    public UpdateScoreStableford() { }

    public boolean update(final ScoreStableford score, final Round round, final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with ScoreStableford = {}", score);
        LOG.debug("with Round = {}", round);
        LOG.debug("with Player = {}", player);

        final String query = """
            UPDATE score
            SET ScoreStroke=?, ScorePoints=?, ScoreExtraStroke=?
            WHERE ScoreHole=?
               AND inscription_player_idplayer=?
               AND inscription_round_idround=?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.strip())) {

            for (ScoreStableford.Score sco : score.getScoreList()) {
                sql.preparedstatement.psCreateUpdateScoreStableford.psMapUpdate(ps, sco, player, round);
                int row = ps.executeUpdate();
                if (row != 0) {
                    LOG.debug("UPDATE score hole = {}", sco.getHole());
                } else {
                    LOG.error("update failed for hole = {}", sco.getHole());
                    LCUtil.showMessageFatal(LCUtil.prepareMessageBean("score.error"));
                    return false;
                }
            }

            LOG.debug("all scores updated for player={} round={}", player.getIdplayer(), round.getIdround());
            if (updateInscriptionFinalResult.update(player, round)) {
                LOG.debug("updateInscriptionFinalResult OK");
            }

            String msg = LCUtil.prepareMessageBean("score.modify");
            LOG.debug("scores updated player={} {} round={} {}", player.getIdplayer(), player.getPlayerLastName(), round.getIdround(), round.getRoundName());
            LCUtil.showMessageInfo(msg);
            return true;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
