package find;

import entite.Lesson;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@ApplicationScoped
public class FindLessonBooked implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindLessonBooked() { }

    /** Returns true if a lesson slot is already booked (proId + startDate). */
    public boolean find(final Lesson lesson) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for proId={} startDate={}", lesson.getEventProId(), lesson.getEventStartDate());

        if (lesson.getEventProId() == null || lesson.getEventStartDate() == null) {
            LOG.debug("proId or startDate null — skipping check");
            return false;
        }

        final String query = """
            SELECT COUNT(*)
            FROM lesson
            WHERE eventProId    = ?
              AND eventStartDate = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, lesson.getEventProId());
            ps.setTimestamp(2, Timestamp.valueOf(lesson.getEventStartDate()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LOG.debug("lesson already booked proId={} startDate={}",
                            lesson.getEventProId(), lesson.getEventStartDate());
                    return true;
                }
                return false;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
