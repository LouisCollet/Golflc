package read;

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
import java.util.Arrays;

@ApplicationScoped
public class ReadArrayStrokes implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadArrayStrokes() { }

    public int[] read(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for player = {}", player.getIdplayer());
        LOG.debug("for round = {}", round.getIdround());

        final String query = """
                SELECT *
                FROM score
                WHERE score.player_has_round_round_idround = ?
                   AND score.player_has_round_player_idplayer = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getIdround());
            ps.setInt(2, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int[] arrayStrokes = new int[18];
                int i = 0;
                int plus = 0;
                while (rs.next()) {
                    if (i == 0) {
                        plus = rs.getInt("ScoreHole") - 1;
                        LOG.debug("plus = {}", plus);
                    }
                    int j = i + plus;
                    arrayStrokes[j] = rs.getInt("ScoreStroke");
                    i++;
                }
                LOG.debug("ending ReadArrayStrokes = {}", Arrays.toString(arrayStrokes));
                return arrayStrokes;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return new int[0];
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new int[0];
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Player player = new Player(); player.setIdplayer(324713);
        // Round round = new Round(); round.setIdround(630);
        // int[] strokes = new ReadArrayStrokes().read(player, round);
        // LOG.debug("array strokes = {}", Arrays.toString(strokes));
    } // end main
    */

} // end class
