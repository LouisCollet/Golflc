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
public Course load(Connection conn, int idcourse) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.info("entering LoadCourse.load");
    final String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");

    final String query = "SELECT "
        + co
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
                   c = entite.Course.mapCourse(rs);
		}  //end while
    return c;
}catch (SQLException e){
    String msg = "SQLException in LoadCOurse() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
  //  LCUtil.showMessageFatal("Exception in LoadClub = " + ex.toString() );
     return null;
}
finally{
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
   Course course = lc.load(conn, 104);
      LOG.info(" club = " + course.toString());
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class
