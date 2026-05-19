package Controllers;

import context.ApplicationContext;
import entite.Club;
import entite.Course;
import entite.composite.ECourseList;
import entite.composite.EUnavailable;
import static exceptions.LCException.handleGenericException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static interfaces.Log.LOG;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@Named("unavailableClubC")
@SessionScoped
public class UnavailableClubController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private ApplicationContext appContext;
    @Inject private Controllers.UnavailableController unavailableController;
    @Inject private create.CreateUnavailablePeriod createUnavailablePeriodService;
    @Inject private update.UpdateUnavailablePeriod updateUnavailablePeriodService;
    @Inject private lists.UnavailableListForDate unavailableListForDate;
    @Inject private find.FindUnavailablePeriodOverlapping findUnavailablePeriodOverlapping;
    @Inject private read.ReadUnavailableStructure readUnavailableStructure;
    @Inject private lists.ClubsListLocalAdmin clubsListLocalAdmin;
    @Inject private lists.CourseListForClub courseListForClubService;

    private EUnavailable unavailableDB = null;
    private List<Course> courseListForClub = Collections.emptyList();

    // Delegate — unavailable state lives in appContext (shared with ClubController)
    public EUnavailable getUnavailable() {
        return appContext.getUnavailable();
    } // end method

    public void setUnavailable(EUnavailable unavailable) {
        appContext.setUnavailable(unavailable);
    } // end method

    public List<Course> getCourseListForClub() { return courseListForClub; }
    public void setCourseListForClub(List<Course> l) { this.courseListForClub = l; }

    public List<String> getUnavailabilityTypeKeys() {
        return List.of(
                "unavailable.type.maintenance",
                "unavailable.type.impraticable",
                "unavailable.type.competition");
    } // end method

    public String getClubTimeZone() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Club club = appContext.getClub();
        if (club != null && club.getAddress() != null && club.getAddress().getZoneId() != null) {
            return club.getAddress().getZoneId();
        }
        LOG.warn("clubZoneId not found, falling back to UTC");
        return "UTC";
    } // end method

    // ========================================
    // Unavailable — action methods
    // ========================================

    public String createUnavailablePeriod() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            EUnavailable unavailable = appContext.getUnavailable();
            if (appContext.getClub() == null || appContext.getClub().getIdclub() == null) {
                String msg = LCUtil.prepareMessageBean("message.selectclub");
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            if (unavailable.period().getStartDate() == null || unavailable.period().getEndDate() == null) {
                String msg = LCUtil.prepareMessageBean("tarif.member.period.dates.required");
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            unavailable.period().setIdclub(appContext.getClub().getIdclub());
            unavailable.structure().setPeriodSaved(true);
            unavailable.structure().setMenuLaunched(true);
            LOG.debug("period saved to bean: start={}, end={}", unavailable.period().getStartDate(), unavailable.period().getEndDate());
            String msgInfo = unavailable.period().getStartDate() + " → " + unavailable.period().getEndDate();
            LOG.info(msgInfo);
            showMessageInfo(msgInfo);
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
                String msg = LCUtil.prepareMessageBean("unavailable.type.required");
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            LOG.debug("availability saved to bean: type={}, label={}",
                    unavailable.period().getUnavailabilityType(),
                    unavailable.period().getUnavailabilityLabel());
            String msgType = unavailable.period().getUnavailabilityType()
                    + (unavailable.period().getUnavailabilityLabel() != null
                       ? " — " + unavailable.period().getUnavailabilityLabel() : "");
            LOG.info(msgType);
            showMessageInfo(msgType);
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
                String msg = LCUtil.prepareMessageBean("message.selectclub");
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            EUnavailable unavailable = appContext.getUnavailable();
            Club club = appContext.getClub();

            // 1. Period (insert si pas encore persisté en DB)
            if (unavailable.period().getStartDate() == null || unavailable.period().getEndDate() == null) {
                String msg = LCUtil.prepareMessageBean("tarif.member.period.dates.required");
                LOG.warn(msg);
                showMessageFatal(msg);
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
                    String msg = LCUtil.prepareMessageBean("unavailable.availability.notsaved");
                    LOG.error(msg);
                    showMessageFatal(msg);
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

            // 3. Structure → club.GroundCondition
            if (unavailableController.updateClub(unavailable, club)) {
                unavailable.structure().setStructureExists(true);
                String msg = LCUtil.prepareMessageBean("unavailable.availability.saved");
                LOG.info(msg);
                showMessageInfo(msg);
            } else {
                String msg = LCUtil.prepareMessageBean("unavailable.availability.notsaved");
                LOG.error(msg);
                showMessageFatal(msg);
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
            LOG.info("unavailable structure={}", unavailable.structure().getStructureList());
            showMessageInfo(LCUtil.prepareMessageBean("unavailable.structure.show")
                    + "<br/> Unavailable Structure = " + unavailable.structure().getStructureList().toString());
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
                LOG.debug("unavailable period found={}", lun);
                showMessageInfo("Unavailable period: " + lun.toString());
                return "ground_condition_show.xhtml?faces-redirect=true";
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
                return "ground_condition_show.xhtml?faces-redirect=true";
            } else {
                appContext.setUnavailable(lun);
                LOG.debug("showUnavailablePeriods - element is = {}", lun);
                return "ground_condition_show.xhtml?faces-redirect=true";
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
            return "ground_condition_show.xhtml?faces-redirect=true";
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
                    String msg = (unavailable.period().getStartDate() == null || unavailable.period().getEndDate() == null)
                            ? LCUtil.prepareMessageBean("tarif.member.period.dates.required")
                            : LCUtil.prepareMessageBean("unavailable.period.required");
                    LOG.warn(msg);
                    showMessageFatal(msg);
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
                    String msg = LCUtil.prepareMessageBean("unavailable.type.required");
                    LOG.warn(msg);
                    showMessageFatal(msg);
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
                    String msg = LCUtil.prepareMessageBean("unavailable.structure.notfound");
                    LOG.warn(msg);
                    showMessageFatal(msg);
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
            courseListForClub = courseListForClubService.list(appContext.getClub());
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
