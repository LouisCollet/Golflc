package create;

import entite.Player;
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
import java.time.Instant;
import manager.PlayerManager;
import static utils.LCUtil.showMessageFatal;

@ApplicationScoped
public class CreateLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject
    private PlayerManager playerManager;

    public CreateLesson() { }

    public boolean create(final Lesson lesson, final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with Lesson = " + lesson);
        LOG.debug("with Player = " + player);

        try (Connection conn = dao.getConnection()) {
            final String query = utils.LCUtil.generateInsertQuery(conn, "lesson");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setNull(1, java.sql.Types.INTEGER);
                ps.setTimestamp(2, Timestamp.valueOf(lesson.getEventStartDate()));
                ps.setTimestamp(3, Timestamp.valueOf(lesson.getEventEndDate()));
                ps.setInt(4, lesson.getEventProId());
                ps.setInt(5, player.getIdplayer());
                ps.setBoolean(6, lesson.isEventAllDay());
                ps.setString(7, lesson.getEventTitle());
                lesson.setEventDescription(player.getPlayerLastName() + ", " + player.getPlayerFirstName()
                        + " (" + player.getIdplayer() + ")");
                ps.setString(8, lesson.getEventDescription());
                ps.setTimestamp(9, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "Lesson Created = " + lesson;
                    LOG.info(msg);
                    return true;
                } else {
                    String msg = "<br/>ERROR insert Lesson : " + lesson;
                    LOG.debug(msg);
                    showMessageFatal(msg);
                    return false;
                }
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
        Lesson event = new Lesson();
        event.setEventStartDate(LocalDateTime.parse("2021-05-30T15:45:00"));
        event.setEventEndDate(LocalDateTime.parse("2021-05-30T16:15:00"));
        event.setEventProId(3);
        event.setEventAllDay(true);
        event.setEventTitle("Golf lesson Bernard Nicolay");
        Player player = new Player();
        player.setIdplayer(206658);
        player = playerManager.readPlayer(player.getIdplayer());
        boolean lp = new CreateLesson().create(event, player);
        LOG.debug("from main, after lp = " + lp);
    } // end main
    */

} // end class
