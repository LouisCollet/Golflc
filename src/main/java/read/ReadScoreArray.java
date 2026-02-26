package read;

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
import java.util.Arrays;
import javax.sql.DataSource;

@ApplicationScoped
public class ReadScoreArray implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ReadScoreArray() { }

    public int[] load(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - player = " + player.toString());
        LOG.debug(methodName + " - round = " + round.toString());

        final String query = """
                SELECT *
                FROM score, round
                WHERE score.player_has_round_player_idplayer = ?
                AND round.idround = ?
                AND score.player_has_round_round_idround = round.idround
                AND round.idround = score.player_has_round_round_idround
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setInt(2, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int[] strokes = new int[18];
                while (rs.next()) {
                    int rowNum = rs.getRow() - 1;
                    if (rs.getInt("RoundStart") == 1) {
                        strokes[rs.getRow() - 1] = rs.getInt("ScoreStroke");
                    } else {
                        strokes[rowNum + 9] = rs.getInt("ScoreStroke");
                    }
                }
                LOG.debug(methodName + " - exiting with strokes[] = " + Arrays.toString(strokes));
                return strokes;
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
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Player player = new Player(); player.setIdplayer(324713);
        // Round round = new Round(); round.setIdround(676);
        // int[] i = new ReadScoreArray().load(player, round);
        // LOG.debug("result main = " + Arrays.toString(i));
    } // end main
    */

} // end class
