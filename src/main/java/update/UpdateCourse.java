package update;

import entite.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import sql.SqlFactory;
import utils.LCUtil;

@ApplicationScoped
public class UpdateCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public UpdateCourse() { }

    public boolean update(final Course course) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);

            if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
                LOG.error("course id is required for update");
                throw new IllegalArgumentException("Course ID is required for update");
            }

            LOG.debug("updating course = {}", course);

            String query = new SqlFactory().generateQueryUpdate(conn, "course");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateUpdateCourse.mapUpdate(ps, course);
                LCUtil.logps(ps);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    LOG.error("no rows updated for course id = {}", course.getIdcourse());
                    throw new SQLException("No rows updated — course id = " + course.getIdcourse());
                }
            }

            String msg = LCUtil.prepareMessageBean("course.modify");
            LOG.debug("course updated name = {} id = {}", course.getCourseName(), course.getIdcourse());
            LCUtil.showMessageInfo(msg);
            conn.commit();

            return true;

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
