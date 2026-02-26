
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
}


/*
import entite.Course;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseRowMapper implements RowMapper<Course> {
 
    @Override
   public Course map(ResultSet rs) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();  
   try{  
           //LOG.debug("entering map for method = " + methodName);
        Course course = new Course();
	course.setIdcourse(rs.getInt("idcourse"));
        course.setCourseName(rs.getString("coursename") );
        course.setCourseHoles(rs.getShort("CourseHoles"));
        course.setCoursePar(rs.getShort("coursePar"));
        course.setClub_idclub(rs.getInt("course.club_idclub"));
        course.setCourseBeginDate(rs.getTimestamp("courseBeginDate").toLocalDateTime());
        course.setCourseEndDate(rs.getTimestamp("courseEndDate").toLocalDateTime());
   return course;
 }catch(Exception e){
        handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class*/