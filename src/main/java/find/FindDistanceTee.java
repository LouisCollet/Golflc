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

public class FindDistanceTee{
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 public int find(Course course, Tee tee, Connection conn) throws SQLException{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.debug("entering " + methodName);
 //    LOG.debug("course = " + course.toString());
     LOG.debug("tee = " + tee);

    final String query = """
              SELECT idtee
              FROM tee, course
              WHERE tee.course_idcourse = course.idcourse
                AND course.idcourse = ?
                AND tee.TeeStart = ?
                AND tee.TeeGender = "M"
                AND tee.TeeHolesPlayed = "01-18"
      """;

    ps = conn.prepareStatement(query);
    ps.setInt(1,course.getIdcourse());
    ps.setString(2,tee.getTeeStart());  // YELLOW, RED ...
  //  utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    int i = 0;
    int distanceTee = 0;
    while (rs.next()) {
        i++;
 //        LOG.debug("found tee in rs.getInt = " + rs.getInt(1));
        distanceTee = rs.getInt(1);
    }
    if(i == 0){
 //        LOG.debug("greenfee non payé !");
            String msg = LCUtil.prepareMessageBean("distancetee.notfound", " for course = " + course.getIdcourse() + " / " + tee.getTeeStart());
            LOG.debug(msg);
            LCUtil.showMessageFatal(msg);
     }
    if(i == 1){
            String msg = LCUtil.prepareMessageBean("distancetee.found", tee.getTeeDistanceTee() + " for course = " + course.getIdcourse() + " / " + tee.getTeeStart());
                 //   player.getPlayerLastName() + " / " + player.getIdplayer()
                 //   + " for round : " + round.getRoundName();
          //  String msg = "distancetee.found" + " for course = " + course.getIdcourse() + " / " + tee.getTeeStart();
            LOG.info(msg);
        //    LCUtil.showMessageInfo(msg);
        }
    return distanceTee;
    
 //   }
  //   return 0;
} catch(SQLException sqle){
    String msg = "Â£Â£Â£ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return 00;
}catch(Exception nfe){
    String msg = "Â£Â£Â£ Exception in " + methodName + nfe.getMessage();
        LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 00;
}
finally{
      DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method
    
 void main() throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();
    Course course = new Course();
    course.setIdcourse(164); // 164 ok
    Tee tee = new Tee();
    tee.setTeeStart("YELLOW");
    //tee.setTeeGender("M");
    // tee.setTeeHolesPlayed("01-18");
    int i = new FindDistanceTee().find(course, tee, conn);
    LOG.debug("main result = " + i);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class