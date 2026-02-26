package lists;

import entite.Club;
import entite.Course;
import entite.Tee;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import utils.LCUtil;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static interfaces.Log.LOG;

/**
 * Liste Club/Course/Tee pour un club donné
 * ✅ Migré vers CDI + DataSource (plus de Connection en paramètre)
 * ✅ try-with-resources (plus de finally/closeQuietly)
 * ✅ Cache statique conservé (compatible avec ClubManager.setListe)
 */
@ApplicationScoped
public class ClubCourseTeeListOne implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Injection DataSource WildFly - plus de conn en paramètre
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    private static final String QUERY = """
        SELECT *
        FROM club, course, tee
        WHERE club.idclub = ?
              AND club.idclub = course.club_idclub
              AND tee.course_idcourse = course.idcourse
        ORDER BY idclub, idcourse, teegender DESC, teestart DESC
        """;

    // ========================================
    // MÉTHODE PRINCIPALE
    // ========================================

    /**
     * Liste toutes les courses et tees pour un club donné.
     * ✅ Plus de Connection en paramètre
     * ✅ Cache invalidé par ClubManager via setListe(null)
     *
     * @param club le club dont on veut les courses/tees
     * @return liste de ECourseList2, jamais null
     */
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

        // ✅ try-with-resources : Connection, PreparedStatement, ResultSet fermés automatiquement
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(QUERY)) {

            ps.setInt(1, club.getIdclub());
            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {

                RowMapper<Club>   clubMapper   = new ClubRowMapper();
                RowMapper<Course> courseMapper = new CourseRowMapper();
                RowMapper<Tee>    teeMapper    = new TeeRowMapper();

                List<ECourseList> result = new ArrayList<>();

                while (rs.next()) {
                    ECourseList ecl = ECourseList.builder()
                        .club(clubMapper.map(rs))
                        .course(courseMapper.map(rs))
                        .tee(teeMapper.map(rs))
                        .build();
                    result.add(ecl);
                }

                if (result.isEmpty()) {
                    String msg = "Empty Result List in " + methodName + " for club " + club.getIdclub();
                    LOG.warn(msg);
           //         LCUtil.showMessageFatal(msg);
                } else {
                    LOG.debug("{} - ResultSet has {} lines for club {}",
                             methodName, result.size(), club.getIdclub());
                }

                liste = result;                             // ✅ mise en cache
                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // CACHE - Getters / Setters statiques
    // ========================================
    // Conservés tels quels — utilisés par ClubManager pour invalider le cache

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
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class
*/