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

public class ClubDetailList implements interfaces.Log{
    private static List<ECourseList> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 
    
public List<ECourseList> list(Club club, final Connection conn) throws SQLException{
if(liste == null){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
      LOG.debug(" ... entering " + methodName);
      LOG.debug(" ... with club = " + club);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{

  final String query = """
        SELECT *
        FROM club, course, tee
        WHERE club.idclub = course.club_idclub
           AND tee.course_idcourse = course.idcourse
           AND idclub = ?
        ORDER by idclub, idcourse, teegender, teestart
    """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, club.getIdclub()); //player.getIdplayer());
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
   //      return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 //      liste.forEach(item -> LOG.debug("Course list " + item));  // java 8 lambda                   
  return liste;
}catch (SQLException e){ 
        String msg = "SQL Exception in ClubDetailList : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in ClubDetailList() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
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
        ClubDetailList.liste = liste;
    }
  void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
     Club club = new Club();
     club.setIdclub(101);
     List<ECourseList> ec = new ClubDetailList().list(club, conn);
        LOG.debug("from main, ec = " + ec);
 }catch (Exception e){
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
    
    
    
} //end class