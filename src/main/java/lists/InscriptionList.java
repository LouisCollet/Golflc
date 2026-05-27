
package lists;

import entite.Club;
import entite.Course;
import entite.Round;
import entite.composite.ECourseList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.RoundRowMapper;
import utils.LCUtil;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static interfaces.Log.LOG;

/**
 * Liste des inscriptions (rounds disponibles)
 * ✅ @ApplicationScoped — stateless, cache partagé
 * ✅ @Inject GenericDAO — plus de DataSource/Connection manuelle
 * ✅ cache d'instance — plus static
 */
@Named
@ApplicationScoped
public class InscriptionList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — plus static
    private List<ECourseList> liste = null;

    public InscriptionList() { }

    // ========================================
    // LIST
    // ========================================

    public List<ECourseList> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
                /* lists.InscriptionList.list */
                WITH selection AS (
                    SELECT * FROM round
                )
                SELECT * FROM selection
                    JOIN course
                        ON course.idcourse = selection.course_idcourse
                    JOIN club
                        ON club.idclub = course.club_idclub
                    ORDER BY roundDate DESC
                    LIMIT 30
                """;

        RowMapper<Club>       clubMapper   = new ClubRowMapper();
        RowMapper<Course>     courseMapper = new CourseRowMapper();
        RowMapperRound<Round> roundMapper  = new RoundRowMapper();

        liste = new ArrayList<>(dao.queryList(query, rs -> {
            Club club = clubMapper.map(rs);
            return ECourseList.builder()
                    .club(club)
                    .course(courseMapper.map(rs))
                    .round(roundMapper.map(rs, club))
                    .build();
        }));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    // ========================================
    // CACHE
    // ========================================

    public List<ECourseList> getListe()                 { return liste; }
    public void               setListe(List<ECourseList> liste) { this.liste = liste; }

    /**
     * Invalidation explicite du cache
     * ✅ Plus clair que setListe(null)
     */
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            List<ECourseList2> p1 = list();
            LOG.debug(methodName + " - number extracted = " + p1.size());
            LOG.debug(methodName + " - InscriptionList   = " + p1.toString());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end main
    */

} // end class
