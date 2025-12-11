package delete;

import entite.Course;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteCourse implements interfaces.Log, interfaces.GolfInterface
{
    public boolean delete(final Course course, final Connection conn) throws Exception{
    PreparedStatement ps = null;
try
{       LOG.debug("starting Delete Course ... = " );
        LOG.debug(" for idcourse "  + course);
    final String query = """
        DELETE from course
        WHERE course.idcourse = ?
       """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, course.getIdcourse());
    LCUtil.logps(ps); 
    int row_delete = ps.executeUpdate();
        LOG.debug("deleted Course = " + row_delete);
    String msg = "<br/>There are " + row_delete + " Course deleted = " + course;
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String msg = "SQL Exception in DeleteCourse = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteCourse() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
   
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
     Course course = new Course();
     course.setIdcourse(128);
    boolean b = new DeleteCourse().delete(course, conn);
       LOG.debug("from main - resultat deleteCourse = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class