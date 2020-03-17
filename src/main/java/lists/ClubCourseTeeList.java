package lists;

import entite.ECourseList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;


public class ClubCourseTeeList implements interfaces.Log{
    private static List<ECourseList> liste = null;

public List<ECourseList> list(final Connection conn) throws SQLException{
if(liste == null){
        LOG.info(" ... entering getCourseList !! ");
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     //LOG.debug("starting getScoreCardList... = " );
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String te = utils.DBMeta.listMetaColumnsLoad(conn, "tee");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
String query =
        "SELECT "
          +  co + "," + cl + "," + te + 
        " FROM club, course, tee " +
        " WHERE club.idclub = course.club_idclub" +
        "	 and tee.course_idcourse = course.idcourse" +
//"--	 and club.idclub = 101\n" +
    ///    " GROUP by idcourse" +   // enlevé le 23-12-2018 pour faire apparaître tous les tees !!!
        " ORDER by idclub, idcourse, teegender DESC, teestart DESC";
    
     ps = conn.prepareStatement(query);
//     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet CourseList has " + rs.getRow() + " lines.");
        if(rs.getRow() == 0){
             String msg = "-- Empty Result Table for CourseList !! ";
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
             throw new Exception(msg);
         }
        
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
	while(rs.next()){
		ECourseList ecl = new ECourseList();
         //       Club c = new Club();
         //       c = entite.Club.mapClub(rs);
                ecl.setClub(entite.Club.mapClub(rs));
           //             LOG.debug("line 112");
       //         Course o = new Course();
      //          o = entite.Course.mapCourse(rs);
                ecl.setCourse(entite.Course.mapCourse(rs));
          //              LOG.debug("line 113");
     //           Tee t = new Tee();
     //           t = entite.Tee.mapTee(rs);
                ecl.setTee(entite.Tee.mapTee(rs));
	liste.add(ecl);
	} // end while
  //     LOG.debug(" -- before forEach " );
 //      liste.forEach(item -> LOG.info("Course list " + item));  // java 8 lambda                   
    return liste;
}catch (SQLException e){ 
        String msg = "SQL Exception in getCourseList : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in getCourseList() " + ex;
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
        ClubCourseTeeList.liste = liste;
    }
    
  public static void main(String[] args) throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
 //   Player player = new Player();
 //*   player.setIdplayer(324713);
    List<ECourseList> lp = new ClubCourseTeeList().list(conn);
        LOG.info("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
    
    
    
    
} //end class