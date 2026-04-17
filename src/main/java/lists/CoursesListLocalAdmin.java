package lists;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;

/**
 * fix multi-user 2026-03-07 — cache supprimé (données per-admin dans singleton = fuite de données)
 */
@ApplicationScoped
public class CoursesListLocalAdmin implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CoursesListLocalAdmin() { }

    public List<ECourseList> list(final Player localAdmin) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            SELECT *
            FROM course, club
            WHERE club.ClubLocalAdmin = ?
               AND course.club_idclub = club.idclub
               AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
            """;

        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();

        List<ECourseList> result = dao.queryList(query, rs -> ECourseList.builder()
                .club(clubMapper.map(rs))
                .course(courseMapper.map(rs))
                .build(),
                localAdmin.getIdplayer());

        if (result.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + result.size());
        }
        return result;
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
        LOG.debug("entering {}", methodName);
        Player localAdmin = new Player();
        localAdmin.setIdplayer(324715);
        List<ECourseList> lp = list(localAdmin);
        LOG.debug("from main, after lp = " + lp);
    } // end main
    */

} // end class
