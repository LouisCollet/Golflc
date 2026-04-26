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
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class CreateStatisticsStableford implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private find.FindCountScore findCountScore;

    public CreateStatisticsStableford() { }

    public boolean create(final Player player, final Round round, final ScoreStableford score) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with round = {}", round);
        LOG.debug("with scoreStableford = {}", score);

        int rows = findCountScore.find(player, round, "rows");
        if (rows == 0) {
            String msg = "Create in statistics : this must be an error !!!";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }

        final String query = """
            UPDATE score
            SET ScoreFairway=?, ScoreGreen=?, ScorePutts=?, ScoreBunker=?, ScorePenalty=?
            WHERE ScoreHole = ?
              AND inscription_player_idplayer=?
              AND inscription_round_idround=?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            for (ScoreStableford.Statistics stt : score.getStatisticsList()) {
                ps.setInt(1, stt.getFairway());
                ps.setInt(2, stt.getGreen());
                ps.setInt(3, stt.getPutt());
                ps.setInt(4, stt.getBunker());
                ps.setInt(5, stt.getPenalty());
                ps.setInt(6, stt.getHole());
                ps.setInt(7, player.getIdplayer());
                ps.setInt(8, round.getIdround());
                utils.LCUtil.logps(ps);
                int x = ps.executeUpdate();
                if (x != 0) {
                    LOG.debug("successful update statistics hole = {}", stt.getHole());
                } else {
                    String msg = "ERROR updateStatisticsStableford hole = " + stt.getHole();
                    LOG.debug(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            } // end for
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
    // BRIDGE — @Deprecated — pour les appelants legacy (new CreateStatisticsStableford().create(..., conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #create(Player, Round, ScoreStableford)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(630);
        ScoreStableford score = new ScoreStableford();
        // boolean b = create(player, round, score);
        LOG.debug("from main, CreateStatisticsStableford = ");
    } // end main
    */

} // end class
