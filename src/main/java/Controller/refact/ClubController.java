package Controller.refact;

import Controllers.DialogController;
import context.ApplicationContext;
import entite.*;
import entite.composite.ECourseList;
import entite.composite.EPlayerPassword;
import entite.composite.EUnavailable;
import enumeration.ClubSelectionPurpose;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import manager.ClubManager;
import service.CountryService;
import java.io.Serializable;
import jakarta.faces.annotation.SessionMap;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;
import static interfaces.Log.LOG;
import service.CoordinatesService;
import jakarta.faces.context.FacesContext;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@Named("clubC")
@SessionScoped
public class ClubController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private ClubManager clubManager;
    @Inject private CountryService countryService;
    @Inject private ApplicationContext appContext;
// ========== EN HAUT DE LA CLASSE (avec les autres @Inject) ==========
    @Inject private DialogController dialogController; // migration manuelle
    // ========== EN HAUT DE LA CLASSE (avec les autres @Inject) ==========
    @Inject private lists.CourseListForClub courseListForClubService; // migrated 2026-02-24
    @Inject private jakarta.faces.context.ExternalContext externalContext;  // ✅ injecté    
    @Inject private CoordinatesService coordinatesService;
    @Inject private contexte.ClubSelectionContextBean clubSelectionContext; // migrated 2026-02-25
    @Inject private read.ReadClub readClubService; // migrated 2026-02-25
    @Inject private read.ReadCourse readCourseService; // migrated 2026-02-25
    @Inject private read.ReadTee readTeeService; // migrated 2026-02-25
    @Inject private read.ReadHole readHoleService; // migrated 2026-02-25
    @Inject private Controllers.UnavailableController unavailableController; // migrated 2026-02-25
    @Inject @SessionMap private Map<String, Object> sessionMap; // migrated 2026-02-25
    @Inject private read.ReadUnavailableStructure readUnavailableStructure; // migrated 2026-02-25
    @Inject private Controllers.CourseController courseController; // migrated 2026-02-25 — pour reset() legacy
    @Inject private create.CreateUnavailablePeriod createUnavailablePeriodService; // migrated 2026-02-25 — Groupe D
    @Inject private lists.UnavailableListForDate unavailableListForDate; // migrated 2026-02-25 — Groupe D
    @Inject private lists.CourseListOnly courseListOnly; // migrated 2026-02-25 — Groupe B
    @Inject private lists.ClubDetailList clubDetailList; // migrated 2026-02-25 — Groupe B
    @Inject private lists.CourseList courseListService; // migrated 2026-02-25 — Groupe B
    @Inject private find.FindTarifMembersData findTarifMembersData; // migrated 2026-02-25 — Groupe C
    @Inject private Controller.refact.PlayerController playerController; // migrated 2026-02-25 — Groupe C (createModifyPlayer)
    @Inject private Controllers.ChartController chartController; // migrated 2026-02-26

    // ========== CHAMPS À AJOUTER avec les autres champs ==========
private List<Flight> flightList = Collections.emptyList();
private int cptFlight = 0;

    private Tee tee;
    private Hole hole;
    private Country country;
    private HolesGlobal holesGlobal;

    private List<Course> courseListForClub;
    private List<Tee> teeListForCourse;
    private List<Hole> holeListForTee;
    private List<Club> filteredClubs;

    private boolean STROKEINDEX = true;
    private List<ECourseList> filteredCourses = null; // PrimeFaces DataTable filtering
    private String lineModelCourse; // migrated 2026-02-25 from CourseController
    private TarifMember tarifMember; // migrated 2026-02-25 from CourseController
// ========== CHAMPS À AJOUTER ==========

