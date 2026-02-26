package lists;

import entite.Club;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import connection_package.DBConnection;
import utils.LCUtil;
import static interfaces.Log.LOG;
public class CourseListForClub2 {

 private static List<String> liste = null;
    
public List<String> list(Club club, final @NotNull Connection conn) throws SQLException{    
    final String methodName = utils.LCUtil.getCurrentMethodName();
if(liste == null){
        LOG.debug(" ... entering " + methodName);
        LOG.debug(" ... with club  " + club);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
  final String query = """
      SELECT *
      FROM course
      WHERE club_idclub = ?
      AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
      """;
     ps = conn.prepareStatement(query);
     ps.setInt(1, club.getIdclub());
     utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
	while(rs.next()){
            liste.add(rs.getString("idcourse") + " - " + rs.getString("CourseName"));
	} // end while
  //     LOG.debug(" -- before forEach " );
      liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda
      if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
   //      return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 return liste;
}catch (SQLException e){ 
        String error = "SQL Exception in " + methodName + ": " + e;
	LOG.error(error);
        LCUtil.showMessageFatal(error);
        return null;
}catch (Exception ex){
    String error = "Exception in " + methodName + " / " + ex;
    LOG.error(error);
    LCUtil.showMessageFatal(error);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{ // liste not null
    LOG.debug("escaped to " + methodName + "repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<String> getListe() {
        return liste;
    }

    public static void setListe(List<String> liste) {
        CourseListForClub2.liste = liste;
    }
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
      Club club = new Club();
      club.setIdclub(101);
    List<String> lp = new CourseListForClub2().list(club, conn);
        LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class