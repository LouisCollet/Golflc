package Controllers;

import Controllers.DialogController;
import context.ApplicationContext;
import entite.Club;
import entite.Country;
import entite.Course;
import entite.Flight;
import entite.Hole;
import entite.HolesGlobal;
import entite.Round;
import entite.TarifMember;
import entite.Tee;
import entite.composite.ECourseList;
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
    @Inject private lists.CourseListForClub courseListForClubService;
    @Inject private CoordinatesService coordinatesService;
    @Inject private contexte.SelectionContextBean clubSelectionContext;
    @Inject private read.ReadClub readClubService;
    @Inject private read.ReadCourse readCourseService;
    @Inject private read.ReadTee readTeeService;
    @Inject private read.ReadHole readHoleService;
    @Inject private Controllers.UnavailableController unavailableController;
    @Inject private read.ReadUnavailableStructure readUnavailableStructure;
    @Inject private Controllers.NavigationController navigationController;
    @Inject private lists.CourseListOnly courseListOnly;
    @Inject private lists.ClubDetailList clubDetailList;
    @Inject private lists.CourseList courseListService;
    @Inject private find.FindTarifMembersData findTarifMembersData;
    @Inject private Controllers.PlayerController playerController;
    @Inject private Controllers.ChartController chartController;
    @Inject private lists.ProfessionalListForClub professionalListForClub;
    @Inject private create.CreateProfessional createProfessionalService;
    @Inject private update.UpdateProfessional updateProfessionalService;
    @Inject private lists.ClubsListLocalAdmin clubsListLocalAdmin;
    @Inject private Controllers.MemberController memberController;

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

    private boolean strokeIndex = true;
    private List<ECourseList> filteredCourses = null;
    private String lineModelCourse;
    private TarifMember tarifMember;
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
    // CDI EVENT — ResetEvent observer
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} source={}", methodName, event.getSource());
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
        strokeIndex       = true;
        filteredCourses   = null;
        lineModelCourse   = null;
        tarifMember       = new TarifMember();
        LOG.debug("ClubController reset done");
    } // end method

    // ========================================
    // Délégation - Club et Course
    // ========================================

    public Club getClub() {
        Club club = appContext.getClub();
        if (club != null && club.getIdclub() != null && club.getIdclub() > 0
                && (club.getClubName() == null || club.getClubName().isBlank())) {
            try {
                club = readClubService.read(club);
                appContext.setClub(club);
                LOG.debug("getClub: lazy-loaded clubName={} for idclub={}", club.getClubName(), club.getIdclub());
            } catch (Exception e) {
                LOG.warn("getClub: could not lazy-load club name for idclub={}", club.getIdclub());
            }
        }
        return club;
    } // end method

    public void setClub(Club club) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        appContext.setClub(club);
        if (club != null && club.getIdclub() != null && club.getIdclub() > 0) {
            loadCoursesForClub(club.getIdclub());
        } else {
            courseListForClub = Collections.emptyList();
        }
    } // end method

    public Integer getClubId() {
        return appContext.getClub().getIdclub();
    } // end method

    public void setClubId(Integer id) {
        Club c = new Club();
        c.setIdclub(id);
        appContext.setClub(c);
        LOG.debug("setClubId: fresh club set with idclub={}", id);
    } // end method

    public Course getCourse() {
        return appContext.getCourse();
    } // end method

    public void setCourse(Course course) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        appContext.setCourse(course);
        if (course != null && course.getIdcourse() != null && course.getIdcourse() > 0) {
            loadTeesForCourse(course.getIdcourse());
        } else {
            teeListForCourse = Collections.emptyList();
        }
    } // end method

    // ========================================
    // CREATE - Club
    // ========================================

    public String createClub() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Club club = appContext.getClub();
            LOG.debug("club={}", club);

            if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
                String msg = "Club name is required";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }

            ClubManager.SaveResult result = clubManager.createClub(club);

            if (result.isSuccess()) {
                LOG.debug("club created : we go to course !!");
                Course course = appContext.getCourse();
                course.setNextCourse(true);
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
                String msg = "No club selected for modification";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
                String msg = "Club name is required";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }

            ClubManager.SaveResult result = clubManager.modifyClub(club);

            if (result.isSuccess()) {
                LOG.debug("club is Modified !!");
                Course course = appContext.getCourse();
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
                String msg = "No club selected for deletion";
                LOG.warn(msg);
                showMessageFatal(msg);
                return;
            }
            int clubId = club.getIdclub();
            ClubManager.SaveResult result = clubManager.deleteClub(clubId);
            boolean ok = result.isSuccess();
            LOG.debug("result of deleteClub = {}", ok);
            if (ok) {
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

            LOG.debug("club={}", club);
            LOG.debug("course={}", course);

            if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
                String msg = "Please select a club first";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
                String msg = "Course name is required";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }

            ClubManager.SaveResult result = clubManager.createCourse(course, club.getIdclub());

            if (result.isSuccess()) {
                tee.setNextTee(true);
                invalidateClubCaches();
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

            if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
                String msg = "No club selected";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                String msg = "No course selected for modification";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
                String msg = "Course name is required";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            LOG.debug("course to be modified = {}", course);

            ClubManager.SaveResult result = clubManager.modifyCourse(course, club.getIdclub());

            if (result.isSuccess()) {
                String msg = "course Modified";
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
                String msg = "No course selected for deletion";
                LOG.warn(msg);
                showMessageFatal(msg);
                return;
            }
            int courseId = course.getIdcourse();
            ClubManager.SaveResult result = clubManager.deleteCourse(courseId);
            boolean ok = result.isSuccess();
            LOG.debug("result of deleteCourse = {}", ok);
            if (ok) {
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
                String msg = "Please select a course first";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (tee.getTeeSlope() == null || tee.getTeeRating() == null) {
                String msg = "Tee slope and rating are required";
                LOG.warn(msg);
                showMessageFatal(msg);
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
                LOG.debug("{}", result.getMessage());

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
            Course course = appContext.getCourse();

            LOG.debug("tee to be modified = {}", tee.toString());

            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                String msg = "No course selected";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
                String msg = "No tee selected for modification";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (tee.getTeeSlope() == null || tee.getTeeRating() == null) {
                String msg = "Tee slope and rating are required";
                LOG.warn(msg);
                showMessageFatal(msg);
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
            String msg = "No tee selected for deletion";
            LOG.warn(msg);
            showMessageFatal(msg);
            return;
        }
        int teeId = tee.getIdtee();
        ClubManager.SaveResult result = clubManager.deleteTee(teeId);
        boolean ok = result.isSuccess();
        LOG.debug("result of deleteTee = {}", ok);
        if (ok) {
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
                String msg = "Please select a tee first";
                LOG.warn(msg);
                showMessageFatal(msg);
                return;
            }
            if (course == null || course.getIdcourse() == null) {
                String msg = "Please select a course first";
                LOG.warn(msg);
                showMessageFatal(msg);
                return;
            }
            if (hole.getHoleNumber() == null || hole.getHoleNumber() < 1 || hole.getHoleNumber() > 18) {
                String msg = "Hole number must be between 1 and 18";
                LOG.warn(msg);
                showMessageFatal(msg);
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
                LOG.debug("hole created");
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
                String msg = "No club selected";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                String msg = "No course selected";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
                String msg = "No tee selected";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (holesGlobal == null || holesGlobal.getDataHoles() == null) {
                String msg = "No holes data provided";
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }

            holesGlobal.setType(param);
            ClubManager.SaveResult result = clubManager.updateHolesGlobal(holesGlobal, tee);

            if (result.isSuccess()) {
                LOG.info("hole global created");
                showMessageInfo(utils.LCUtil.prepareMessageBean("hole.global.create"));
                loadHolesForTee(tee.getIdtee());
            } else {
                String msg = "FAILURE Create Holes Global";
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
    // Helpers
    // ========================================

    public boolean hasClub() {
        Club c = appContext.getClub();
        return c != null && c.getIdclub() != null && c.getIdclub() > 0;
    }

    public boolean hasCourse() {
        Course c = appContext.getCourse();
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
        if (clubId <= 0) {
            String msg = "Invalid club ID: " + clubId;
            LOG.warn(msg);
            showMessageFatal(msg);
            return;
        }
        try {
            Club club = clubManager.readClub(clubId);
            appContext.setClub(club);
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
            appContext.setCourse(course);
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
            Club club = appContext.getClub();
            if (club != null && club.getIdclub() != null && club.getIdclub() > 0) {
                loadCoursesForClub(club.getIdclub());
                LOG.debug("Course list refreshed for club {}", club.getIdclub());
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public List<ECourseList> listClubsCoursesTees(String param) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {} param={}", methodName, param);
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

        LOG.warn("unknown param: {}", param);
        return Collections.emptyList();

    } catch (Exception ex) {
        handleGenericException(ex, methodName);
        return Collections.emptyList();
    }
} // end method

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
                    cacheInvalidator.invalidateCourseListForClub();
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
        LOG.debug("selectedClub={}", selectedClub);
        setClub(selectedClub);
        appContext.setCourse(new Course());
        invalidateClubCaches();
        LOG.debug("club={}", appContext.getClub());
        dialogController.closeDialog(null);
        return null;
    } // end method

    public String selectedCourseFromDialog(Course selectedCourse) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("selectedCourse={}", selectedCourse);
        setCourse(selectedCourse);
        dialogController.closeDialog(null);
        LOG.debug("course={}", appContext.getCourse());
    return null;
}


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
                LOG.debug("returning {} local-admin clubs for purpose={}", localAdminClubs.size(), code);
                return localAdminClubs;
            }
        }
        return clubManager.listClubs();
    } catch (Exception ex) {
        handleGenericException(ex, methodName);
        return Collections.emptyList();
    }
} // end method

    public List<Flight> listFlights() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    cptFlight++;
    try {
        if (cachedFlightsLoaded) {
            LOG.debug("returning cached flightList size={}", flightList.size());
            return flightList;
        }
        Round round   = appContext.getRound();
        Club club     = appContext.getClub();
        Course course = appContext.getCourse();
        LOG.debug("round={} club={} course={}", round, club, course);
        if (round.getRoundDate() == null) {
            String msg = "Fatal error — round date is null";
            LOG.error(msg);
            showMessageFatal(msg);
            return Collections.emptyList();
        }

        if (club.getAddress().getLatLng().getLat() == 0) {
            String msg = "Club latitude is unknown";
            LOG.error(msg);
            showMessageFatal(msg);
            return Collections.emptyList();
        }

        flightList = clubManager.computeAvailableFlights(round, club, course);
        cachedFlightsLoaded = true;
        return flightList;
    } catch (Exception ex) {
        handleGenericException(ex, methodName);
        return Collections.emptyList();
    }
} // end method

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
        Club club = appContext.getClub();
        LOG.debug("for club = {}", club);

        if (club.getClubWebsite() == null || club.getClubWebsite().trim().isEmpty()) {
            String msg = "Website must be completed";
            LOG.warn(msg);
            showMessageFatal(msg);
            return null;
        }

        String url = club.getClubWebsite().trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        // Security: validate URL to prevent open redirect
        java.net.URI uri = new java.net.URI(url);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
            String msg = "Invalid website URL";
            LOG.warn(msg);
            showMessageFatal(msg);
            return null;
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            String msg = "Invalid website URL";
            LOG.warn(msg);
            showMessageFatal(msg);
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

    public void updateCoordinates() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Club club = getClub();
            LOG.debug("club={}", club);
            coordinatesService.updateCoordinates(club);
            showMessageInfo("Coordinates updated");
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method
    
    public void convertYtoM() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    LOG.debug("with hole = {}", hole);
    LOG.debug("with hole via délégation = {}", getHole());
    try {
        Short yards = hole.getHoleDistance();
        LOG.debug("yards = {}", yards);

        if (yards == null || yards == 0) {
            String msg = "Distance must be completed";
            LOG.warn(msg);
            showMessageFatal(msg);
            return;
        }

        // Conversion yards → mètres (1 yard = 0.9144 m)
        Short metres = (short) Math.round(yards * 0.9144);
        hole.setHoleDistance(metres);

        LOG.debug("converted {} yards → {} metres", yards, metres);

    } catch (Exception e) {
        handleGenericException(e, methodName);
    }
} // end method

    // ========================================
    // Load from ECourseList (DataTable row)
    // ========================================

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
                String msg = "error : club not retrieved";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

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
                String msg = "Idclub forced because it was null";
                LOG.error(msg);
                showMessageFatal(msg);
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
    // ========================================

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

    // Called from selectClubDelete/selectClubDialog — routes via SelectionPurpose (PAYMENT_COTISATION → cotisation flow).
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
        appContext.setClub(new Club());
        courseListForClub = Collections.emptyList();
        resetCourse();
        LOG.debug("Club reset");
    } // end method

    public void resetCourse() {
        appContext.setCourse(new Course());
        teeListForCourse = Collections.emptyList();
        resetTee();
        LOG.debug("Course reset");
    } // end method

    public void resetTee() {
        tee = new Tee();
        holeListForTee = Collections.emptyList();
        resetHole();
        LOG.debug("Tee reset");
    } // end method

    public void resetHole() {
        hole = new Hole();
        LOG.debug("Hole reset");
    } // end method

    public void resetAll() {
        resetClub();
        country     = new Country();
        holesGlobal = new HolesGlobal();
        filteredClubs = Collections.emptyList();
        LOG.debug("Complete club context reset");
    } // end method

    // ========================================
    // Utilitaires
    // ========================================

    private void setNextStep(boolean value) {
        LOG.debug("NextStep set to: {}", value);
    }


    // ========================================
    // Getters / Setters
    // ========================================

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

    public boolean isStrokeIndex() { return strokeIndex; }
    public void setStrokeIndex(boolean strokeIndex) { this.strokeIndex = strokeIndex; }

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

    public String viewHolesGlobal() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        tee.setTeeHolesPlayed("01-18");
        return "modify_hole.xhtml?faces-redirect=true&cmd=" + appContext.getInputSelectCourse();
    } // end method

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
                String msg = "Error: tee not retrieved";
                LOG.error(msg);
                showMessageFatal(msg);
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

    public String modifyGroundCondition(String type) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} type={}", methodName, type);
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

    public String selectTravel(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            // ecl is null when called from include_course_selector (club/course already in appContext)
            if (ecl != null) {
                appContext.setClub(ecl.club());
                appContext.setCourse(ecl.course());
            }

            String msg = "Select Travel Successful — Club: " + appContext.getClub().getClubName()
                    + " / Course: " + appContext.getCourse().getCourseName();
            LOG.info(msg);
            showMessageInfo(msg);
            LOG.debug("inputSelectCourse = {}", appContext.getInputSelectCourse());
            return "maps_home_club.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String selectChart(ECourseList ecl) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            // ecl is null when called from include_course_selector (club/course already in appContext)
            if (ecl != null) {
                appContext.setClub(ecl.club());
                appContext.setCourse(ecl.course());
            }
            String msg = "Select Course Successful — Club: " + appContext.getClub().getClubName()
                    + " / Course: " + appContext.getCourse().getCourseName();
            LOG.info(msg);
            showMessageInfo(msg);
            LOG.debug("inputSelectCourse = {}", appContext.getInputSelectCourse());
            String v = chartController.lineModelCourse(appContext.getPlayer(), appContext.getCourse());
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

    public String to_course_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} string={}", methodName, s);
        navigationController.reset(s);
        appContext.getCourse().setCreateModify(true);
        return "course.xhtml?faces-redirect=true&operation=" + s;
    } // end method

    public String to_tee_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} string={}", methodName, s);
        navigationController.reset(s);
        tee.setCreateModify(true);
        return "tee.xhtml?faces-redirect=true&operation=" + s;
    } // end method

    public List<Course> listCoursesForClub(String clubid) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} clubid={}", methodName, clubid);
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
            cachedCoursesForClub = courseListForClubService.list(club);
            return cachedCoursesForClub;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // Resets club to blank on initial page load (non-postback) so the form starts empty.
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

    public String getLineModelCourse() { return lineModelCourse; }
    public void setLineModelCourse(String lineModelCourse) { this.lineModelCourse = lineModelCourse; }

    public TarifMember getTarifMember() { return tarifMember; }
    public void setTarifMember(TarifMember tarifMember) { this.tarifMember = tarifMember; }

    public String to_clubModify_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} string={}", methodName, s);
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

    // "ini" resets all context — called when user starts a new club creation flow.
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

    // Called from unavailable_menu.xhtml — sets menuLaunched=true and loads existing structure from DB.
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
        LOG.debug("entering {} string={}", methodName, s);
        navigationController.reset("Reset to_selectClub2 " + s);
        appContext.setInputSelectCourse(s);
        appContext.setAdminType("admin");
        LOG.debug("club selected for :  = {}", appContext.getInputSelectCourse());
        return "selectClubCourse.xhtml?faces-redirect=true";
    } // end method

    public String to_update_help(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} string={}", methodName, s);
        navigationController.reset("Reset to_update_help " + s);
        appContext.setInputSelectCourse(s);
        return "editor_help.xhtml?faces-redirect=true";
    } // end method

    public String to_selectClub_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} string={}", methodName, s);
        navigationController.reset("Reset to_selectClub" + s);
        appContext.setInputSelectCourse(s);
        appContext.setInputSelectClub(s);
        return "selectClub.xhtml?faces-redirect=true&cmd=" + appContext.getInputSelectCourse();
    } // end method

    public String to_selectClubDialog_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} string={}", methodName, s);
        navigationController.reset("Reset to_selectClubDialog" + s);
        appContext.setInputSelectClub(s);
        return "selectClubDialog.xhtml?faces-redirect=true&cmd=" + appContext.getInputSelectClub();
    } // end method

    public void to_reset_menu(String ini) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        navigationController.reset(ini);
        String msg = "Reset PLAYER done = " + ini;
        LOG.info(msg);
        showMessageInfo(msg);
    } // end method

    // ========================================
    // List methods
    // ========================================

    public void findCourseListForClub() throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        courseListForClub = courseListForClubService.list(appContext.getClub());
    } // end method

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
                String msg = "FATAL error : professional NOT created — " + appContext.getProfessional();
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
                LOG.info("professional updated id={}", selectedProfessional.getProId());
                showMessageInfo("Professional updated: " + selectedProfessional.getProId());
            } else {
                String msg = "Error updating Professional ProId=" + selectedProfessional.getProId();
                LOG.error(msg);
                showMessageFatal(msg);
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
                LOG.info("proAmount updated amount={}", selectedProfessional.getProAmount());
                showMessageInfo("ProAmount updated: " + selectedProfessional.getProAmount());
            } else {
                String msg = "Error updating ProAmount for ProId=" + selectedProfessional.getProId();
                LOG.error(msg);
                showMessageFatal(msg);
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method

    public entite.Professional getSelectedProfessional() { return selectedProfessional; }
    public void setSelectedProfessional(entite.Professional p) { this.selectedProfessional = p; }

    public String navigateToTarifPro(entite.Professional professional) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        appContext.setProfessional(professional);
        return enumeration.SelectionPurpose.TARIF_PRO.navigationToFinal();
    } // end method

    public List<ECourseList> listCoursesPublic() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            SelectionPurpose purpose = clubSelectionContext.getPurpose();
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
            return clubDetailList.list(c);
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // Selection methods
    // ========================================

    public String selectedClub(Club c) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (c == null) {
            String msg = "No club selected";
            LOG.warn(msg);
            showMessageFatal(msg);
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
                    tarifMember = findTarifMembersData.find(appContext.getClub(), appContext.getRound());
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
            LOG.debug("course selected name={}", appContext.getCourse().getCourseName());
            String msgCourse = "Select Course Successful — " + appContext.getCourse().getCourseName();
            LOG.info(msgCourse);
            showMessageInfo(msgCourse);
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

            LOG.debug("club selected name={} select={}", appContext.getClub().getClubName(), select);

            SelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose()).orElse(SelectionPurpose.CREATE_PLAYER);
            LOG.debug("with purpose = {}", purpose);
            
            if (purpose == SelectionPurpose.LOCAL_ADMIN) {
                   LOG.debug(": inputSelectCourse = {}", appContext.getInputSelectCourse());
                   
              }
            if (appContext.getInputSelectCourse() == null) {
                String msg = "No InputSelectCourse set";
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
                LOG.debug("purpose is CREATE_TARIF_GREENFEE — return to tarif_greenfee_wizard.xhtml");
                return purpose.navigationToFinal();
            }

            if (purpose == SelectionPurpose.CREATE_TARIF_MEMBER) {
                LOG.debug("purpose is CREATE_TARIF_MEMBER — return to tarif_member_wizard.xhtml");
                return purpose.navigationToFinal();
            }

            if (select.equals("PaymentCotisationSpontaneous")) {
                LOG.debug("entering PaymentCotisationSpontaneous");
                LOG.debug("club = {}", appContext.getClub());
                LOG.debug("round = {}", appContext.getRound());
                
                tarifMember = findTarifMembersData.find(appContext.getClub(), appContext.getRound());
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

} // end class