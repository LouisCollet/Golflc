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
import static interfaces.ScheduleColors.*;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

@ApplicationScoped
public class ReadLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private lists.LessonProList   lessonProList;   // migrated 2026-02-23
    @Inject private calc.CalcLessonPrice  calcLessonPrice;

    public ReadLesson() { }

    public ScheduleModel read(Professional professional, entite.Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with Professional = {}", professional);

        try {
            ScheduleModel scheduleModel = new DefaultScheduleModel();
            List<Lesson> listLessons = lessonProList.list(professional);
            LOG.debug("number of lessons = {}", listLessons.size());

            for (Lesson lesson : listLessons) {
                var v = calcLessonPrice.calc(professional, lesson.getEventStartDate(), club);
                LOG.debug("lesson price var v = {}", v);
                lesson.setLessonAmount(v);
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
                        .backgroundColor(paid ? BLUE_BG : GREEN_BG)
                        .styleClass("ui-custompanelgrid")
                        .borderColor(paid ? BLUE_BORDER : GREEN_BORDER)
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
