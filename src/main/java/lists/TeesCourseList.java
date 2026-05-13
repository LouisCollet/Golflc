package lists;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import entite.Tee;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rowmappers.TeeRowMapper;
import static interfaces.Log.LOG;

@ApplicationScoped
public class TeesCourseList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private transient Cache<Integer, List<Tee>> cache;

    public TeesCourseList() { }

    @PostConstruct
    void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(50)
                .build();
    } // end method

    public List<Tee> list(final int courseId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for courseId = {}", courseId);

        List<Tee> cached = cache.getIfPresent(courseId);
        if (cached != null) {
            LOG.debug("returning cached list size = {}", cached.size());
            return cached;
        }

        final String query = """
            SELECT *
            FROM tee, course
            WHERE tee.course_idcourse = course.idcourse
              AND course.idcourse = ?
            """;

        List<Tee> result = dao.queryList(query, new TeeRowMapper(), courseId);

        if (result.isEmpty()) {
            LOG.warn("empty result list for courseId = {}", courseId);
        } else {
            LOG.debug("list size = {}", result.size());
            cache.put(courseId, result);
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
        List<Tee> tees = new TeesCourseList().list(41);
        LOG.debug("tee list for a course = {}", tees.size());
        tees.forEach(item -> LOG.debug("Tee list for a Course {}", item));
    } // end main
    */

} // end class
