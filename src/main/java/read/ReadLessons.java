package read;

import entite.Lesson;
import entite.Professional;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

@ApplicationScoped
public class ReadLessons implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private lists.LessonProList lessonProList; // migrated 2026-02-23

    public ReadLessons() { }

    public ScheduleModel read(Professional professional) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with Professional = " + professional);

        try {
            ScheduleModel scheduleModel = new DefaultScheduleModel();
            List<Lesson> listLessons = lessonProList.list(professional);
            LOG.debug(methodName + " - number of lessons = " + listLessons.size());

            for (Lesson lesson : listLessons) {
                org.primefaces.model.ScheduleEvent<?> scheduleEvent = DefaultScheduleEvent.builder()
                        .title(lesson.getEventTitle() + " pro = " + lesson.getEventProId())
                        .startDate(lesson.getEventStartDate())
                        .endDate(lesson.getEventEndDate())
                        .description("PAID - " + lesson.getEventDescription())
                        .dynamicProperty("key-id", lesson.getEventPlayerId())
                        .allDay(lesson.isEventAllDay())
                        .textColor("blue")
                        .backgroundColor("yellow")
                        .styleClass("ui-custompanelgrid")
                        .borderColor("orange")
                        .overlapAllowed(false)
                        .resizable(false)
                        .build();
                scheduleModel.addEvent(scheduleEvent);
            } // end for

            LOG.debug(methodName + " - number of events in model = " + scheduleModel.getEventCount());
            return scheduleModel;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return new DefaultScheduleModel();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new DefaultScheduleModel();
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Professional professional = new Professional();
        professional.setProId(1);
        professional.setProPlayerId(324713);
        ScheduleModel model = read(professional);
        LOG.debug("model events count = " + model.getEventCount());
    } // end main
    */

} // end class
