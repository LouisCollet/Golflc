package lists;

import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;

@Named
@ApplicationScoped
public class CourseListOnly implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<ECourseList> liste = null;

    public CourseListOnly() { }

    public List<ECourseList> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
                SELECT *
                FROM club, course
                WHERE club.idclub = course.club_idclub
                   AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
                GROUP by idcourse
                ORDER by clubname, coursename
                """;

        RowMapper<ECourseList> compositeMapper = rs -> ECourseList.builder()
                .club(new ClubRowMapper().map(rs))
                .course(new CourseRowMapper().map(rs))
                .build();

        liste = dao.queryList(query, compositeMapper);
        return liste;
    } // end method

    public List<ECourseList> getListe()                       { return liste; }
    public void              setListe(List<ECourseList> liste) { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        List<ECourseList> lp = new CourseListOnly().list();
        LOG.debug("nombre de courses = " + lp.size());
        lp.forEach(item -> LOG.debug("Course list " + item.course().getCourseName()));
    } // end main
    */

} // end class
