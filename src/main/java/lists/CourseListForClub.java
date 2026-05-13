package lists;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import entite.Club;
import entite.Course;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rowmappers.CourseRowMapper;
import static interfaces.Log.LOG;

@ApplicationScoped
public class CourseListForClub implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private transient Cache<Integer, List<Course>> cache;

    public CourseListForClub() { }

    @PostConstruct
    void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(50)
                .build();
    } // end method

    public List<Course> list(final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with club = {}", club);

        int clubId = club.getIdclub();
        List<Course> cached = cache.getIfPresent(clubId);
        if (cached != null) {
            LOG.debug("returning cached list size = {}", cached.size());
            return cached;
        }

        final String query = """
            SELECT *
            FROM course
            WHERE club_idclub = ?
            AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
            """;

        List<Course> result = dao.queryList(query, new CourseRowMapper(), clubId);

        if (result.isEmpty()) {
            LOG.warn("empty result list for clubId = {}", clubId);
        } else {
            LOG.debug("list size = {}", result.size());
            cache.put(clubId, result);
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
        Club club = new Club();
        club.setIdclub(102);
        var lp = new CourseListForClub().list(club);
        LOG.debug("from main, after lp = {}", lp);
    } // end main
    */

} // end class