private EPlayerPassword selectedPlayerEPP = null;

    @PostConstruct
    public void init() {  // à synchroniser plus tard
        LOG.debug("ClubController @PostConstruct - initializing");
        tee         = new Tee();
        hole        = new Hole();
        country     = new Country();
        holesGlobal = new HolesGlobal();
        courseListForClub   = Collections.emptyList();
        teeListForCourse    = Collections.emptyList();
        holeListForTee      = Collections.emptyList();
        filteredClubs       = Collections.emptyList();
        tarifMember         = new TarifMember();
        lineModelCourse     = null;
        LOG.debug("ClubController initialized");
    }

    // ========================================
    // Délégation - Club et Course
    // ========================================

    public Club getClub() {
        return appContext.getClub(); 
    }

    public void setClub(Club club) {
        appContext.setClub(club);   
        if (club != null && club.getIdclub() != null && club.getIdclub() > 0) {
            loadCoursesForClub(club.getIdclub());
        } else {
            courseListForClub = Collections.emptyList();
        }
    }

    public Course getCourse() {
        return appContext.getCourse();
    }

    public void setCourse(Course course) {
        appContext.setCourse(course);
        if (course != null && course.getIdcourse() != null && course.getIdcourse() > 0) {
            loadTeesForCourse(course.getIdcourse());
        } else {
            teeListForCourse = Collections.emptyList();
        }
    }

    // ========================================
    // CREATE - Club
    // ========================================

    public String createClub() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Club club = appContext.getClub();
            LOG.debug("with club = " + club);
            LOG.debug("alternative par délégatiion? with club = " + getClub());

            if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
                showMessageFatal("Club name is required");
                return null;
            }

            ClubManager.SaveResult result = clubManager.createClub(club);

            if (result.isSuccess()) {
                LOG.debug("club created : we go to course !!");
                Course course = appContext.getCourse();     // ✅ current supprimé
                course.setNextCourse(true);
             //   showMessageInfo(result.getMessage());
                return "course.xhtml?faces-redirect=true";
            } else {
                LOG.error("Club creation failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // UPDATE - Club
    // ========================================

    public String modifyClub() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Club club = appContext.getClub();               // ✅ current supprimé

            LOG.debug("club to be modified = {}", club.toString());

            if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
                showMessageFatal("No club selected for modification");
                return null;
            }
            if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
                showMessageFatal("Club name is required");
                return null;
            }

            ClubManager.SaveResult result = clubManager.modifyClub(club);

            if (result.isSuccess()) {
                LOG.debug("club is Modified !!");
                Course course = appContext.getCourse();     // ✅ current supprimé
                course.setNextCourse(false);
                showMessageInfo(result.getMessage());
            } else {
                LOG.error("Club modification failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
            }
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // DELETE - Club
    // ========================================

    public String deleteClub(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " for {}", ecl.club());
            Club club = ecl.club();
            if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
                showMessageFatal("No club selected for deletion");
                return null;
            }
            int clubId = club.getIdclub();
            ClubManager.SaveResult result = clubManager.deleteClub(clubId);
            boolean OK = result.isSuccess();
            LOG.debug("result of deleteClub = {}", OK);
            if (OK) {
                listCourses();
                showMessageInfo(result.getMessage());
                return "deleteClubCourseTee.xhtml?faces-redirect=true";
            } else {
                LOG.error("Club deletion failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // CREATE - Course
    // ========================================

    public String createCourse() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Club club     = appContext.getClub(); 
            Course course = appContext.getCourse();

            LOG.debug("with club = " + club);
            LOG.debug("alternative via délégation with club = " + getClub());
            LOG.debug("with course = " + course);
            LOG.debug("start to create course, clubID = {}", club.getIdclub());

            if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
                showMessageFatal("Please select a club first");
                return null;
            }
            if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
                showMessageFatal("Course name is required");
                return null;
            }

            ClubManager.SaveResult result = clubManager.createCourse(course, club.getIdclub());

            if (result.isSuccess()) {
                
                tee.setNextTee(true);
                loadCoursesForClub(club.getIdclub());  /// ??? why ??
               // showMessageInfo(result.getMessage());
                LOG.debug(result.getMessage());
                LOG.debug("course created, next step = tee");
                return "tee.xhtml?faces-redirect=true";
            } else {
                LOG.error("Course creation failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // UPDATE - Course
    // ========================================

    public String modifyCourse() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Club club     = appContext.getClub();
            Course course = appContext.getCourse();

            LOG.debug("course to be modified = {}", course.toString());

            if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
                showMessageFatal("No club selected");
                return null;
            }
            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                showMessageFatal("No course selected for modification");
                return null;
            }
            if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
                showMessageFatal("Course name is required");
                return null;
            }

            ClubManager.SaveResult result = clubManager.modifyCourse(course, club.getIdclub());

            if (result.isSuccess()) {
                String msg = "course Modified !!";
                LOG.info(msg);
                showMessageInfo(msg);
                loadCoursesForClub(club.getIdclub());
            } else {
                LOG.error("Course modification failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
            }
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // DELETE - Course
    // ========================================

    public String deleteCourse(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " for {}", ecl.course());
            Course course = ecl.course();
            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                showMessageFatal("No course selected for deletion");
                return null;
            }
            int courseId = course.getIdcourse();
            ClubManager.SaveResult result = clubManager.deleteCourse(courseId);
            boolean OK = result.isSuccess();
            LOG.debug("result of deleteCourse = {}", OK);
            if (OK) {
                listCourses();
                showMessageInfo(result.getMessage());
                return "deleteClubCourseTee.xhtml?faces-redirect=true";
            } else {
                LOG.error("Course deletion failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // CREATE - Tee
    // ========================================

    public String createTee() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Course course = appContext.getCourse();
            LOG.debug("with course = " + course);
            LOG.debug("with tee = " + tee);
            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                showMessageFatal("Please select a course first");
                return null;
            }
            if (tee.getTeeSlope() == null || tee.getTeeRating() == null) {
                showMessageFatal("Tee slope and rating are required");
                return null;
            }

            ClubManager.SaveResult result = clubManager.createTee(tee, course.getIdcourse());

            if (result.isSuccess()) {
                LOG.debug("tee created : we go to hole !!");
                hole.setNextHole(true);

                try {
                    Tee t = clubManager.readTee(tee.getIdtee());
                    if (t == null || t.isNotFound()) {
                        LOG.debug("Tee not found! = {}", t);
                    } else {
                        tee = t;
                        LOG.debug("loaded tee = {}", t.toString());
                    }
                } catch (Exception e) {
                    LOG.warn("Could not reload tee: {}", e.getMessage());
                }

                loadTeesForCourse(course.getIdcourse());
               // showMessageInfo(result.getMessage());
                LOG.debug(result.getMessage());

                if (tee.getTeeStart().equals("YELLOW") &&
                    tee.getTeeGender().equals("M") &&
                    tee.getTeeHolesPlayed().equals("01-18")) {
                    LOG.debug("master tee ==> 3 lignes avec par, index et distances : la totale");
                    return "holes_global.xhtml?faces-redirect=true";

                } else if (!tee.getTeeStart().equals("YELLOW") &&
                           tee.getTeeGender().equals("M") &&
                           tee.getTeeHolesPlayed().equals("01-18")) {
                    LOG.debug("distance tee ==> 1 ligne avec les distances");
                    return "holes_distance.xhtml?faces-redirect=true";

                } else {
                    String msg = "No holes registration needed : we already have all the information with MasterTee and DistanceTee";
                    LOG.info(msg);
                    showMessageInfo(msg);
                    return null;
                }
            } else {
                LOG.error("Tee creation failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // UPDATE - Tee
    // ========================================

    public String modifyTee() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Course course = appContext.getCourse();         // ✅ current supprimé

            LOG.debug("tee to be modified = {}", tee.toString());

            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                showMessageFatal("No course selected");
                return null;
            }
            if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
                showMessageFatal("No tee selected for modification");
                return null;
            }
            if (tee.getTeeSlope() == null || tee.getTeeRating() == null) {
                showMessageFatal("Tee slope and rating are required");
                return null;
            }

            ClubManager.SaveResult result = clubManager.modifyTee(tee, course.getIdcourse());

            if (result.isSuccess()) {
                LOG.debug("tee Modified !!");
                tee.setNextTee(true);
                loadTeesForCourse(course.getIdcourse());
                showMessageInfo(result.getMessage());
            } else {
                LOG.error("Tee modification failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
            }
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // DELETE - Tee
    // ========================================

    public String deleteTee(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " for {}", ecl.tee());
        Tee tee = ecl.tee();
        if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
            showMessageFatal("No tee selected for deletion");
            return null;
        }
        int teeId = tee.getIdtee();
        ClubManager.SaveResult result = clubManager.deleteTee(teeId);
        boolean OK = result.isSuccess();
        LOG.debug("result of deleteTee = {}", OK);
        if (OK) {
            listCourses();
            showMessageInfo(result.getMessage());
            return "deleteClubCourseTee.xhtml?faces-redirect=true";
        } else {
            LOG.error("Tee deletion failed: {}", result.getMessage());
            showMessageFatal(result.getMessage());
            return null;
        }
    } // end method

    // ========================================
    // CREATE - Hole
    // ========================================

    public void createHole() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Course course = appContext.getCourse();
               LOG.debug("with course = " + course);
               LOG.debug("with tee = " + tee);
            if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
                showMessageFatal("Please select a tee first");
                return;
            }
            if (course == null || course.getIdcourse() == null) {
                showMessageFatal("Please select a course first");
                return;
            }
            if (hole.getHoleNumber() == null || hole.getHoleNumber() < 1 || hole.getHoleNumber() > 18) {
                showMessageFatal("Hole number must be between 1 and 18");
                return;
            }

            ClubManager.SaveResult result = clubManager.createHole(
                hole,
                tee.getIdtee(),
                course.getIdcourse()
            );

            if (result.isSuccess()) {
                LOG.debug("hole created : we go to hole !!");
                setNextStep(true);
                loadHolesForTee(tee.getIdtee());
                hole = new Hole();
              //  showMessageInfo(result.getMessage());
                LOG.debug(result.getMessage());
            } else {
                LOG.error("Hole creation failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method

    // ========================================
    // UPDATE - Holes (Batch)
    // ========================================

    public String createHolesGlobal(String param) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with param = {}", param);

            Club club     = appContext.getClub(); 
            Course course = appContext.getCourse();
            
                LOG.debug("for club = {}", club);
                LOG.debug("with course = " + course);
                LOG.debug("with tee = " + tee);
                LOG.debug("with holesGlobal = " + holesGlobal);
            if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
                showMessageFatal("No club selected");
                return null;
            }
            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                showMessageFatal("No course selected");
                return null;
            }
            if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
                showMessageFatal("No tee selected");
                return null;
            }
            if (holesGlobal == null || holesGlobal.getDataHoles() == null) {
                showMessageFatal("No holes data provided");
                return null;
            }

            holesGlobal.setType(param);
            ClubManager.SaveResult result = clubManager.updateHolesGlobal(holesGlobal, tee);

            if (result.isSuccess()) {
                String msg = utils.LCUtil.prepareMessageBean("hole.global.create");
                LOG.info(msg);
                showMessageInfo(msg);
                loadHolesForTee(tee.getIdtee());
            } else {
                String msg = "FAILURE Create Holes Global !!";
                LOG.error(msg);
                showMessageFatal(msg);
            }
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // DELETE - Holes
    // ========================================
/*
    public String deleteHoles(ECourseList2 ecl) {
        try {
            LOG.debug("entering deleteHoles for Tee = {}", ecl.tee());
            Tee tee = ecl.tee();
            if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
                showMessageFatal("No tee selected");
                return null;
            }
            int teeId = tee.getIdtee();
            ClubManager.SaveResult result = clubManager.deleteAllHolesForTee(teeId);
            boolean OK = result.isSuccess();
            LOG.debug("result of deleteHoles = {}", OK);
            if (OK) {
                // was: lists.CourseList.setListe(null);
                courseList.invalidateCache();   // migrated 2026-02-24
                listCourses();
                showMessageInfo(result.getMessage());
                return "deleteClubCourseTee.xhtml?faces-redirect=true";
            } else {
                showMessageFatal(result.getMessage());
                return null;
            }
        } catch (Exception ex) {
            String msg = "Exception in deleteHoles" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    }
*/
    // ========================================
    // Autocomplete - Pays
    // ========================================
