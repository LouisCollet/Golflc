package Controllers;

import entite.Club;
import entite.composite.EClubPro;
import entite.Player;
import entite.Professional;
import entite.Lesson;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.faces.annotation.ApplicationMap;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.schedule.ScheduleEntryMoveEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.primefaces.model.ScheduleEvent;
import static utils.LCUtil.showDialogFatal;
import static utils.LCUtil.showDialogInfo;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
// https://primefaces.github.io/primefaces/15_0_0/#/components/schedule
@Named("schedulerC")
//@SessionScoped // mod 11-12-2024
@ViewScoped // mod 16-12-2024
public class SchedulerProController implements Serializable{
@Inject private Club club;
private static Professional professional;
@Inject private Player player;
@Inject private Player playerPro;
private static List<Lesson> listLessons = new ArrayList<>();
private static ScheduleModel scheduleModel;
private ScheduleEvent<Object> scheduleEvent = new DefaultScheduleEvent<>();
private Connection conn = null;
private static int idCurrentPlayer;
private ScheduleModel model;

public SchedulerProController(){  // constructor
   LOG.debug("entering constructor");
   
}
//  @Inject @SessionMap
//  private Map<String, Object> sessionMap;
//  @Inject @ApplicationMap
//  private Map<String, Object> applicationMap;

 // @PostConstruct
 //public void init(){ // attention !! ne peut absolument pas avoir : throws SQLException
 //       LOG.debug("entering postConstruct");
 //         LOG.debug("init terminated");
 //   }

  @Inject @ApplicationMap
  private Map<String, Object> applicationMap;
// coming from selectProForClub.xhtml
  public String readLessons(EClubPro ecp, int idplayer) throws Exception {
  try{
           LOG.debug("entering readLessons ");
           LOG.debug("applicationMap Connection is now = " + applicationMap.get("Connection"));
        conn = (Connection) applicationMap.get("Connection");
           LOG.debug("Connection from map = " + conn);
           LOG.debug("with EclubPro = " + ecp);
           LOG.debug("with idplayer = " + idplayer);
        player.setIdplayer(idplayer);
        player = new read.ReadPlayer().read(player, conn);
           LOG.debug("Player is readed = " + player);
        idCurrentPlayer = idplayer;
           LOG.debug("with idCurrentPlayer = " + idCurrentPlayer);
      //     player n'est pas connu à ce moment !!'
           //LOG.debug("ecp GetPlayer = " + ecp.getPlayer()); = le pro
        playerPro = ecp.getPlayer();
           LOG.debug("with player Pro = " + playerPro);
        club = ecp.getClub();
           LOG.debug("club = " + club);
        professional = ecp.getProfessional();
        scheduleModel = new read.ReadLessons().read(professional, conn);
               LOG.debug("lessons found = " + scheduleModel.getEventCount());
    return "schedule_pro.xhtml?faces-redirect=true";
   }catch (Exception ex){
            String msg = "Exception in readLessons " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }  
   }  // end method

 public void dateSelect(SelectEvent<LocalDateTime> event) throws Exception{ // click on cell in schedule_pro.xhtml
        LOG.debug("entering dateSelect");
   //  LOG.debug("entering dateSelect with StartDate = " + event.getObject().toString());
     LOG.debug("entering dateSelect with StartDate formatted = " + event.getObject().format(ZDF_TIME_HHmm));
 try{
	scheduleEvent = DefaultScheduleEvent.builder()
            //    .title("Please complete the title") // new 25-01-2023
                .startDate(event.getObject())
                .endDate(event.getObject().plusMinutes(30))
                .description("not yet paid !")
                .borderColor("black")  // fonctionne pas ??
                .textColor("black")
            //    .dynamicProperty(scheduleEvent.getDynamicProperties().get("key-id"),event.getObject().toString())
                        // LOG.debug(" with key-id = " + scheduleEvent.getDynamicProperties().get("key-id"));
     //           .styleClass("styleClass") // mod 30-01-2023 en créer une autre pour les différencier ??ui-custompanelgrid
                .build();
	LOG.debug("onDateSelect voici l'event : " + scheduleEvent.getEndDate());
    }catch (Exception ex){
            String msg = "Exception in dateSelect " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
         //   return null;
  }         
        
        
        
} // end onScheduleDateSelect
 


