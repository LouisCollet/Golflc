package find;

import entite.Course;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class FindMasterTee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public FindMasterTee() { }

    public int find(Course course) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(" with course = " + course.toString());

        final String query = """
           SELECT idtee
           FROM tee, course
           WHERE tee.course_idcourse = course.idcourse
             AND course.idcourse = ?
             AND tee.TeeStart = "YELLOW"
             AND tee.TeeGender = "M"
             AND tee.TeeHolesPlayed = "01-18"
          """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, course.getIdcourse());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int i = 0;
                int masterTee = 0;
                if (rs.next()) {
                    i++;
                    masterTee = rs.getInt(1);
                }
                if (i == 0) {
                    String msg = LCUtil.prepareMessageBean("mastertee.notfound",
                            " for course = " + course.getIdcourse());
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                }
                if (i == 1) {
                    String msg = "mastertee.found" + " = " + masterTee + " for course = " + course.getIdcourse();
                    LOG.info(msg);
                }
                return masterTee;
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
    LOG.debug("entering " + methodName);
    // tests locaux
} // end main
*/

} // end class
