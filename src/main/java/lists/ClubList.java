package lists;

import entite.Club;
import entite.ECourseList;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import utils.DBConnection;
import utils.LCUtil;

public class ClubList implements interfaces.Log{

    private static List<ECourseList> liste = null;

public List<ECourseList> list(final @NotNull Connection conn) throws SQLException{
    String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
if(liste == null){
        LOG.info(" ... entering CourseList !! ");
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
 //    String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
  //   String te = utils.DBMeta.listMetaColumnsLoad(conn, "tee");
String query =
        "SELECT " + cl 
            + "        FROM club "
    //        + "        WHERE club.idclub = course.club_idclub"
    //        + "    AND course.CourseEnd >=  MAKEDATE(year(now()),1)"   // new 28/07/2017 >= 01/01 année en cours
    //+ "        GROUP by idcourse, idtee "  // mod 12/12/2017 was idcourse
    //        + "        ORDER by clubname, coursename, idtee, teestart"
        ;
     ps = conn.prepareStatement(query);
     utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet ClubList has " + rs.getRow() + " lines.");
           if(rs.getRow() == 0){
               String msg = "-- Empty Result Table for ClubList !! ";
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
             throw new Exception(msg);
            }    
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
	while(rs.next()){
		ECourseList ecl = new ECourseList();
                Club c = new Club();
                c = entite.Club.mapClub(rs);
                ecl.setClub(c);

  //              Course o = new Course();
  //              o = entite.Course.mapCourse(rs);
   //             ecl.setCourse(o);

  //              Tee t = new Tee();
   //             t = entite.Tee.mapTee(rs);
   //             ecl.setTee(t);
	 liste.add(ecl);
	} // end while
  //     LOG.debug(" -- before forEach " );
 //      liste.forEach(item -> LOG.info("Course list " + item + "/"));  // java 8 lambda                   
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
    public static void main(String[] args) throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
 //   Player player = new Player();
 //*   player.setIdplayer(324713);
    List<ECourseList> lp = new ClubList().list(conn);
        LOG.info("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class