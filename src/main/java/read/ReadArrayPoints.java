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
public class ReadArrayPoints implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ReadArrayPoints() { }

    public int[] read(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for player = " + player.getIdplayer());
        LOG.debug(methodName + " - for round = " + round.getIdround());

        final String query = """
                SELECT *
                FROM score
                WHERE score.player_has_round_round_idround = ?
                   AND score.player_has_round_player_idplayer = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getIdround());
            ps.setInt(2, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int[] arrayPoints = new int[18];
                int i = 0;
                int plus = 0;
                while (rs.next()) {
                    if (i == 0) {
                        plus = rs.getInt("ScoreHole") - 1;
                        LOG.debug(methodName + " - plus = " + plus);
                    }
                    int j = i + plus;
                    arrayPoints[j] = rs.getInt("ScorePoints");
                    i++;
                }
                LOG.debug(methodName + " - ending ReadArrayPoints = " + Arrays.toString(arrayPoints));
                return arrayPoints;
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
        // Round round = new Round(); round.setIdround(630);
        // int[] points = new ReadArrayPoints().read(player, round);
        // LOG.debug("array points = " + Arrays.toString(points));
    } // end main
    */

} // end class
