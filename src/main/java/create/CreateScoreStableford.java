package create;

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
public class CreateScoreStableford implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private update.UpdateInscriptionFinalResult updateInscriptionFinalResult;

    public CreateScoreStableford() { }

    public boolean create(final ScoreStableford score, final Round round, final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with ScoreStableford = {}", score);
        LOG.debug("for Round = {}", round);
        LOG.debug("for Player = {}", player);

        try (Connection conn = dao.getConnection()) {

            final String query = LCUtil.generateInsertQuery(conn, "score");
            try (PreparedStatement ps = conn.prepareStatement(query)) {

                for (ScoreStableford.Score sco : score.getScoreList()) {
                    sql.preparedstatement.psCreateUpdateScoreStableford.psMapCreate(ps, sco, player, round);
                    int row = ps.executeUpdate();
                    if (row != 0) {
                        score.setIdscore(LCUtil.generatedKey(conn));
                        LOG.debug("inserted score hole={}", sco.getHole());
                    } else {
                        LOG.error("insert score failed for hole={}", sco.getHole());
                        LCUtil.showMessageFatal(LCUtil.prepareMessageBean("score.error"));
                        return false;
                    }
                } // end for

                LOG.debug("all scores inserted, updating final result");
                if (updateInscriptionFinalResult.update(player, round)) {
                    LOG.debug("update InscriptionFinalResult OK");
                }

                LOG.debug("scores created round={} player={} lastScore={}",
                        round.getIdround(), player.getIdplayer(), LCUtil.generatedKey(conn));
                LCUtil.showMessageInfo(LCUtil.prepareMessageBean("score.created")
                        + " — " + player.getPlayerLastName() + " / " + round.getRoundName());
                return true;
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
    // BRIDGE — @Deprecated — pour les appelants legacy (new CreateScoreStableford().create(..., conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #create(ScoreStableford, Round, Player)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        // boolean b = create(score, round, player);
        LOG.debug("from main, CreateScoreStableford = ");
    } // end main
    */

} // end class
