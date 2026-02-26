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
public class CreateOrUpdateScoreStableford implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private find.FindCountScore         findCountScore;
    @Inject private create.CreateScoreStableford createScoreStableford;
    @Inject private update.UpdateScoreStableford updateScoreStableford;

    public CreateOrUpdateScoreStableford() { }

    public boolean status(final ScoreStableford score, final Round round, final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with ScoreStableford = " + score);
        LOG.debug("with Round = " + round);
        LOG.debug("with Player = " + player);

        try {
            int rows = findCountScore.find(player, round, "rows");
            LOG.debug(methodName + " - number of rows = " + rows);

            if (rows == 0) {
                LOG.debug(methodName + " - this is an INSERT");
                return createScoreStableford.create(score, round, player);
            } else {
                LOG.debug(methodName + " - this is an UPDATE");
                return updateScoreStableford.update(score, round, player);
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
    // BRIDGE — @Deprecated — pour les appelants legacy (new CreateOrUpdateScoreStableford().status(..., conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #status(ScoreStableford, Round, Player)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(300);
        ScoreStableford score = new ScoreStableford();
        // boolean b = status(score, round, player);
        LOG.debug("from main, CreateOrUpdateScoreStableford = ");
    } // end main
    */

} // end class
