package lists;

import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class CourseList implements interfaces.Log{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    static List<ECourseList> liste = null;

public List<ECourseList> list(final @NotNull Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
  final String query = """
        SELECT *
        FROM club, course, tee
        WHERE club.idclub = course.club_idclub
            AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
            AND tee.course_idcourse = course.idcourse
        GROUP BY idcourse, idtee
        ORDER by clubname, coursename, idtee, teestart
        """ ;
     ps = conn.prepareStatement(query);
     utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
	while(rs.next()){
		ECourseList ecl = new ECourseList();
                ecl.setClub(entite.Club.dtoMapper(rs));
                ecl.setCourse(entite.Course.dtoMapper(rs));
                ecl.setTee(entite.Tee.dtoMapper(rs));
	 liste.add(ecl);
	} // end while
     if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
  //       return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 //      liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda                   
   // return liste;
    return List.copyOf(liste); // new 02-12-2025 non testé immutable eviter erreur modification ??
}catch (SQLException e){ 
        String error = "SQL Exception in " + methodName + " / " + e;
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
}else{
  //  LOG.debug("escaped to CourseListlist repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        CourseList.liste = liste;
    }
 void main() throws SQLException, Exception{
  //void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
    List<ECourseList> lp = new CourseList().list(conn);
        LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class