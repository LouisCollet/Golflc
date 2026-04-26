package find;

import entite.MatchplayPlayerResult;
import entite.Player;
import entite.Round;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class FindMatchplayResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindMatchplayResult() { }

    public List<MatchplayPlayerResult> find(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for player = " + player);
        LOG.debug("for round = " + round);

        final String query = """
            SELECT player.PlayerFirstName, player.PlayerLastName, player.idplayer, round.idround,
                   score.ScoreHole, score.ScoreStroke
            FROM round
            JOIN player
                ON player.idplayer = ?
                AND round.idround = ?
            JOIN score
                ON score.inscription_player_idplayer = player.idplayer
                AND score.inscription_round_idround = round.idround
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setInt(2, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<MatchplayPlayerResult> liste = new ArrayList<>();
                while (rs.next()) {
                    liste.add(entite.MatchplayPlayerResult.map(rs));
                }
                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result for player = " + player.getIdplayer());
                } else {
                    LOG.debug(methodName + " - result size = " + liste.size());
                }
                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    /*
    void main(String[] args) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(694);
        // var p1 = find(player, round);
        // LOG.debug("result found = " + p1);
        LOG.debug("from main, FindMatchplayResult = ");
    } // end main
    */

} // end class
