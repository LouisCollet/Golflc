package read;

import entite.Course;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ReadCourse{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
   
public Course read(Course course,Connection conn) throws SQLException{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.debug("entering " + methodName);//
        LOG.debug("with Course " + course);//
    final String query = """
        SELECT *
        FROM Course
        WHERE idcourse = ?
     """;
     ps = conn.prepareStatement(query);
     ps.setInt(1, course.getIdcourse());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
 //    Course c = new Course(); 
     while(rs.next()){
         course = Course.dtoMapper(rs);
	}  //end while
  //   LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
  return course;
}catch (SQLException e){
    String msg = "SQLException in " + methodName + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception in " + methodName + ex);
    LCUtil.showMessageFatal("Exception in LoadClub = " + ex.toString() );
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