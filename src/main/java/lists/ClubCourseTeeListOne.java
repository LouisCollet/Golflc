package lists;

import entite.Club;
import entite.composite.ECourseList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;


public class ClubCourseTeeListOne implements interfaces.Log{
    private static List<ECourseList> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 
    
public List<ECourseList> list(Club club,final Connection conn) throws SQLException{
if(liste == null){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{ 
    final String query = """
        SELECT *
        FROM club, course, tee
        WHERE club.idclub = ?
              AND club.idclub = course.club_idclub
              AND tee.course_idcourse = course.idcourse
        ORDER by idclub, idcourse, teegender DESC, teestart DESC;
    """;
     ps = conn.prepareStatement(query);
     ps.setInt(1, club.getIdclub());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
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
   //      return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 //      liste.forEach(item -> LOG.debug("Course list " + item));  // java 8 lambda                   
    return liste;
}catch (SQLException e){ 
        String msg = "SQL Exception in " + methodName + " / " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
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
   //  LOG.debug("escaped to handicaplist repetition with lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        ClubCourseTeeListOne.liste = liste;
    }
    
  void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
   Club club = new Club();
   club.setIdclub(199);
    List<ECourseList> lp = new ClubCourseTeeListOne().list(club, conn);
        LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class
