package read;

import entite.Professional;
import entite.Lesson;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;
import utils.DBConnection;
import static utils.LCUtil.showMessageFatal;

public class ReadLessons{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
   private ScheduleModel scheduleModel;
   private org.primefaces.model.ScheduleEvent<?> scheduleEvent = new DefaultScheduleEvent<>();
   private List<Lesson> listLessons = new ArrayList<>();
   
   // completes ScheduleModel from DB
   
public ScheduleModel read(Professional professional, Connection conn){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
try{  
        LOG.debug("entering read " + methodName);
        LOG.debug(" with Professional = " + professional);
     scheduleModel = new DefaultScheduleModel();
        LOG.debug(" starting creating listeEvent ... ");
  // calendrier des lessons pour un pro   
    listLessons  = new lists.LessonProList().list(professional,conn); // DB extract
    LOG.debug("number of lessons = "+ listLessons.size());
 //     LOG.debug(" number of events in listeEvent = " + listLessons.size());
 //  Initializes the PrimeFaces's Schedule Component with the list of the events
           for(Lesson lesson : listLessons){
             scheduleEvent = DefaultScheduleEvent.builder()
		.title(lesson.getEventTitle() + " pro = " + lesson.getEventProId())
                .startDate(lesson.getEventStartDate()) 
                .endDate(lesson.getEventEndDate()) 
		.description("PAID - " + lesson.getEventDescription())
                .dynamicProperty("key-id", lesson.getEventPlayerId()) // new 06-02-2023  we create a dynamic property  
                .allDay(lesson.isEventAllDay())
                .textColor("blue")
                .backgroundColor("yellow")
                .styleClass("ui-custompanelgrid")   // 02-06-2021ui-custompanelgrid
	//	.draggable(true) // default
		.borderColor("orange")
                .overlapAllowed(false)
                
		.resizable(false) // sinon le resize provoque la modification de l'heure de fin et il faut alors modify ...
                .build();
             scheduleModel.addEvent(scheduleEvent);
          } // end for
     LOG.debug(" number of events in model = " + scheduleModel.getEventCount());
 return scheduleModel;
   }catch (Exception ex){
            String msg = "Exception in " + methodName + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }finally{
 //   DBConnection.closeQuietly(null, null, rs, ps);
  }

} // end read
    
 void main() throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();
    Professional professional = new Professional();
    professional.setProId(1);
    professional.setProPlayerId(324713);
    ScheduleModel blocking = new ReadLessons().read(professional, conn);
        LOG.debug("Blocking found = " + blocking);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
 
} // end class