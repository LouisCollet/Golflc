package delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteCourse implements interfaces.Log, interfaces.GolfInterface
{
    public String deleteCourse(final int idcourse, final Connection conn) throws Exception
    {
    PreparedStatement ps = null;
try
{       LOG.info("starting Delete Course ... = " );
        LOG.info("Delete course for idcourse "  + idcourse);
    String query = 
       " DELETE from course" +
       " WHERE course.idcourse = ?" 
        ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, idcourse);
    LCUtil.logps(ps); 
    int row_delete = ps.executeUpdate();
        LOG.info("deleted Course = " + row_delete);
    String msg = "<br/> <h1>There are " + row_delete + " Course deleted = " + idcourse;
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        return "Course deleted ! ";
}catch (SQLException e){
    String msg = "SQL Exception in DeleteCourse = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (Exception ex){
    String msg = "Exception in DeleteCourse() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
   
 public static void main(String[] args) throws SQLException, Exception 
 {
     DBConnection dbc = new DBConnection();
     Connection conn = dbc.getConnection();
 try{
       LOG.info("Input main = ");
    int idcourse = 339;
 //  Date date =SDF.parse("01/01/2000");
    DeleteCourse dc = new DeleteCourse();
    dc.deleteCourse(idcourse, conn);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class