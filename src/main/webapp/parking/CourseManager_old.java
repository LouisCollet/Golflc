package manager;

import entite.*;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class CourseManager implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Services CRUD injectés
    @Inject private create.CreateCourse createCourseService;
    @Inject private create.CreateTee createTeeService;
    @Inject private create.CreateHole createHoleService;
    @Inject private update.UpdateCourse updateCourseService;
    @Inject private update.UpdateTee updateTeeService;
    @Inject private lists.CourseList courseListService;

    /**
     * Crée un course
     */
    public void createCourse(Course course) throws SQLException, Exception {
        LOG.debug("CourseManager.createCourse for course: " + course.getCourseName());
        createCourseService.create(course);  // ✅ Le service gère sa connection
        
        // ✅ Invalidation des caches concernés
        lists.CourseList.setListe(null);
        lists.CourseListForClub.setListe(null);
        lists.CoursesListLocalAdmin.setListe(null);
        lists.ClubCourseTeeListOne.setListe(null);
        find.FindSlopeRating.setListe(null);
    }

    /**
     * Modifie un course
     */
    public void modifyCourse(Course course) throws SQLException, Exception {
        LOG.debug("CourseManager.modifyCourse for course: " + course.getCourseName());
        updateCourseService.update(course);  // ✅ Le service gère sa connection
        
        // ✅ Invalidation des caches concernés
        lists.CourseList.setListe(null);
        lists.CourseListForClub.setListe(null);
        lists.CoursesListLocalAdmin.setListe(null);
        lists.ClubCourseTeeListOne.setListe(null);
        find.FindSlopeRating.setListe(null);
    }

    /**
     * Crée un tee
     */
    public void createTee(Tee tee) throws SQLException, Exception {
        LOG.debug("CourseManager.createTee for tee: " + tee);
        createTeeService.create(tee);  // ✅ Le service gère sa connection
        
        // ✅ Invalidation des caches concernés
        lists.TeesCourseList.setListe(null);
        lists.ClubCourseTeeListOne.setListe(null);
        find.FindSlopeRating.setListe(null);
    }

    /**
     * Modifie un tee
     */
    public void modifyTee(Tee tee) throws SQLException, Exception {
        LOG.debug("CourseManager.modifyTee for tee: " + tee);
        updateTeeService.update(tee);  // ✅ Le service gère sa connection
        
        // ✅ Invalidation des caches concernés
        lists.TeesCourseList.setListe(null);
        lists.ClubCourseTeeListOne.setListe(null);
        find.FindSlopeRating.setListe(null);
    }

    /**
     * Crée un hole
     */
    public void createHole(Hole hole) throws SQLException, Exception {
        LOG.debug("CourseManager.createHole for hole: " + hole.getHoleNumber());
        createHoleService.create(hole);  // ✅ Le service gère sa connection
        
        // ✅ Invalidation des caches concernés (si nécessaire)
        // Généralement pas de cache sur les holes individuels
    }

    /**
     * Liste les courses d'un club
     */
    public List<ECourseList> listCoursesForClub(int clubId) throws SQLException, Exception {
        LOG.debug("CourseManager.listCoursesForClub for clubId: " + clubId);
        return courseListService.list();  // ✅ Le service gère sa connection
    }

} // end class