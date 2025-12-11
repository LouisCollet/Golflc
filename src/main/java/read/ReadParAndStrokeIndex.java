package read;

import entite.Course;
import entite.ScoreStableford;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;

public class ReadParAndStrokeIndex {
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

public ScoreStableford read(Connection conn, final Course course, ScoreStableford scoreStableford) throws SQLException{
final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("entering " + methodName);
    LOG.debug("with course  = " + course);
    LOG.debug("with scoreStableford = " + scoreStableford); // output
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
/*    
final String query = """
      SELECT *
      FROM hole
      WHERE hole.tee_course_idcourse = ?
      GROU P BY HoleNumber
 """; 
à remplacer par le 30/09/2024
    */
final String query = """
WITH
   selection1 AS (
      SELECT * FROM hole
         WHERE hole.tee_course_idcourse = ?
    ),
   selection2 AS ( -- master tee
      SELECT idtee FROM tee
      WHERE tee.course_idcourse = ?
        AND tee.TeeStart = "YELLOW"
        AND tee.TeeGender = "M"
        AND tee.TeeHolesPlayed = "01-18"
    )
SELECT * FROM selection1
   JOIN selection2
   WHERE selection1.tee_idtee = selection2.idtee;
""";



     ps = conn.prepareStatement(query);
     ps.setInt(1, course.getIdcourse() );
     ps.setInt(2, course.getIdcourse() );
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     int i = 0;
      int[] PAR = new int[18]; //va contenir les par des 18 trous
      int[] INDEX = new int[18]; //va contenir les index des 18 trous
    //  Integer[] INDEX = new Integer[18]; //va contenir les index des 18 trous
//      Integer[] EXTRA = new Integer[18]; //va contenir les extraStrokes des 18 trous
      while(rs.next()){
            PAR[i]= rs.getInt("HolePar");
            INDEX[i]= rs.getInt("HoleStrokeIndex");
 //           EXTRA[i]= rs.getInt("HoleStrokeIndex");
   //         LOG.debug("i = " + i + " par = " +PAR[i] + " Stroke Index = " + INDEX[i]);
           i++;
        } // end while
     LOG.debug("finishing " + methodName + " with par          = " + Arrays.toString(PAR) );
     LOG.debug("finishing " + methodName + " with Stroke Index = " + Arrays.toString(INDEX) );
     scoreStableford.setParArray(PAR);
     scoreStableford.setIndexArray(INDEX);
return scoreStableford;
}catch (SQLException e){
    String msg = "SQLException in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception ! in " + methodName + ex.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

} //end method

void main() throws SQLException, Exception{
    Connection conn = new DBConnection().getConnection();
    Course course = new Course();
    course.setIdcourse(681);
 //   Tee tee = new Tee();
//    tee.setIdtee(98);
    ScoreStableford scoreStableford = new ScoreStableford();
  //  scoreStableford.setgsetGlobalArray([0][0]);
   
    scoreStableford = new read.ReadParAndStrokeIndex().read(conn, course, scoreStableford);
       LOG.info("scoreStableford with arrays par and stroke index = " +  scoreStableford);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class