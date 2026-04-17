package Controllers;

import Controller.refact.PlayerController;
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

import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import manager.PlayerManager;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.NEW_LINE;
import jakarta.faces.application.FacesMessage;
import static utils.LCUtil.showDialogFatal;
import static utils.LCUtil.showDialogInfo;
import static utils.LCUtil.showMessageInfo;

@Named("schedulerC")
@ViewScoped
public class SchedulerProController implements Serializable {
// ✅ Injection du contexte de session
    @Inject
    private ApplicationContext appContext;
    private static final long serialVersionUID = 1L;
    @Inject
    private PlayerManager playerManager;
    @Inject private read.ReadLesson readLessons;           // migrated 2026-02-23
    @Inject private cache.CacheInvalidator cacheInvalidator; // 2026-03-29
    @Inject private DialogController dialogC;
    @Inject private Controller.refact.PaymentController payC; // panier leçons — 2026-03-29
    @Inject private update.UpdateLesson updateLessonService;
  
    // @Inject  enlevé 14-02-2026
    private Club club;

  //  @Inject
  //  private PlayerController playerC; // Utilisation du controller pour accéder à player

    private entite.Professional professional;
    private List<Lesson> listLessons = new ArrayList<>();
    private ScheduleModel scheduleModel = new org.primefaces.model.DefaultScheduleModel();

 //   private ScheduleEvent<Object> scheduleEvent = new DefaultScheduleEvent<>();
    //private ScheduleEvent<Object> scheduleEvent;
    private ScheduleEvent<Object> selectedEvent = new DefaultScheduleEvent<>();;
    private String selectedClubName    = "";
    private String selectedProName     = "";
    private String selectedStudentName = "";
    private int idCurrentPlayer;
    private boolean scheduleLoaded = false; // guard preRenderView — 2026-03-29

    public SchedulerProController() { }

    @PostConstruct
    public void init() {
        LOG.debug("SchedulerProController init");
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
                scheduleModel = readLessons.read(professional);
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

    // Méthode principale pour lire les leçons depuis ECourseList2
    public String readLessons(ECourseList ecp, int idplayer) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {

            appContext.getPlayer().setIdplayer(idplayer);
         //   playerC.setPlayer(new read.ReadPlayer().read(playerC.getPlayer(), conn));
         // autre modification 
          //  appContext.readPlayer(appContext.getPlayer().getIdplayer());
             playerManager.readPlayer(appContext.getPlayer().getIdplayer());
            
            LOG.debug("Player read: {}", appContext.getPlayer());

            idCurrentPlayer = idplayer;

            // Pro, club et professional — store in appContext (survives @ViewScoped redirect)
            appContext.setPlayerPro(ecp.player());
            appContext.setClub(ecp.club());
            appContext.setProfessional(ecp.professional());
            club = ecp.club();
            professional = ecp.professional();

            cacheInvalidator.invalidateProfessionalCaches(); // données fraîches pour le pro sélectionné — 2026-03-29
            return "schedule_pro.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }

    public void dateSelect(SelectEvent<LocalDateTime> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            selectedEvent = DefaultScheduleEvent.<Object>builder()
                    .startDate(event.getObject())
                    .endDate(event.getObject().plusMinutes(30))
                    .description("")
                    .backgroundColor("#dc3545")
                    .borderColor("#bd2130")
                    .textColor("white")
                    .dynamicProperty("lesson-object", null) // pending event — no DB lesson yet
                    .build();
            // Populate display fields for the dialog
            selectedClubName    = club != null && club.getClubName() != null ? club.getClubName() : "";
            selectedProName     = buildPlayerName(appContext.getPlayerPro());
            selectedStudentName = buildPlayerName(appContext.getPlayer());
            LOG.debug("dateSelect: club={} pro={} student={}", selectedClubName, selectedProName, selectedStudentName);
            dialogC.showLessonDialog();
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    }

    public void viewChange(SelectEvent<LocalDateTime> event) {
        LOG.debug("Entering viewChange: {}", event);
    }
@SuppressWarnings("unchecked")
    public String moveLesson(ScheduleEntryMoveEvent ev) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
         //   scheduleEvent = ev.getScheduleEvent();
            selectedEvent = (ScheduleEvent<Object>) ev.getScheduleEvent();
            final long minutes = ev.getDeltaAsDuration().toMinutes();

            if (!EventUserMgt(idCurrentPlayer)) return null;

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
    }

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
            utils.LCUtil.showMessageFatal(utils.LCUtil.prepareMessageBean("lesson.booked.by.other"));
            return;
        }

        selectedClubName    = lesson.getEventClubName()  != null ? lesson.getEventClubName()  : "";
        selectedProName     = lesson.getProName()         != null ? lesson.getProName()         : "";
        selectedStudentName = lesson.getStudentName()     != null ? lesson.getStudentName()     : "";
        LOG.debug("club={} pro={} student={} paid={}", selectedClubName, selectedProName, selectedStudentName, lesson.isLessonPaid());
        dialogC.showLessonDialog();
    }

    public void onViewChange(SelectEvent<String> selectEvent) {
        String view = selectEvent.getObject();
        showMessageInfo("View Changed", "View:" + view);
      //  FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, ;
     //   addMessage(message);
    }

    public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
        
        var event = DefaultScheduleEvent.builder()
                .startDate(selectEvent.getObject())
                .endDate(selectEvent.getObject().plusHours(1))
                .build();
         showMessageInfo("Date Selected", "View:" + event.toString());
    }
    
    
    
    
    public boolean modifyEvent(LocalDateTime ldt) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (professional == null) {
                professional = appContext.getProfessional();
            }
            if (!EventValidations()) return false;

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
    }

    public boolean EventValidations() {
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
    }

    public boolean EventUserMgt(int idplayer) {
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
    }

    /**
     * Create a lesson from the schedule event selected by the user.
     */
    public String createLesson() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (!EventValidations()) return null;
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
            lesson.setLessonAmount(professional.getProAmount());
            lesson.setProName(buildPlayerName(appContext.getPlayerPro()));
            lesson.setStudentName(buildPlayerName(appContext.getPlayer()));
            lesson.setEventClubName(club != null ? club.getClubName() : "");

            payC.addLesson(lesson);
            scheduleModel.addEvent(selectedEvent);
            String msg = "Lesson added to cart: " + lesson.getEventTitle();
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
        LOG.debug("entering for player={}", idplayer);
        try {
            // Restore from appContext if lost after @ViewScoped redirect
            if (professional == null) {
                professional = appContext.getProfessional();
            }
            if (!EventUserMgt(idplayer)) return null;

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

    // --- Getters & Setters refactorés pour accéder via PlayerController ---
    public Player getPlayer() { return appContext.getPlayer(); }
    public void setPlayer(Player p) { appContext.setPlayer(p); }

    public Player getPlayerPro() { return appContext.getPlayerPro(); }
    public void setPlayerPro(Player p) { appContext.setPlayerPro(p); }

    public entite.Professional getProfessional() { return professional; }
    public void setProfessional(entite.Professional p) { professional = p; }

 //   public ScheduleEvent<Object> getScheduleEvent() { return scheduleEvent; }
 //   public void setScheduleEvent(ScheduleEvent<Object> e) { scheduleEvent = e; }

    public ScheduleEvent<Object> getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(ScheduleEvent<Object> selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    
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

    /**
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
            LOG.debug(enumeration.WorkingDay.printWorkingDays(wd.mask())); // added 08-04-2026 by LC
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
