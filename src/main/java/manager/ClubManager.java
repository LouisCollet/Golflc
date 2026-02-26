package manager;

import entite.*;
import entite.composite.ECourseList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.util.ArrayList;
import static utils.LCUtil.showMessageInfo;

/**
 * Service métier COMPLET pour la gestion des clubs, courses, tees et holes
 * ✅ @ApplicationScoped - Stateless, partagé entre tous
 * ✅ Pattern SaveResult uniforme
 * ✅ Gère toute la hiérarchie : Club → Course → Tee → Hole
 */
@ApplicationScoped
public class ClubManager implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // INJECTIONS CDI - Services CRUD
    // ========================================
    
    // Services Club
    @Inject private create.CreateClub createClubService;
    @Inject private update.UpdateClub updateClubService;
    @Inject private read.ReadClub readClubService;
    @Inject private delete.DeleteClub deleteClubService;
    
    // Services Course
    @Inject private create.CreateCourse createCourseService;
    @Inject private update.UpdateCourse updateCourseService;
    @Inject private read.ReadCourse readCourseService;
    @Inject private delete.DeleteCourse deleteCourseService;
    
    // Services Tee
    @Inject private create.CreateTee createTeeService;
    @Inject private update.UpdateTee updateTeeService;
    @Inject private read.ReadTee readTeeService;
    @Inject private delete.DeleteTee deleteTeeService;
    
    // Services Hole
    @Inject private create.CreateHole createHoleService;
    @Inject private read.ReadHole readHoleService;
    @Inject private update.UpdateHole updateHoleService;
    @Inject private delete.DeleteHole deleteHoleService;

    // Services Lists
    @Inject private lists.ClubList            clubListService;
    @Inject private lists.ClubsListLocalAdmin clubsListLocalAdminService;
    @Inject private lists.CourseList          courseListService;
    @Inject private lists.TeesCourseList      teeListService;
    @Inject private lists.HoleList            holeListService;
    @Inject private lists.ClubCourseTeeListOne clubCourseTeeListOneService;
    @Inject private lists.SunriseSunsetList   sunriseSunsetList;
    @Inject private lists.AllFlightsList      allFlightsList;
    @Inject private create.CreateTableFlights createTableFlightsService;
    @Inject private lists.FlightAvailableList flightAvailableList;
    @Inject private find.FindSlopeRating      findSlopeRating;
    @Inject private lists.ClubDetailList      clubDetailListService;         // migrated 2026-02-24
    @Inject private lists.CourseListForClub   courseListForClubService;      // migrated 2026-02-24
    @Inject private lists.CoursesListLocalAdmin coursesListLocalAdminService; // migrated 2026-02-24
    // ========================================
    // CLUB - CRUD
    // ========================================
    
    public SaveResult createClub(Club club) {
        LOG.debug("entering createClub");
        LOG.debug("with club = " + club);
        if (club == null) {
            return SaveResult.failure("Club cannot be null");
        }
        if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
            return SaveResult.failure("Club name is required");
        }
        
        try {
            boolean success = createClubService.create(club);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.ClubList.setListe(null);
            //    clubListService.invalidateCache();
                // lists.ClubsListLocalAdmin.setListe(null);
                clubsListLocalAdminService.invalidateCache();
                // was: lists.ClubDetailList.setListe(null);
                clubDetailListService.invalidateCache(); // migrated 2026-02-24

                String msg = String.format("Club created: %s (ID: %d)",
                    club.getClubName(), club.getIdclub());
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Club creation failed");
            }
        } catch (Exception e) {
            LOG.error("Exception creating club", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }
    
    public SaveResult modifyClub(Club club) {
        LOG.debug("entering modifyClub");
        if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
            return SaveResult.failure("Invalid club - ID required");
        }
        
        try {
            boolean success = updateClubService.update(club);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.ClubList.setListe(null);
           //     clubListService.invalidateCache();
                // lists.ClubsListLocalAdmin.setListe(null);
                clubsListLocalAdminService.invalidateCache();
                // was: lists.ClubDetailList.setListe(null);
                clubDetailListService.invalidateCache(); // migrated 2026-02-24

                String msg = String.format("Club updated: %s (ID: %d)",
                    club.getClubName(), club.getIdclub());
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Club update failed");
            }
        } catch (Exception e) {
            LOG.error("Exception updating club", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    public Club readClub(int clubId) throws Exception {
        LOG.debug("entering readClub");
        if (clubId <= 0) {
            throw new IllegalArgumentException("Invalid club ID: " + clubId);
        }
        Club club = new Club();
        club.setIdclub(clubId);
        return readClubService.read(club);
    }

    public SaveResult deleteClub(int clubId) {
        LOG.debug("entering deleteClub");
        if (clubId <= 0) {
            return SaveResult.failure("Invalid club ID");
        }
        
        try {
            Club club = readClub(clubId);
            boolean success = deleteClubService.delete(club);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.ClubList.setListe(null);
        //        clubListService.invalidateCache();
                // lists.ClubsListLocalAdmin.setListe(null);
                clubsListLocalAdminService.invalidateCache();
                // lists.CourseList.setListe(null);
                courseListService.invalidateCache();
                // was: lists.CourseListForClub.setListe(null);
                courseListForClubService.invalidateCache(); // migrated 2026-02-24

                String msg = String.format("Club deleted: %s (ID: %d)",
                    club.getClubName(), clubId);
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Club deletion failed");
            }
        } catch (Exception e) {
            LOG.error("Exception deleting club {}", clubId, e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    public SaveResult deleteClubCascading(int clubId) {
         LOG.debug("entering deleteClubCascading");
        if (clubId <= 0) {
            return SaveResult.failure("Invalid club ID");
        }
        
        try {
            Club club = readClub(clubId);
            LOG.warn("⚠️ Cascading delete requested for club: {} (ID: {})", 
                     club.getClubName(), clubId);
            boolean success = deleteClubService.deleteCascading(club);
            
            if (success) {
                // ✅ Invalidation MASSIVE des caches
                // lists.ClubList.setListe(null);
//                clubListService.invalidateCache();
                // lists.ClubsListLocalAdmin.setListe(null);
                clubsListLocalAdminService.invalidateCache();
                // lists.CourseList.setListe(null);
  //              courseListService.invalidateCache();
                // was: lists.CourseListForClub.setListe(null);
                courseListForClubService.invalidateCache(); // migrated 2026-02-24
                // was: lists.CoursesListLocalAdmin.setListe(null);
                coursesListLocalAdminService.invalidateCache(); // migrated 2026-02-24
                // lists.TeesCourseList.setListe(null);
    //            teeListService.invalidateCache();
                // lists.HoleList.setListe(null);
      //          holeListService.invalidateCache();
                
                String msg = String.format("Club and all related data deleted: %s", 
                    club.getClubName());
                LOG.info(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Cascading deletion failed");
            }
        } catch (Exception e) {
            LOG.error("Exception in cascading delete for club {}", clubId, e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    // ========================================
    // COURSE - CRUD
    // ========================================
    
    public SaveResult createCourse(Course course, int clubId) {
          LOG.debug("entering createCourse");
          LOG.debug("with course = " + course);
          LOG.debug("with clubId = " + clubId);
        if (course == null) {
            return SaveResult.failure("Course cannot be null");
        }
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            return SaveResult.failure("Course name is required");
        }
        if (clubId <= 0) {
            return SaveResult.failure("Valid club ID is required");
        }
        try {
            course.setClub_idclub(clubId);
            LOG.debug("Creating course '{}' for club ID {}", course.getCourseName(), clubId);
            boolean success = createCourseService.create(course);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.CourseList.setListe(null);
             //   courseListService.invalidateCache();
                // was: lists.CourseListForClub.setListe(null);
                courseListForClubService.invalidateCache(); // migrated 2026-02-24
                // was: lists.CoursesListLocalAdmin.setListe(null);
                coursesListLocalAdminService.invalidateCache(); // migrated 2026-02-24
                // lists.ClubCourseTeeListOne.setListe(null);
            //    clubCourseTeeListOneService.invalidateCache();

                String msg = String.format("Course created: %s (ID: %d) for club %d",
                    course.getCourseName(), course.getIdcourse(), clubId);
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Course creation failed");
            }
        } catch (Exception e) {
            LOG.error("Exception creating course", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }
    
    public SaveResult modifyCourse(Course course, int clubId) {
         LOG.debug("entering modifyCourse");
        if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
            return SaveResult.failure("Invalid course - ID required");
        }
        if (clubId <= 0) {
            return SaveResult.failure("Valid club ID is required");
        }
        
        try {
            course.setClub_idclub(clubId);
            LOG.debug("Modifying course '{}' (ID: {}) for club {}", 
                     course.getCourseName(), course.getIdcourse(), clubId);
            boolean success = updateCourseService.update(course);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.CourseList.setListe(null);
             //   courseListService.invalidateCache();
                // was: lists.CourseListForClub.setListe(null);
                courseListForClubService.invalidateCache(); // migrated 2026-02-24
                // was: lists.CoursesListLocalAdmin.setListe(null);
                coursesListLocalAdminService.invalidateCache(); // migrated 2026-02-24
                // lists.ClubCourseTeeListOne.setListe(null);
             //   clubCourseTeeListOneService.invalidateCache();
                // was: find.FindSlopeRating.setListe(null);
                findSlopeRating.invalidateCache();      // migrated 2026-02-24
                
                String msg = String.format("Course updated: %s (ID: %d)", 
                    course.getCourseName(), course.getIdcourse());
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Course update failed");
            }
        } catch (Exception e) {
            LOG.error("Exception updating course", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    public Course readCourse(int courseId) throws Exception {
         LOG.debug("entering readCourse");
        if (courseId <= 0) {
            throw new IllegalArgumentException("Invalid course ID: " + courseId);
        }
        Course course = new Course();
        course.setIdcourse(courseId);
        return readCourseService.read(course);
    }

    public SaveResult deleteCourse(int courseId) {
         LOG.debug("entering deleteCourse");
        if (courseId <= 0) {
            return SaveResult.failure("Invalid course ID");
        }
        
        try {
            Course course = readCourse(courseId);
            boolean success = deleteCourseService.delete(course);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.CourseList.setListe(null);
                courseListService.invalidateCache();
                // was: lists.CourseListForClub.setListe(null);
                courseListForClubService.invalidateCache(); // migrated 2026-02-24
                // was: lists.CoursesListLocalAdmin.setListe(null);
                coursesListLocalAdminService.invalidateCache(); // migrated 2026-02-24
                // lists.TeesCourseList.setListe(null);
             //   teeListService.invalidateCache();

                String msg = String.format("Course deleted: %s (ID: %d)",
                    course.getCourseName(), courseId);
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Course deletion failed");
            }
        } catch (Exception e) {
            LOG.error("Exception deleting course {}", courseId, e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    // ========================================
    // TEE - CRUD
    // ========================================
    
    public SaveResult createTee(Tee tee, int courseId) {
         LOG.debug("entering createTee");
        if (tee == null) {
            return SaveResult.failure("Tee cannot be null");
        }
        if (courseId <= 0) {
            return SaveResult.failure("Valid course ID is required");
        }
        
        try {
            tee.setCourse_idcourse(courseId);
            LOG.debug("Creating tee {} {} for course {}", 
                     tee.getTeeStart(), tee.getTeeGender(), courseId);
            boolean success = createTeeService.create(tee);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.TeesCourseList.setListe(null);
            //    teeListService.invalidateCache();
                // lists.ClubCourseTeeListOne.setListe(null);
            //    clubCourseTeeListOneService.invalidateCache();
                // was: find.FindSlopeRating.setListe(null);
                findSlopeRating.invalidateCache();      // migrated 2026-02-24

                String msg = String.format("Tee created: %s %s (ID: %d) for course %d",
                    tee.getTeeStart(), tee.getTeeGender(), tee.getIdtee(), courseId);
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Tee creation failed");
            }
        } catch (Exception e) {
            LOG.error("Exception creating tee", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    public SaveResult modifyTee(Tee tee, int courseId) {
         LOG.debug("entering modifyTee");
        if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
            return SaveResult.failure("Invalid tee - ID required");
        }
        if (courseId <= 0) {
            return SaveResult.failure("Valid course ID is required");
        }
        
        try {
            tee.setCourse_idcourse(courseId);
            LOG.debug("Modifying tee {} {} (ID: {})", 
                     tee.getTeeStart(), tee.getTeeGender(), tee.getIdtee());
            boolean success = updateTeeService.update(tee);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.TeesCourseList.setListe(null);
            //    teeListService.invalidateCache();
                // lists.ClubCourseTeeListOne.setListe(null);
             //   clubCourseTeeListOneService.invalidateCache();
                // was: find.FindSlopeRating.setListe(null);
                findSlopeRating.invalidateCache();      // migrated 2026-02-24

                String msg = String.format("Tee updated: %s %s (ID: %d)",
                    tee.getTeeStart(), tee.getTeeGender(), tee.getIdtee());
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Tee update failed");
            }
        } catch (Exception e) {
            LOG.error("Exception updating tee", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    public Tee readTee(int teeId) throws Exception {
         LOG.debug("entering readTee");
        if (teeId <= 0) {
            throw new IllegalArgumentException("Invalid tee ID: " + teeId);
        }
        Tee tee = new Tee();
        tee.setIdtee(teeId);
        return readTeeService.read(tee);
    }

    public SaveResult deleteTee(int teeId) {
         LOG.debug("entering deleteTee");
        if (teeId <= 0) {
            return SaveResult.failure("Invalid tee ID");
        }
        
        try {
            Tee tee = readTee(teeId);
            boolean success = deleteTeeService.delete(tee);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.TeesCourseList.setListe(null);
            //    teeListService.invalidateCache();
                // lists.ClubCourseTeeListOne.setListe(null);
             //   clubCourseTeeListOneService.invalidateCache();
                // lists.HoleList.setListe(null);
             //   holeListService.invalidateCache();
                
                String msg = String.format("Tee deleted: %s %s (ID: %d)",
                    tee.getTeeStart(), tee.getTeeGender(), teeId);
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Tee deletion failed");
            }
        } catch (Exception e) {
            LOG.error("Exception deleting tee {}", teeId, e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    // ========================================
    // HOLE - CRUD
    // ========================================
    
    public SaveResult createHole(Hole hole, int teeId, int courseId) {
        LOG.debug("entering createHole");
        
        if (hole == null) {
            return SaveResult.failure("Hole cannot be null");
        }
        if (hole.getHoleNumber() == null || hole.getHoleNumber() <= 0 || hole.getHoleNumber() > 18) {
            return SaveResult.failure("Hole number must be between 1 and 18");
        }
        if (teeId <= 0) {
            return SaveResult.failure("Valid tee ID is required");
        }
        if (courseId <= 0) {
            return SaveResult.failure("Valid course ID is required");
        }
        
        try {
            hole.setTee_idtee(teeId);
            hole.setTee_course_idcourse(courseId);
            LOG.debug("Creating hole #{} for tee {} and course {}", 
                     hole.getHoleNumber(), teeId, courseId);
            boolean success = createHoleService.create(hole);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.HoleList.setListe(null);
            //    holeListService.invalidateCache();

                String msg = String.format("Hole #%d created (ID: %d) for tee %d", 
                    hole.getHoleNumber(), hole.getIdhole(), teeId);
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Hole creation failed");
            }
        } catch (Exception e) {
            LOG.error("Exception creating hole", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    public HolesGlobal readHole(Tee tee) throws Exception {
        if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
            throw new IllegalArgumentException("Valid tee is required");
        }
        return readHoleService.read(tee);
    }

    public SaveResult deleteHole(int holeId) {
        if (holeId <= 0) {
            return SaveResult.failure("Invalid hole ID");
        }
        
        try {
            boolean success = deleteHoleService.delete(holeId);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.HoleList.setListe(null);
           //     holeListService.invalidateCache();

                String msg = String.format("Hole deleted (ID: %d)", holeId);
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Hole deletion failed");
            }
        } catch (Exception e) {
            LOG.error("Exception deleting hole {}", holeId, e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    public SaveResult updateHolesGlobal(HolesGlobal holesGlobal, Tee tee) {
        
        LOG.debug("entering updteHolesGlobal");
        LOG.debug("with holesGlobal = " + holesGlobal);
        LOG.debug("with Tee = " + tee);
        if (holesGlobal == null || holesGlobal.getDataHoles() == null) {
            return SaveResult.failure("Holes data cannot be null");
        }
        if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
            return SaveResult.failure("Valid tee is required");
        }
        try {
            boolean success = updateHoleService.update(holesGlobal, tee);
            
            if (success) {
                // ✅ Invalidation des caches
                // lists.HoleList.setListe(null);
           //     holeListService.invalidateCache(); 
                
         //       String msg = String.format("Batch update: %d holes updated for tee %d",
         //           holesGlobal.getDataHoles().length, tee.getIdtee());
                // ✅ Cohérent avec le reste du code migré
               String msg = "Update Holes Global: " + holesGlobal.getDataHoles().length + " holes updated for tee " + tee.getIdtee();
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Batch holes update failed");
            }
        } catch (Exception e) {
            LOG.error("Exception in batch holes update", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    // ========================================
    // LISTS - Méthodes de listing
    // ========================================
    
    /**
     * Liste tous les clubs
     */
    public List<Club> listClubs() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            List<Club> result = clubListService.list();
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * Liste les courses pour un club
     */
// ✅ APRÈS — corriger la méthode
public List<ECourseList> listCoursesForClub(int clubId) {
    if (clubId <= 0) {
        LOG.warn("Invalid club ID: {}", clubId);
        return Collections.emptyList();
    }
    try {
        Club club = new Club();
        club.setIdclub(clubId);
        List<ECourseList> courses = clubCourseTeeListOneService.list(club); // ✅ clubId passé
        LOG.debug("Found {} courses/tees for club {}", 
                 courses != null ? courses.size() : 0, clubId);
        return (courses != null) ? courses : Collections.emptyList();
    } catch (Exception e) {
        LOG.error("Error listing courses for club {}", clubId, e);
        return Collections.emptyList();
    }
}

    /**
     * Liste les tees pour un course
     */
    public List<Tee> listTeesForCourse(int courseId) {
        if (courseId <= 0) {
            LOG.warn("Invalid course ID: {}", courseId);
            return Collections.emptyList();
        }
        
        try {
            List<Tee> tees = teeListService.list(courseId); // ✅ Service injecté
            LOG.debug("Found {} tees for course {}", 
                     tees != null ? tees.size() : 0, courseId);
            return (tees != null) ? tees : Collections.emptyList();
        } catch (Exception e) {
            LOG.error("Error listing tees for course {}", courseId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Liste les holes pour un tee
     */
    public List<Hole> listHolesForTee(int teeId) {
        if (teeId <= 0) {
            LOG.warn("Invalid tee ID: {}", teeId);
            return Collections.emptyList();
        }
        
        try {
            List<Hole> holes = holeListService.listForTee(teeId); // ✅ Service injecté
            LOG.debug("Found {} holes for tee {}", 
                     holes != null ? holes.size() : 0, teeId);
            return (holes != null) ? holes : Collections.emptyList();
        } catch (Exception e) {
            LOG.error("Error listing holes for tee {}", teeId, e);
            return Collections.emptyList();
        }
    }
// ========== MÉTHODE À AJOUTER dans ClubManager ==========
/**
 * Calcule les flights disponibles pour le round, club et course courants
 * 1. Récupère sunrise/sunset via API
 * 2. Génère la table des flights (toutes les 12 min)
 * 3. Insère en base
 * 4. Retourne les flights non encore réservés
 */
public List<Flight> computeAvailableFlights(Round round, Club club, Course course) throws SQLException {
    final String methodName = "ClubManager.computeAvailableFlights";

    // Étape 1 — sunrise / sunset via API
    Flight flight = sunriseSunsetList.list(round, club);            // ✅ injecté
    LOG.debug("{} - step 1 flight={}", methodName, flight);
    if (flight == null) {
        LOG.error("{} - flight is null after SunriseSunsetList", methodName);
        return Collections.emptyList();
    }

    // Étape 2 — génération de la table des flights (toutes les 12 min)
    ArrayList<Flight> flights = allFlightsList.createTableFlights(  // ✅ injecté
            flight, club.getAddress().getZoneId());
    LOG.debug("{} - step 2 flightList size={}", methodName, flights.size());

    // Étape 3 — insertion en base + récupération des flights disponibles
    if (createTableFlightsService.create(flights, course)) {        // ✅ injecté
        LOG.debug("{} - step 3 CreateTableFlights OK", methodName);

        // Invalidation du cache avant de relire les flights disponibles
        // lists.FlightAvailableList.setListe(null);
    //    flightAvailableList.invalidateCache();
        return flightAvailableList.listAllFlights();                 // ✅ injecté
    }

    LOG.error("{} - step 3 CreateTableFlights FAILED", methodName);
    return Collections.emptyList();
}

    // ========================================
    // UTILITAIRES
    // ========================================
    
    public boolean clubExists(int clubId) {
        try { 
            readClub(clubId); 
            return true; 
        } catch (Exception e) { 
            return false; 
        }
    }

    public boolean courseExists(int courseId) {
        try { 
            readCourse(courseId); 
            return true; 
        } catch (Exception e) { 
            return false; 
        }
    }

    public boolean teeExists(int teeId) {
        try { 
            readTee(teeId); 
            return true; 
        } catch (Exception e) { 
            return false; 
        }
    }

    public int countCoursesForClub(int clubId) { 
        return listCoursesForClub(clubId).size(); 
    }
    
    public int countTeesForCourse(int courseId) { 
        return listTeesForCourse(courseId).size(); 
    }
    
    public int countHolesForTee(int teeId) { 
        return listHolesForTee(teeId).size(); 
    }

    // ========================================
    // SAVE RESULT PATTERN
    // ========================================
    
    public static class SaveResult implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final boolean success;
        private final String message;
        
        private SaveResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static SaveResult success(String message) { 
            return new SaveResult(true, message); 
        }
        
        public static SaveResult failure(String message) { 
            return new SaveResult(false, message); 
        }
        
        public boolean isSuccess() { 
            return success; 
        }
        
        public String getMessage() { 
            return message; 
        }

        @Override
        public String toString() {
            return String.format("SaveResult{success=%s, message='%s'}", success, message);
        }
    }

} // end class