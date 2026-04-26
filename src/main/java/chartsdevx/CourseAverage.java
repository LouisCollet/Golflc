package chartsdevx;

import entite.Average;
import entite.Course;
import entite.Player;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

@ApplicationScoped
public class CourseAverage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public CourseAverage() { }

    public List<Average> stat(final Player player, final Course course) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("  with player = " + player);
        LOG.debug("  with course = " + course);

        // CTE Common Table Expression — disable ONLY_FULL_GROUP_BY for this session
        final String queryMode = """
            SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
            """;

        final String query = """
            WITH chart_data AS(
                SELECT scorehole, scorepar, scorestroke, scoreextrastroke, scorePoints, scorestrokeindex, course.idcourse, round.RoundHoles, round.idround, round.RoundDate
                FROM score, round, course
                WHERE ROUND.course_idcourse = ?
                AND score.inscription_round_idround = round.idround
                AND score.inscription_player_idplayer = ?
                GROUP BY idround, scorehole
                ORDER BY idround DESC
            )
            SELECT scorehole, scorepar, scoreStrokeIndex, scoreExtraStroke,
                ROUND(AVG(scorestroke),1) AS averageStroke,
                ROUND(AVG(scorePoints),1) AS averagePoints,
                COUNT(distinct idround) as countRound
            FROM chart_data
            GROUP BY scorehole;
            """;

        try (Connection conn = dataSource.getConnection()) {
            // First: disable ONLY_FULL_GROUP_BY
            try (PreparedStatement psMode = conn.prepareStatement(queryMode)) {
                utils.LCUtil.logps(psMode);
                boolean b = psMode.execute();
                LOG.debug(" rs for ONLY_FULL_GROUP_BY = " + b);
            }

            // Then: execute the main query
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, course.getIdcourse());
                ps.setInt(2, player.getIdplayer());
                utils.LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {
                    List<Average> liste = new ArrayList<>();
                    int i = 0;
                    while (rs.next()) {
                        i++;
                        liste.add(entite.Average.map(rs));
                    } // end while
                    LOG.debug("ResultSet getStatAvg has " + i + " lines.");
                    return liste;
                }
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
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Course course = new Course();
        course.setIdcourse(86);
        List<Average> av = new CourseAverage().stat(player, course);
        LOG.debug("from main, average = " + av.toString());
    } // end main
    */

} // end class
