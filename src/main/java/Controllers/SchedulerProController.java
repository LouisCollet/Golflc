package Controllers;

import Controller.refact.PlayerController;
import context.ApplicationContext;
import entite.Club;
import entite.Player;
import entite.Professional;
import entite.Lesson;
import entite.composite.ECourseList;

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
import static utils.LCUtil.showDialogFatal;
import static utils.LCUtil.showDialogInfo;
import static utils.LCUtil.showMessageFatal;
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
    @Inject private read.ReadLessons readLessons;           // migrated 2026-02-23
    @Inject private update.UpdateLesson updateLesson;       // migrated 2026-02-24
  
    // @Inject  enlevé 14-02-2026
    private Club club;

    @Inject
    private PlayerController playerC; // Utilisation du controller pour accéder à player

    private Professional professional;
    private List<Lesson> listLessons = new ArrayList<>();
    private ScheduleModel scheduleModel;

    private ScheduleEvent<Object> scheduleEvent = new DefaultScheduleEvent<>();
    //private ScheduleEvent<Object> scheduleEvent;

    private int idCurrentPlayer;

    public SchedulerProController() {
        LOG.debug("SchedulerProController constructor");
    }

    // Méthode principale pour lire les leçons depuis ECourseList2
    public String readLessons(ECourseList ecp, int idplayer) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {

            appContext.getPlayer().setIdplayer(idplayer);
         //   playerC.setPlayer(new read.ReadPlayer().read(playerC.getPlayer(), conn));
         // autre modification 
          //  appContext.readPlayer(appContext.getPlayer().getIdplayer());
             playerManager.readPlayer(appContext.getPlayer().getIdplayer());
            
            LOG.debug("Player read: " + appContext.getPlayer());

            idCurrentPlayer = idplayer;

            // Pro, club et professional
            appContext.setPlayerPro(ecp.player());
            
            
            club = ecp.club();
            professional = ecp.professional();

            // scheduleModel = new read.ReadLessons().read(professional, conn);
            scheduleModel = readLessons.read(professional); // migrated 2026-02-23
            LOG.debug("Lessons loaded: " + scheduleModel.getEventCount());

            return "schedule_pro.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }

    public void dateSelect(SelectEvent<LocalDateTime> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            scheduleEvent = DefaultScheduleEvent.<Object>builder()//DefaultScheduleEvent.builder()
                    .startDate(event.getObject())
                    .endDate(event.getObject().plusMinutes(30))
                    .description("not yet paid!")
                    .borderColor("black")
                    .textColor("black")
                    .build();
            LOG.debug("onDateSelect event: " + scheduleEvent);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    }

    public void viewChange(SelectEvent<LocalDateTime> event) {
        LOG.debug("Entering viewChange: " + event);
    }
@SuppressWarnings("unchecked")
    public String moveLesson(ScheduleEntryMoveEvent ev) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
         //   scheduleEvent = ev.getScheduleEvent();
            scheduleEvent = (ScheduleEvent<Object>) ev.getScheduleEvent();
            final long minutes = ev.getDeltaAsDuration().toMinutes();

            if (!EventUserMgt(idCurrentPlayer)) return null;

            LocalDateTime ldt = scheduleEvent.getStartDate().minusMinutes(minutes);
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

    public void eventSelect(SelectEvent<ScheduleEvent<Object>> event) {
        scheduleEvent = event.getObject();
        LOG.debug("eventSelect: " + scheduleEvent);
    }

    public boolean modifyEvent(LocalDateTime ldt) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            if (!EventValidations()) return false;

            Lesson evBefore = new Lesson();
            evBefore.setEventStartDate(ldt);
            evBefore.setEventProId(professional.getProId());

            Lesson evAfter = new Lesson();
            evAfter.setEventStartDate(scheduleEvent.getStartDate());
            evAfter.setEventEndDate(scheduleEvent.getStartDate().plusMinutes(30));
            evAfter.setEventTitle(scheduleEvent.getTitle());

            if (updateLesson.update(evBefore, evAfter)) {
                scheduleModel.updateEvent(scheduleEvent);
                return true;
            } else {
                String msg = "Fatal error Modify Schedule Table!";
                LOG.error(msg);
                showMessageFatal(msg);
                showDialogFatal(msg, evAfter.toString());
                return false;
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    }

    public boolean EventValidations() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            if (scheduleEvent.getStartDate().toLocalTime().isBefore(LocalTime.parse("09:00:00"))) {
                showDialogFatal("Too early", scheduleEvent.getStartDate().format(ZDF_TIME_HHmm));
                return false;
            }
            if (scheduleEvent.getStartDate().isAfter(scheduleEvent.getEndDate())) {
                showDialogFatal("Start Date after End Date",
                        scheduleEvent.getStartDate().format(ZDF_TIME_HHmm)
                                + " / " + scheduleEvent.getEndDate().format(ZDF_TIME_HHmm));
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
        LOG.debug("entering " + methodName);
        try {
            int idStudent = (int) scheduleEvent.getDynamicProperties().get("key-id");

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

    // --- Getters & Setters refactorés pour accéder via PlayerController ---
    public Player getPlayer() { return appContext.getPlayer(); }
    public void setPlayer(Player p) { appContext.setPlayer(p); }

    public Player getPlayerPro() { return appContext.getPlayerPro(); }
    public void setPlayerPro(Player p) { appContext.setPlayerPro(p); }

    public Professional getProfessional() { return professional; }
    public void setProfessional(Professional p) { professional = p; }

    public ScheduleEvent<Object> getScheduleEvent() { return scheduleEvent; }
    public void setScheduleEvent(ScheduleEvent<Object> e) { scheduleEvent = e; }

    public ScheduleModel getScheduleModel() { return scheduleModel; }
    public void setScheduleModel(ScheduleModel m) { scheduleModel = m; }

    public Club getClub() { return club; }
    public void setClub(Club c) { club = c; }

    public List<Lesson> getListLessons() { return listLessons; }
    public void setListLessons(List<Lesson> l) { listLessons = l; }
} // end class
