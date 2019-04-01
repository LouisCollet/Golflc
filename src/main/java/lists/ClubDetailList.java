package lists;

import entite.Club;
import entite.ECourseList;
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

public List<ECourseList> list(Club club, final Connection conn) throws SQLException{
if(liste == null){
        LOG.info(" ... entering ClubDetailList !! ");
        LOG.info(" ... entering ClubDetailList with club = " + club);
  //      if(course.getIdcourse() == null){
   //          LOG.info("for testing purposes, courseid forced !!!! to 86");
   //        course.setIdcourse(86);
  //      }
       
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
     //   + " idclub, clubname, idcourse,coursename,tee.idtee,clubcity, clubcountry, clubAddress,  clubLatitude, clubLongitude ,  courseholes," +
     //   " coursepar,  courseBegin, courseEnd , tee.TeeStart " +
        " FROM club, course, tee " +
        " WHERE club.idclub = course.club_idclub" +
        "	 and tee.course_idcourse = course.idcourse" +
        "	 and idclub = ?" +
//        " GROUP by idcourse" +
        " ORDER by idclub, idcourse, teegender, teestart";
    
    ps = conn.prepareStatement(query);
    ps.setInt(1, club.getIdclub()); //player.getIdplayer());
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet ClubDetailList has " + rs.getRow() + " lines.");
           if(rs.getRow() == 0)
            {String msg = "-- Empty Result Table for ClubDetailListt !! ";
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
             throw new Exception(msg);
            }    
        
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
	while(rs.next())
        {
		ECourseList ecl = new ECourseList();
                entite.Club c = new Club();
                c = entite.Club.mapClub(rs);
                ecl.setClub(c);
           //             LOG.debug("line 112");
                entite.Course o = new entite.Course();
                o = entite.Course.mapCourse(rs);
                ecl.setCourse(o);
          //              LOG.debug("line 113");
                entite.Tee t = new entite.Tee();
                t = entite.Tee.mapTee(rs);
                ecl.setTee(t);
	liste.add(ecl);
	} // end while
  //     LOG.debug(" -- before forEach " );
 //      liste.forEach(item -> LOG.info("Course list " + item));  // java 8 lambda                   
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
    
         public static void main(String[] args) throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
     //   Player player = new Player();
     //   player.setIdplayer(324713);
     //   Round round = new Round(); 
     //   round.setIdround(300);
     Club club = new Club();
     club.setIdclub(101);
     List<ECourseList> ec = new ClubDetailList().list(club, conn);
        LOG.info("from main, ec = " + ec);
 }catch (Exception e){
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
    
    
    
} //end class