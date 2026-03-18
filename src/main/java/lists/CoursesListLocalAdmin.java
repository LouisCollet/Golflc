package lists;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.composite.ECourseList;
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
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;

/**
 * fix multi-user 2026-03-07 — cache supprimé (données per-admin dans singleton = fuite de données)
 */
@ApplicationScoped
public class CoursesListLocalAdmin implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public CoursesListLocalAdmin() { }

    public List<ECourseList> list(final Player localAdmin) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String query = """
            SELECT *
            FROM course, club
            WHERE club.ClubLocalAdmin = ?
               AND course.club_idclub = club.idclub
               AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, localAdmin.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<ECourseList> result = new ArrayList<>();
                RowMapper<Club> clubMapper = new ClubRowMapper();
                RowMapper<Course> courseMapper = new CourseRowMapper();

                while (rs.next()) {
                    ECourseList ecl = ECourseList.builder()
                            .club(clubMapper.map(rs))
                            .course(courseMapper.map(rs))
                            .build();
                    result.add(ecl);
                }
                if (result.isEmpty()) {
                    LOG.warn(methodName + " - empty result list");
                } else {
                    LOG.debug(methodName + " - list size = " + result.size());
                }
                return result;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * No-op — cache removed (fix multi-user 2026-03-07)
     */
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - no-op (cache removed)");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player localAdmin = new Player();
        localAdmin.setIdplayer(324715);
        List<ECourseList> lp = list(localAdmin);
        LOG.debug("from main, after lp = " + lp);
    } // end main
    */

} // end class
