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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class CreateLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateLesson() { }

    public boolean create(final Lesson lesson, final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with Lesson = {}", lesson);
        LOG.debug("with Player = {}", player);

        lesson.setEventDescription(player.getPlayerLastName() + ", " + player.getPlayerFirstName()
                + " (" + player.getIdplayer() + ")");

        try (Connection conn = dao.getConnection()) {
            final String query = utils.LCUtil.generateInsertQuery(conn, "lesson");
            try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                sql.preparedstatement.psCreateUpdateLesson.psMapCreate(ps, lesson, player);
                int row = ps.executeUpdate();
                if (row != 0) {
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            lesson.setEventId(gk.getInt(1));
                            LOG.debug("generated EventId = {}", lesson.getEventId());
                        }
                    }
                    String msg = prepareMessageBean("lesson.success") + lesson;
                    LOG.info(msg);
                    showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "ERROR insert Lesson : " + lesson;
                    LOG.debug(msg);
                    showMessageFatal(msg);
                    return false;
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || (e.getSQLState() != null && e.getSQLState().startsWith("23"))) {
                String msg = utils.LCUtil.prepareMessageBean("lesson.already.booked") + lesson ;
                       //    + lesson.getEventStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                LOG.warn("- {}", msg);
                showMessageFatal(msg);
                return false;
            }
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
