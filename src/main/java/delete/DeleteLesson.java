package delete;

import entite.Lesson;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class DeleteLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public DeleteLesson() { }

    public boolean delete(final Lesson lesson) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with lesson = " + lesson);

        final String query = """
                DELETE FROM lesson
                WHERE EventProId = ?
                AND EventStartDate = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, lesson.getEventProId());
            ps.setTimestamp(2, Timestamp.valueOf(lesson.getEventStartDate()));
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate();
            if (row != 0) {
                String msg = "ScheduleEvent Deleted = " + lesson;
                LOG.info(msg);
                showMessageInfo(msg);
                return true;
            } else {
                String msg = "ERROR ScheduleEvent NOT Deleted !!: " + lesson;
                LOG.debug(msg);
                showMessageFatal(msg);
                return false;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return false;
        }
    } // end method

    /** @deprecated Use {@link #delete(Lesson)} instead — migrated 2026-02-24 */
    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Lesson event = new Lesson();
        event.setEventStartDate(LocalDateTime.parse("2021-05-23T12:45:30"));
        event.setEventProId(1);
        boolean lp = new DeleteLesson().delete(event);
        LOG.debug("from main, after lp = " + lp);
    } // end main
    */

} // end class
