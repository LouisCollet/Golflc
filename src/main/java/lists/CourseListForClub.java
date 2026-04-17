package lists;

import entite.Club;
import entite.Course;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.CourseRowMapper;

@ApplicationScoped
public class CourseListForClub implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<Course> liste = null;

    public CourseListForClub() { }

    public List<Course> list(final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with club = " + club);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM course
            WHERE club_idclub = ?
            AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
            """;

        liste = new ArrayList<>(dao.queryList(query, new CourseRowMapper(), club.getIdclub()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    // ✅ Getters/setters d'instance
    public List<Course> getListe()              { return liste; }
    public void setListe(List<Course> liste)    { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Club club = new Club();
        club.setIdclub(102);
        var lp = new CourseListForClub().list(club);
        LOG.debug("from main, after lp = " + lp);
    } // end main
    */

} // end class
