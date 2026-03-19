package update;

import entite.Lesson;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Service de mise à jour d'un événement de leçon (déplacement dans l'agenda)
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdateLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public UpdateLesson() { }

    public boolean update(final Lesson before, final Lesson after) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(" for ScheduleEvent before = " + before);
        LOG.debug(" for ScheduleEvent after = " + after);

        final String query = """
                UPDATE lesson
                SET EventStartDate = ?, EventEndDate = ?
                WHERE EventProId = ?
                AND EventStartDate = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setTimestamp(1, Timestamp.valueOf(after.getEventStartDate()));
            ps.setTimestamp(2, Timestamp.valueOf(after.getEventEndDate()));
            ps.setInt(3, before.getEventProId());
            ps.setTimestamp(4, Timestamp.valueOf(before.getEventStartDate()));
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            if (row != 0) {
                String msg = "successful UPDATE ScheduleEvent = " + after;
                LOG.info(msg);
                showMessageInfo(msg);
                return true;
            } else {
                String msg = "Unsuccessful result in " + methodName + " for event : " + after;
                LOG.error(msg);
                showMessageFatal(msg);
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
        LOG.debug("entering " + methodName);
        Lesson before = new Lesson();
        before.setEventStartDate(java.time.LocalDateTime.parse("2021-05-12T08:00:00"));
        before.setEventProId(1);
        Lesson after = new Lesson();
        after.setEventStartDate(java.time.LocalDateTime.parse("2021-05-13T15:45:00"));
        after.setEventEndDate(java.time.LocalDateTime.parse("2021-05-13T16:15:00"));
        boolean b = new UpdateLesson().update(before, after);
        LOG.debug("from main, result = " + b);
    } // end main
    */

} // end class
