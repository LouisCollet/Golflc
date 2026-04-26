package Controller.refact;

import Controllers.DialogController;
import context.ApplicationContext;
import entite.*;
import entite.composite.ECourseList;
import entite.composite.EPlayerPassword;
import entite.composite.EUnavailable;
import enumeration.SelectionPurpose;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import manager.ClubManager;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    @Inject private ApplicationContext appContext;
    @Inject private cache.CacheInvalidator cacheInvalidator;
    @Inject private DialogController dialogController;
    @Inject private lists.CourseListForClub courseListForClubService; // migrated 2026-02-24
    // externalContext injection removed — fix multi-user 2026-03-07 (request-scoped, must not be cached in @SessionScoped)
    @Inject private CoordinatesService coordinatesService;
    @Inject private contexte.SelectionContextBean clubSelectionContext; // migrated 2026-02-25
    @Inject private read.ReadClub readClubService; // migrated 2026-02-25
    @Inject private read.ReadCourse readCourseService; // migrated 2026-02-25
    @Inject private read.ReadTee readTeeService; // migrated 2026-02-25
    @Inject private read.ReadHole readHoleService; // migrated 2026-02-25
    @Inject private Controllers.UnavailableController unavailableController; // migrated 2026-02-25
    // @Inject @SessionMap sessionMap — removed 2026-02-28, migrated to appContext
    @Inject private read.ReadUnavailableStructure readUnavailableStructure; // migrated 2026-02-25
    @Inject private Controller.refact.NavigationController navigationController; // renamed from CourseController 2026-02-28
    @Inject private create.CreateUnavailablePeriod createUnavailablePeriodService; // migrated 2026-02-25 — Groupe D
    @Inject private update.UpdateUnavailablePeriod updateUnavailablePeriodService;
    @Inject private lists.UnavailableListForDate unavailableListForDate; // migrated 2026-02-25 — Groupe D
    @Inject private lists.CourseListOnly courseListOnly; // migrated 2026-02-25 — Groupe B
    @Inject private lists.ClubDetailList clubDetailList; // migrated 2026-02-25 — Groupe B
    @Inject private lists.CourseList courseListService; // migrated 2026-02-25 — Groupe B
    @Inject private find.FindTarifMembersData findTarifMembersData; // migrated 2026-02-25 — Groupe C
    @Inject private Controller.refact.PlayerController playerController; // migrated 2026-02-25 — Groupe C (createModifyPlayer)
    @Inject private Controllers.ChartController chartController; // migrated 2026-02-26
    @Inject private lists.ProfessionalListForClub professionalListForClub; // migrated 2026-02-28 from NavigationController
    @Inject private create.CreateProfessional createProfessionalService; // migrated 2026-02-28 from CourseController
    @Inject private update.UpdateProfessional updateProfessionalService;
    @Inject private lists.ClubsListLocalAdmin clubsListLocalAdmin;
    @Inject private Controller.refact.MemberController memberController; // added 2026-04-22 — cotisation flow restoration
    @Inject private find.FindUnavailablePeriodOverlapping findUnavailablePeriodOverlapping; // added 2026-04-25

    private entite.Professional selectedProfessional = new entite.Professional();
private List<Flight> flightList = Collections.emptyList();
private int cptFlight = 0;

    // ✅ Session-level cache — avoid repeated DB queries on JSF re-render
    private List<ECourseList> cachedClubsCoursesTees = null;
    private List<Course> cachedCoursesForClub = null;
    private boolean cachedFlightsLoaded = false;

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
    private EUnavailable unavailableDB = null; // DB snapshot for viz comparison — unavailable wizard
    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
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
    } // end method

    // ========================================
    // CDI EVENT — ResetEvent observer — 2026-02-26
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} — source: {}", event.getSource());
        flightList        = Collections.emptyList();
        cptFlight         = 0;
        tee               = new Tee();
        hole              = new Hole();
        country           = new Country();
        holesGlobal       = new HolesGlobal();
        courseListForClub  = Collections.emptyList();
        teeListForCourse  = Collections.emptyList();
        holeListForTee    = Collections.emptyList();
        filteredClubs     = Collections.emptyList();
        STROKEINDEX       = true;
        filteredCourses   = null;
        lineModelCourse   = null;
        tarifMember       = new TarifMember();
