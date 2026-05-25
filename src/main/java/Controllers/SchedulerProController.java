package Controllers;

import static interfaces.GolfInterface.ZDF_TIME_HHmm;

import context.ApplicationContext;
import entite.Club;
import entite.Player;
import entite.Lesson;
import entite.composite.ECourseList;
import enumeration.WorkingDay;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.schedule.ScheduleEntryMoveEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import static interfaces.Log.LOG;
import manager.PlayerManager;
import static exceptions.LCException.handleGenericException;
import static interfaces.ScheduleColors.GREEN_BG;
import static interfaces.ScheduleColors.GREEN_BORDER;
import static interfaces.ScheduleColors.RED_BG;
import static interfaces.ScheduleColors.RED_BORDER;
import static utils.LCUtil.showDialogFatal;
import static utils.LCUtil.showDialogInfo;
import static utils.LCUtil.showMessageInfo;

@Named("schedulerC")
@ViewScoped
public class SchedulerProController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private ApplicationContext appContext;
    @Inject private PlayerManager playerManager;
    @Inject private read.ReadLesson readLessons;
    @Inject private cache.CacheInvalidator cacheInvalidator;
    @Inject private DialogController dialogC;
    @Inject private Controllers.PaymentController payC;
    @Inject private update.UpdateLesson updateLessonService;
    @Inject private calc.CalcLessonPrice calcLessonPrice;

    private Club club;
    private entite.Professional professional;
    private List<Lesson> listLessons = new ArrayList<>();
    private ScheduleModel scheduleModel = new org.primefaces.model.DefaultScheduleModel();
    private ScheduleEvent<Object> selectedEvent = new DefaultScheduleEvent<>();
    private String selectedClubName    = "";
    private String selectedProName     = "";
    private String selectedStudentName = "";
    private Double selectedLessonAmount = null;
    private int idCurrentPlayer;
    // guard preRenderView — avoids reload on AJAX postback
    private boolean scheduleLoaded = false;

    public SchedulerProController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end method

    /**
     * Appelé par preRenderView dans schedule_pro.xhtml uniquement.
     * onPostback=false avec le guard scheduleLoaded évite tout rechargement AJAX.
     */
    public void onLoad() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (scheduleLoaded) return;
        try {
            professional = appContext.getProfessional();
            club = appContext.getClub();
            if (appContext.getPlayer() != null && appContext.getPlayer().getIdplayer() != null) {
                idCurrentPlayer = appContext.getPlayer().getIdplayer();
                LOG.debug("idCurrentPlayer set from appContext = {}", idCurrentPlayer);
            }
            if (professional != null) {
                scheduleModel = readLessons.read(professional, club);
                LOG.debug("schedule loaded, events = {}", scheduleModel.getEventCount());
            } else {
                LOG.warn("professional is null in appContext, schedule not loaded");
            }
            scheduleLoaded = true;
        } catch (Exception e) {
            LOG.error("failed to load schedule: {}", e.getMessage(), e);
            // ne pas throw depuis un listener preRenderView
        }
    } // end method

    public String readLessons(ECourseList ecp, int idplayer) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            appContext.getPlayer().setIdplayer(idplayer);
            playerManager.readPlayer(appContext.getPlayer().getIdplayer());
            LOG.debug("Player read: {}", appContext.getPlayer());
            idCurrentPlayer = idplayer;
            // Pro, club et professional — store in appContext (survives @ViewScoped redirect)
            appContext.setPlayerPro(ecp.player());
            appContext.setClub(ecp.club());
            appContext.setProfessional(ecp.professional());
            club = ecp.club();
            professional = ecp.professional();
            // données fraîches pour le pro sélectionné
            cacheInvalidator.invalidateProfessionalCaches();
            return "schedule_pro.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public void dateSelect(SelectEvent<LocalDateTime> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            selectedEvent = DefaultScheduleEvent.<Object>builder()
                .startDate(event.getObject())
                .endDate(event.getObject().plusMinutes(30))
                .description("")
                .backgroundColor(RED_BG)
                .borderColor(RED_BORDER)
                .textColor("white")
                .dynamicProperty("lesson-object", null) // pending event — no DB lesson yet
                .build();
            selectedClubName    = club != null && club.getClubName() != null ? club.getClubName() : "";
            selectedProName     = buildPlayerName(appContext.getPlayerPro());
            selectedStudentName = buildPlayerName(appContext.getPlayer());
            selectedLessonAmount = calcLessonPrice.calc(professional, event.getObject(), club);
            LOG.debug("dateSelect: club={} pro={} student={} amount={}", selectedClubName, selectedProName,
                    selectedStudentName, selectedLessonAmount);
            dialogC.showLessonDialog();
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void viewChange(SelectEvent<LocalDateTime> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end method

    @SuppressWarnings("unchecked")
    public String moveLesson(ScheduleEntryMoveEvent ev) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            selectedEvent = (ScheduleEvent<Object>) ev.getScheduleEvent();
            final long minutes = ev.getDeltaAsDuration().toMinutes();
            if (!eventUserMgt(idCurrentPlayer)) return null;
            LocalDateTime ldt = selectedEvent.getStartDate().minusMinutes(minutes);
            if (modifyEvent(ldt)) {
                LOG.debug("Event modified");
            } else {
                LOG.debug("Event NOT modified");
            }
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public void onEventSelect(SelectEvent<ScheduleEvent<Object>> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        selectedEvent = event.getObject();
        Lesson lesson = (Lesson) selectedEvent.getDynamicProperties().get("lesson-object");
        if (lesson == null) {
            // new event created by dateSelect — not yet in DB
            LOG.debug("lesson-object is null — new pending event");
            dialogC.showLessonDialog();
            return;
        }
        // Authorization check — only the student or the pro can open the dialog
        Integer currentPlayerId = appContext.getPlayer() != null ? appContext.getPlayer().getIdplayer() : null;
        Integer studentId   = lesson.getEventPlayerId();
        Integer proPlayerId = professional != null ? professional.getProPlayerId() : null;
        boolean isStudent = currentPlayerId != null && currentPlayerId.equals(studentId);
        boolean isPro     = currentPlayerId != null && currentPlayerId.equals(proPlayerId);
        if (!isStudent && !isPro) {
            LOG.debug("player {} is neither student {} nor pro {} — blocking dialog", currentPlayerId, studentId, proPlayerId);
            String msg = utils.LCUtil.prepareMessageBean("lesson.booked.by.other");
            LOG.warn(msg);
            utils.LCUtil.showMessageFatal(msg);
            return;
        }
        selectedClubName    = lesson.getEventClubName() != null ? lesson.getEventClubName() : "";
        selectedProName     = lesson.getProName()       != null ? lesson.getProName()        : "";
        selectedStudentName = lesson.getStudentName()   != null ? lesson.getStudentName()    : "";
        selectedLessonAmount = lesson.getLessonAmount() != null
                ? lesson.getLessonAmount()
                : calcLessonPrice.calc(professional, lesson.getEventStartDate(), club);
        LOG.debug("club={} pro={} student={} amount={} paid={}", selectedClubName, selectedProName, selectedStudentName,
                selectedLessonAmount, lesson.isLessonPaid());
        dialogC.showLessonDialog();
    } // end method

    public void onViewChange(SelectEvent<String> selectEvent) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String view = selectEvent.getObject();
        String msg = "View: " + view;
        LOG.info(msg);
        showMessageInfo("View Changed", msg);
    } // end method

    public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        var event = DefaultScheduleEvent.builder()
                .startDate(selectEvent.getObject())
                .endDate(selectEvent.getObject().plusHours(1))
                .build();
        String msg = event.toString();
        LOG.info(msg);
        showMessageInfo("Date Selected", msg);
    } // end method

    public boolean modifyEvent(LocalDateTime ldt) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (professional == null) {
                professional = appContext.getProfessional();
            }
            if (!eventValidations()) return false;
            Lesson evBefore = new Lesson();
            evBefore.setEventStartDate(ldt);
            evBefore.setEventProId(professional.getProId());
            Lesson evAfter = new Lesson();
            evAfter.setEventStartDate(selectedEvent.getStartDate());
            evAfter.setEventEndDate(selectedEvent.getStartDate().plusMinutes(30));
            evAfter.setEventTitle(selectedEvent.getTitle());
            updateLessonService.update(evBefore, evAfter);
            payC.updatePendingLesson(evBefore, evAfter);
            scheduleModel.updateEvent(selectedEvent);
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public boolean eventValidations() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (selectedEvent.getStartDate().toLocalTime().isBefore(LocalTime.parse("09:00:00"))) {
                showDialogFatal("Too early", selectedEvent.getStartDate().format(ZDF_TIME_HHmm));
                return false;
            }
            if (selectedEvent.getStartDate().isAfter(selectedEvent.getEndDate())) {
                showDialogFatal("Start Date after End Date",
                        selectedEvent.getStartDate().format(ZDF_TIME_HHmm)
                                + " / " + selectedEvent.getEndDate().format(ZDF_TIME_HHmm));
                return false;
            }
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public boolean eventUserMgt(int idplayer) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Lesson lesson = (Lesson) selectedEvent.getDynamicProperties().get("lesson-object");
            if (lesson == null) {
                // Pending event (not yet in DB) — belongs to the current player by definition
                return true;
            }
            int idStudent = lesson.getEventPlayerId();
            if (idplayer == idStudent || idplayer == professional.getProPlayerId()) {
                showDialogInfo("Modification authorized", "Student/Pro verified");
                return true;
            } else {
                showDialogFatal("You cannot delete/move other's lesson", "Original student = " + idStudent);
                return false;
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /**
     * Create a lesson from the schedule event selected by the user.
     */
    public String createLesson() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (!eventValidations()) return null;
            // Restore from appContext if lost after @ViewScoped redirect
            if (professional == null) {
                professional = appContext.getProfessional();
            }
            // A pro cannot book a lesson with themselves
            if (professional.getProPlayerId() != null
                    && professional.getProPlayerId().equals(appContext.getPlayer().getIdplayer())) {
                showDialogFatal(utils.LCUtil.prepareMessageBean("lesson.self.booking.error"),
                        "Player #" + appContext.getPlayer().getIdplayer());
                return null;
            }
            Lesson lesson = new Lesson();
            lesson.setEventStartDate(selectedEvent.getStartDate());
            lesson.setEventEndDate(selectedEvent.getEndDate());
            lesson.setEventTitle(selectedEvent.getTitle());
            lesson.setEventProId(professional.getProId());
            lesson.setLessonAmount(calcLessonPrice.calc(professional, selectedEvent.getStartDate(), club));
            lesson.setProName(buildPlayerName(appContext.getPlayerPro()));
            lesson.setStudentName(buildPlayerName(appContext.getPlayer()));
            lesson.setEventClubName(club != null ? club.getClubName() : "");
            payC.addLesson(lesson);
            selectedEvent = org.primefaces.model.DefaultScheduleEvent.<Object>builder()
                    .title(lesson.getEventTitle())
                    .startDate(lesson.getEventStartDate())
                    .endDate(lesson.getEventEndDate())
                    .description(selectedEvent.getDescription() != null ? selectedEvent.getDescription() : "")
                    .dynamicProperty("lesson-booked", true)
                    .dynamicProperty("lesson-paid",   false)
                    .dynamicProperty("lesson-object", lesson)
                    .textColor("white")
                    .backgroundColor(GREEN_BG)
                    .borderColor(GREEN_BORDER)
                    .overlapAllowed(false)
                    .resizable(false)
                    .build();
            scheduleModel.addEvent(selectedEvent);
            LOG.info("lesson added to scheduleModel: {}", selectedEvent);
            String date = lesson.getEventStartDate() != null
                    ? lesson.getEventStartDate().format(ZDF_TIME_HHmm)
                    : "?";
            String msg = "Lesson booked: " + lesson.getStudentName()
                    + " | Pro: " + lesson.getProName()
                    + " | " + date
                    + " | " + lesson.getEventClubName();
            LOG.info(msg);
            showMessageInfo(msg);
            dialogC.hideLessonDialog();
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Delete a lesson from the schedule. Only the student or the pro can delete.
     */
    public String deleteLesson(int idplayer) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("player={}", idplayer);
        try {
            // Restore from appContext if lost after @ViewScoped redirect
            if (professional == null) {
                professional = appContext.getProfessional();
            }
            if (!eventUserMgt(idplayer)) return null;
            Lesson lesson = new Lesson();
            lesson.setEventStartDate(selectedEvent.getStartDate());
            lesson.setEventProId(professional.getProId());
            payC.removeLesson(lesson);
            scheduleModel.deleteEvent(selectedEvent);
            String msg = "Lesson removed from cart: " + selectedEvent.getTitle();
            LOG.info(msg);
            showMessageInfo(msg);
            dialogC.hideLessonDialog();
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // --- Getters & Setters ---
    public Player getPlayer() { return appContext.getPlayer(); }
    public void setPlayer(Player p) { appContext.setPlayer(p); }

    public Player getPlayerPro() { return appContext.getPlayerPro(); }
    public void setPlayerPro(Player p) { appContext.setPlayerPro(p); }

    public entite.Professional getProfessional() { return professional; }
    public void setProfessional(entite.Professional p) { professional = p; }

    public ScheduleEvent<Object> getSelectedEvent() { return selectedEvent; }
    public void setSelectedEvent(ScheduleEvent<Object> selectedEvent) { this.selectedEvent = selectedEvent; }

    public Double getSelectedLessonAmount() { return selectedLessonAmount; }
    public void   setSelectedLessonAmount(Double v) { selectedLessonAmount = v; }

    public ScheduleModel getScheduleModel() { return scheduleModel; }
    public void setScheduleModel(ScheduleModel m) { scheduleModel = m; }

    public Club getClub() { return club; }
    public void setClub(Club c) { club = c; }

    public List<Lesson> getListLessons() { return listLessons; }
    public void setListLessons(List<Lesson> l) { listLessons = l; }

    public boolean isNotProfessional() {
        return professional == null || professional.getProId() == null;
    } // end method

    private String buildPlayerName(entite.Player p) {
        if (p == null) return "";
        String first = p.getPlayerFirstName() != null ? p.getPlayerFirstName() : "";
        String last  = p.getPlayerLastName()  != null ? p.getPlayerLastName()  : "";
        return (first + " " + last).trim();
    } // end method

    public String getSelectedClubName()    { return selectedClubName; }
    public String getSelectedProName()     { return selectedProName; }
    public String getSelectedStudentName() { return selectedStudentName; }

    /*
     * Returns a JSON array of FullCalendar day indices (0=Sun..6=Sat) for days the pro does NOT work.
     * Used in initSchedule() extender: this.cfg.options.hiddenDays = #{schedulerC.hiddenDaysJson};
     */
    public String getHiddenDaysJson() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (professional == null) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (WorkingDay wd : WorkingDay.values()) {
            LOG.debug(enumeration.WorkingDay.printWorkingDays(wd.mask()));
            LOG.debug(enumeration.WorkingDay.printWorkingDaysLine(wd.mask()));
            if (!professional.isWorkingOn(wd.dayOfWeek())) {
                sb.append(wd.fullCalendarIndex()).append(",");
            }
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");
        LOG.debug("hiddenDaysJson = {}", sb);
        return sb.toString();
    } // end method

} // end class