/* transférée countryController
    public List<Country> completeCountry(String query) {
        try {
            if (query == null || query.trim().isEmpty()) return Collections.emptyList();

            List<Country> allCountries = countryService.getCountries();
            if (allCountries == null || allCountries.isEmpty()) {
                LOG.warn("Country list is empty from CountryService");
                return Collections.emptyList();
            }

            String lowerQuery = query.toLowerCase();
            List<Country> filtered = allCountries.stream()
                .filter(c -> c.getName() != null &&
                            c.getName().toLowerCase().contains(lowerQuery))
                .limit(10)
                .collect(Collectors.toList());

            LOG.debug("Found {} countries matching '{}'", filtered.size(), query);
            return filtered;
        } catch (Exception e) {
            LOG.error("Error in completeCountry", e);
            return Collections.emptyList();
        }
    } // end method
*/


    // ========================================
    // Helpers
    // ========================================

    public boolean hasClub() {
        Club c = appContext.getClub();                      // ✅ current supprimé
        return c != null && c.getIdclub() != null && c.getIdclub() > 0;
    }

    public boolean hasCourse() {
        Course c = appContext.getCourse();                  // ✅ current supprimé
        return c != null && c.getIdcourse() != null && c.getIdcourse() > 0;
    }

    public boolean hasTee() {
        return tee != null && tee.getIdtee() != null && tee.getIdtee() > 0;
    }

    public boolean canCreateCourse() { return hasClub(); }
    public boolean canCreateTee()    { return hasCourse(); }
    public boolean canCreateHole()   { return hasTee(); }

    public int getCourseCount() { return courseListForClub != null ? courseListForClub.size() : 0; }
    public int getTeeCount()    { return teeListForCourse  != null ? teeListForCourse.size()  : 0; }
    public int getHoleCount()   { return holeListForTee    != null ? holeListForTee.size()    : 0; }

    // ========================================
    // Chargement des données
    // ========================================

    public void loadClub(int clubId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        if (clubId <= 0) { LOG.warn("Invalid club ID: {}", clubId); showMessageFatal("Invalid club ID"); return; }
        try {
            Club club = clubManager.readClub(clubId);
            appContext.setClub(club);                       // ✅ current supprimé
            loadCoursesForClub(clubId);
            LOG.debug("Club loaded: {} (ID: {})", club.getClubName(), clubId);
            showMessageInfo("Club loaded: " + club.getClubName());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void loadCoursesForClub(int clubId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            var eclList = clubManager.listCoursesForClub(clubId);
            courseListForClub = eclList.stream().map(ecl -> ecl.course()).collect(java.util.stream.Collectors.toList());
            LOG.debug("Loaded {} courses for club {}", courseListForClub.size(), clubId);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            courseListForClub = Collections.emptyList();
        }
    } // end method

    public void loadCourse(int courseId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        if (courseId <= 0) { LOG.warn("Invalid course ID: {}", courseId); return; }
        try {
            Course course = clubManager.readCourse(courseId);
            appContext.setCourse(course);                   // ✅ current supprimé
            loadTeesForCourse(courseId);
            LOG.debug("Course loaded: {} (ID: {})", course.getCourseName(), courseId);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void loadTeesForCourse(int courseId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            teeListForCourse = clubManager.listTeesForCourse(courseId);
            LOG.debug("Loaded {} tees for course {}", teeListForCourse.size(), courseId);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            teeListForCourse = Collections.emptyList();
        }
    } // end method

    public void loadTee(int teeId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        if (teeId <= 0) { LOG.warn("Invalid tee ID: {}", teeId); return; }
        try {
            tee = clubManager.readTee(teeId);
            loadHolesForTee(teeId);
            LOG.debug("Tee loaded: {} {} (ID: {})", tee.getTeeStart(), tee.getTeeGender(), teeId);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void loadHolesForTee(int teeId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            holeListForTee = null;   // important à corriger !!!
                //    clubManager.listHolesForTee(teeId);

            LOG.debug("Loaded {} holes for tee {}", holeListForTee.size(), teeId);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            holeListForTee = Collections.emptyList();
        }
    } // end method

    private void listCourses() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Club club = appContext.getClub();               // ✅ current supprimé
            if (club != null && club.getIdclub() != null && club.getIdclub() > 0) {
                loadCoursesForClub(club.getIdclub());
                LOG.debug("Course list refreshed for club {}", club.getIdclub());
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

// ========== MÉTHODE MIGRÉE 19-02-2026 ==========
/**
 * Liste les clubs/courses/tees selon le paramètre
 * ✅ Plus de Connection, plus de throws SQLException
 * ✅ Délègue à ClubManager
 */
public List<ECourseList> listClubsCoursesTees(String param) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName + " with param=" + param);

    try {
        Club club = appContext.getClub();

        if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
            LOG.warn("{} - no club selected", methodName);
            return Collections.emptyList();
        }

        LOG.debug("{} with club={}", methodName, club.getIdclub());

        if ("one_club".equals(param)) {
            return clubManager.listCoursesForClub(club.getIdclub());  // ✅ via manager
        }

        // all_clubs commenté dans l'original — conservé commenté
        // if ("all_clubs".equals(param)) {
        //     return clubManager.listAllClubsCoursesTees();
        // }

        LOG.warn("{} - unknown param: {}", methodName, param);
        return Collections.emptyList();

    } catch (Exception ex) {
        handleGenericException(ex, methodName);
        return Collections.emptyList();
    }
} // end method
// ========== MÉTHODE MIGRÉE ==========
/* 19-02-2026
 * Sélection d'un club depuis un dialogue de sélection
 * @param selectedClub le club sélectionné
 * @return null (reste sur la même page)
 */
public String selectedClubFromDialog(Club selectedClub) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    LOG.debug("param club = " + selectedClub);
    LOG.debug("original/old club value = " + appContext.getClub());
    
    // ✅ Mise à jour du club via appContext
    // setClub() va automatiquement charger les courses via loadCoursesForClub()
    setClub(selectedClub);
    
    // ✅ Reset du course
    appContext.setCourse(new Course());
    
    // ✅ Invalidation du cache (déjà fait dans setClub via loadCoursesForClub, mais on peut garder pour être sûr)
    // was: lists.CourseListForClub.setListe(null);
    courseListForClubService.invalidateCache(); // migrated 2026-02-24
    
    LOG.debug("Exiting selectedClubFromDialog with club = " + appContext.getClub());
    
    // ✅ Fermeture du dialogue
    dialogController.closeDialog(null);
    
    return null;
}
    // ========== MÉTHODE MIGRÉE ==========
/**
 * Sélection d'un course depuis un dialogue de sélection
 * Même pattern que selectedClubFromDialog
 */
public String selectedCourseFromDialog(Course selectedCourse) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    LOG.debug("param course = " + selectedCourse);
    LOG.debug("original course value = " + appContext.getCourse());

    // ✅ setCourse() charge automatiquement les tees via loadTeesForCourse()
    setCourse(selectedCourse);

    // ✅ Fermeture du dialogue
    dialogController.closeDialog(null);

    LOG.debug("Exiting " + methodName + " with course = " + appContext.getCourse());
    return null;
}


// ========== MÉTHODE MIGRÉE ==========
/**
 * Liste tous les clubs pour le dialogue de sélection
 * @param type type de dialogue (non utilisé)
 * @return liste de tous les clubs
 */
public List<Club> listClubsDialog(String type) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    try {
        return clubManager.listClubs(); // ✅ via manager
    } catch (Exception ex) {
        handleGenericException(ex, methodName);
        return Collections.emptyList();
    }
} // end method
    // ========== MÉTHODE MIGRÉE 19-02-2026 ==========
/**
 * Liste les flights disponibles pour le round et club courants
 * ✅ Délègue entièrement à clubManager
 * ✅ Validations en amont dans le controller
 */
public List<Flight> listFlights() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    cptFlight++;
    try {
        // Guard clause — éviter les itérations multiples
        if (cptFlight != 1) {
            LOG.debug("{} - escaped repetition with cptFlight={}", methodName, cptFlight);
            return flightList;
        }
        LOG.debug("{} - starting with cptFlight=1", methodName);
            
        Round round  = appContext.getRound();
            LOG.debug("round from appContext = " + round); // comment est-il arrivé ici ? dans coursecontroller : modification de appContext.setRound(round);   // ✅ synchronise
        Club club   = appContext.getClub();
            LOG.debug("club from appContext = " + club);
        Course course = appContext.getCourse();
            LOG.debug("course from appContext = " + course);
        // Validation round
        if (round.getRoundDate() == null) {
            String msg = methodName + " Fatal error ! ClubController - round date is null";
            LOG.error(msg);
            showMessageFatal(msg);
            return Collections.emptyList();
        }

        // Validation coordonnées GPS
        if (club.getAddress().getLatLng().getLat() == 0) {
            String msg = methodName + " - club latitude is unknown";
            LOG.error(msg);
            showMessageFatal(msg);
            return Collections.emptyList();
        }

        // ✅ Délégation totale à clubManager — controller ne connaît pas les services
        flightList = clubManager.computeAvailableFlights(round, club, course);
        return flightList;
    } catch (Exception ex) {
        handleGenericException(ex, methodName);
        return Collections.emptyList();
    }
} // end method

