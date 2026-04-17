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
public class ReadLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private lists.LessonProList lessonProList; // migrated 2026-02-23

    public ReadLesson() { }

    public ScheduleModel read(Professional professional) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with Professional = {}", professional);

        try {
            ScheduleModel scheduleModel = new DefaultScheduleModel();
            List<Lesson> listLessons = lessonProList.list(professional);
            LOG.debug("number of lessons = {}", listLessons.size());

            for (Lesson lesson : listLessons) {
                boolean paid = lesson.isLessonPaid();
                org.primefaces.model.ScheduleEvent<?> scheduleEvent = DefaultScheduleEvent.builder()
                        .title(lesson.getEventTitle())
                        .startDate(lesson.getEventStartDate())
                        .endDate(lesson.getEventEndDate())
                        .description(lesson.getEventDescription())
                        .dynamicProperty("lesson-object", lesson)  // full entity — use lesson.getEventPlayerId(), getProName(), etc.
                        .dynamicProperty("lesson-booked",   true)   // always true — DB lessons are booked
                        .dynamicProperty("lesson-paid",     paid)
                        .allDay(lesson.isEventAllDay())
                        .textColor("white")
                        .backgroundColor(paid ? "#0d6efd" : "#28a745")  // blue=paid, green=booked
                        .styleClass("ui-custompanelgrid")
                        .borderColor(paid ? "#0a58ca" : "#1e7e34")
                        .overlapAllowed(false)
                        .resizable(false)
                        .build();
                scheduleModel.addEvent(scheduleEvent);
            } // end for

            LOG.debug("number of events in model = {}", scheduleModel.getEventCount());
            return scheduleModel;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return new DefaultScheduleModel();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new DefaultScheduleModel();
        }
    } // end method
} // end class
