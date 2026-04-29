package sql.preparedstatement;

import entite.Lesson;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

public class psCreateLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PreparedStatement psMapCreate(PreparedStatement ps, Lesson lesson, Player player) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setNull(1, Types.INTEGER);
            ps.setTimestamp(2, Timestamp.valueOf(lesson.getEventStartDate()));
            ps.setTimestamp(3, Timestamp.valueOf(lesson.getEventEndDate()));
            ps.setInt(4, lesson.getEventProId());
            ps.setInt(5, player.getIdplayer());
            ps.setBoolean(6, lesson.isEventAllDay());
            ps.setString(7, lesson.getEventTitle());
            ps.setString(8, lesson.getEventDescription());
            ps.setNull(9, java.sql.Types.INTEGER);                       // PaymentsLessonId — null on creation
            ps.setTimestamp(10, Timestamp.from(Instant.now()));
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

} // end class
