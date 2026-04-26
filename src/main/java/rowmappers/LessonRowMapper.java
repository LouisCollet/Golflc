package rowmappers;

import entite.Lesson;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class LessonRowMapper extends AbstractRowMapper<Lesson> {

    @Override
    public Lesson map(ResultSet rs) throws SQLException {
        try {
            Lesson lesson = new Lesson();
            lesson.setEventId(getInteger(rs, "EventId"));
            lesson.setEventStartDate(getLocalDateTime(rs, "EventStartDate"));
            lesson.setEventEndDate(getLocalDateTime(rs, "EventEndDate"));
            lesson.setEventProId(getInteger(rs, "EventProId"));
            lesson.setEventPlayerId(getInteger(rs, "EventPlayerId"));
            lesson.setEventAllDay(Boolean.TRUE.equals(getBoolean(rs, "EventAllDay")));
            lesson.setEventTitle(getString(rs, "EventTitle"));
            lesson.setEventDescription(getString(rs, "EventDescription"));
            lesson.setPaymentsLessonId(getInteger(rs, "PaymentsLessonId"));
            lesson.setEventClubName(getString(rs, "ClubName"));
            lesson.setProName(getString(rs, "ProName"));
            lesson.setStudentName(getString(rs, "StudentName"));
            return lesson;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class
