
package lists;

import entite.Club;
import entite.Course;
import entite.Round;
import entite.composite.ECourseList;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.RoundRowMapper;
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

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;

/**
 * Liste des inscriptions (rounds disponibles)
 * ✅ @ApplicationScoped — stateless, cache partagé
 * ✅ @Resource DataSource — plus de Connection en paramètre
 * ✅ try-with-resources — plus de finally/closeQuietly
 * ✅ Collections.emptyList() — jamais null
 * ✅ cache d'instance — plus static
 */
@Named
@ApplicationScoped
public class InscriptionList implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ DataSource injecté — standard golflc
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — plus static
    private List<ECourseList> liste = null;

    public InscriptionList() { }

    // ========================================
    // LIST
    // ========================================

    public List<ECourseList> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
                /* lists.InscriptionList.list */
                WITH selection AS (
                    SELECT * FROM round
                )
                SELECT * FROM selection
                    JOIN course
                        ON course.idcourse = selection.course_idcourse
                    JOIN club
                        ON club.idclub = course.club_idclub
                    ORDER BY roundDate DESC
                    LIMIT 30
                """;

        // ✅ try-with-resources — plus de finally/closeQuietly
        try (Connection        conn = dataSource.getConnection();
             PreparedStatement ps   = conn.prepareStatement(query)) {

            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {

                liste = new ArrayList<>();

                RowMapper<Club>       clubMapper   = new ClubRowMapper();
                RowMapper<Course>     courseMapper = new CourseRowMapper();
                RowMapperRound<Round> roundMapper  = new RoundRowMapper();

                while (rs.next()) {
                    Club club = clubMapper.map(rs);
                    ECourseList ecl = ECourseList.builder()
                            .club(club)
                            .course(courseMapper.map(rs))
                            .round(roundMapper.map(rs, club))
                            .build();
                    liste.add(ecl);
                }
            } // rs fermé ici // rs fermé ici
        } catch (SQLException sqle) {
            handleSQLException(sqle, methodName);
            return Collections.emptyList();             // ✅ jamais null
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();             // ✅ jamais null
        } // ps + conn fermés ici

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    // ========================================
    // CACHE
    // ========================================

    public List<ECourseList> getListe()                 { return liste; }
    public void               setListe(List<ECourseList> liste) { this.liste = liste; }

    /**
     * Invalidation explicite du cache
     * ✅ Plus clair que setListe(null)
     */
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            List<ECourseList2> p1 = list();
            LOG.debug(methodName + " - number extracted = " + p1.size());
            LOG.debug(methodName + " - InscriptionList   = " + p1.toString());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end main
    */

} // end class
/*
import entite.Club;
import entite.Course;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import connection_package.DBConnection;
import entite.composite.ECourseList2;
import utils.LCUtil;
import static interfaces.Log.LOG;
public class InscriptionList {
    private static List<ECourseList2> liste = null;
    
    
public List<ECourseList2> list(final Connection conn) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    
    LOG.debug(" ... entering InscriptionList !!");
if(liste == null){
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.debug("starting getInscriptionList.. = " );
/*String query = """
           
        SELECT *
        FROM round
        JOIN course
            ON round.course_idcourse = course.idcourse
        JOIN club
            ON club.idclub = course.club_idclub
        GROU P BY idround
        ORDER by rounddate DESC
        LIMIT 30;
""";
 final String query = """
            \n   /* lists.InscriptionList.list  
   WITH selection AS (
    SELECT * from round
    )
   SELECT * FROM selection
   JOIN course
      ON course.idcourse = selection.course_idcourse
   JOIN club
      ON club.idclub = course.club_idclub
   ORDER BY roundDate DESC
   LIMIT 30;
""";

        ps = conn.prepareStatement(query);
        utils.LCUtil.logps(ps);
	rs =  ps.executeQuery();
        liste = new ArrayList<>();
        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapperRound<Round> roundMapper = new RoundRowMapper(); // accepte rs et 
	while(rs.next()){
            Club club = clubMapper.map(rs);
            ECourseList2 ecl = ECourseList2.builder()
               .club(club)
               .course(courseMapper.map(rs))
               //     .handicapIndex(handicapIndexMapper.map(rs))
                //    .inscription(inscriptionMapper.map(rs))
                .round(roundMapper.map(rs,club))
                .build();
        //    ECourseList ecl = new ECourseList(); // liste pour sélectionner un round player = entite.Player.mapPlayer(rs);
         ///   ecl.setClub(clubMapper.map(rs));
         //   ecl.setCourse(courseMapper.map(rs));
          //  ecl.setRound(new entite.Round().dtoMapper(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
           // ecl.setRound(roundMapper.map(rs, ecl.getClub()));
	liste.add(ecl);
	} //end while
     
  //  if(liste == null){
     if(liste.isEmpty()){   
        String msg = "££ Empty Result List in " + methodName;
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
   //    return null;
    }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }    
  // LOG.debug("Inscription liste = " + liste.toString());
    return liste;
}catch (SQLException e){
    handleSQLException(e, methodName);
    return null;
}catch (Exception e){
     handleGenericException(e, methodName);
     return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps);
}
}else{
    //     LOG.debug("escaped to listinscription repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
} //end else    
} //end method

    public static List<ECourseList2> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList2> liste) {
        InscriptionList.liste = liste;
    }
    
  void main() throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
  //  Player player = new Player();
  //  player.setIdplayer(324713);
  //  Round round = new Round(); 
  //  round.setIdround(260);
  //  Club club = new Club();
  //  club.setIdclub(1006);
    List<ECourseList2> p1 = new InscriptionList().list(conn);
        LOG.debug("number extracted = " + p1.size());
        LOG.debug("Inscription list = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} //end class
*/