//        selectedPlayerEPP = null;
        LOG.debug("ClubController reset done");
    } // end method

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
        LOG.debug("entering {}", methodName);
        try {
            Club club = appContext.getClub();
            LOG.debug("with club = {}", club);
            LOG.debug("alternative via délégation with club = {}", getClub());

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

    public String modifyClub() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Club club = appContext.getClub();

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

    public void deleteClub(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("for {}", ecl.club());
            Club club = ecl.club();
            if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
                showMessageFatal("No club selected for deletion");
                return;
            }
            int clubId = club.getIdclub();
            ClubManager.SaveResult result = clubManager.deleteClub(clubId);
            boolean OK = result.isSuccess();
            LOG.debug("result of deleteClub = {}", OK);
            if (OK) {
                invalidateClubCaches();
                showMessageInfo(result.getMessage());
            } else {
                LOG.error("Club deletion failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method

    // ========================================
    // CREATE - Course
    // ========================================

    public String createCourse() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Club club     = appContext.getClub(); 
            Course course = appContext.getCourse();

            LOG.debug("with club = {}", club);
            LOG.debug("alternative via délégation with club = {}", getClub());
            LOG.debug("with course = {}", course);
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
                invalidateClubCaches();
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

    public String modifyCourse() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
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
                invalidateClubCaches();
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

    public void deleteCourse(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("for {}", ecl.course());
            Course course = ecl.course();
            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                showMessageFatal("No course selected for deletion");
                return;
            }
            int courseId = course.getIdcourse();
            ClubManager.SaveResult result = clubManager.deleteCourse(courseId);
            boolean OK = result.isSuccess();
            LOG.debug("result of deleteCourse = {}", OK);
            if (OK) {
                invalidateClubCaches();
                showMessageInfo(result.getMessage());
            } else {
                LOG.error("Course deletion failed: {}", result.getMessage());
                showMessageFatal(result.getMessage());
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method

    // ========================================
    // CREATE - Tee
    // ========================================

    public String createTee() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Course course = appContext.getCourse();
            LOG.debug("course = {}", course);
            LOG.debug("tee = {}", tee);
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

                invalidateClubCaches();
               // showMessageInfo(result.getMessage());
                LOG.debug(result.getMessage());

                if (tee.getTeeStart().equals("YELLOW") &&
                    tee.getTeeGender().equals("M") &&
                    tee.getTeeHolesPlayed().equals("01-18")) {
                    LOG.debug("master tee ==> 3 lignes avec par, index et distances : la totale");
                    return "hole.xhtml?faces-redirect=true";

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
        LOG.debug("entering {}", methodName);
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
                invalidateClubCaches();
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

    public void deleteTee(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for {}", ecl.tee());
        Tee tee = ecl.tee();
        if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
            showMessageFatal("No tee selected for deletion");
            return;
        }
        int teeId = tee.getIdtee();
        ClubManager.SaveResult result = clubManager.deleteTee(teeId);
        boolean OK = result.isSuccess();
        LOG.debug("result of deleteTee = {}", OK);
        if (OK) {
            invalidateClubCaches();
            showMessageInfo(result.getMessage());
        } else {
            LOG.error("Tee deletion failed: {}", result.getMessage());
            showMessageFatal(result.getMessage());
        }
    } // end method

    // ========================================
    // CREATE - Hole
    // ========================================

    public void createHole() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Course course = appContext.getCourse();
            LOG.debug("course = {}", course);
            LOG.debug("tee = {}", tee);
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
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("with param = {}", param);

            Club club     = appContext.getClub(); 
            Course course = appContext.getCourse();
            
                LOG.debug("for club = {}", club);
                LOG.debug("course = {}", course);
                LOG.debug("tee = {}", tee);
                LOG.debug("holesGlobal = {}", holesGlobal);
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
                cacheInvalidator.invalidateClubCaches();
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
        LOG.debug("entering {}", methodName);
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
        LOG.debug("entering {}", methodName);
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
        LOG.debug("entering {}", methodName);
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
        LOG.debug("entering {}", methodName);
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
        LOG.debug("entering {}", methodName);
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
        LOG.debug("entering {}", methodName);
        try {
            holeListForTee = clubManager.listHolesForTee(teeId);
            LOG.debug("Loaded {} holes for tee {}", holeListForTee.size(), teeId);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            holeListForTee = Collections.emptyList();
        }
    } // end method

    private void listCourses() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
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
    LOG.debug("entering {} with param={}", param);
    if (cachedClubsCoursesTees != null) {
        LOG.debug("returning cached list size = {}", cachedClubsCoursesTees.size());
        return cachedClubsCoursesTees;
    }

    try {
        Club club = appContext.getClub();

        if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
            LOG.warn("no club selected");
            return Collections.emptyList();
        }

        LOG.debug("with club={}", club.getIdclub());

        if ("one_club".equals(param)) {
            cachedClubsCoursesTees = clubManager.listCoursesForClub(club.getIdclub());  // ✅ via manager
            return cachedClubsCoursesTees;
        }

        // all_clubs commenté dans l'original — conservé commenté
        // if ("all_clubs".equals(param)) {
        //     return clubManager.listAllClubsCoursesTees();
        // }

        LOG.warn("unknown param: {}", param);
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
// ── Dropdown-based club / course selection (selectClubCourse.xhtml) ─────────

    public Integer getSelectedClubId() {
        return appContext.getClub() != null ? appContext.getClub().getIdclub() : null;
    } // end method

    public void setSelectedClubId(Integer clubId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (clubId == null) return;
        try {
            clubManager.listClubs().stream()
                .filter(c -> clubId.equals(c.getIdclub()))
                .findFirst()
                .ifPresent(c -> {
                    appContext.setClub(c);
                    courseListForClubService.invalidateCache();
                    cachedCoursesForClub = null;
                    resetCourse();
                    autoSelectCourseIfSingle();
                });
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public List<Course> getCoursesForSelect() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (appContext.getClub() == null || appContext.getClub().getIdclub() == null) {
            return Collections.emptyList();
        }
        try {
            return courseListForClubService.list(appContext.getClub());
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public Integer getSelectedCourseId() {
        // auto-select on initial page load when only one course exists
        if (appContext.getCourse() == null || appContext.getCourse().getIdcourse() == null) {
            autoSelectCourseIfSingle();
        }
        return appContext.getCourse() != null ? appContext.getCourse().getIdcourse() : null;
    } // end method

    private void autoSelectCourseIfSingle() {
        List<Course> courses = getCoursesForSelect();
        if (courses.size() == 1) {
            appContext.setCourse(courses.get(0));
            LOG.debug("auto-selected single course id={}", courses.get(0).getIdcourse());
        }
    } // end method

    public void setSelectedCourseId(Integer courseId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (courseId == null) { resetCourse(); return; }
        try {
            courseListForClubService.list(appContext.getClub()).stream()
                .filter(c -> courseId.equals(c.getIdcourse()))
                .findFirst()
                .ifPresent(c -> appContext.setCourse(c));
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

// ── Dialog-based selection (legacy dialogs, kept for other screens) ──────────

public String selectedClubFromDialog(Club selectedClub) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    LOG.debug("param club = {}", selectedClub);
    LOG.debug("original/old club value = {}", appContext.getClub());
    
    // ✅ Mise à jour du club via appContext
    // setClub() va automatiquement charger les courses via loadCoursesForClub()
    setClub(selectedClub);
    
    // ✅ Reset du course
    appContext.setCourse(new Course());
    
    // ✅ Invalidation du cache (déjà fait dans setClub via loadCoursesForClub, mais on peut garder pour être sûr)
    // was: lists.CourseListForClub.setListe(null);
    invalidateClubCaches();

    LOG.debug("Exiting with club = {}", appContext.getClub());
    
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
    LOG.debug("entering {}", methodName);
    LOG.debug("param course = {}", selectedCourse);
    LOG.debug("original course value = {}", appContext.getCourse());

    // ✅ setCourse() charge automatiquement les tees via loadTeesForCourse()
    setCourse(selectedCourse);

    // ✅ Fermeture du dialogue
    dialogController.closeDialog(null);

    LOG.debug("Exiting with course = {}", appContext.getCourse());
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
    LOG.debug("entering {} - type={}", methodName, type);
    try {
        // For tarif purposes, restrict to clubs where the player is local administrator
        var purpose = clubSelectionContext.getPurpose();
        if (purpose != null) {
            String code = purpose.getCode();
            if ("CreateTarifGreenfee".equals(code) || "CreateTarifMember".equals(code)) {
                List<Club> localAdminClubs = clubsListLocalAdmin.list(appContext.getPlayer());
                LOG.debug("{} - returning {} local-admin clubs for purpose={}", methodName, localAdminClubs.size(), code);
                return localAdminClubs;
            }
        }
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
    LOG.debug("entering {}", methodName);
    cptFlight++;
    try {
        // Guard clause — session-level cache
        if (cachedFlightsLoaded) {
            LOG.debug("returning cached flightList size = {}", flightList.size());
            return flightList;
        }
        LOG.debug("starting with cptFlight=1");
            
        Round round  = appContext.getRound();
            LOG.debug("round from appContext = {}", round);
        Club club   = appContext.getClub();
            LOG.debug("club from appContext = {}", club);
        Course course = appContext.getCourse();
            LOG.debug("course from appContext = {}", course);
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
        cachedFlightsLoaded = true;
        return flightList;
    } catch (Exception ex) {
        handleGenericException(ex, methodName);
        return Collections.emptyList();
    }
} // end method

/*
 * Invalide tous les caches session liés aux clubs/courses.
 */
/**
 * Invalide tous les caches liés aux clubs/courses/tees/holes.
 * Hiérarchie : Club > Course > Tee > Hole
 * Supprimer un niveau invalide aussi tous les niveaux enfants.
 */
public void invalidateClubCaches() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    // Session caches
    this.cachedClubsCoursesTees = null;
    this.cachedCoursesForClub = null;
    this.cachedFlightsLoaded = false;
    this.cptFlight = 0;
    // CDI @ApplicationScoped caches — via CacheInvalidator (centralisé)
    cacheInvalidator.invalidateClubCaches();
    LOG.debug("session + CDI caches invalidated");
} // end method

public void invalidateFlightCache() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    this.cachedFlightsLoaded = false;
    this.cptFlight = 0;
    LOG.debug("flight cache invalidated");
} // end method

public String findClubWebsite() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    try {
        Club club = appContext.getClub();                               // ✅ appContext
        LOG.debug("for club = {}", club);

        if (club.getClubWebsite() == null || club.getClubWebsite().trim().isEmpty()) {
            showMessageFatal("Website must be completed !");
            return null;
        }

        // ✅ Ajouter https:// si pas déjà présent
        String url = club.getClubWebsite().trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        // ✅ Security: validate URL to prevent open redirect
        java.net.URI uri = new java.net.URI(url);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
            showMessageFatal("Invalid website URL");
            return null;
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            showMessageFatal("Invalid website URL");
            return null;
        }

        LOG.debug("redirecting to = {}", url);
        FacesContext.getCurrentInstance().getExternalContext().redirect(url);
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
                LOG.debug("for club = {}", club);  // a été complété par clubWebsiteListener, 
            if(club.getClubWebsite() == null){
                club.setClubWebsite("Website must be completed !");
                return null;
            }
            FacesContext.getCurrentInstance().getExternalContext().redirect("http://" + club.getClubWebsite());  // https ???
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
        LOG.debug("entering {}", methodName);
        try {
            Club club = getClub();
            LOG.debug("pour club = {}", club);
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
    LOG.debug("entering {}", methodName);
    LOG.debug("with hole = {}", hole);
    LOG.debug("with hole via délégation = {}", getHole());
    try {
        Short yards = hole.getHoleDistance();               // ✅ Short comme dans l'entité
        LOG.debug("yards = {}", yards);

        if (yards == null || yards == 0) {
            showMessageFatal("Distance must be completed");
            return;
        }

        // Conversion yards → mètres (1 yard = 0.9144 m)
        Short metres = (short) Math.round(yards * 0.9144);
        hole.setHoleDistance(metres);                       // ✅ setHoleDistance(Short)

        LOG.debug("converted {} yards → {} metres", yards, metres);

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
        LOG.debug("entering {}", methodName);
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
        LOG.debug("entering {}", methodName);
        try {
            Club club = readClubService.read(ecl.club());
            appContext.setClub(club);
            LOG.debug("adding a course for idclub = {} {}", ecl.club().getIdclub(), club.getClubName());
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
        LOG.debug("entering {}", methodName);
        try {
            Course course = readCourseService.read(ecl.course());
            Club club = readClubService.read(ecl.club());
            appContext.setClub(club);
            LOG.debug("idclub after loadCourse= {}", club.getIdclub());
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
        LOG.debug("entering {}", methodName);
        try {
            Club club = readClubService.read(ecl.club());
            appContext.setClub(club);
            LOG.debug("club handled is {} : {}", club.getIdclub(), club.getClubName());
            Course course = readCourseService.read(ecl.course());
            appContext.setCourse(course);
            LOG.debug("course handled is {} : {}", course.getIdcourse(), course.getCourseName());
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
        LOG.debug("entering {}", methodName);
        try {
            tee = readTeeService.read(ecl.tee());
            Course course = readCourseService.read(ecl.course());
            Club club = readClubService.read(ecl.club());
            appContext.setClub(club);
            appContext.setCourse(course);
            LOG.debug("idcourse after loadTee= {}", course.getIdcourse());
            LOG.debug("idtee after loadTee= {}", tee.getIdtee());
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
        LOG.debug("entering {}", methodName);
        LOG.debug("with type = {}", type);
        try {
            tee = readTeeService.read(ecl.tee());
            holesGlobal = readHoleService.read(tee);
            LOG.debug("holesGlobal dataHoles = {}", Arrays.deepToString(holesGlobal.getDataHoles()));
            Course course = readCourseService.read(ecl.course());
            Club club = readClubService.read(ecl.club());
            appContext.setClub(club);
            appContext.setCourse(course);
            LOG.debug("course after loadHoles = {}", course);
            LOG.debug("tee after loadHoles = {}", tee);
            hole.setCreateModify(false);
            if ("global".equals(type)) {
                return "hole.xhtml?faces-redirect=true&operation=modify holes Global";
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

    /*
     * Action du bouton "Choix Club et Parcours" (selectClubCourse.xhtml)
     * Navigue vers la page finale selon le SelectionPurpose.
     */
    /**
     * Central action for selectClubCourse.xhtml — routes based on SelectionPurpose.
     * Phase 2: all routing via purpose enum, no more inputSelectCourse string matching.
     */
    public String clubAndCourseAction() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        enumeration.SelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                .orElse(enumeration.SelectionPurpose.CREATE_PLAYER);
        LOG.debug("with purpose = {}", purpose);

        // Recharge le club complet depuis la DB si l'utilisateur a saisi l'ID manuellement
        try {
            Club club = appContext.getClub();
            if (club != null && club.getIdclub() != null && club.getIdclub() > 0
                    && (club.getClubName() == null || club.getClubName().isBlank())) {
                club = readClubService.read(club);
                appContext.setClub(club);
                LOG.debug("club reloaded from DB: {}", club);
            }
            // Recharge le course complet (sauf si purpose ne nécessite pas de course)
            if (purpose != enumeration.SelectionPurpose.CREATE_TARIF_MEMBER
                    && purpose != enumeration.SelectionPurpose.CREATE_TARIF_GREENFEE) {
                Course course = appContext.getCourse();
                if (course != null && course.getIdcourse() != null && course.getIdcourse() > 0
                        && (course.getCourseName() == null || course.getCourseName().isBlank())) {
                    course = readCourseService.read(course);
                    appContext.setCourse(course);
                    LOG.debug("course reloaded from DB: {}", course);
                }
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }

        return switch (purpose) {
            case CHART_ROUND, CHART_COURSE -> selectChart(null);
            case CREATE_TARIF_MEMBER -> purpose.navigationToFinal();
            case CREATE_TARIF_GREENFEE -> purpose.navigationToFinal();
            case CREATE_ROUND -> purpose.navigationToFinal();
            default -> purpose.navigationToFinal();
        };
    } // end method

    /**
     * Action du bouton de sélection club (selectClubDelete.xhtml / selectClubDialog.xhtml)
     * Navigue selon le contexte inputSelectClub dans la session.
     */
    public String selectorClubNextView() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            enumeration.SelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                    .orElse(enumeration.SelectionPurpose.CREATE_PLAYER);
            LOG.debug("with purpose = {}", purpose);
            if (purpose == enumeration.SelectionPurpose.PAYMENT_COTISATION) {
                LOG.debug("purpose is PAYMENT_COTISATION");
                LOG.debug("pour le club = {}", appContext.getClub().getIdclub());
                return memberController.findTarifCotisationForToday();
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

    public List<String> getUnavailabilityTypeKeys() {
        return List.of(
                "unavailable.type.maintenance",
                "unavailable.type.impraticable",
                "unavailable.type.competition");
    } // end method

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
        LOG.debug("entering {}", methodName);
        tee.setTeeHolesPlayed("01-18");
        return "modify_hole.xhtml?faces-redirect=true&cmd=" + appContext.getInputSelectCourse();
    } // end method

    /**
     * Charge un hole à partir d'une sélection ECourseList.
     * Migré depuis CourseController — 2026-02-25
     */
    public String loadHole(ECourseList ecl) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            tee = readTeeService.read(ecl.tee());
            Course course = readCourseService.read(ecl.course());
            Club club = readClubService.read(ecl.club());
            appContext.setCourse(course);
            appContext.setClub(club);
            LOG.debug("idcourse = {}", course.getIdcourse());
            LOG.debug("idtee = {}", tee.getIdtee());
            if (course.getIdcourse() == null) {
                course.setIdcourse(tee.getCourse_idcourse());
                LOG.debug("idcourse forced");
            }
            if (tee != null) {
                tee.setCreateModify(false);
                return "hole.xhtml?faces-redirect=true&operation=modify hole";
            } else {
                LOG.error("tee not retrieved");
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
        LOG.debug("entering {} for type = {}", type);
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

    // selectCourse(ECourseList) removed 2026-03-23 — dead code, routing via clubAndCourseAction + SelectionPurpose

    /**
     * Sélection d'un course pour afficher le trajet (maps).
     * Migré depuis CourseController — 2026-02-25
     */
    public String selectTravel(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            // ecl is null when called from include_course_selector (club/course already in appContext)
            if (ecl != null) {
                appContext.setClub(ecl.club());
                appContext.setCourse(ecl.course());
            }

            String msg = "Select Travel Successful = "
                    + " <br/> Club name = " + appContext.getClub().getClubName()
                    + " <br/> Course name = " + appContext.getCourse().getCourseName();
            LOG.debug(msg);
            showMessageInfo(msg);
            LOG.debug("inputSelectCourse = {}", appContext.getInputSelectCourse());
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
        LOG.debug("entering {}", methodName);
        try {
            // ecl is null when called from include_course_selector (club/course already in appContext)
            if (ecl != null) {
                appContext.setClub(ecl.club());
                appContext.setCourse(ecl.course());
            }
            String msg = "Select Course Successful = "
                    + " <br/> Club name = " + appContext.getClub().getClubName()
                    + " <br/> Course name = " + appContext.getCourse().getCourseName()
                    + " / " + appContext.getCourse().getIdcourse();
            LOG.debug(msg);
            showMessageInfo(msg);
            LOG.debug("inputSelectCourse = {}", appContext.getInputSelectCourse());
            // new Controllers.ChartController().lineModelCourse(conn, ...)
            String v = chartController.lineModelCourse(appContext.getPlayer(), appContext.getCourse()); // migrated 2026-02-26
            setLineModelCourse(v);
            LOG.debug("Chart returned = {}", getLineModelCourse());
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
        LOG.debug("entering {} with string = {}", s);
        navigationController.reset(s);
        appContext.getCourse().setCreateModify(true);
        return "course.xhtml?faces-redirect=true&operation=" + s;
    } // end method

    /**
     * Navigation vers tee.xhtml pour créer/modifier un tee.
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_tee_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with string = {}", s);
        navigationController.reset(s);
        tee.setCreateModify(true);
        return "tee.xhtml?faces-redirect=true&operation=" + s;
    } // end method

    /**
     * Liste les courses pour un club donné (par ID string).
     * Migré depuis CourseController — 2026-02-25
     */
    public List<Course> listCoursesForClub(String clubid) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with param clubid = {}", clubid);
        if (cachedCoursesForClub != null) {
            LOG.debug("returning cached list size = {}", cachedCoursesForClub.size());
            return cachedCoursesForClub;
        }
        try {
            Club club = appContext.getClub();
            LOG.debug("club = {}", club);
            if (clubid == null || clubid.isEmpty()) {
                LOG.debug("param clubid == null or empty");
            } else {
                club.setIdclub(Integer.parseInt(clubid));
            }
            LOG.debug("Club = {}", club);
            cachedCoursesForClub = courseListForClubService.list(club); // migrated 2026-02-24
            return cachedCoursesForClub;
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
        LOG.debug("entering {}", methodName);
        Club club = appContext.getClub();
        LOG.debug("idclub = {}", club.getIdclub());
        LOG.debug("currentPhaseId = {}", FacesContext.getCurrentInstance().getCurrentPhaseId());
        boolean isPostback = FacesContext.getCurrentInstance().isPostback();
        LOG.debug("isPostBack = {}", isPostback);
        if ((!isPostback) && (club.getIdclub() != null)) {
            appContext.setClub(new Club());
            LOG.debug("club forced to null");
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
        LOG.debug("entering with string = {}", s);
        navigationController.reset("clubRestart " + s);
        tee.setModifyClubCourseTee(true);
        appContext.setInputSelectClub(s);
        if (s.equals("clubModify")) {
            return "selectClubModify.xhtml?faces-redirect=true";
        } else {
            appContext.setInputSelectCourse(s);
            return "modify_ClubCourseTee.xhtml?faces-redirect=true";
        }
    } // end method

    /**
     * Setter inputClub — réinitialise club/course/tee/hole si "ini".
     * Migré depuis CourseController — 2026-02-25
     */
    public void setInputClub(String inputClub) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering (new club !) = {}", inputClub);
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

    // to_selectPurpose_xhtml moved to NavigationController 2026-03-23

    /**
     * Initialise le menu unavailable : set menuLaunched=true, charge la structure si elle existe.
     * Appelé depuis unavailable_menu.xhtml après sélection club/course.
     */
    public String initUnavailableMenu() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            EUnavailable unavailable = appContext.getUnavailable();
            if (unavailable == null || unavailable.structure() == null) {
                entite.UnavailableStructure structure = new entite.UnavailableStructure();
                unavailable = new EUnavailable(structure, null);
                appContext.setUnavailable(unavailable);
                LOG.debug("EUnavailable initialized");
            }
            unavailable.structure().setMenuLaunched(true);
            var v = readUnavailableStructure.read(appContext.getClub());
            LOG.debug("structure from DB = {}", v);
            if (v != null && !v.getStructureList().isEmpty()) {
                unavailable.structure().setStructureList(v.getStructureList());
                unavailable.structure().setStructureExists(true);
                LOG.debug("structure exists");
            } else {
                LOG.debug("NO structure exists");
            }
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String to_selectCourseLA_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering with string = {}", s);
        navigationController.reset("Reset to_selectClub2 " + s);
        appContext.setInputSelectCourse(s);
        appContext.setAdminType("admin");
        LOG.debug("club selected for :  = {}", appContext.getInputSelectCourse());
        return "selectClubCourse.xhtml?faces-redirect=true"; // consolidated 2026-03-23
    } // end method

    public String to_update_help(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering with string = {}", s);
        navigationController.reset("Reset to_update_help " + s);
        appContext.setInputSelectCourse(s);
        return "editor_help.xhtml?faces-redirect=true";
    } // end method

    public String to_selectClub_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering with string = {}", s);
        navigationController.reset("Reset to_selectClub" + s);
        appContext.setInputSelectCourse(s);
        appContext.setInputSelectClub(s);
        return "selectClub.xhtml?faces-redirect=true&cmd=" + appContext.getInputSelectCourse();
    } // end method

    public String to_selectClubDialog_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering with string = {}", s);
        navigationController.reset("Reset to_selectClubDialog" + s);
        appContext.setInputSelectClub(s);
        return "selectClubDialog.xhtml?faces-redirect=true&cmd=" + appContext.getInputSelectClub();
    } // end method

    // ========================================
    // Groupe E — Extra navigation methods
    // migrated from CourseController 2026-02-25 (Phase 2)
    // ========================================

    public void to_reset_menu(String ini) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        navigationController.reset(ini);
        // sessionMap.put("playerid/playerlastname/playerage") — removed 2026-02-28, dead code
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
        LOG.debug("entering {}", methodName);
        courseListForClub = courseListForClubService.list(appContext.getClub()); // migrated 2026-02-25
    } // end method

    /** Migrated 2026-02-28 from NavigationController */
    public List<ECourseList> listProfessionalForClub() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return professionalListForClub.list();
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * Créer un professional pour un club.
     * Migré depuis CourseController — 2026-02-28
     * @return 
     */
    public String createProfessional() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("club = {}", appContext.getClub());
            LOG.debug("playerTemp = {}", appContext.getPlayerTemp());
            LOG.debug("professional = {}", appContext.getProfessional());

            appContext.getProfessional().setProClubId(appContext.getClub().getIdclub());
            appContext.getProfessional().setProPlayerId(appContext.getPlayerTemp().getIdplayer());

            if (createProfessionalService.create(appContext.getProfessional())) {
                String msg = "professional created";
                LOG.info(msg);
                showMessageInfo(msg);
            } else {
                String msg = "FATAL error : professional NOT created - " + appContext.getProfessional();
                LOG.error(msg);
                showMessageFatal(msg);
            }
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public List<ECourseList> listProfessionalsForLocalAdmin() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return professionalListForClub.listForLocalAdmin(appContext.getPlayer().getIdplayer());
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    public void updateProFull() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (updateProfessionalService.updateFull(selectedProfessional)) {
                cacheInvalidator.invalidateProfessionalCaches();
                String msg = "Professional updated: " + selectedProfessional.getProId();
                LOG.info(msg);
                showMessageInfo(msg);
            } else {
                showMessageFatal("Error updating Professional ProId=" + selectedProfessional.getProId());
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method

    public void updateProAmount() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (updateProfessionalService.updateAmount(selectedProfessional)) {
                cacheInvalidator.invalidateProfessionalCaches();
                String msg = "ProAmount updated: " + selectedProfessional.getProAmount();
                LOG.info(msg);
                showMessageInfo(msg);
            } else {
                showMessageFatal("Error updating ProAmount for ProId=" + selectedProfessional.getProId());
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method

    public entite.Professional getSelectedProfessional() { return selectedProfessional; }
    public void setSelectedProfessional(entite.Professional p) { this.selectedProfessional = p; }

    public List<ECourseList> listCoursesPublic() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            SelectionPurpose purpose = clubSelectionContext.getPurpose(); // Phase 2 — 2026-03-23
            LOG.debug("with purpose = {}", purpose);
            if (purpose == SelectionPurpose.CHART_COURSE
                    || purpose == SelectionPurpose.CREATE_ROUND
                    || purpose == SelectionPurpose.CREATE_TARIF_GREENFEE) {
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
        LOG.debug("entering {}", methodName);
        LOG.debug("with id club = {}", id);
        try {
            Club c = new Club();
            c.setIdclub(Integer.valueOf(id));
            cacheInvalidator.invalidateClubCaches(); // centralized 2026-03-22
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
        LOG.debug("entering {}", methodName);
        if (c == null) {
            LOG.warn("Selected club param is null!");
            showMessageFatal("Aucun club sélectionné !");
            return null;
        }
        LOG.debug("with param club = {}", c);
        appContext.setClub(c);
        LOG.debug("with club = {}", appContext.getClub());
        try {
            enumeration.SelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                    .orElse(enumeration.SelectionPurpose.CREATE_PLAYER);
            LOG.debug("with purpose = {}", purpose);

            return switch (purpose) {
                case CREATE_PLAYER -> {
                    appContext.getPlayer().setPlayerHomeClub(appContext.getClub().getIdclub());
                    LOG.debug("Home club set for CREATE_PLAYER: {}", appContext.getPlayer());
                    dialogController.closeDialog(null);
                    yield playerController.getCreateModifyPlayer().equals("M") ?
                            "player_modify.xhtml?faces-redirect=true" :
                            "player.xhtml?faces-redirect=true";
                }
                case LOCAL_ADMIN -> {
                    LOG.debug("inside LOCAL_ADMIN");
                    appContext.getLocalAdmin().setPlayerHomeClub(appContext.getClub().getIdclub());
                    LOG.debug("Home club set for LOCAL_ADMIN: {}", appContext.getLocalAdmin());
                    dialogController.closeDialog(null);
                    yield null;
                }
                case CREATE_PRO -> {
                    appContext.getPlayerPro().setPlayerHomeClub(appContext.getClub().getIdclub());
                    LOG.debug("Home club set for CREATE_PRO: {}", appContext.getPlayerPro());
                    dialogController.closeDialog(null);
                    yield "professional.xhtml?faces-redirect=true";
                }
                case CREATE_ROUND -> {
                    LOG.debug("club setted for CREATE_ROUND: {}", appContext.getClub());
                    LOG.debug("course setted for CREATE_ROUND: {}", appContext.getCourse());
                    dialogController.closeDialog(null);
                    yield null;
                }
                case PAYMENT_COTISATION -> {
                    appContext.getRound().setRoundDate(LocalDateTime.now());
                    tarifMember = findTarifMembersData.find(appContext.getClub(), appContext.getRound()); // migrated 2026-02-26 navigationController.getRound() → appContext
                    LOG.debug("TarifMember loaded for club = {}", appContext.getClub());
                    dialogController.closeDialog(null);
                    yield null;
                }
                case MENU_UNAVAILABLE -> {
                    LOG.debug("Menu unavailable selected, club = {}", appContext.getClub());
                    dialogController.closeDialog(null);
                    yield null;
                }
                default -> {
                    LOG.warn("Unknown SelectionPurpose: {}", purpose);
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
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("with course input parameter = {}", c);
            LOG.debug("for club = {}", appContext.getClub());
            enumeration.SelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                    .orElse(enumeration.SelectionPurpose.CREATE_PLAYER);
            LOG.debug("with purpose = {}", purpose);
            appContext.setCourse(c);
            LOG.debug("course is now = {}", appContext.getCourse());
            String msg = "Select Course Successfull = <br/> CourseName = " + appContext.getCourse().getCourseName();
            LOG.debug(msg);
            showMessageInfo(msg);
            LOG.debug(": inputSelectClub = {}", appContext.getInputSelectClub());

            dialogController.closeDialog(null);

            if ("MenuUnavailable".equals(appContext.getInputSelectClub())) {
                LOG.debug("handling menu unavailable");
                // Initialize EUnavailable if null
                EUnavailable unavailable = appContext.getUnavailable();
                if (unavailable == null || unavailable.structure() == null) {
                    entite.UnavailableStructure structure = new entite.UnavailableStructure();
                    unavailable = new EUnavailable(structure, null);
                    appContext.setUnavailable(unavailable);
                    LOG.debug("EUnavailable initialized");
                }
                LOG.debug("for unavailable = {}", unavailable);
                unavailable.structure().setMenuLaunched(true);
                var v = readUnavailableStructure.read(appContext.getClub());
                LOG.debug("variable v found = ENTITE UnavailableStructure {}", v);
                if (v != null && !v.getStructureList().isEmpty()) {
                    unavailable.structure().setStructureList(v.getStructureList());
                    unavailable.structure().setStructureExists(true);
                    LOG.debug("structure exists {}", v);
                } else {
                    LOG.debug("NO structure exists {}", v);
                }
                dialogController.closeDialog(null);
                return null;
            }

            if ("CREATE COMPETITION".equals(appContext.getInputSelectClub())) {
                LOG.debug("competition = {}", appContext.getCompetition());
                appContext.getCompetition().competitionDescription().setCompetitionCourseId(appContext.getCourse().getIdcourse());
                appContext.getCompetition().competitionDescription().setCompetitionCourseIdName(
                        Integer.toString(appContext.getCourse().getIdcourse()) + " - " + appContext.getCourse().getCourseName());
                LOG.debug("competition updated CourseId = {}", appContext.getCompetition().competitionDescription());
                dialogController.closeDialog(null);
                return null;
            }

            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String selectClub(Club c, String select) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("with select = {}", select);
            LOG.debug("with in_club = {}", c);
            // c is null when called from include_club_selector (club already in appContext)
            if (c != null) {
                appContext.setClub(c);
            }

            String msg = "Select Club Successfull = "
                    + " <br/> Club name = " + appContext.getClub().getClubName()
                    + " <br/> inputSelectCourse = " + select;
            LOG.debug(msg);

            SelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose()).orElse(SelectionPurpose.CREATE_PLAYER);
            LOG.debug("with purpose = {}", purpose);
            
            if (purpose == SelectionPurpose.LOCAL_ADMIN) {
                   LOG.debug(": inputSelectCourse = {}", appContext.getInputSelectCourse());
                   
              }
            if (appContext.getInputSelectCourse() == null) {
                msg = "No InputSelectCourse !";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }
            if (select.equals("CreatePro")) {
                
                // ici intervenir pour soution avec purpose
                return "professional.xhtml?faces-redirect=true";
            }

            if (select.equals("CreateUnavailablePeriod")) {
                var v = readUnavailableStructure.read(appContext.getClub());
                EUnavailable unavailable = appContext.getUnavailable();
                unavailable.withStructure(v);
                LOG.debug("returned with unavailable structure");
                if (unavailable.structure() == null) {
                    String msgerr = LCUtil.prepareMessageBean("unavailable.structure.notfound");
                    LOG.error(msgerr);
                    LCUtil.showMessageInfo(msgerr);
                    return "unavailable_structure.xhtml?faces-redirect=true";
                } else {
                    appContext.getClub().setUnavailableStructure(unavailable.structure());
                    LOG.debug("structure length = {}", appContext.getClub().getUnavailableStructure().getStructureList().size());
                    return "unavailable_period.xhtml?faces-redirect=true";
                }
            }

            if (purpose == SelectionPurpose.CREATE_TARIF_GREENFEE) {
                     LOG.debug("purpose is CREATE_TARIF_GREENFEE");
                     LOG.debug(" return TO tarif_greenfee_wizard.xhtml");
                     return purpose.navigationToFinal(); //
                 }
            if (select.equals("createTarifGreenfee")) { // old solution
                return "tarif_greenfee_wizard.xhtml?faces-redirect=true";
            }

                 if (purpose == SelectionPurpose.CREATE_TARIF_MEMBER) {
                     LOG.debug("purpose is CREATE_TARIF_MEMBER");
                       LOG.debug(" return TO tarif_member_wizard.xhtml");
                     return purpose.navigationToFinal();
                 }
            
            if (select.equals("createTarifMember")) { // old solution
                LOG.debug("select.equals(createTarifMember"); // si équivalence, alors on n'a plus besoin de select
                return "tarif_member_wizard.xhtml?faces-redirect=true";
            }

            if (select.equals("PaymentCotisationSpontaneous")) {
                LOG.debug("entering PaymentCotisationSpontaneous");
                LOG.debug("club = {}", appContext.getClub());
                LOG.debug("round = {}", appContext.getRound());
                
                tarifMember = findTarifMembersData.find(appContext.getClub(), appContext.getRound()); // migrated 2026-02-26 navigationController.getRound() → appContext
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

    // selectCourseLA removed 2026-03-23 — dead code, routing via clubAndCourseAction + SelectionPurpose
    private void selectCourseLA_placeholder() {
    } // end method

    // selectClubCourse() and selectedCourse() removed 2026-03-23 — dead code, replaced by clubAndCourseAction

    // ========================================
    // Groupe D — Unavailable methods
    // migrated from CourseController 2026-02-25 (Phase 2)
    // ========================================

    public String createUnavailablePeriod() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            EUnavailable unavailable = appContext.getUnavailable();
            if (appContext.getClub() == null || appContext.getClub().getIdclub() == null) {
                showMessageFatal(LCUtil.prepareMessageBean("message.selectclub"));
                return null;
            }
            if (unavailable.period().getStartDate() == null || unavailable.period().getEndDate() == null) {
                showMessageFatal(LCUtil.prepareMessageBean("tarif.member.period.dates.required"));
                return null;
            }
            unavailable.period().setIdclub(appContext.getClub().getIdclub());
            unavailable.structure().setPeriodSaved(true);
            unavailable.structure().setMenuLaunched(true);
            LOG.debug("period saved to bean: start={}, end={}", unavailable.period().getStartDate(), unavailable.period().getEndDate());
            showMessageInfo(unavailable.period().getStartDate() + " → " + unavailable.period().getEndDate());
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String updateUnavailablePeriodAvailability() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            EUnavailable unavailable = appContext.getUnavailable();
            if (unavailable.period().getUnavailabilityType() == null
                    || unavailable.period().getUnavailabilityType().isBlank()) {
                showMessageFatal(LCUtil.prepareMessageBean("unavailable.type.required"));
                return null;
            }
            LOG.debug("availability saved to bean: type={}, label={}",
                    unavailable.period().getUnavailabilityType(),
                    unavailable.period().getUnavailabilityLabel());
            showMessageInfo(unavailable.period().getUnavailabilityType()
                    + (unavailable.period().getUnavailabilityLabel() != null
                       ? " — " + unavailable.period().getUnavailabilityLabel() : ""));
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String saveFullUnavailability() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (appContext.getClub() == null || appContext.getClub().getIdclub() == null) {
                showMessageFatal(LCUtil.prepareMessageBean("message.selectclub"));
                return null;
            }
            EUnavailable unavailable = appContext.getUnavailable();
            Club club = appContext.getClub();

            // 1. Period (insert si pas encore persisté en DB)
            if (unavailable.period().getStartDate() == null || unavailable.period().getEndDate() == null) {
                showMessageFatal(LCUtil.prepareMessageBean("tarif.member.period.dates.required"));
                return null;
            }
            unavailable.period().setIdclub(club.getIdclub());
            if (!unavailable.structure().isPeriodPersistedToDB()) {
                if (findUnavailablePeriodOverlapping.find(unavailable.period())) {
                    return null; // message already shown by the service
                }
                if (createUnavailablePeriodService.create(unavailable.period())) {
                    unavailable.structure().setPeriodSaved(true);
                    unavailable.structure().setMenuLaunched(true);
                    unavailable.structure().setPeriodPersistedToDB(true);
                    LOG.debug("period inserted to DB");
                } else {
                    showMessageFatal(LCUtil.prepareMessageBean("unavailable.availability.notsaved"));
                    return null;
                }
            }

            // 2. Availability type/label (update le record le plus récent du club)
            if (unavailable.period().getUnavailabilityType() != null
                    && !unavailable.period().getUnavailabilityType().isBlank()) {
                if (!updateUnavailablePeriodService.updateAvailability(unavailable.period())) {
                    LOG.warn("updateAvailability returned false — type/label not persisted");
                }
            }

            // 3. Structure → club.ClubUnavailableStructure
            if (unavailableController.updateClub(unavailable, club)) {
                unavailable.structure().setStructureExists(true);
                showMessageInfo(LCUtil.prepareMessageBean("unavailable.availability.saved"));
            } else {
                showMessageFatal(LCUtil.prepareMessageBean("unavailable.availability.notsaved"));
            }
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String inputUnvailableStructure() throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("for club = {}", appContext.getClub());
            EUnavailable unavailable = appContext.getUnavailable();
            LOG.debug("for unavailable = {}", unavailable);
            unavailable = unavailableController.inputUnvailableStructure(unavailable);
            appContext.setUnavailable(unavailable);
            LOG.debug("back with unavailable = {}", unavailable);
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String showUnavailableStructure() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            EUnavailable unavailable = appContext.getUnavailable();
            LOG.debug("for unavailable = {}", unavailable);
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
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("for club = {}", appContext.getClub());
            if (appContext.getClub() == null) {
                LOG.warn("club is null, skipping unavailable check");
                return null;
            }
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
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("for club = {}", appContext.getClub());
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
        LOG.debug("entering for club input = {}", c);
        try {
            EUnavailable lun = unavailableListForDate.list(LocalDateTime.now(), c);
            if (lun == null) {
                LOG.debug("lun is null");
                LOG.debug("no unavailabilities known");
                appContext.setUnavailable(null);
                return "unavailable_show.xhtml?faces-redirect=true";
            } else {
                appContext.setUnavailable(lun);
                LOG.debug("showUnavailablePeriods - element is = {}", lun);
                return "unavailable_show.xhtml?faces-redirect=true";
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String showUnavailablePeriods(ECourseList ecl) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            appContext.setClub(ecl.club());
            EUnavailable lun = unavailableListForDate.list(LocalDateTime.now(), ecl.club());
            LOG.debug("showUnavailablePeriods - element of list is = {}", lun);
            appContext.setUnavailable(lun);
            return "unavailable_show.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // Wizard unavailable — flow + helpers
    // ========================================

    public String onUnavailableWizardFlow(org.primefaces.event.FlowEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("oldStep = {}, newStep = {}", event.getOldStep(), event.getNewStep());
        try {
            // Period → Availability: dates required then period must be saved
            if ("PeriodTab".equals(event.getOldStep()) && "AvailabilityTab".equals(event.getNewStep())) {
                EUnavailable unavailable = appContext.getUnavailable();
                if (!unavailable.structure().isPeriodSaved()) {
                    if (unavailable.period().getStartDate() == null || unavailable.period().getEndDate() == null) {
                        showMessageFatal(LCUtil.prepareMessageBean("tarif.member.period.dates.required"));
                    } else {
                        showMessageFatal(LCUtil.prepareMessageBean("unavailable.period.required"));
                    }
                    return event.getOldStep();
                }
                LOG.debug("PeriodTab validated — periodSaved=true");
            }
            // Availability → Structure: validate type is selected
            if ("AvailabilityTab".equals(event.getOldStep()) && "StructureTab".equals(event.getNewStep())) {
                EUnavailable unavailable = appContext.getUnavailable();
                if (unavailable == null || unavailable.period() == null
                        || unavailable.period().getUnavailabilityType() == null
                        || unavailable.period().getUnavailabilityType().isBlank()) {
                    showMessageFatal(LCUtil.prepareMessageBean("unavailable.type.required"));
                    return event.getOldStep();
                }
                LOG.debug("AvailabilityTab validated — type = {}", unavailable.period().getUnavailabilityType());
            }
            // Structure → Editor: validate at least one item in structure
            if ("StructureTab".equals(event.getOldStep()) && "EditorTab".equals(event.getNewStep())) {
                EUnavailable unavailable = appContext.getUnavailable();
                if (unavailable == null || unavailable.structure() == null
                        || unavailable.structure().getStructureList() == null
                        || unavailable.structure().getStructureList().isEmpty()) {
                    showMessageFatal(LCUtil.prepareMessageBean("unavailable.structure.notfound"));
                    return event.getOldStep();
                }
                LOG.debug("StructureTab validated — {} items", unavailable.structure().getStructureList().size());
            }
            return event.getNewStep();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return event.getOldStep();
        }
    } // end method

    public void initUnavailableWizard() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        unavailableDB = null;
        EUnavailable unavailable = appContext.getUnavailable();
        if (unavailable == null) {
            appContext.setUnavailable(new EUnavailable(new entite.UnavailableStructure(), new entite.UnavailablePeriod()));
            LOG.debug("EUnavailable initialized fresh");
        } else {
            unavailable.structure().setPeriodSaved(false);
            unavailable.structure().setPeriodPersistedToDB(false);
            unavailable.structure().setMenuLaunched(false);
            unavailable.period().setStartDate(null);
            unavailable.period().setEndDate(null);
            LOG.debug("EUnavailable reset for new wizard session");
        }
    } // end method

    public List<Club> getClubsForUnavailableWizard() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return clubsListLocalAdmin.list(appContext.getPlayer());
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    public Integer getUnavailableWizardClubId() {
        Club club = appContext.getClub();
        return (club != null) ? club.getIdclub() : null;
    } // end method

    public void setUnavailableWizardClubId(Integer clubId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (clubId == null) return;
        try {
            List<Club> clubs = clubsListLocalAdmin.list(appContext.getPlayer());
            Club selected = clubs.stream()
                    .filter(c -> clubId.equals(c.getIdclub()))
                    .findFirst()
                    .orElse(null);
            if (selected != null) {
                appContext.setClub(selected);
                LOG.debug("unavailable wizard club set to {}", selected.getClubName());
            }
            courseListForClub = Collections.emptyList();
            findCourseListForClub();
            // Fresh EUnavailable for this club — load existing structure from DB
            EUnavailable fresh = new EUnavailable(new entite.UnavailableStructure(), new entite.UnavailablePeriod());
            entite.UnavailableStructure v = readUnavailableStructure.read(appContext.getClub());
            if (v != null && !v.getStructureList().isEmpty()) {
                fresh.structure().setStructureList(v.getStructureList());
                fresh.structure().setStructureExists(true);
                fresh.structure().setItemExists(true);
            }
            appContext.setUnavailable(fresh);
            unavailableDB = null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method

    public Integer getUnavailableWizardCourseId() {
        entite.UnavailablePeriod p = appContext.getUnavailable() != null ? appContext.getUnavailable().period() : null;
        if (p == null) return null;
        return p.isAllCourses() ? Integer.valueOf(9999) : p.getCourseId();
    } // end method

    public void setUnavailableWizardCourseId(Integer value) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        entite.UnavailablePeriod p = appContext.getUnavailable() != null ? appContext.getUnavailable().period() : null;
        if (p == null) return;
        if (value == null) {
            p.setAllCourses(false);
            p.setCourseId(null);
        } else if (value == 9999) {
            p.setAllCourses(true);
            p.setCourseId(null);
        } else {
            p.setAllCourses(false);
            p.setCourseId(value);
        }
        LOG.debug("allCourses = {}, courseId = {}", p.isAllCourses(), p.getCourseId());
    } // end method

    public void resetUnavailableDB() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        unavailableDB = null;
    } // end method

    public EUnavailable getUnavailableDB() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (unavailableDB != null) return unavailableDB;
        try {
            unavailableDB = unavailableListForDate.list(java.time.LocalDateTime.now(), appContext.getClub());
            if (unavailableDB == null) {
                unavailableDB = new EUnavailable(new entite.UnavailableStructure(), null);
            }
            LOG.debug("unavailableDB loaded from DB");
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            unavailableDB = new EUnavailable(new entite.UnavailableStructure(), null);
        }
        return unavailableDB;
    } // end method

    public void removeStructureItem(entite.Structure item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            EUnavailable unavailable = appContext.getUnavailable();
            unavailable.structure().getStructureList().remove(item);
            if (unavailable.structure().getStructureList().isEmpty()) {
                unavailable.structure().setItemExists(false);
            }
            LOG.debug("structureList size after remove = {}", unavailable.structure().getStructureList().size());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

} // end class