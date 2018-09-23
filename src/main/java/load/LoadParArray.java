package load;

import entite.Course;
import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class LoadParArray implements interfaces.Log
{
    private final static int[] PAR = new int[18]; //va contenir les par des 18 trous
// à modifier = remplacer course par round !!
public int [] LoadParArray(Connection conn, final Player player, final Course course) throws SQLException
 
        // pour un joueur particulier et un course !!! mod 15/08/2014 was with round (the other way, par from score
        // faudra adapter pour parties à 9 holes, a start 1 or 10
{
    LOG.info("starting LoadParArray ... = "); // + Arrays.toString(par) );
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
  //   LOG.info("starting LoadParArray with player = " + player.getIdplayer() + " , course = " + course.getIdcourse() );
final String query = "SELECT idcourse, course.CourseName , idhole, holenumber, holepar" +
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
"        GROUP BY hole.holenumber" +   //new 31/07/2016
"        ORDER BY holenumber"
;
        LOG.info("player = " + player.getIdplayer()); 
        LOG.info("course  = " + course.getIdcourse() ); 
        LOG.info("holes  = " + PAR.length);
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, course.getIdcourse() );
          //    String p = ps.toString();
          utils.LCUtil.logps(ps); 
		//get round data from database
    rs =  ps.executeQuery();
    rs.beforeFirst(); //  Initially the cursor is positionned before the first row
      int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
      while (rs.next())
        {rowNum = rs.getRow() - 1;
            PAR [rowNum]= rs.getInt("HolePar"); 
        } // end while
 //       LOG.info("finishing LoadParArray with par = " + Arrays.toString(par) );
return PAR;
}catch (SQLException e){
    String msg = "SQLException in LoadParArray() = " + e.toString() + ", SQLState = " + e.getSQLState().toString()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    LOG.error("NullPointerException in LoadParArray() " + npe);
    LCUtil.showMessageFatal("Exception = " + npe.toString() );
     return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in LoadParArray = " + ex.toString() );
     return null;
}
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

} //end method
private static void main(String[] args) throws SQLException // testing purposes
{
/*Connection conn = DBConnection.getConnection();
    Player player = null;
    Round round = null; 
player.setIdplayer(324713);
round.setIdround(206);
int [] t = LoadParArray(conn, player, round );
for (int x: par )
        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);
        */
}// end main
} // end class
