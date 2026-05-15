package read;

import entite.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import rowmappers.CourseRowMapper;
import utils.LCUtil;

@ApplicationScoped
public class ReadCourse implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public Course read(Course course) throws SQLException, Exception {

        final String methodName = LCUtil.getCurrentMethodName();
        String msg;

        // Validation
        if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
            msg = "Valid course ID is required";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }

        LOG.debug("entering {}", methodName);
        LOG.debug("with Course {}", course);

        final String query = """
            SELECT *
            FROM Course
            WHERE idcourse = ?
            """;

        return dao.querySingle(query, new CourseRowMapper(), course.getIdcourse());
    }

} // end class