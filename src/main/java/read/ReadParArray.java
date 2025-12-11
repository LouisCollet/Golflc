package read;

import entite.Course;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;

public class ReadParArray {
//    private final static int[] PAR = new int[18]; //va contenir les par des 18 trous
// à modifier = remplacer course par round !!
    // but = afficher la ligne PAR dans score_stableford.xhtml
public int[] read(Connection conn, final Player player, final Course course) throws SQLException{
        // pour un joueur particulier et un course !!! mod 15/08/2014 was with round (the other way, par from score
        // faudra adapter pour parties à 9 holes, a start 1 or 10
  int[] PAR = new int[18]; //va contenir les par des 18 trous
    LOG.debug("starting LoadParArray ... = ");
    LOG.debug("player = " + player); 
    LOG.debug("course  = " + course);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{

 /*   
final String query =
    "SELECT * " + 
    " FROM course" +
    " JOIN player" +
    " 		ON player.idplayer = ?" +
    " JOIN tee" +
    "      ON course.idcourse = tee.course_idcourse" +
    "		AND course.idcourse = ?" +
    "		AND tee.TeeGender = player.PlayerGender" +  // activé 31/07/2016
    " JOIN hole" +
    "      ON hole.tee_idtee = tee.idtee" +
    "      AND hole.tee_course_idcourse = course.idcourse" +
    "        GROU P BY hole.holenumber" +   //new 31/07/2016
    "        ORDER BY holenumber"
;
*/
final String query = """
    SELECT *
    FROM course
    JOIN player
    	ON player.idplayer = ?
    JOIN tee
        ON course.idcourse = tee.course_idcourse
    	AND course.idcourse = ?
    	AND tee.TeeGender = player.PlayerGender
    JOIN hole
        ON hole.tee_idtee = tee.idtee
        AND hole.tee_course_idcourse = course.idcourse
    GROUP BY hole.holenumber
    ORDER BY holenumber
   """;

   //     LOG.debug("player = " + player.getIdplayer()); 
   //     LOG.debug("course  = " + course.getIdcourse() ); 
        LOG.debug("holes  = " + PAR.length);
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, course.getIdcourse() );
          //    String p = ps.toString();
          utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();
      int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
      while(rs.next()){
          rowNum = rs.getRow() - 1;
            PAR [rowNum]= rs.getInt("HolePar"); 
        } // end while
        LOG.debug("finishing LoadParArray with par = " + Arrays.toString(PAR) );
return PAR;
}catch (SQLException e){
    String msg = "SQLException in LoadParArray() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in LoadParArray = " + ex.toString() );
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

// void main() throws SQLException, Exception{
public static void main(String args[])throws SQLException, Exception{    
   Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Course course = new Course();
    course.setIdcourse(86);
    int [] t = new ReadParArray().read(conn, player, course);
       LOG.debug("main result =  " + Arrays.toString(t));
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class