package sql.preparedstatement;

import entite.Lesson;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

public class psCreateUpdateLesson implements Serializable, interfaces.Log, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    /**
     * INSERT lesson — PaymentsLessonId set to null on creation.
     */
    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final Lesson lesson,
            final Player player) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setNull     (1,  Types.INTEGER);
            ps.setTimestamp(2,  Timestamp.valueOf(lesson.getEventStartDate()));
            ps.setTimestamp(3,  Timestamp.valueOf(lesson.getEventEndDate()));
            ps.setInt      (4,  lesson.getEventProId());
            ps.setInt      (5,  player.getIdplayer());
            ps.setBoolean  (6,  lesson.isEventAllDay());
            ps.setString   (7,  lesson.getEventTitle());
            ps.setString   (8,  lesson.getEventDescription());
            ps.setNull     (9,  Types.INTEGER);                      // PaymentsLessonId — null on creation
            ps.setTimestamp(10, Timestamp.from(Instant.now()));
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * UPDATE lesson SET EventStartDate=?, EventEndDate=?
     *            WHERE EventProId=? AND EventStartDate=?
     * @param before original lesson (provides the WHERE keys)
     * @param after  rescheduled lesson (provides the new dates)
     */
    public static PreparedStatement psMapUpdate(
            PreparedStatement ps,
            final Lesson before,
            final Lesson after) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setTimestamp(1, Timestamp.valueOf(after.getEventStartDate()));
            ps.setTimestamp(2, Timestamp.valueOf(after.getEventEndDate()));
            ps.setInt      (3, before.getEventProId());
            ps.setTimestamp(4, Timestamp.valueOf(before.getEventStartDate()));
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
