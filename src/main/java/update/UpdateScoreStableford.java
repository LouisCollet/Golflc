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

    @Inject
    private dao.GenericDAO dao;

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
               AND player_has_round_player_idplayer=?
               AND player_has_round_round_idround=?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.strip())) {

            for (ScoreStableford.Score sco : score.getScoreList()) {
                ps.setInt(1, sco.getStrokes());
                ps.setInt(2, sco.getPoints());
                ps.setInt(3, sco.getExtra());
                ps.setInt(4, sco.getHole());
                ps.setInt(5, player.getIdplayer());
                ps.setInt(6, round.getIdround());
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    LOG.debug("successful update hole = {}", sco.getHole());
                } else {
                    String msg = "NOT NOT successful update, hole = " + sco.getHole();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            } // end for

            LOG.debug("just before SUM totalPoints");
            if (updateInscriptionFinalResult.update(player, round)) {
                LOG.debug("update InscriptionFinalResult OK");
            }

            String msg = "<br/>Successful update scores <br/> for player = "
                    + "id = " + player.getIdplayer()
                    + " name " + player.getPlayerLastName()
                    + "<br/> , round id = " + round.getIdround()
                    + " , round name = " + round.getRoundName();
            LOG.debug(msg);
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

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new UpdateScoreStableford().update(..., conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #update(ScoreStableford, Round, Player)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(300);
        // boolean b = update(score, round, player);
        LOG.debug("from main, UpdateScoreStableford = ");
    } // end main
    */

} // end class
