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
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 public int find(Connection conn, Course course) throws SQLException{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.debug("entering " + methodName);
     LOG.debug(" with course = " + course.toString());
 //    LOG.debug("tee = " + tee);

 final String query = """
       SELECT idtee
       FROM tee, course
       WHERE tee.course_idcourse = course.idcourse
         AND course.idcourse = ?
         AND tee.TeeStart = "YELLOW"
         AND tee.TeeGender = "M"
         AND tee.TeeHolesPlayed = "01-18"
      """;
    ps = conn.prepareStatement(query);
    ps.setInt(1,course.getIdcourse());
    utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    int i = 0;
    int masterTee = 0;
    if(rs.next()){ 
         i++;
         masterTee = rs.getInt(1);
     }
     if(i == 0){
            String msg = LCUtil.prepareMessageBean("mastertee.notfound", " for course = " + course.getIdcourse());
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
     }
    if(i == 1){
          //  String msg = LCUtil.prepareMessageBean("distancetee.found") + " = " + distanceTee + " for course = " + course.getIdcourse() + " / " + tee.getTeeStart();
                 //   player.getPlayerLastName() + " / " + player.getIdplayer()
                 //   + " for round : " + round.getRoundName();
            String msg = "mastertee.found" + " = " + masterTee + " for course = " + course.getIdcourse(); // + " / " + tee.getTeeStart();     
            LOG.info(msg);
        //    LCUtil.showMessageInfo(msg);
        }
return masterTee;
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
    course.setIdcourse(148);
    Tee tee = new Tee();
    tee.setTeeStart("YELLOW");
    tee.setTeeGender("M");
    tee.setTeeHolesPlayed("01-18");
    int i = new FindMasterTee().find(conn, course);
    LOG.debug("main result = " + i);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class