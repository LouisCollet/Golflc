package find;

import entite.Course;
import entite.Tee;
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
import utils.LCUtil;

@ApplicationScoped
public class FindDistanceTee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindDistanceTee() { }

    public int find(Course course, Tee tee) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("tee = " + tee);

        final String query = """
              SELECT idtee
              FROM tee, course
              WHERE tee.course_idcourse = course.idcourse
                AND course.idcourse = ?
                AND tee.TeeStart = ?
                AND tee.TeeGender = "M"
                AND tee.TeeHolesPlayed = "01-18"
          """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, course.getIdcourse());
            ps.setString(2, tee.getTeeStart());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int i = 0;
                int distanceTee = 0;
                while (rs.next()) {
                    i++;
                    distanceTee = rs.getInt(1);
                }
                if (i == 0) {
                    String msg = LCUtil.prepareMessageBean("distancetee.notfound",
                            " for course = " + course.getIdcourse() + " / " + tee.getTeeStart());
                    LOG.debug(msg);
                    LCUtil.showMessageFatal(msg);
                }
                if (i == 1) {
                    String msg = LCUtil.prepareMessageBean("distancetee.found",
                            tee.getTeeDistanceTee() + " for course = " + course.getIdcourse() + " / " + tee.getTeeStart());
                    LOG.info(msg);
                }
                return distanceTee;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return 0;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return 0;
        }
    } // end method

/*
void main() throws SQLException, Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    // tests locaux
} // end main
*/

} // end class