public String findClubWebsite() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    try {
        Club club = appContext.getClub();                               // ✅ appContext
        LOG.debug("for club = " + club);

        if (club.getClubWebsite() == null || club.getClubWebsite().trim().isEmpty()) {
            showMessageFatal("Website must be completed !");
            return null;
        }

        // ✅ Ajouter https:// si pas déjà présent
        String url = club.getClubWebsite();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        LOG.debug(methodName + " - redirecting to = " + url);
        externalContext.redirect(url);
        return null;

    } catch (Exception e) {
        handleGenericException(e, methodName);
        return null;
    }
} // end method


/*
public String findClubWebsite(){ //used in player.xhtml
       LOG.debug("entering findClubWebsite " );
 try{ 
     
     Club club = getClub();
                LOG.debug("for club = " + club );  // a été complété par clubWebsiteListener, 
            if(club.getClubWebsite() == null){
                club.setClubWebsite("Website must be completed !");
                return null;
            }
            externalContext.redirect("http://" + club.getClubWebsite());  // https ???
            return null;
  }catch (Exception e){
            String msg = "Â£ Exception in CourseController - findClubWebsite = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
           // return null;
        }
} // end method
*/
        /**
     * Met à jour les coordonnées du joueur
     */
    public void updateCoordinates() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Club club = getClub();
            LOG.debug("pour club = " + club);
          //  coordinatesService.updateCoordinates(appContext.getClub());
             coordinatesService.updateCoordinates(club);
            showMessageInfo("Coordinates updated");
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method
    
    // Dans ClubController
