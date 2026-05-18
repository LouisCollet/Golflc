
package rowmappers;

import entite.Course;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class CourseRowMapper extends AbstractRowMapper<Course> {

    @Override
    public Course map(ResultSet rs) throws SQLException {
    //    final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            Course course = new Course();
            course.setIdcourse(getInteger(rs,"idcourse"));
            course.setCourseName(getString(rs, "coursename"));
            course.setCourseHoles(getShort(rs, "CourseHoles"));
            course.setCoursePar(getShort(rs, "coursePar"));
            course.setClub_idclub(getInteger(rs,"course.club_idclub"));
            // Timestamp → LocalDateTime null-safe
            course.setCourseBeginDate(getLocalDateTime(rs, "courseBeginDate"));
            course.setCourseEndDate(getLocalDateTime(rs, "courseEndDate"));

            return course;

        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    }
} //end class
