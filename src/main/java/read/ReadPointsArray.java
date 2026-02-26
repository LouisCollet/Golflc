package read;

import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
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
public class ReadPointsArray implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ReadPointsArray() { }

    public int[][] load(int[][] points, final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for round = " + round);
        LOG.debug(methodName + " - player = " + player.getIdplayer());
        LOG.debug(methodName + " - points = " + Arrays.deepToString(points));

        final String query =
                "SELECT * "
                + " FROM course"
                + " JOIN player"
                + "   ON player.idplayer = ?"
                + " JOIN round"
                + "   ON round.idround = ?"
                + " JOIN player_has_round"
                + "   ON InscriptionIdRound = round.idround"
                + "   AND round.course_idcourse = course.idcourse"
                + " JOIN tee"
                + "  ON course.idcourse = tee.course_idcourse"
                + "  AND tee.TeeGender = player.PlayerGender"
                + "   AND tee.idtee = player_has_round.InscriptionIdTee"
                + " JOIN hole"
                + "   ON hole.tee_idtee = tee.TeeMasterTee"
                + "   AND hole.tee_course_idcourse = course.idcourse"
                + "   AND hole.HoleNumber"
                + "      BETWEEN round.RoundStart and round.RoundStart + round.RoundHoles - 1 "
                + "   GROUP by hole.HoleNumber"
                + "   ORDER by hole.HoleNumber";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setInt(2, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int rowNum = rs.getRow() - 1;
                    points[rowNum][0] = rs.getInt("HoleNumber");
                    points[rowNum][1] = rs.getInt("HolePar");
                    points[rowNum][2] = rs.getInt("HoleStrokeIndex");
                    points[rowNum][3] = 0;
                    points[rowNum][4] = 0;
                    points[rowNum][5] = 0;
                }
                LOG.debug(methodName + " - Hole" + TAB + "Par" + TAB + "Index" + TAB +
                        "Stroke" + TAB + "Extra" + TAB + "Points");
                LOG.debug(methodName + NEW_LINE + "Array completed = " + Arrays.deepToString(points));
                return points;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return new int[0][0];
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new int[0][0];
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // @Inject private manager.PlayerManager playerManager;
        // Player player = new Player(); player.setIdplayer(324713);
        // player = playerManager.readPlayer(player.getIdplayer());
        // Round round = new Round(); round.setIdround(484);
        // int[][] a = load(points, player, round);
        // LOG.debug("array points filled = " + Arrays.deepToString(a));
    } // end main
    */

} // end class
