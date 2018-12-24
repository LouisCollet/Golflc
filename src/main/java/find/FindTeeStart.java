package find;

import entite.Course;
import entite.Player;
import exceptions.LCCustomException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

// liste des tee d'un course //
public class FindTeeStart implements interfaces.Log{
   private static List<String> liste = null;
   final private static String ClassName = Thread.currentThread().getStackTrace()[1].getClassName(); 
   
public List<String> teeStart (final Course course , final Player player, final Connection conn) throws SQLException{   
if(liste == null)
{ 
    LOG.info("starting FindTeeStart for course = " + course.toString());
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{   
    String query =
     " SELECT idcourse, idtee, teestart" +
      " FROM course, tee" +
      " WHERE course.idcourse = ?" +
      " AND tee.TeeGender = ?" +
      " and tee.course_idcourse = course.idcourse"
    ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, course.getIdcourse()); 
    ps.setString(2, player.getPlayerGender()); 
         utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet FindTeeStart has " + rs.getRow() + " lines.");
    if(rs.getRow() == 0)
      {   String msg = "££ Empty Result Table in " + ClassName + " for course = " + course.getIdcourse();
      //String msg = "NullPointerException in " + npe;
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
          throw new LCCustomException(msg);
      }     
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
		while(rs.next())
                {
			liste.add(rs.getString("teestart"));  // liste YELLOW, BLUE, etc.
		}
        liste.forEach(item -> LOG.info("TeeStart list " + item));  // java 8 lambda
    return liste;

}catch (LCCustomException e){
  //  String msg = " SQL Exception in getScoreCardList1() " + e;
  //  LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return null;    
}catch (NullPointerException npe){
    String msg = "NullPointerException in " + ClassName + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (SQLException e){
    String msg = "SQL Exception in FindTeeStart : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in FindTeeStart() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}
else{
///       LOG.debug("escaped to FindSubscription repetition with lazy loading");
    return liste;  //plusieurs fois ??
  // }
} //end method
}
    public static List<String> getListe() {
        return liste;
    }

    public static void setListe(List<String> liste) {
        FindTeeStart.liste = liste;
    }
}  // end class