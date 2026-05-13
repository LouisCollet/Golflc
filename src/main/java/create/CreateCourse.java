package create;

import entite.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.DATE_BEGIN_COURSE;
import static interfaces.GolfInterface.DATE_END_COURSE;
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

    public boolean upsert(final Course course) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            INSERT INTO course (idcourse, CourseName, CourseHoles, CoursePar, club_idclub, CourseBeginDate, CourseEndDate, CourseModificationDate)
            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE
                CourseName             = VALUES(CourseName),
                CourseHoles            = VALUES(CourseHoles),
                CoursePar              = VALUES(CoursePar),
                club_idclub            = VALUES(club_idclub),
                CourseBeginDate        = VALUES(CourseBeginDate),
                CourseEndDate          = VALUES(CourseEndDate),
                CourseModificationDate = CURRENT_TIMESTAMP
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            if (course.getIdcourse() != null && course.getIdcourse() > 0) {
                ps.setInt(1, course.getIdcourse());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, course.getCourseName());
            ps.setShort(3, (short) 18);
            ps.setShort(4, course.getCoursePar());
            ps.setInt(5, course.getClub_idclub());
            ps.setTimestamp(6, Timestamp.valueOf(DATE_BEGIN_COURSE));
            ps.setTimestamp(7, Timestamp.valueOf(DATE_END_COURSE));
            LCUtil.logps(ps);

            int rows = ps.executeUpdate();
            LOG.debug("course upserted id = {} name = {} rows = {}", course.getIdcourse(), course.getCourseName(), rows);
            return rows > 0;

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
