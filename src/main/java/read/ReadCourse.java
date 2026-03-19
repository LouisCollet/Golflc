package read;

import entite.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import rowmappers.CourseRowMapper;
import utils.LCUtil;

/**
 * Service de lecture de Course
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Inject GenericDAO - Connection pooling
 * ✅ Pattern RowMapper conservé
 */
@ApplicationScoped
public class ReadCourse implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    /**
     * Lit un Course par ID
     *
     * @param course Course avec l'ID à rechercher
     * @return Course complet
     * @throws Exception en cas d'erreur
     */
    public Course read(Course course) throws SQLException, Exception {

        final String methodName = LCUtil.getCurrentMethodName();
        String msg;

        // Validation
        if (course == null || course.getIdcourse() == null || course.getIdcourse() == 0) {
            msg = "Valid course ID is required";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }

        LOG.debug("entering new version {}", methodName);
        LOG.debug("with Course {}", course);

        final String query = """
            SELECT *
            FROM Course
            WHERE idcourse = ?
            """;

        return dao.querySingle(query, new CourseRowMapper(), course.getIdcourse());
    }

    /**
     * Main pour tests
     */
    public static void main(String[] args) {
        try {
            Course course = new Course();
            course.setIdcourse(90); // english la tournette

            LOG.debug("Main ready (CDI required for execution)");
            LOG.debug("Test course ID: {}", course.getIdcourse());

        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
        }
    }
}
/*
import entite.Course;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import connection_package.DBConnection;
import utils.LCUtil;

public class ReadCourse{


public Course read(Course course,Connection conn) throws SQLException, Exception, Exception{
   final String methodName = utils.LCUtil.getCurrentMethodName();
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.debug("entering new version " + methodName);//
        LOG.debug("with Course " + course);//
    final String query = """
        SELECT *
        FROM Course
        WHERE idcourse = ?
     """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, course.getIdcourse());
    utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    RowMapper<Course> courseMapper = new CourseRowMapper();
     while(rs.next()){
         //course = Course.dtoMapper(rs);
         course = courseMapper.map(rs);
	}  //end while
  //   LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
  return course;
}catch (SQLException e){
 handleSQLException(e, methodName);
    return null;
}catch (Exception e){
    handleGenericException(e, methodName);
    return null;
}
finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

void main() throws SQLException, Exception{
   Connection conn = new DBConnection().getConnection();
   Course course = new Course();
   course.setIdcourse(90); // english la tournette
   course = new ReadCourse().read(course,conn);
      LOG.debug(" from main : course = " + course.toString());
   DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class
*/