// Dans ClubController
public void convertYtoM() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    LOG.debug("with hole = " + hole);
     LOG.debug("with hole via délégation = " + getHole());
    try {
        Short yards = hole.getHoleDistance();               // ✅ Short comme dans l'entité
        LOG.debug(methodName + " - yards = " + yards);

        if (yards == null || yards == 0) {
            showMessageFatal("Distance must be completed");
            return;
        }

        // Conversion yards → mètres (1 yard = 0.9144 m)
        Short metres = (short) Math.round(yards * 0.9144);
        hole.setHoleDistance(metres);                       // ✅ setHoleDistance(Short)

        LOG.debug(methodName + " - converted " + yards + " yards → " + metres + " metres");

    } catch (Exception e) {
        handleGenericException(e, methodName);
    }
}
/*

---

### Pourquoi ça fonctionne sans paramètre
```
1. Utilisateur saisit 350 dans holeDistance
2. Clic sur commandButton (ajax="false")
3. JSF soumet le formulaire → setHoleDistance(350) appelé automatiquement
4. convertYtoM() lit hole.getHoleDistance() → 350 ✅
5. setHoleDistance(320) → update="holeDistance" rafraîchit le champ
    */
    
    
    // ========================================
    // Load from ECourseList (DataTable row)
    // migrated from CourseController 2026-02-25
    // ========================================

    /**
     * Charge un club depuis une ligne du DataTable (modify_ClubCourseTee.xhtml)
     */
    public String loadClub(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Club club = readClubService.read(ecl.club());
            if (club != null) {
                appContext.setClub(club);
                club.setCreateModify(false);
                return "club.xhtml?faces-redirect=true&operation=modify";
            } else {
                String msg = "error : club not retrieved !!";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Prépare l'ajout d'un nouveau course pour un club (modify_ClubCourseTee.xhtml)
     */
    public String addCourse(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Club club = readClubService.read(ecl.club());
            appContext.setClub(club);
            LOG.debug("adding a course for idclub = " + ecl.club().getIdclub() + " " + club.getClubName());
            Course course = new Course();
            course.setCreateModify(true);
            appContext.setCourse(course);
            return "course.xhtml?faces-redirect=true&operation=add";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Charge un course depuis une ligne du DataTable (modify_ClubCourseTee.xhtml)
     */
    public String loadCourse(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Course course = readCourseService.read(ecl.course());
            Club club = readClubService.read(ecl.club());
            appContext.setClub(club);
            LOG.debug("idclub after loadCourse= " + club.getIdclub());
            if (club.getIdclub() == null) {
                club.setIdclub(course.getClub_idclub());
                LOG.error("Idclub forced because it was null");
                showMessageFatal("Idclub forced because it was null");
            }
            if (course != null) {
                appContext.setCourse(course);
                course.setCreateModify(false);
                return "course.xhtml?faces-redirect=true&operation=modify";
            } else {
                LOG.error("error : course not retrieved !!");
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Prépare l'ajout d'un nouveau tee pour un course (modify_ClubCourseTee.xhtml)
     */
    public String addTee(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Club club = readClubService.read(ecl.club());
            appContext.setClub(club);
            LOG.debug("club handled is " + club.getIdclub() + " : " + club.getClubName());
            Course course = readCourseService.read(ecl.course());
            appContext.setCourse(course);
            LOG.debug("course handled is " + course.getIdcourse() + " : " + course.getCourseName());
            Tee newTee = new Tee();
            newTee.setCreateModify(true);
            this.tee = newTee;
            return "tee.xhtml?faces-redirect=true&operation=add";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Charge un tee depuis une ligne du DataTable (modify_ClubCourseTee.xhtml)
     */
    public String loadTee(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            tee = readTeeService.read(ecl.tee());
            Course course = readCourseService.read(ecl.course());
            Club club = readClubService.read(ecl.club());
            appContext.setClub(club);
            appContext.setCourse(course);
            LOG.debug("idcourse after loadTee= " + course.getIdcourse());
            LOG.debug("idtee after loadTee= " + tee.getIdtee());
            if (course.getIdcourse() == null) {
                course.setIdcourse(tee.getCourse_idcourse());
                LOG.debug("idcourse forced");
            }
            if (tee != null) {
                tee.setCreateModify(false);
                return "tee.xhtml?faces-redirect=true&operation=modify";
            } else {
                LOG.error("error : tee not retrieved !!");
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Charge les holes depuis une ligne du DataTable (modify_ClubCourseTee.xhtml)
     */
    public String loadHoles(ECourseList ecl, String type) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with type = " + type);
        try {
            tee = readTeeService.read(ecl.tee());
            holesGlobal = readHoleService.read(tee);
            LOG.debug("holesGlobal dataHoles = " + Arrays.deepToString(holesGlobal.getDataHoles()));
            Course course = readCourseService.read(ecl.course());
            Club club = readClubService.read(ecl.club());
            appContext.setClub(club);
            appContext.setCourse(course);
            LOG.debug("course after loadHoles = " + course);
            LOG.debug("tee after loadHoles = " + tee);
            hole.setCreateModify(false);
            if ("global".equals(type)) {
                return "holes_global.xhtml?faces-redirect=true&operation=modify holes Global";
            } else {
                return "holes_distance.xhtml?faces-redirect=true&operation=modify holes Distance";
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // Navigation — Club/Course selection
    // migrated from CourseController 2026-02-25
    // ========================================

    /**
     * Action du bouton "Choix Club et Parcours" (selectClubCourse.xhtml)
     * Navigue vers la page finale selon le ClubSelectionPurpose.
     */
    public String clubAndCourseAction() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        enumeration.ClubSelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                .orElse(enumeration.ClubSelectionPurpose.CREATE_PLAYER);
        LOG.debug(methodName + " with purpose = " + purpose);
        return purpose.navigationToFinal();
    } // end method

    /**
     * Action du bouton de sélection club (selectClubDelete.xhtml / selectClubDialog.xhtml)
     * Navigue selon le contexte inputSelectClub dans la session.
     */
    public String selectorClubNextView() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            enumeration.ClubSelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                    .orElse(enumeration.ClubSelectionPurpose.CREATE_PLAYER);
            LOG.debug(methodName + " with purpose = " + purpose);
            if (purpose == enumeration.ClubSelectionPurpose.PAYMENT_COTISATION) {
                LOG.debug("purpose is PAYMENT_COTISATION");
                LOG.debug("pour le club = " + appContext.getClub().getIdclub());
                return "cotisation.xhtml?faces-redirect=true";
            } else {
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // Reset
    // ========================================

    public void resetClub() {
        appContext.setClub(new Club());                     // ✅ current supprimé
        courseListForClub = Collections.emptyList();
        resetCourse();
        LOG.debug("Club reset");
    }

    public void resetCourse() {
        appContext.setCourse(new Course());                 // ✅ current supprimé
        teeListForCourse = Collections.emptyList();
        resetTee();
        LOG.debug("Course reset");
    }

    public void resetTee() {
        tee = new Tee();
        holeListForTee = Collections.emptyList();
        resetHole();
        LOG.debug("Tee reset");
    }

    public void resetHole() {
        hole = new Hole();
        LOG.debug("Hole reset");
    }

    public void resetAll() {
        resetClub();
        country     = new Country();
        holesGlobal = new HolesGlobal();
        filteredClubs = Collections.emptyList();
        LOG.debug("Complete club context reset");
    }

    // ========================================
    // Utilitaires
    // ========================================

    private void setNextStep(boolean value) {
        LOG.debug("NextStep set to: {}", value);
    }


    // ========================================
    // Getters / Setters
    // ========================================

    // ========== GETTERS / SETTERS ==========
    
    public Tee getTee() { if (tee == null) tee = new Tee(); return tee; }
    public void setTee(Tee tee) {
        this.tee = tee;
        if (tee != null && tee.getIdtee() != null && tee.getIdtee() > 0) loadHolesForTee(tee.getIdtee());
    }

    public Hole getHole() { if (hole == null) hole = new Hole(); return hole; }
    public void setHole(Hole hole) { this.hole = hole; }

    public Country getCountry() { if (country == null) country = new Country(); return country; }
    public void setCountry(Country country) { this.country = country; }

    public HolesGlobal getHolesGlobal() { if (holesGlobal == null) holesGlobal = new HolesGlobal(); return holesGlobal; }
    public void setHolesGlobal(HolesGlobal holesGlobal) { this.holesGlobal = holesGlobal; }

    public List<Course> getCourseListForClub() { return courseListForClub; }
    public void setCourseListForClub(List<Course> l) { this.courseListForClub = l; }

    public List<Tee> getTeeListForCourse() { return teeListForCourse; }
    public void setTeeListForCourse(List<Tee> l) { this.teeListForCourse = l; }

    public List<Hole> getHoleListForTee() { return holeListForTee; }
    public void setHoleListForTee(List<Hole> l) { this.holeListForTee = l; }

    public List<Club> getFilteredClubs() { return filteredClubs; }
    public void setFilteredClubs(List<Club> l) { this.filteredClubs = l; }

    public boolean isSTROKEINDEX() { return STROKEINDEX; }
    public void setSTROKEINDEX(boolean STROKEINDEX) { this.STROKEINDEX = STROKEINDEX; }

    public List<ECourseList> getFilteredCourses() { return filteredCourses; }
    public void setFilteredCourses(List<ECourseList> filteredCourses) { this.filteredCourses = filteredCourses; }

    // ========================================
    // UNAVAILABLE — délégation vers appContext
    // ========================================

    public EUnavailable getUnavailable() {
        return appContext.getUnavailable();
    }

    public void setUnavailable(EUnavailable unavailable) {
        appContext.setUnavailable(unavailable);
    }

    // ========================================
    // MÉTHODES MIGRÉES depuis CourseController — 2026-02-25
    // ========================================

    /**
     * Ouvre la page modify_holes_global pour éditer les holes du tee sélectionné.
     * Migré depuis CourseController — 2026-02-25
     */
    public String viewHolesGlobal() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        tee.setTeeHolesPlayed("01-18");
        return "modify_holes_global.xhtml?faces-redirect=true&cmd=" + sessionMap.get("inputSelectCourse");
    } // end method

    /**
     * Charge un hole à partir d'une sélection ECourseList.
     * Migré depuis CourseController — 2026-02-25
     */
    public String loadHole(ECourseList ecl) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            tee = readTeeService.read(ecl.tee());
            Course course = readCourseService.read(ecl.course());
            Club club = readClubService.read(ecl.club());
            appContext.setCourse(course);
            appContext.setClub(club);
            LOG.debug(methodName + " - idcourse = " + course.getIdcourse());
            LOG.debug(methodName + " - idtee = " + tee.getIdtee());
            if (course.getIdcourse() == null) {
                course.setIdcourse(tee.getCourse_idcourse());
                LOG.debug(methodName + " - idcourse forced");
            }
            if (tee != null) {
                tee.setCreateModify(false);
                return "hole.xhtml?faces-redirect=true&operation=modify hole";
            } else {
                LOG.error(methodName + " - tee not retrieved");
                showMessageFatal("Error: tee not retrieved");
                return null;
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Met à jour la structure unavailable d'un club (modify ou delete).
     * Migré depuis CourseController — 2026-02-25
     */
    public String modifyClubUnavailableStructure(String type) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " for type = " + type);
        try {
            EUnavailable unavailable = appContext.getUnavailable();
            Club club = appContext.getClub();
            if ("delete".equals(type)) {
                unavailable.structure().setStructureList(null);
            }
            if (unavailableController.updateClub(unavailable, club)) {
                unavailable.structure().setStructureExists(true);
            }
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // Migrated from CourseController — 2026-02-25
    // ========================================

    /**
     * Sélection d'un course depuis la liste ECourseList.
     * Routing method — redirige selon inputSelectCourse en session.
     * Migré depuis CourseController — 2026-02-25
     */
    public String selectCourse(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            appContext.setClub(ecl.club());
            LOG.debug("club = " + appContext.getClub());
            appContext.setCourse(ecl.course());
            LOG.debug("course = " + appContext.getCourse());

            String msg = "Select Course Successful = "
                    + " <br/> Club name = " + appContext.getClub().getClubName()
                    + " <br/> Course name = " + appContext.getCourse().getCourseName()
                    + " <br/> inputSelectCourse = " + sessionMap.get("inputSelectCourse");
            LOG.debug(msg);

            LOG.debug(methodName + ", inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
            if (sessionMap.get("inputSelectCourse") == null) {
                msg = "No InputSelectCourse !";
                LOG.error(msg);
                showMessageFatal(msg);
            }

            if (sessionMap.get("inputSelectCourse").equals("CreateRound")) {
                return "round.xhtml?faces-redirect=true&cmd=round";
            }

            if (sessionMap.get("inputSelectCourse").equals("ini")) {
                return "round.xhtml?faces-redirect=true&cmd=ini";
            }

            if (sessionMap.get("inputSelectCourse").equals("CreateTarifGreenfee")) {
                return "tarif_greenfee_menu.xhtml?faces-redirect=true";
            }

            if (sessionMap.get("inputSelectCourse").equals("CreateTarifMember")) {
                return "tarif_members_menu.xhtml?faces-redirect=true";
            }

            if (sessionMap.get("inputSelectCourse").equals("CreateUnavailablePeriod")) {
                var v = readUnavailableStructure.read(ecl.club());
                EUnavailable unavailable = appContext.getUnavailable();
                unavailable.withStructure(v);
                LOG.debug(methodName + " - returned with unavailable structure");
                if (unavailable.structure() == null) {
                    String msgerr = LCUtil.prepareMessageBean("unavailable.structure.notfound");
                    LOG.error(msgerr);
                    LCUtil.showMessageInfo(msgerr);
                    return "unavailable_structure.xhtml?faces-redirect=true";
                } else {
                    appContext.getClub().setUnavailableStructure(unavailable.structure());
                    LOG.debug(methodName + " - structure length = " + appContext.getClub().getUnavailableStructure().getStructureList().size());
                    return "unavailable_period.xhtml?faces-redirect=true";
                }
            }

            if (sessionMap.get("inputSelectCourse").equals("PaymentTarifMember")) {
                LOG.debug(methodName + " - we are in paymentTarifMember");
                LOG.debug(methodName + " - round = " + appContext.getRound());
                if (tarifMember == null) {
                    String msgerr = LCUtil.prepareMessageBean("tarif.member.notfound");
                    LOG.error(msgerr);
                    LCUtil.showMessageFatal(msgerr);
                    return null;
                } else {
                    return "cotisation.xhtml?faces-redirect=true";
                }
            }

            if (sessionMap.get("inputSelectCourse").equals("ChartCourse")) {
                return "statChartCourse.xhtml?faces-redirect=true";
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
        return null;
    } // end method

    /**
     * Sélection d'un course pour afficher le trajet (maps).
     * Migré depuis CourseController — 2026-02-25
     */
    public String selectTravel(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " with ECourseList = " + ecl.toString());
            appContext.setClub(ecl.club());
            appContext.setCourse(ecl.course());

            String msg = "Select Travel Successful = "
                    + " <br/> Club name = " + appContext.getClub().getClubName()
                    + " <br/> Course name = " + appContext.getCourse().getCourseName();
            LOG.debug(msg);
            showMessageInfo(msg);
            LOG.debug(methodName + " - inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
            return "maps_home_club.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Sélection d'un course pour afficher le chart statistique.
     * Migré depuis CourseController — 2026-02-25
     * Note: utilise encore new ChartController() legacy — migration ChartController hors scope
     */
    public String selectChart(ECourseList ecl) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with ecl = " + ecl.toString());
        try {
            appContext.setClub(ecl.club());
            appContext.setCourse(ecl.course());
            String msg = "Select Course Successful = "
                    + " <br/> Club name = " + appContext.getClub().getClubName()
                    + " <br/> Course name = " + appContext.getCourse().getCourseName()
                    + " / " + appContext.getCourse().getIdcourse();
            LOG.debug(msg);
            showMessageInfo(msg);
            LOG.debug(methodName + " - inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
            // new Controllers.ChartController().lineModelCourse(conn, ...)
            String v = chartController.lineModelCourse(appContext.getPlayer(), appContext.getCourse()); // migrated 2026-02-26
            setLineModelCourse(v);
            LOG.debug(methodName + " - Chart returned = " + getLineModelCourse());
            return "statChartCourse.xhtml?faces-redirect=true";
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Navigation vers course.xhtml pour créer/modifier un course.
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_course_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset(s);
        appContext.getCourse().setCreateModify(true);
        return "course.xhtml?faces-redirect=true&operation=" + s;
    } // end method

    /**
     * Navigation vers tee.xhtml pour créer/modifier un tee.
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_tee_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset(s);
        tee.setCreateModify(true);
        return "tee.xhtml?faces-redirect=true&operation=" + s;
    } // end method

    /**
     * Liste les courses pour un club donné (par ID string).
     * Migré depuis CourseController — 2026-02-25
     */
    public List<Course> listCoursesForClub(String clubid) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with param clubid = " + clubid);
        try {
            Club club = appContext.getClub();
            LOG.debug(methodName + " - club = " + club);
            if (clubid == null || clubid.isEmpty()) {
                LOG.debug(methodName + " - param clubid == null or empty");
            } else {
                club.setIdclub(Integer.parseInt(clubid));
            }
            LOG.debug(methodName + " - Club = " + club);
            return courseListForClubService.list(club); // migrated 2026-02-24
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * Pre-render handler pour club.xhtml — reset le club si pas un postback.
     * Migré depuis CourseController — 2026-02-25
     */
    public void preRenderClub() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Club club = appContext.getClub();
        LOG.debug(methodName + " - idclub = " + club.getIdclub());
        LOG.debug(methodName + " - currentPhaseId = " + FacesContext.getCurrentInstance().getCurrentPhaseId());
        boolean isPostback = FacesContext.getCurrentInstance().isPostback();
        LOG.debug(methodName + " - isPostBack = " + isPostback);
        if ((!isPostback) && (club.getIdclub() != null)) {
            appContext.setClub(new Club());
            LOG.debug(methodName + " - club forced to null");
        }
    } // end method

    // ========================================
    // Getters/Setters — migrated from CourseController 2026-02-25
    // ========================================

    public String getLineModelCourse() { return lineModelCourse; }
    public void setLineModelCourse(String lineModelCourse) { this.lineModelCourse = lineModelCourse; }

    public TarifMember getTarifMember() { return tarifMember; }
    public void setTarifMember(TarifMember tarifMember) { this.tarifMember = tarifMember; }

    // ========================================
    // Migrated from CourseController — 2026-02-25 (Étape 3)
    // ========================================

    // convertYtoM() — already exists at line ~1017 in this class

    /**
     * Navigation vers modify_ClubCourseTee.xhtml ou selectClubModify.xhtml.
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_clubModify_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset("clubRestart " + s);
        tee.setModifyClubCourseTee(true);
        sessionMap.put("inputSelectClub", s);
        if (s.equals("clubModify")) {
            return "selectClubModify.xhtml?faces-redirect=true";
        } else {
            sessionMap.put("inputSelectCourse", s);
            return "modify_ClubCourseTee.xhtml?faces-redirect=true";
        }
    } // end method

    /**
     * Setter inputClub — réinitialise club/course/tee/hole si "ini".
     * Migré depuis CourseController — 2026-02-25
     */
    public void setInputClub(String inputClub) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " (new club !) = " + inputClub);
        if ("ini".equals(inputClub)) {
            appContext.setClub(new Club());
            appContext.setCourse(new Course());
            tee = new Tee();
            hole = new Hole();
        }
    } // end method

    // ========================================
    // Groupe A — Navigation to_select* methods
    // migrated from CourseController 2026-02-25 (Phase 2)
    // ========================================

    public String to_selectCourse_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset("Reset to_selectCourse " + s);
        sessionMap.put("inputSelectCourse", s);
        LOG.debug("course selected for :  = " + sessionMap.get("inputSelectCourse"));
        return "selectCourse.xhtml?faces-redirect=true&cmd=" + sessionMap.get("inputSelectCourse");
    } // end method

    public String to_selectCourse2_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset("Reset to_selectCourse " + s);
        sessionMap.put("inputSelectCourse", s);
        sessionMap.put("inputSelectClub", s);
        LOG.debug("course selected for :  = " + sessionMap.get("inputSelectCourse"));
        LOG.debug("club selected for :  = " + sessionMap.get("inputSelectClub"));
        return "selectClubCourse.xhtml?faces-redirect=true";
    } // end method

    public String to_selectGrpc_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset("Reset to_selectGrpc " + s);
        return "grpc_server.xhtml?faces-redirect=true";
    } // end method

    public String to_selectClubLA_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset("Reset to_selectClubLA " + s);
        if (s.equals("CreatePro")) {
            sessionMap.put("inputSelectClub", s);
            return "professional.xhtml?faces-redirect=true";
        }
        sessionMap.put("inputSelectCourse", s);
        LOG.debug("club selected for :  = " + sessionMap.get("inputSelectCourse"));
        return "selectClubLocalAdmin.xhtml?faces-redirect=true";
    } // end method

    public String to_selectClubSYS_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset("Reset to_selectClubSYS " + s);
        if (s.equals("CreatePro")) {
            sessionMap.put("inputSelectClub", s);
            return "professional.xhtml?faces-redirect=true";
        }
        sessionMap.put("inputSelectCourse", s);
        LOG.debug("club selected for :  = " + sessionMap.get("inputSelectCourse"));
        return "selectClubLocalAdmin.xhtml?faces-redirect=true";
    } // end method

    public String to_selectPurpose_xhtml(String menuSelection) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = {}", menuSelection);
        courseController.reset("Reset from to_selectPurpose_xhtml, with : " + menuSelection);

        // 1. Résolution du purpose
        enumeration.ClubSelectionPurpose purpose = enumeration.ClubSelectionPurpose.fromCode(menuSelection);
        LOG.debug("purpose resolved = {}", purpose);

        // 2. Ouverture du contexte CDI
        clubSelectionContext.open(purpose);

        // 3. Navigation déléguée à l'enum
        var navigation = purpose.navigationToFirst();
        LOG.debug("navigation resolved = {}", navigation);

        if (menuSelection.equals("clubCreate")) {
            appContext.getClub().setCreateModify(true);
        }

        return navigation;
    } // end method

    public String to_selectCourseLA_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset("Reset to_selectClub2 " + s);
        sessionMap.put("inputSelectCourse", s);
        sessionMap.put("adminType", "admin");
        LOG.debug("club selected for :  = " + sessionMap.get("inputSelectCourse"));
        return "selectCourseLocalAdmin.xhtml?faces-redirect=true";
    } // end method

    public String to_update_help(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset("Reset to_update_help " + s);
        sessionMap.put("inputSelectCourse", s);
        return "editor_help.xhtml?faces-redirect=true";
    } // end method

    public String to_selectClub_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset("Reset to_selectClub" + s);
        sessionMap.put("inputSelectCourse", s);
        sessionMap.put("inputSelectClub", s);
        return "selectClub.xhtml?faces-redirect=true&cmd=" + sessionMap.get("inputSelectCourse");
    } // end method

    public String to_selectClubDialog_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        courseController.reset("Reset to_selectClubDialog" + s);
        sessionMap.put("inputSelectClub", s);
        return "selectClubDialog.xhtml?faces-redirect=true&cmd=" + sessionMap.get("inputSelectClub");
    } // end method

    // ========================================
    // Groupe E — Extra navigation methods
    // migrated from CourseController 2026-02-25 (Phase 2)
    // ========================================

    public void to_reset_menu(String ini) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        courseController.reset(ini);
        sessionMap.put("playerid", 0);
        sessionMap.put("playerlastname", "");
        sessionMap.put("playerage", 0);
        String msg = "Reset PLAYER done = " + ini;
        LOG.debug(msg);
        showMessageInfo(msg);
    } // end method

    // ========================================
    // Groupe B — List methods
    // migrated from CourseController 2026-02-25 (Phase 2)
    // ========================================

    public void findCourseListForClub() throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        courseListForClub = courseListForClubService.list(appContext.getClub()); // migrated 2026-02-25
    } // end method

    public List<ECourseList> listCoursesPublic() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("listCourseOnly ? sessionMap = " + sessionMap.get("inputSelectCourse"));
            if (sessionMap.get("inputSelectCourse").equals("ChartCourse")
                    || sessionMap.get("inputSelectCourse").equals("CreateRound")
                    || sessionMap.get("inputSelectCourse").equals("createTarifGreenfee")) {
                return courseListOnly.list();
            } else {
                return courseListService.list();
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listDetailClub(String id) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with id club = " + id);
        try {
            Club c = new Club();
            c.setIdclub(Integer.valueOf(id));
            clubDetailList.invalidateCache(); // migrated 2026-02-25
            return clubDetailList.list(c);    // migrated 2026-02-25
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // Groupe C — Selection methods
    // migrated from CourseController 2026-02-25 (Phase 2)
    // ========================================

    public String selectedClub(Club c) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        if (c == null) {
            LOG.warn("Selected club param is null!");
            showMessageFatal("Aucun club sélectionné !");
            return null;
        }
        LOG.debug(methodName + " with param club = " + c);
        appContext.setClub(c);
        LOG.debug(methodName + " with club = " + appContext.getClub());
        try {
            enumeration.ClubSelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                    .orElse(enumeration.ClubSelectionPurpose.CREATE_PLAYER);
            LOG.debug(methodName + " with purpose = " + purpose);

            return switch (purpose) {
                case CREATE_PLAYER -> {
                    appContext.getPlayer().setPlayerHomeClub(appContext.getClub().getIdclub());
                    LOG.debug("Home club set for CREATE_PLAYER: " + appContext.getPlayer());
                    dialogController.closeDialog(null);
                    yield playerController.getCreateModifyPlayer().equals("M") ?
                            "player_modify.xhtml?faces-redirect=true" :
                            "player.xhtml?faces-redirect=true";
                }
                case LOCAL_ADMIN -> {
                    LOG.debug("inside LOCAL_ADMIN");
                    appContext.getLocalAdmin().setPlayerHomeClub(appContext.getClub().getIdclub());
                    LOG.debug("Home club set for LOCAL_ADMIN: " + appContext.getLocalAdmin());
                    dialogController.closeDialog(null);
                    yield null;
                }
                case CREATE_PRO -> {
                    appContext.getPlayerPro().setPlayerHomeClub(appContext.getClub().getIdclub());
                    LOG.debug("Home club set for CREATE_PRO: " + appContext.getPlayerPro());
                    dialogController.closeDialog(null);
                    yield "professional.xhtml?faces-redirect=true";
                }
                case CREATE_ROUND -> {
                    LOG.debug("club setted for CREATE_ROUND: " + appContext.getClub());
                    LOG.debug("course setted for CREATE_ROUND: " + appContext.getCourse());
                    dialogController.closeDialog(null);
                    yield null;
                }
                case PAYMENT_COTISATION -> {
                    courseController.getRound().setRoundDate(LocalDateTime.now());
                    tarifMember = findTarifMembersData.find(appContext.getClub(), courseController.getRound());
                    LOG.debug("TarifMember loaded for club = " + appContext.getClub());
                    dialogController.closeDialog(null);
                    yield null;
                }
                case MENU_UNAVAILABLE -> {
                    LOG.debug("Menu unavailable selected, club = " + appContext.getClub());
                    dialogController.closeDialog(null);
                    yield null;
                }
                default -> {
                    LOG.warn("Unknown ClubSelectionPurpose: " + purpose);
                    appContext.getPlayer().setPlayerHomeClub(appContext.getClub().getIdclub());
                    if (appContext.getCompetition() != null) {
                        appContext.getCompetition().competitionDescription().setCompetitionClubId(appContext.getClub().getIdclub());
                    }
                    dialogController.closeDialog(null);
                    yield playerController.getCreateModifyPlayer().equals("M") ?
                            "player_modify.xhtml?faces-redirect=true" :
                            "player.xhtml?faces-redirect=true";
                }
            };

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        } finally {
            clubSelectionContext.clear();
        }
    } // end method

    public String selectedCourseForClub(Course c) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " with course input parameter = " + c);
            LOG.debug(methodName + " for club = " + appContext.getClub());
            enumeration.ClubSelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                    .orElse(enumeration.ClubSelectionPurpose.CREATE_PLAYER);
            LOG.debug(methodName + " with purpose = " + purpose);
            appContext.setCourse(c);
            LOG.debug(methodName + " course is now = " + appContext.getCourse());
            String msg = "Select Course Successfull = <br/> CourseName = " + appContext.getCourse().getCourseName();
            LOG.debug(msg);
            showMessageInfo(msg);
            LOG.debug(methodName + " : inputSelectClub = " + sessionMap.get("inputSelectClub"));

            dialogController.closeDialog(null);

            if ("MenuUnavailable".equals(sessionMap.get("inputSelectClub"))) {
                LOG.debug("handling menu unavailable");
                EUnavailable unavailable = appContext.getUnavailable();
                LOG.debug(methodName + " for unavailable = " + unavailable);
                unavailable.structure().setMenuLaunched(true);
                var v = readUnavailableStructure.read(appContext.getClub());
                LOG.debug(methodName + " variable v found = ENTITE UnavailableStructure " + v);
                if (v != null && !v.getStructureList().isEmpty()) {
                    unavailable.structure().setStructureList(v.getStructureList());
                    unavailable.structure().setStructureExists(true);
                    LOG.debug("structure exists " + v);
                } else {
                    LOG.debug("NO structure exists " + v);
                }
                dialogController.closeDialog(null);
                return null;
            }

            if ("CREATE COMPETITION".equals(sessionMap.get("inputSelectClub"))) {
                LOG.debug(methodName + " - competition = " + appContext.getCompetition());
                appContext.getCompetition().competitionDescription().setCompetitionCourseId(appContext.getCourse().getIdcourse());
                appContext.getCompetition().competitionDescription().setCompetitionCourseIdName(
                        Integer.toString(appContext.getCourse().getIdcourse()) + " - " + appContext.getCourse().getCourseName());
                LOG.debug(methodName + " - competition updated CourseId = " + appContext.getCompetition().competitionDescription());
                dialogController.closeDialog(null);
                return null;
            }

            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String selectClub(Club c, String select) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " with select = " + select);
            LOG.debug(methodName + " with in_club = " + c);
            appContext.setClub(c);

            String msg = "Select Club Successfull = "
                    + " <br/> Club name = " + appContext.getClub().getClubName()
                    + " <br/> inputSelectCourse = " + select;
            LOG.debug(msg);

            LOG.debug(methodName + " : inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
            if (sessionMap.get("inputSelectCourse") == null) {
                msg = "No InputSelectCourse !";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }
            if (select.equals("CreatePro")) {
                return "professional.xhtml?faces-redirect=true";
            }

            if (select.equals("CreateUnavailablePeriod")) {
                var v = readUnavailableStructure.read(appContext.getClub());
                EUnavailable unavailable = appContext.getUnavailable();
                unavailable.withStructure(v);
                LOG.debug(methodName + " - returned with unavailable structure");
                if (unavailable.structure() == null) {
                    String msgerr = LCUtil.prepareMessageBean("unavailable.structure.notfound");
                    LOG.error(msgerr);
                    LCUtil.showMessageInfo(msgerr);
                    return "unavailable_structure.xhtml?faces-redirect=true";
                } else {
                    appContext.getClub().setUnavailableStructure(unavailable.structure());
                    LOG.debug(methodName + " - structure length = " + appContext.getClub().getUnavailableStructure().getStructureList().size());
                    return "unavailable_period.xhtml?faces-redirect=true";
                }
            }

            if (select.equals("CreateTarifGreenfee")) {
                return "tarif_greenfee_menu.xhtml?faces-redirect=true";
            }

            if (select.equals("CreateTarifMember")) {
                return "tarif_members_menu.xhtml?faces-redirect=true";
            }

            if (select.equals("PaymentCotisationSpontaneous")) {
                LOG.debug("entering PaymentCotisationSpontaneous");
                LOG.debug("club = " + appContext.getClub());
                LOG.debug("round = " + courseController.getRound());
                tarifMember = findTarifMembersData.find(appContext.getClub(), courseController.getRound());
                if (tarifMember == null) {
                    String msgerr = LCUtil.prepareMessageBean("tarif.member.notfound");
                    LOG.error(msgerr);
                    LCUtil.showMessageFatal(msgerr);
                    return null;
                } else {
                    return "cotisation.xhtml?faces-redirect=true";
                }
            }

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
        return null;
    } // end method

    public String selectCourseLA(ECourseList in_club, String select) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " with select = " + select);
            LOG.debug(methodName + " with in_club = " + in_club);
            appContext.setClub(in_club.club());
            appContext.setCourse(in_club.course());
            String msg = "Select Club Successfull = "
                    + " <br/> Club name = " + appContext.getClub().getClubName()
                    + " <br/> inputSelectCourse = " + select;
            LOG.debug(msg);
            LOG.debug(methodName + " inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
            if (sessionMap.get("inputSelectCourse") == null) {
                msg = "No InputSelectCourse !";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }

            if (select.equals("CreateTarifGreenfee")) {
                return "tarif_greenfee_menu.xhtml?faces-redirect=true";
            }

            if (sessionMap.get("inputSelectCourse").equals("CreateTarifMember")) {
                return "tarif_members_menu.xhtml?faces-redirect=true";
            }

            if (sessionMap.get("inputSelectCourse").equals("CreateUnavailablePeriod")) {
                var v = readUnavailableStructure.read(appContext.getClub());
                EUnavailable unavailable = appContext.getUnavailable();
                unavailable.withStructure(v);
                LOG.debug(methodName + " - returned with unavailable structure");
                if (unavailable.structure() == null) {
                    String msgerr = LCUtil.prepareMessageBean("unavailable.structure.notfound");
                    LOG.error(msgerr);
                    LCUtil.showMessageInfo(msgerr);
                    return "unavailable_structure.xhtml?faces-redirect=true";
                } else {
                    appContext.getClub().setUnavailableStructure(unavailable.structure());
                    LOG.debug(methodName + " - structure length = " + appContext.getClub().getUnavailableStructure().getStructureList().size());
                    return "unavailable_period.xhtml?faces-redirect=true";
                }
            }

            if (select.equals("PaymentTarifMember")) {
                tarifMember = findTarifMembersData.find(appContext.getClub(), courseController.getRound());
                if (tarifMember == null) {
                    String msgerr = LCUtil.prepareMessageBean("tarif.member.notfound");
                    LOG.error(msgerr);
                    LCUtil.showMessageFatal(msgerr);
                    return null;
                } else {
                    return "cotisation.xhtml?faces-redirect=true";
                }
            }

            if (sessionMap.get("inputSelectCourse").equals("CreateRound")) {
                return "round.xhtml?faces-redirect=true&cmd=round";
            }

            if (sessionMap.get("inputSelectCourse").equals("ini")) {
                return "round.xhtml?faces-redirect=true&cmd=ini";
            }

            if (sessionMap.get("inputSelectCourse").equals("ChartCourse")) {
                return "statChartCourse.xhtml?faces-redirect=true";
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
        return null;
    } // end method

    public String selectClubCourse() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " with club = " + appContext.getClub());
            LOG.debug(methodName + " with course = " + appContext.getCourse());
            LOG.debug(methodName + " with sessionMap inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
            return null; // to be completed
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String selectedCourse() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("club = " + appContext.getClub());
            LOG.debug("course = " + appContext.getCourse());
            LOG.debug("sessionMap inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
            enumeration.ClubSelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                    .orElse(enumeration.ClubSelectionPurpose.CREATE_PLAYER);
            LOG.debug(methodName + " with purpose = " + purpose);
            if (purpose == enumeration.ClubSelectionPurpose.CREATE_ROUND) {
                LOG.debug(methodName + " return to " + purpose.navigationToFinal());
                return purpose.navigationToFinal();
            }
            LOG.debug("unknown case in selectedCourse !!");
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // Groupe D — Unavailable methods
    // migrated from CourseController 2026-02-25 (Phase 2)
    // ========================================

    public String createUnavailablePeriod() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " for club = " + appContext.getClub());
            EUnavailable unavailable = appContext.getUnavailable();
            unavailable.period().setIdclub(appContext.getClub().getIdclub());
            String msg = "Indisponibilité to be created = " + unavailable.period();
            LOG.info(msg);

            if (createUnavailablePeriodService.create(unavailable.period())) {
                unavailable.period().setStartDate(null);
                unavailable.period().setEndDate(null);
                msg = "Unavailable Period created = " + unavailable.period();
                LOG.info(msg);
                showMessageInfo(msg);
                return null;
            } else {
                msg = "Unavailable is NOT created !";
                LOG.debug(msg);
                showMessageFatal(msg);
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String inputUnvailableStructure() throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " for club = " + appContext.getClub());
            EUnavailable unavailable = appContext.getUnavailable();
            LOG.debug(methodName + " for unavailable = " + unavailable);
            unavailable = unavailableController.inputUnvailableStructure(unavailable);
            appContext.setUnavailable(unavailable);
            LOG.debug(methodName + " - back with unavailable = " + unavailable);
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String showUnavailableStructure() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            EUnavailable unavailable = appContext.getUnavailable();
            LOG.debug(methodName + " for unavailable = " + unavailable);
            String msg = LCUtil.prepareMessageBean("unavailable.structure.show")
                    + "<br/> Unavailable Structure = "
                    + unavailable.structure().getStructureList().toString();
            LOG.info(msg);
            showMessageInfo(msg);
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String showUnavailablePeriod() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " for club = " + appContext.getClub());
            EUnavailable lun = unavailableListForDate.list(LocalDateTime.now(), appContext.getClub());
            if (lun == null) {
                LOG.debug("pas de période d'indisponibilité");
                appContext.setUnavailable(null);
            } else {
                appContext.setUnavailable(lun);
                String msg = "showUnavailablePeriods - first element of list is = " + lun.toString();
                LOG.debug(msg);
                showMessageInfo(msg);
                return "unavailable_show.xhtml?faces-redirect=true";
            }
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public EUnavailable showUnavailablePeriods() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " for club = " + appContext.getClub());
            EUnavailable unavailable = unavailableListForDate.list(LocalDateTime.now(), appContext.getClub());
            appContext.setUnavailable(unavailable);
            return unavailable;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String showUnavailablePeriods(Club c) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " for club input = " + c);
        try {
            EUnavailable lun = unavailableListForDate.list(LocalDateTime.now(), c);
            if (lun == null) {
                LOG.debug("lun is null");
                LOG.debug("no unavailabilities known");
                appContext.setUnavailable(null);
                return "unavailable_show.xhtml?faces-redirect=true";
            } else {
                appContext.setUnavailable(lun);
                LOG.debug("showUnavailablePeriods - element is = " + lun);
                return "unavailable_show.xhtml?faces-redirect=true";
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String showUnavailablePeriods(ECourseList ecl) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            appContext.setClub(ecl.club());
            EUnavailable lun = unavailableListForDate.list(LocalDateTime.now(), ecl.club());
            LOG.debug("showUnavailablePeriods - element of list is = " + lun);
            appContext.setUnavailable(lun);
            return "unavailable_show.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

} // end class