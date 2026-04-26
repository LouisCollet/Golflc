package create;

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
import utils.LCUtil;

@ApplicationScoped
public class CreateCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateCourse() { }

    public boolean create(final Course course) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);

            if (course == null) {
                LOG.error("course cannot be null");
                throw new IllegalArgumentException("Course cannot be null");
            }

            if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
                LOG.error("course name is required");
                throw new IllegalArgumentException("Course name is required");
            }

            if (course.getClub_idclub() == 0) {
                LOG.error("course must be associated with a club");
                throw new IllegalArgumentException("Course must be associated with a club");
            }

            LOG.debug("creating course = {}", course);

            String query = LCUtil.generateInsertQuery(conn, "course");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateUpdateCourse.mapCreate(ps, course);
                LCUtil.logps(ps);

                int row = ps.executeUpdate();

                if (row == 0) {
                    String msg = "Fatal Error: No row inserted in " + methodName;
                    LOG.error(msg);
                    throw new SQLException(msg);
                }
            }

            int generatedId = LCUtil.generatedKey(conn);
            course.setIdcourse(generatedId);
            LOG.debug("course created name = {} id = {}", course.getCourseName(), course.getIdcourse());
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
