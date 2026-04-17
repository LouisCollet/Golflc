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
import java.sql.Timestamp;
import java.time.Instant;
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
                    ps.setNull(1, java.sql.Types.INTEGER);
                    ps.setInt(2, sco.getHole());
                    ps.setInt(3, sco.getStrokes());
                    ps.setInt(4, sco.getExtra());
                    ps.setInt(5, sco.getPoints());
                    ps.setInt(6, sco.getPar());
                    ps.setInt(7, sco.getIndex());
                    ps.setInt(8, 0);  // ScoreFairway
                    ps.setInt(9, 0);  // ScoreGreen
                    ps.setInt(10, 0); // ScorePutts
                    ps.setInt(11, 0); // ScoreBunker
                    ps.setInt(12, 0); // ScorePenalty
                    ps.setInt(13, player.getIdplayer());
                    ps.setInt(14, round.getIdround());
                    ps.setTimestamp(15, Timestamp.from(Instant.now()));
                    utils.LCUtil.logps(ps);
                    int row = ps.executeUpdate();
                    if (row != 0) {
                        score.setIdscore(LCUtil.generatedKey(conn));
                    } else {
                        String msg = "<br/>NOT NOT insert for hole = " + sco.getHole();
                        LOG.debug(msg);
                        LCUtil.showMessageFatal(msg);
                        return false;
                    }
                    LOG.debug("successful insert for score = {}", sco);
                } // end for

                LOG.debug("just before SUM totalPoints");
                if (updateInscriptionFinalResult.update(player, round)) {
                    LOG.debug("update InscriptionFinalResult OK");
                }

                String msg = "<br/>Successful insert scores"
                        + " , round id = " + round.getIdround()
                        + " , round name = " + round.getRoundName()
                        + " , <br/>player = " + player.getPlayerLastName()
                        + " , player id = " + player.getIdplayer()
                        + " , last score = " + LCUtil.generatedKey(conn);
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
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
