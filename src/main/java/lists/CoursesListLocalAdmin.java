package lists;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import entite.Club;
import entite.Course;
import entite.Player;
import entite.composite.ECourseList;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import static interfaces.Log.LOG;

@ApplicationScoped
public class CoursesListLocalAdmin implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private transient Cache<Integer, List<ECourseList>> cache;

    public CoursesListLocalAdmin() { }

    @PostConstruct
    void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    } // end method

    public List<ECourseList> list(final Player localAdmin) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        int adminId = localAdmin.getIdplayer();
        List<ECourseList> cached = cache.getIfPresent(adminId);
        if (cached != null) {
            LOG.debug("returning cached list size = {}", cached.size());
            return cached;
        }

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
                adminId);

        if (result.isEmpty()) {
            LOG.warn("empty result list for adminId = {}", adminId);
        } else {
            LOG.debug("list size = {}", result.size());
            cache.put(adminId, result);
        }
        return result;
    } // end method

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cache.invalidateAll();
        LOG.debug("cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player localAdmin = new Player();
        localAdmin.setIdplayer(324715);
        List<ECourseList> lp = list(localAdmin);
        LOG.debug("from main, after lp = {}", lp);
    } // end main
    */

} // end class
