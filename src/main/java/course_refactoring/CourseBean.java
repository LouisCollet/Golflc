
package course_refactoring;

import cache.GenericMethodCache;
//import entite.composite.ECourseList;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import java.sql.SQLException;

@Named("courseBean")
@ViewScoped
public class CourseBean implements Serializable {

    @Inject
    private CourseService courseService;

    @Inject
    private GenericMethodCache cache;

    @Resource
    private ManagedExecutorService executor;

    private List<ECourseList> courses = Collections.emptyList();

    private static final String CACHE_KEY = "CourseService#getAllCourses";

    @PostConstruct
    public void init() {
        LOG.debug("CourseBean init – asynchronous load");
        loadCoursesAsync();
    }

    public void loadCoursesAsync() {
        CompletableFuture
            .supplyAsync(this::loadCourses, executor)
            .orTimeout(30, TimeUnit.SECONDS)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    this.courses = result;
                    LOG.debug("Courses loaded, count: " + courses.size());
                } else {
                    LOG.error("Async load failed", ex);
                    this.courses = Collections.emptyList();
                }
                PrimeFaces.current().ajax().update("courseForm:dataTableCourses");
            });
    }

 private List<ECourseList> loadCourses() {
    try {
        List<ECourseList> cached = cache.getList(CACHE_KEY);

        if (cached != null) {
            LOG.debug("Courses served from cache");
            return cached;
        }

        List<ECourseList> result = courseService.getAllCourses();
        cache.put(CACHE_KEY, result, TimeUnit.MINUTES.toMillis(5));
        return result;

    } catch (Exception e) {
        LOG.error("Error loading courses", e);
        throw new RuntimeException(e);
    }
}

    public void refreshCourses() {
        cache.invalidate(CACHE_KEY);
        loadCoursesAsync();
    }

    public List<ECourseList> getCourses() {
        return courses;
    }
  void main() throws SQLException, Exception{
       LOG.debug("ListCourses depuis main :");
       // ne fonctionne pas comment tester ??
        CourseBean bean = new CourseBean();
        bean.init();

        // Attendre que l'exécution asynchrone finisse
        Thread.sleep(1000);

           LOG.debug("Courses depuis main :\n ");
        bean.getCourses().forEach(c -> 
            LOG.debug("club = " + c.club().getClubName() + " / " + c.course().getCourseName())
        );

        // Test cache
        bean.refreshCourses();
        Thread.sleep(1000);
        LOG.debug("Courses après refresh : \n");
        bean.getCourses().forEach(c -> 
             LOG.debug("club = " + c.club().getClubName() + " / " + c.course().getCourseName())
        );
        bean.executor.shutdown();
    }  
} // end class