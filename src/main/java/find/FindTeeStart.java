package find;

import entite.Course;
import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

// liste des tee d'un course //
public class FindTeeStart implements interfaces.Log{
   private static List<String> liste = null;
   final private static String ClassName = Thread.currentThread().getStackTrace()[1].getClassName(); 
   
public List<String> find(final Course course , final Player player, final Connection conn) throws SQLException{   
if(liste == null){ 
    LOG.info("starting FindTeeStart for course = " + course.toString());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{   
    String t = utils.DBMeta.listMetaColumnsLoad(conn, "tee");  // fields list, comma separated
    String query =
      " SELECT " + t + // idcourse, idtee, teestart, teeGender" +
      " FROM course, tee" +
      " WHERE course.idcourse = ?" +
      "     AND tee.TeeGender = ?" +
      "     AND tee.course_idcourse = course.idcourse"
    ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, course.getIdcourse()); 
    ps.setString(2, player.getPlayerGender());
         utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet FindTeeStart has " + rs.getRow() + " lines.");
    if(rs.getRow() == 0)
      {   String msg = "Pas de tee connu pour gender = " + player.getPlayerGender() + " for course = " + course.getIdcourse();
      //String msg = "NullPointerException in " + npe;
        LOG.error(msg);
        showMessageFatal(msg);
   //       throw new LCCustomException(msg);
      }     
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
	while(rs.next()){
		liste.add(rs.getString("TeeStart") + " / " + rs.getString("TeeGender")
                        + " / "+ rs.getString("TeeHolesPlayed") + " / " + rs.getInt("idtee"));  // liste YELLOW, BLUE, etc.
	}
        liste.forEach(item -> LOG.info("TeeStart list " + item));  // java 8 lambda
    return liste;

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
    
    public static void main(String[] args) throws Exception {
    Connection conn = new DBConnection().getConnection();
  try{
        Course course = new Course();
        course.setIdcourse(135);
        Player p = new Player();
        p.setPlayerGender("L"); 
        List<String> b = new FindTeeStart().find(course, p, conn);
        LOG.info("from main, after = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
    
    
}  // end class