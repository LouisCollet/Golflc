package lists;

import entite.Club;
import entite.Course;
import entite.ECourseList;
import entite.Tee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import utils.DBConnection;
import utils.LCUtil;


public class ClubCourseTeeList implements interfaces.Log
{
    private static List<ECourseList> liste = null;

public List<ECourseList> getCourseList(final @NotNull Connection conn) throws SQLException
{  
if(liste == null)
{
        LOG.info(" ... entering ClubCourseTeeList !! ");
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
     //LOG.debug("starting getScoreCardList... = " );
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String te = utils.DBMeta.listMetaColumnsLoad(conn, "tee");
    // String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
  //   String ha = utils.DBMeta.listMetaColumnsLoad(conn, "Handicap");
String query =
        "SELECT "
          +  co + "," + cl + "," + te + 
     //   + " idclub, clubname, idcourse,coursename,tee.idtee,clubcity, clubcountry, clubAddress,  clubLatitude, clubLongitude ,  courseholes," +
     //   " coursepar,  courseBegin, courseEnd , tee.TeeStart " +
        " FROM club, course, tee " +
        " WHERE club.idclub = course.club_idclub" +
        "	 and tee.course_idcourse = course.idcourse" +
//"--	 and club.idclub = 101\n" +
        " GROUP by idcourse" +
        " ORDER by clubname, idcourse, idtee, teestart";
    
     ps = conn.prepareStatement(query);
//     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet CourseList has " + rs.getRow() + " lines.");
           if(rs.getRow() == 0)
            {String msg = "-- Empty Result Table for CourseList !! ";
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
                Club c = new Club();
                c = entite.Club.mapClub(rs);
                  /*      c.setIdclub(rs.getInt("idclub") );
           //                 LOG.debug("line 112");
           //                 LOG.debug("idcclub setted = " + c.getIdclub() );
                        c.setClubName(rs.getString("clubName") );
          //                  LOG.debug("clubname setted = " + c.getClubName() );       
                        c.setClubCity(rs.getString("clubCity") );
          //                  LOG.debug("clubcity setted = " + c.getClubCity() );    
                        c.setClubCountry(rs.getString("clubCountry") );
          //                  LOG.debug("clubcountry setted = " + c.getClubCountry() );       
                        c.setClubAddress(rs.getString("clubAddress") );
          //                  LOG.debug("clubaddress setted from c = " + c.getClubAddress() ); 
                        c.setClubLatitude(rs.getBigDecimal("ClubLatitude") );
                        c.setClubLongitude(rs.getBigDecimal("ClubLongitude") );*/
                ecl.setClub(c);
           //             LOG.debug("line 112");
           //             LOG.debug("clubaddress setted from ecl = " + ecl.Eclub.getClubAddress() );
                Course o = new Course();
                o = entite.Course.mapCourse(rs);
                  /*      o.setIdcourse(rs.getInt("idcourse"));
           //                LOG.debug("idcourse setted = " + o.getIdcourse() );
			o.setCourseName(rs.getString("coursename") );
            //                LOG.debug("course name setted = " + o.getCourseName() );    
                        o.setCourseBegin(rs.getTimestamp("courseBegin")); // mod 01/05/2017 was getDate
             //               LOG.debug("begin date setted = " + o.getCourseBegin() );
                        o.setCourseEnd(rs.getTimestamp("courseend")); // mod 01/05/2017 was getDate
             //               LOG.debug("begin date setted = " + o.getCourseBegin() );*/
                ecl.setCourse(o);
          //              LOG.debug("line 113");
          //               LOG.debug("begin date setted from ecl = " + ecl.Ecourse.getCourseBegin() );
                Tee t = new Tee();
                t = entite.Tee.mapTee(rs);
             //           t.setIdtee(rs.getInt("idtee"));
            //               LOG.debug("idtee setted = " + t.getIdtee() );
	//		t.setTeeStart(rs.getString("teestart") );
            //                LOG.debug("teestart name setted = " + t.getTeeStart() );    
                ecl.setTee(t);
           //         LOG.debug("line 114");
           //         LOG.debug("teestart name setted from ecl = " + ecl.Etee.getTeeStart() );    
     //                   LOG.debug("ECourseList = " + ecl.toString());
			//store all data into a List
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
}catch (NullPointerException npe){   String msg = "NullPointerException in getCourseList() " + npe;
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

} //end class