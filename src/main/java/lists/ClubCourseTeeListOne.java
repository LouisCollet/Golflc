package lists;

import entite.Club;
import entite.Course;
import entite.Tee;
import entite.composite.ECourseList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import utils.LCUtil;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static interfaces.Log.LOG;

/**
 * Liste Club/Course/Tee pour un club donne
 * Migre vers GenericDAO (2026-03-18)
 */
@ApplicationScoped
public class ClubCourseTeeListOne implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    private static final String QUERY = """
        SELECT *
        FROM club
        INNER JOIN course ON club.idclub = course.club_idclub
        LEFT JOIN tee ON tee.course_idcourse = course.idcourse
        WHERE club.idclub = ?
        ORDER BY idclub, idcourse, teegender DESC, teestart DESC
        """;

    // ========================================
    // METHODE PRINCIPALE
    // ========================================

    public List<ECourseList> list(Club club) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (club == null || club.getIdclub() == null || club.getIdclub() <= 0) {
            LOG.warn("{} - club is null or has no ID", methodName);
            return Collections.emptyList();
        }

        if (liste != null) {
            LOG.debug("{} - returning cached list ({} entries)", methodName, liste.size());
            return liste;
        }

        RowMapper<Club>   clubMapper   = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapper<Tee>    teeMapper    = new TeeRowMapper();

        liste = dao.queryList(QUERY, rs -> ECourseList.builder()
                .club(clubMapper.map(rs))
                .course(courseMapper.map(rs))
                .tee(teeMapper.map(rs))
                .build(),
                club.getIdclub());

        if (liste.isEmpty()) {
            LOG.warn("Empty Result List in {} for club {}", methodName, club.getIdclub());
        } else {
            LOG.debug("{} - ResultSet has {} lines for club {}",
                     methodName, liste.size(), club.getIdclub());
        }
        return liste;
    } // end method

    // ========================================
    // CACHE - Getters / Setters
    // ========================================

    public List<ECourseList> getListe()               { return liste; }
    public void setListe(List<ECourseList> liste)     { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

} // end class
