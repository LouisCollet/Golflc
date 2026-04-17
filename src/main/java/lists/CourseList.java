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
import utils.LCUtil;

/**
 * Liste complète des clubs/courses/tees
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Inject GenericDAO — plus de DataSource/Connection manuelle
 * ✅ Lazy loading : si liste != null, retourne le cache
 */
@ApplicationScoped
public class CourseList implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    @Inject private dao.GenericDAO dao;

    /**
     * Liste complète clubs/courses/tees
     *
     * @return Liste des ECourseList
     * @throws SQLException en cas d'erreur
     */
    public List<ECourseList> list() throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        // ✅ LAZY LOADING CONSERVÉ
        if (liste != null) {
            return liste;
        }

        LOG.debug(" ... entering {}", methodName);

        final String query = """
                SELECT *
                FROM club, course, tee
                WHERE club.idclub = course.club_idclub
                    AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
                    AND tee.course_idcourse = course.idcourse
                GROUP BY idcourse, idtee
                ORDER by clubname, coursename, idtee, teestart
                """;

        RowMapper<Club>   clubMapper   = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapper<Tee>    teeMapper    = new TeeRowMapper();

        List<ECourseList> rows = dao.queryList(query, rs -> {
            //ECourseList ecl = new ECourseList();
            //   ecl.setClub(entite.Club.dtoMapper(rs)); // mod
            return ECourseList.builder()
                .club(clubMapper.map(rs))
                .course(courseMapper.map(rs))
                .tee(teeMapper.map(rs))
                .build();
        });

        liste = new ArrayList<>(rows);
        if (liste.isEmpty()) {
            String msg = "££ Empty Result List in " + methodName;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } else {
            LOG.debug("ResultSet {} has {} lines.", methodName, liste.size());
        }
        //     liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda
        // return liste;
        return List.copyOf(liste); // new 02-12-2025 non testé immutable eviter erreur modification ??
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()               { return liste; }
    public void setListe(List<ECourseList> liste)     { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method
} // end class
