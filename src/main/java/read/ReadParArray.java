package read;

import entite.Course;
import entite.Player;
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
import java.util.Arrays;

@ApplicationScoped
public class ReadParArray implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadParArray() { }

    public int[] read(final Player player, final Course course) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("player = {}", player);
        LOG.debug("course = {}", course);

        final String query = """
                SELECT *
                FROM course
                JOIN player
                    ON player.idplayer = ?
                JOIN tee
                    ON course.idcourse = tee.course_idcourse
                    AND course.idcourse = ?
                    AND tee.TeeGender = player.PlayerGender
                JOIN hole
                    ON hole.tee_idtee = tee.idtee
                    AND hole.tee_course_idcourse = course.idcourse
                GROUP BY hole.holenumber
                ORDER BY holenumber
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setInt(2, course.getIdcourse());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int[] par = new int[18];
                while (rs.next()) {
                    int rowNum = rs.getRow() - 1;
                    par[rowNum] = rs.getInt("HolePar");
                }
                LOG.debug("finishing with par = {}", Arrays.toString(par));
                return par;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return new int[0];
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new int[0];
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Player player = new Player(); player.setIdplayer(324713);
        // Course course = new Course(); course.setIdcourse(86);
        // int[] t = new ReadParArray().read(player, course);
        // LOG.debug("main result = {}", Arrays.toString(t));
    } // end main
    */

} // end class
