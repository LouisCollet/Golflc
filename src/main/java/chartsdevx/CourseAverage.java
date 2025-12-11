package chartsdevx;

import entite.Average;
import entite.Course;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class CourseAverage {
    private static List<Average> liste = null;

public List<Average> stat(final Connection conn, final Player player, final Course course) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
      LOG.debug(" ... starting getStatAvg");
      LOG.debug("  with player = "        + player);
      LOG.debug("  with course = "        + course);
      
// CTE Common Table Expression
// invalidate
     String query = """  
       SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
     """;
     ps = conn.prepareStatement(query);
     utils.LCUtil.logps(ps);
     boolean b =  ps.execute(); //boolean rc  = stmt.execute(declareCursor);
     LOG.debug(" rs for ONLY_FULL_GROUP_BY = " + b);
     
query = """
WITH chart_data AS(
          SELECT scorehole, scorepar, scorestroke, scoreextrastroke, scorePoints, scorestrokeindex, course.idcourse, round.RoundHoles, round.idround, round.RoundDate
          FROM score, round, course
          WHERE ROUND.course_idcourse = ?
          AND score.player_has_round_round_idround = round.idround
          AND score.player_has_round_player_idplayer = ?
          GROUP BY idround, scorehole
          ORDER BY idround DESC
        )
          SELECT scorehole, scorepar, scoreStrokeIndex, scoreExtraStroke,
          ROUND(AVG(scorestroke),1) AS averageStroke,
          ROUND(AVG(scorePoints),1) AS averagePoints,
          COUNT(distinct idround) as countRound
          FROM chart_data
GROUP BY scorehole;
""";

     ps = conn.prepareStatement(query);
     ps.setInt(1, course.getIdcourse());
     ps.setInt(2, player.getIdplayer());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
    liste = new ArrayList<>();
     Average average = new Average();
     int i = 0;
     while(rs.next()){
        i++;
        average = entite.Average.map(rs);
        liste.add(average);
     } //end while
       LOG.debug("ResultSet getStatAvg has " + i + " lines.");
   //    liste.forEach(item -> LOG.debug("Average List" + item + "/"));
// revalidate here       
       
    return liste;
    
}catch(SQLException e){
    String msg = "SQL Exception in StatAvg() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch(Exception ex){
    String msg = "Other Exception in CourseAverage! " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
     DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method

 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
      Player player = new Player();
      player.setIdplayer(324713);
      Course course = new Course();
      course.setIdcourse(86);
    List<Average> av = new CourseAverage().stat(conn, player, course);
        LOG.debug("from main, average = " + av.toString());
 }catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
 } // end main//
} //end class