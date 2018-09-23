package load;

import entite.Course;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class LoadCourse
{
// à adapter
public Course LoadCourse(Connection conn, int idcourse) throws SQLException
{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.info("entering LoadCourse");
    String s = utils.DBMeta.listMetaColumnsLoad(conn, "course");
        LOG.info("String from listMetaColumns = " + s);
     //   LOG.info("simple name = " + club.)

final String query = "SELECT "
        + s
        + " FROM Course "
        + " WHERE idcourse = ?" ;
     //   LOG.info("Club  = " + club.getIdclub() ); 
        LOG.info("Course to be modified = " + idcourse); 
     ps = conn.prepareStatement(query);
  //   ps.setInt(1, club.getIdclub());
     ps.setInt(1, idcourse);
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.beforeFirst();
     Course c = new Course(); 
     while(rs.next())
                {
		c.setIdcourse(rs.getInt("idcourse"));
                c.setCourseName(rs.getString("coursename") );
                c.setCourseHoles(rs.getShort("CourseHoles"));
                c.setCoursePar(rs.getShort("coursepar"));
                c.setCourseBegin(rs.getDate("courseBegin"));
                c.setCourseEnd(rs.getDate("courseEnd"));
                c.setClub_idclub(rs.getInt("club_idclub"));
		}  //end while
    return c;
}catch (SQLException e){
    String msg = "SQLException in LoadCOurse() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    LOG.error("NullPointerException in LoadCourse() " + npe);
    LCUtil.showMessageFatal("Exception = " + npe.toString() );
     return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
  //  LCUtil.showMessageFatal("Exception in LoadClub = " + ex.toString() );
     return null;
}
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

} //end method

public static void main(String[] args) throws SQLException, Exception // testing purposes
{
    DBConnection dbc = new DBConnection();
   Connection conn = dbc.getConnection();
  //  Club club = new Club();
//    club.setIdclub(104);
//round.setIdround(206);
   LoadCourse lc = new LoadCourse();
   Course course = lc.LoadCourse(conn, 104);
      LOG.info(" club = " + course.toString());
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class
