package lists;

import entite.Club;
import entite.Course;
import entite.Tee;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;

@ApplicationScoped
public class ClubDetailList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    public ClubDetailList() { }

    public List<ECourseList> list(final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with club = " + club);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM club, course, tee
            WHERE club.idclub = course.club_idclub
               AND tee.course_idcourse = course.idcourse
               AND idclub = ?
            ORDER by idclub, idcourse, teegender, teestart
            """;

        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapper<Tee> teeMapper = new TeeRowMapper();

        liste = new ArrayList<>(dao.queryList(query, rs -> ECourseList.builder()
                .club(clubMapper.map(rs))
                .course(courseMapper.map(rs))
                .tee(teeMapper.map(rs))
                .build(),
                club.getIdclub()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()              { return liste; }
    public void setListe(List<ECourseList> liste)    { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Club club = new Club();
        club.setIdclub(101);
        List<ECourseList> ec = new ClubDetailList().list(club);
        LOG.debug("from main, ec = " + ec);
    } // end main
    */

} // end class
