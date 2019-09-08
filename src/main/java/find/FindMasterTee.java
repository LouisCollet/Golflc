package find;

import entite.Course;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindMasterTee{
final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 

    public int find(Connection conn, Course course, Tee tee)
        throws SQLException{

    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.info("entering " + CLASSNAME);
     LOG.info("course = " + course.toString());
     LOG.info("tee = " + tee);
// à modifier : fonction de TeeStart et non gender !!
String query =
       "SELECT idtee" +
    "   FROM tee, course" +
    "	WHERE tee.course_idcourse = course.idcourse" +
    "       AND course.idcourse = ?" +
//"         AND tee.teegender = ?" +  // mod 11-04-2019
    "       AND tee.TeeStart = ?" +
"           AND tee.TeeHolesPlayed = '01-18';"
          ;
 /*
SELECT idtee
FROM tee, course
	WHERE tee.course_idcourse = course.idcourse
	AND course.idcourse = 128
	AND tee.teegender = "L"
	AND tee.TeeHolesPlayed = "01-18"
*/


    ps = conn.prepareStatement(query);
    ps.setInt(1,course.getIdcourse());
    ps.setString(2,tee.getTeeStart()); // mod 11-04-2019 was gender
     utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    rs.last(); //on se positionne sur la dernière ligne
        LOG.info("ResultSet FindMasterTee has " + rs.getRow() + " lines.");
    if(rs.getRow() == 0){
            String msg = "Empty Result FindMasterTee !! ";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw new Exception(msg);
    }
     rs.beforeFirst();//on replace le curseur avant la première ligne
     if(rs.next()){ 
        return rs.getInt(1);
     }else{
      //  LOG.debug("no next : getCountScore = " + rs.getInt(1) );
        return 00;  //error code
    }
} catch(SQLException sqle){
    String msg = "Â£Â£Â£ SQLException in FindMasterTee = " + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return 00;
}catch(Exception nfe){
    String msg = "Â£Â£Â£ Exception in FindMasterTee = " + nfe.getMessage();
        LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 00;
}
finally{
      DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method
    
    public static void main(String[] args) throws Exception , Exception{

    Connection conn = new DBConnection().getConnection();
    Course course = new Course();
    course.setIdcourse(128);
    Tee tee = new Tee();
    tee.setTeeStart("YELLOW");
 // String s = "";
    int i = new FindMasterTee().find(conn, course, tee);
    LOG.info("main result = " + i);
    DBConnection.closeQuietly(conn, null, null, null);

}// end main

} // end class