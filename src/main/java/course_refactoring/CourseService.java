
package course_refactoring;

import cache.InvalidateCache;
import cache.Cached;
import entite.Course;
import entite.composite.ECourseList;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class CourseService {

    @Inject
    private CourseRepository repository;

    @Cached(ttl = 10, unit = TimeUnit.MINUTES) // @Cached(ttlMillis = 120_000)
    public List<ECourseList> getAllCourses() throws Exception {
        LOG.debug("entering getAllCourses");
        try {
            return repository.findAllValidCourses();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @InvalidateCache
    public void saveCourse(Course c) {
        // write DB
    }

    @InvalidateCache
    public void deleteCourse(long id) {
        // write DB
    }
} //end class