package lists;

import entite.Club;
import entite.Course;
import entite.Tee;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.Resource;
import javax.sql.DataSource;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import utils.LCUtil;

/**
 * Liste complète des clubs/courses/tees
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Resource DataSource - Connection pooling
 * ✅ Lazy loading : si liste != null, retourne le cache
 */
@ApplicationScoped
public class CourseList implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    /**
     * DataSource injecté par WildFly (connection pooling)
     */
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    /**
     * Liste complète clubs/courses/tees
     * 
     * @return Liste des ECourseList2
     * @throws Exception en cas d'erreur
     */
    public List<ECourseList> list() throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        // ✅ LAZY LOADING CONSERVÉ
        if (liste != null) {
            return liste;
        }

        LOG.debug(" ... entering {}", methodName);

        try (Connection conn = dataSource.getConnection()) {

            final String query = """
                SELECT *
                FROM club, course, tee
                WHERE club.idclub = course.club_idclub
                    AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
                    AND tee.course_idcourse = course.idcourse
                GROUP BY idcourse, idtee
                ORDER by clubname, coursename, idtee, teestart
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {

                LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {

                    // ✅ PARTIE NON MODIFIÉE - DÉBUT
                    liste = new ArrayList<>();
                    RowMapper<Club>   clubMapper   = new ClubRowMapper();
                    RowMapper<Course> courseMapper = new CourseRowMapper();
                    RowMapper<Tee>    teeMapper    = new TeeRowMapper();
                    while(rs.next()){
                        //ECourseList ecl = new ECourseList();
                        //   ecl.setClub(entite.Club.dtoMapper(rs)); // mod 
                        ECourseList ecl = ECourseList.builder()
                            .club(clubMapper.map(rs))
                            .course(courseMapper.map(rs))
                            .tee(teeMapper.map(rs))
                            .build();
                        //   ecl.setClub(clubMapper.map(rs));   
                        //   ecl.setCourse(courseMapper.map(rs));
                        //   ecl.setTee(teeMapper.map(rs));
                        liste.add(ecl);
                    } // end while
                    if(liste.isEmpty()){
                        String msg = "££ Empty Result List in " + methodName;
                        LOG.error(msg);
                        LCUtil.showMessageFatal(msg);
              //          return null;
                    }else{
                        LOG.debug("ResultSet {} has {} lines.", methodName, liste.size());
                    }
                    //     liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda
                    // return liste;
                    return List.copyOf(liste); // new 02-12-2025 non testé immutable eviter erreur modification ??
                    // ✅ PARTIE NON MODIFIÉE - FIN
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()               { return liste; }
    public void setListe(List<ECourseList> liste)     { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class


/*
import entite.Club;
import entite.Course;
import entite.Tee;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.validation.constraints.NotNull;
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
public class CourseList {
    
    static List<ECourseList2> liste = null;

public List<ECourseList2> list(final @NotNull Connection conn) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
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
    RowMapper<Club>   clubMapper   = new ClubRowMapper();
    RowMapper<Course> courseMapper = new CourseRowMapper();
    RowMapper<Tee>    teeMapper    = new TeeRowMapper();
	while(rs.next()){
		//ECourseList ecl = new ECourseList();
             //   ecl.setClub(entite.Club.dto Mapper(rs)); // mod 
                  ECourseList2 ecl = ECourseList2.builder()
                    .club(clubMapper.map(rs))
                    .course(courseMapper.map(rs))
                    .tee(teeMapper.map(rs))
                .build();

             //   ecl.setClub(clubMapper.map(rs));   
             //   ecl.setCourse(courseMapper.map(rs));
             //   ecl.setTee(teeMapper.map(rs));
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
    handleSQLException(e, methodName);
    return null;
}catch (Exception e){
    handleGenericException(e, methodName);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
  //  LOG.debug("escaped to CourseListlist repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ECourseList2> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList2> liste) {
        CourseList.liste = liste;
    }
 void main() throws SQLException, Exception{
  //void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
    List<ECourseList2> lp = new CourseList().list(conn);
        LOG.debug("number of courses = " + lp.size());
        LOG.debug("from main, after lp = " + lp.toString());
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class


*/