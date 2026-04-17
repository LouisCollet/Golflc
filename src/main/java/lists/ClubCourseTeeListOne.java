package lists;

import entite.Club;
import entite.Course;
import entite.Tee;
import entite.composite.ECourseList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import utils.LCUtil;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static interfaces.Log.LOG;

/**
 * Liste Club/Course/Tee pour un club donne
 * Migre vers GenericDAO (2026-03-18)
 */
@ApplicationScoped
public class ClubCourseTeeListOne implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    private static final String QUERY = """
        SELECT *
        FROM club
        INNER JOIN course ON club.idclub = course.club_idclub
        LEFT JOIN tee ON tee.course_idcourse = course.idcourse
        WHERE club.idclub = ?
        ORDER BY idclub, idcourse, teegender DESC, teestart DESC
        """;

    // ========================================
    // METHODE PRINCIPALE
    // ========================================

    public List<ECourseList> list(Club club) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (club == null || club.getIdclub() == null || club.getIdclub() <= 0) {
            LOG.warn("{} - club is null or has no ID", methodName);
            return Collections.emptyList();
        }

        if (liste != null) {
            LOG.debug("{} - returning cached list ({} entries)", methodName, liste.size());
            return liste;
        }

        RowMapper<Club>   clubMapper   = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapper<Tee>    teeMapper    = new TeeRowMapper();

        liste = dao.queryList(QUERY, rs -> ECourseList.builder()
                .club(clubMapper.map(rs))
                .course(courseMapper.map(rs))
                .tee(teeMapper.map(rs))
                .build(),
                club.getIdclub());

        if (liste.isEmpty()) {
            LOG.warn("Empty Result List in {} for club {}", methodName, club.getIdclub());
        } else {
            LOG.debug("{} - ResultSet has {} lines for club {}",
                     methodName, liste.size(), club.getIdclub());
        }
        return liste;
    } // end method

    // ========================================
    // CACHE - Getters / Setters
    // ========================================

    public List<ECourseList> getListe()               { return liste; }
    public void setListe(List<ECourseList> liste)     { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

} // end class
/*
import entite.Club;
import entite.Course;
import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import connection_package.DBConnection;
import entite.composite.ECourseList2;
import utils.LCUtil;
import static interfaces.Log.LOG;

public class ClubCourseTeeListOne {
    private static List<ECourseList2> liste = null;


public List<ECourseList2> list(Club club,final Connection conn) throws SQLException, Exception{
if(liste == null){
    final String methodName = utils.LCUtil.getCurrentMethodName();
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
    RowMapper<Club> clubMapper = new ClubRowMapper();
    RowMapper<Course> courseMapper = new CourseRowMapper();
    RowMapper<Tee> teeMapper = new TeeRowMapper();
	while(rs.next()){
		// ecl = new ECourseList();

                ECourseList2 ecl = ECourseList2.builder()
                .club(clubMapper.map(rs))
                .course(courseMapper.map(rs))
             //   .player(playerMapper.map(rs))
             //   .inscription(inscriptionMapper.map(rs))
             //   .round(roundMapper.map(rs,club))
                .tee(teeMapper.map(rs))
            .build();


            //    ecl.setClub(clubMapper.map(rs));
            //    ecl.setCourse(courseMapper.map(rs));
            //    ecl.setTee(teeMapper.map(rs));
	liste.add(ecl);
	} // end while
     if(liste.isEmpty()){
         String msg = "Pound Pound Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
   //      return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 //      liste.forEach(item -> LOG.debug("Course list " + item));  // java 8 lambda
    return liste;
}catch (SQLException e){
    handleSQLException(e, methodName);
    return null;
}catch (Exception e){
    handleGenericException(e, methodName);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

}else{
   //  LOG.debug("escaped to handicaplist repetition with lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ECourseList2> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList2> liste) {
        ClubCourseTeeListOne.liste = liste;
    }

  void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
   Club club = new Club();
   club.setIdclub(199);
    List<ECourseList2> lp = new ClubCourseTeeListOne().list(club, conn);
        LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null);
          }
   } // end main//
} //end class
*/