 public void viewChange(SelectEvent<LocalDateTime> event) {
          LOG.debug("entering viewChange with = " + event.toString());
    // attention : change la date de fin de l'event !!
    // faut donc une modification 
    // plus simple de ne pas l'autoriser, comme les lessons sont de 30 minutes !!!
  //     String msg = "Event resized Start-Delta:" + event.getDeltaStartAsDuration()
   //     + ", End-Delta: " + event.getDeltaEndAsDuration();
//	LOG.debug("resize -startup delta " + msg);
}
@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public String moveLesson(ScheduleEntryMoveEvent ev) throws SQLException {//schedule_pro.xhtml
 try{ // drag and drop ne fonctionne pas 
       LOG.debug("draggable - entering moveLesson - SQL modify / UPDATE");
    scheduleEvent = ev.getScheduleEvent();
           LOG.debug(" with professional= " + professional.getProPlayerId());
           LOG.debug(" with player= " + player.getIdplayer());
           LOG.debug(" with event target StartDate   = " + scheduleEvent.getStartDate());
           LOG.debug(" with event target EndDate     = " + scheduleEvent.getEndDate());
           LOG.debug(" with event Description = " + scheduleEvent.getDescription());
           LOG.debug("idCurrentPlayer = " + idCurrentPlayer);

    if (! EventUserMgt(idCurrentPlayer)){  // erreur user mgt
        // BUG ! il faudrait faire le restore car il est déjà sur targetDate/draggedDate !!
            return null;
        }
   
  //  final long seconds = ev.getDeltaAsDuration().toSeconds();
    final long minutes = ev.getDeltaAsDuration().toMinutes();
    String  msg = "draggable - entering onEventMove  SQL modify with minutes  " + minutes;
        LOG.debug(msg);
  //  final LocalDateTime ldt = scheduleEvent.getStartDate().minusSeconds(minutes);
     final LocalDateTime ldt = scheduleEvent.getStartDate().minusMinutes(minutes);
     msg = "draggable - event to be modified (old date) = " + ldt;
     showMessageInfo(msg);
     LOG.debug(msg);
       msg = "draggable - event to be modified (new date)  = " + scheduleEvent.getStartDate()+ " / " + scheduleEvent.getEndDate();
      LOG.debug(msg); showMessageInfo(msg);
    if(modifyEvent(ldt)){
          LOG.debug("event modified");
    }else{
          LOG.debug("event NOT modified !!");
    }
       return null;
  }catch (Exception ex){
            String msg = "Exception in moveLesson " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} //end moveLesson
// @SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
@SuppressWarnings({"rawtypes"}) //, "unchecked", "deprecation"})
//@Deprecated // new 29-11-2025 alors eventSelect est barré ==> deprecated !!
 public void eventSelect(SelectEvent<ScheduleEvent<Object>> event) {
    scheduleEvent = event.getObject();
         LOG.debug("entering eventSelect ");
         LOG.debug(" with event getStartDate = " + scheduleEvent.getStartDate());
         LOG.debug(" with event getEndDate = " + scheduleEvent.getEndDate());
         LOG.debug(" with event getDescription = " + scheduleEvent.getDescription()); //PAID - Collet, Louis Jean (324713) 
         LOG.debug(" with event getTitle = " + scheduleEvent.getTitle());
         LOG.debug(" idCurrentPlayer = " + idCurrentPlayer); // will be used in moveLesson
//    eventSelected = scheduleEvent;  // sera utilisé dans modify pour avoir l'ancienne date de début
   //      LOG.debug("eventSelected getStartDate = " + eventSelected.getStartDate());
  }
 
public boolean modifyEvent(LocalDateTime ldt) {
try{ //PENDING
        LOG.debug("entering modifyEvent");
        LOG.debug("with Event" + scheduleEvent);
        LOG.debug("with input param = initial startDate " + ldt);

     if (!EventValidations()){  // erreur validations
            return false;
     }
      
 //   update DB     
      entite.Lesson evBefore = new entite.Lesson();
      evBefore.setEventStartDate(ldt);
      evBefore.setEventProId(professional.getProId());
      entite.Lesson evAfter = new entite.Lesson();
      evAfter.setEventStartDate(scheduleEvent.getStartDate());
      evAfter.setEventEndDate(scheduleEvent.getStartDate().plusMinutes(30));
      evAfter.setEventTitle(scheduleEvent.getTitle());

       if(new update.UpdateLesson().update(evBefore, evAfter, conn)){
              String msg = "Lesson modified in DB = " + evAfter;
              LOG.info(msg);
              showMessageInfo(msg);
              scheduleModel.updateEvent(scheduleEvent);
              // à faire tenir compte eventAfter !
              // envoi un mail au pro !
   //   A FAIRE        boolean b = new mail.ScheduleMail().modifyLesson(professional, lesson, conn);
   //                    LOG.debug("mail sent !" + b);
          //    showDialogInfo("Lesson modified in DB = " , evAfter.toString());
              return true;
       }else{
              String msg = "Fatal error Modify Schedule Table!!";
              LOG.error(msg); showMessageFatal(msg); showDialogFatal(msg, evAfter.toString());
              return false;
       }
 }catch (Exception ex){
            String msg = "Exception in modifyEvent " + ex;
            LOG.error(msg);
            showMessageFatal(msg); 
            return false;
}
 } // end method
public boolean EventValidations() {
  try{
           LOG.debug(" entering EventValidations with professional= " + professional);
           LOG.debug(" with player= " + player);
           LOG.debug(" with ScheduleEvent  = " + scheduleEvent);
           LOG.debug(" with event StartDate   = " + scheduleEvent.getStartDate());
           LOG.debug(" with event EndDate     = " + scheduleEvent.getEndDate());
           LOG.debug(" with event Description = " + scheduleEvent.getDescription());
       if(scheduleEvent.getStartDate().toLocalTime().isBefore(LocalTime.parse("09:00:00"))){
            String msg1 = "Too early : minimum 09:00";
            String msg = msg1 + scheduleEvent.getStartDate().format(ZDF_TIME_HHmm);
            LOG.debug(msg);
            showDialogFatal(msg1,scheduleEvent.getStartDate().format(ZDF_TIME_HHmm));
            return false;
       }
       if(scheduleEvent.getStartDate().isAfter(scheduleEvent.getEndDate())){
            String msg1 = "Start Date after End Date !";
            String msg = msg1 + scheduleEvent.getStartDate().format(ZDF_TIME_HHmm);
            LOG.debug(msg);
            showDialogFatal(msg, scheduleEvent.getStartDate().format(ZDF_TIME_HHmm)
                    + " / " +  scheduleEvent.getEndDate().format(ZDF_TIME_HHmm));
            return false;
       }
     return true;
 }catch (Exception ex){
            String msg = "Exception in EventValidations " + ex;
            LOG.error(msg);
            showMessageFatal(msg); 
            return false;
}
} // end method
 
public boolean EventUserMgt(int idplayer) {
  try{
           LOG.debug(" entering EventUserMgt with professional= " + professional.getProPlayerId());
           LOG.debug(" with current idplayer= " + idplayer);  // current player
     //      LOG.debug(" with current idStudent = " + idStudent);  // current player
      //     LOG.debug(" with professional = " + player);  // c'est le pro !current player
           LOG.debug(" with event ScheduleEvent   = " + scheduleEvent);   
           LOG.debug(" with event StartDate   = " + scheduleEvent.getStartDate());
           LOG.debug(" with event EndDate     = " + scheduleEvent.getEndDate());
           LOG.debug(" with event Description = " + scheduleEvent.getDescription());
           LOG.debug(" with key-id = " + scheduleEvent.getDynamicProperties().get("key-id"));

   // setted in ReadLessons .dynamicProperty("key-id", lesson.getEventPlayerId()) 
        //   LOG.debug(" with dynamic properties = " + scheduleEvent.getDynamicProperties()); // c'est un Map
//           Map<String, Object> map = scheduleEvent.getDynamicProperties();
  //            LOG.debug(" with dynamic property key-id = " + map.get("key-id"));
        //      int idStudent = (int) map.get("key-id");
           int idStudent = (int) scheduleEvent.getDynamicProperties().get("key-id"); // original lesson owner
              LOG.debug("originaly idStudent = " + idStudent);
              LOG.debug("current idplayer = " + idplayer);
  //            LOG.debug("idCurrentPlayer = " + idCurrentPlayer);
              LOG.debug("pro giving lesson = " + professional.getProPlayerId());
              LOG.debug("student qui veut prendre une lesson = " + player);

              
        if(idplayer == idStudent){ // le joueur veut modifier/supprimer sa leçon current player / original player
             String msg = "Your handle your own lesson : modification or deletion authorized !";
             LOG.info(msg);
             showDialogInfo(msg,"idStudent = " + idStudent + " ,idPlayer = " + idplayer);
             showMessageInfo(msg);
             return true;
        }
        if(idplayer == professional.getProPlayerId() ){ 
              // le pro veut supprimer la lesson
            String msg = "You are the pro : modification or deletion authorized !";
            LOG.info(msg);
            showDialogInfo(msg,"idStudent = " + idStudent + " ,pro = " + professional.getProPlayerId());
            showMessageInfo(msg);
            return true;
        }else{
            String msg = "You cannot delete (or Move) an other's lesson !";
            LOG.error(msg);
            showMessageInfo(msg);
            showDialogFatal(msg, "Original student = " + String.valueOf(idStudent)); 
            return false;
        } 
 }catch (Exception ex){
            String msg = "Exception in EventUserMgt " + ex;
            LOG.error(msg);
            showMessageFatal(msg); 
            return false;
}
} // end method

public String deleteLesson(String idplayer) {
 try{
         LOG.debug("entering deleteLesson" );
         LOG.debug("with current idplayer param = " + idplayer );
  //       LOG.debug("with current idplayer from readLessons = " + idCurrentPlayer );
//         LOG.debug("with current idStudent = " + idStudent);
           LOG.debug(" with event = " + scheduleEvent);
        if(scheduleEvent == null) {
            LOG.debug(" with scheduleEvent = null !");
            return null;
        }
        if (! EventUserMgt(Integer.parseInt(idplayer))){
            return null;
        }
             LOG.debug("entering actual delete");// + ev.getTitle());
          ScheduleEvent<?> ev = scheduleModel.getEvent(scheduleEvent.getId());
          String msg = "Titre = " + ev.getTitle()
                     + " Start Date =  "+ ev.getStartDate().format(ZDF_TIME_HHmm);
          //                  + " / " + ev.getDescription() + " / " + + " / " + eventModel.getEventCount();
          LOG.debug(msg);

  //  deleting line table from DB 
        entite.Lesson lesson = new entite.Lesson();
        lesson.setEventStartDate(scheduleEvent.getStartDate());
        lesson.setEventProId(professional.getProId());
        if(new delete.DeleteLesson().delete(lesson, conn)){
            scheduleModel.deleteEvent(ev);
              LOG.debug("eventDeleted ! Count after deletion = " + scheduleModel.getEventCount());
            msg = "Lesson deleted in DB : " + msg;
               LOG.info(msg);
            showMessageInfo(msg); 
            showDialogInfo("Lesson deleted in DB <br/>", msg);
            boolean b = new mail.ScheduleMail().deleteLesson(professional, lesson, conn);
              LOG.debug("mail sent !" + b);
        }else{
            msg = "Fatal error DeleteScheduleEvent !!!" + msg;
             LOG.error(msg);showMessageFatal(msg); showDialogInfo(msg);
        }
   return null;
    }catch (Exception ex){
            String msg = "Exception in DeleteEvent " + ex;
            LOG.error(msg);
            showMessageFatal(msg); 
            return null;
}
} //end deleteLesson

public String createLesson() {
 try{
           LOG.debug("entering createLesson" );
        if (!EventValidations()){  // erreur validations
            return null;
        } 
           LOG.debug("entering createLesson with event   = " + scheduleEvent);
           LOG.debug("entering createLesson with StartDate   = " + scheduleEvent.getStartDate().format(ZDF_TIME_HHmm));
           LOG.debug("entering createLesson with EndDate   = " + scheduleEvent.getEndDate());
           LOG.debug("entering createLesson with Description = " + scheduleEvent.getDescription());
	if(scheduleEvent.isAllDay()) {
		// see https://github.com/primefaces/primefaces/issues/1164
		if (scheduleEvent.getStartDate().toLocalDate().equals(scheduleEvent.getEndDate().toLocalDate())) {
			scheduleEvent.setEndDate(scheduleEvent.getEndDate().plusDays(1));
		}
	}
            LOG.debug("player.getIdplayer() = " + player.getIdplayer());
            LOG.debug("idCurrentPlayer est le même ? " + idCurrentPlayer);
            LOG.debug("professional check = " + professional);
        var listPro = new lists.FindCountListProfessional().list(player, conn); // new 10-02-2023
        
        // pas juste ??
    if(! player.getIdplayer().equals(professional.getProPlayerId())) {   // à vérifier !!
        if(!listPro.isEmpty()){
            String msg = "Le student est lui-même pro : rejected !";
            LOG.info(msg);
            showDialogInfo(msg,"idStudent = " + player.getIdplayer() + " pro = " + professional.getProPlayerId());
            return null;
        }
    }
  	if(scheduleEvent.getId() != null){
              scheduleModel.updateEvent(scheduleEvent);
              String msg = "updateEvent done ! not developped at the moment !!";
              showMessageFatal(msg);
              showDialogInfo(msg);
             LOG.debug("back from modifyEvent");
             return null;
        }
         
    //     if (event.getId() == null) {
    //        eventModel.addEvent(event);
     //   }
     //   else {
     //       eventModel.updateEvent(event);
     //   }
        if(scheduleEvent.isAllDay()){
             LOG.debug("scheduleEvent isAllDay");
             scheduleEvent.setStartDate(scheduleEvent.getStartDate().with(LocalTime.of(9, 0)));
             scheduleEvent.setEndDate(scheduleEvent.getEndDate().with(LocalTime.of(19, 0)));
        }
       scheduleModel.addEvent(scheduleEvent);   // utilisé pour écran
 
  // create lesson     
      Lesson lesson = new Lesson();
      lesson.setEventStartDate(scheduleEvent.getStartDate().with(LocalTime.of(9, 0))); // mod 07-02-2023
      lesson.setEventEndDate(scheduleEvent.getEndDate().with(LocalTime.of(19, 0)));  // mod 07-02-2023
  //       LOG.debug("all-day endDate formatted = " + lesson.getEventEndDate().format(ZDF_TIME_HHmm));
      lesson.setEventTitle(scheduleEvent.getTitle());
      lesson.setEventDescription(scheduleEvent.getDescription());
      lesson.setEventProId(professional.getProId());
      lesson.setEventAllDay(scheduleEvent.isAllDay());
      lesson.setEventPlayerId(player.getIdplayer());
      
   // new 27-01-2023
       listLessons.add(lesson);
          LOG.debug("we add lesson = " + lesson);
   // la création DB a lieu après payement dans courseC !
       listLessons.forEach(item -> LOG.debug("listLessons Start Date : " + item.getEventStartDate()));
     return "schedule_pro.xhtml?faces-redirect=true";
  }catch (Exception ex){
            String msg = "Exception in addEvent " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} //end addEvent
// used in courseC
    public static Professional getProfessional() {
        return professional;
    }

    public static void setProfessional(Professional professional) {
        SchedulerProController.professional = professional;
    }
public boolean isNotProfessional() { // new 08-02-2023
   try {
       // a faire vérifier que le demandeur de lesson n'est pas lui-même professionnel !
       // si oui, il ne peut s'inscrire pour une lesson chez un autre pro
       
       
        if(!player.getIdplayer().equals(professional.getProPlayerId())){
            return true;
        }else{
            LOG.debug("isProfessional !");
            return false;
        }
 } catch (Exception ex) {
       String msg = "Exception in isNotProfessional! " + ex;
       LOG.error(msg);
       showMessageFatal(msg);
       return false;
   }
}
 //   public Lesson getLesson() {
  //      return lesson;
  //  }

  //  public void setLesson(Lesson lesson) {
  //      this.lesson = lesson;
 //   }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayerPro() {
        return playerPro;
    }

    public void setPlayerPro(Player playerPro) {
        this.playerPro = playerPro;
    }

    public static List<Lesson> getListLessons() {
        return listLessons;
    }

    public static void setListLessons(List<Lesson> listLessons) {
        SchedulerProController.listLessons = listLessons;
    }

    public ScheduleEvent<Object> getScheduleEvent() {
        return scheduleEvent;
    }

    public void setScheduleEvent(ScheduleEvent<Object> scheduleEvent) {
        this.scheduleEvent = scheduleEvent;
    }

 //   public org.primefaces.model.ScheduleEvent<?> getEventSelected() {
 //       return eventSelected;
 //   }

 //   public void setEventSelected(org.primefaces.model.ScheduleEvent<?> eventSelected) {
 //       this.eventSelected = eventSelected;
 //   }
// used in schedule_pro.xhtml
    public ScheduleModel getScheduleModel() {
        return scheduleModel;
    }

    public void setScheduleModel(ScheduleModel scheduleModel) {
        SchedulerProController.scheduleModel = scheduleModel;
    }
    
/*public ScheduleBean() { copy from showcase
        eventModel = new ScheduleModel<ScheduleEvent>();
        DefaultScheduleEvent event = DefaultScheduleEvent.builder()
                .title("title")
                .startDate(LocalDateTime.of(2019, 7, 27, 12, 00))
                .endDate(LocalDateTime.of(2019, 7, 27, 12, 30))
                .build();

        eventModel.addEvent(event);
    }
*/
    public ScheduleModel getModel() {
        return model;
    }
/*
public static void beforePreparePaymentLesson(ScheduleEvent event, Lesson lesson) throws Exception{ // lesson et professional
    // faire le move vers next
    // vient de ScheduleProController
  LOG.debug("entering beforePreparePaymentLesson");
  LOG.debug("with event = " + event);
  LOG.debug("with Lesson = " + lesson);
}
*/
} //end Class