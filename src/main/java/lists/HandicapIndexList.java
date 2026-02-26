package lists;

import entite.Club;
import entite.Course;
import entite.HandicapIndex;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.composite.ECourseList;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.HandicapIndexRowMapper;
import rowmappers.InscriptionRowMapper;
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
 * Liste des HandicapIndex pour un joueur
 * ✅ @ApplicationScoped — stateless, cache partagé
 * ✅ @Resource DataSource — plus de Connection en paramètre
 * ✅ try-with-resources — plus de finally/closeQuietly
 * ✅ Collections.emptyList() — jamais null
 */
@Named
@ApplicationScoped
public class HandicapIndexList implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ DataSource injecté — plus de Connection en paramètre
   @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — plus static
    private List<ECourseList> liste = null;

    // ========================================
    // LIST
    // ========================================

    public List<ECourseList> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        try {
            LOG.debug(methodName + " - idplayer = " + player.getIdplayer());

            final String query = """
                    WITH selection AS (
                        SELECT * FROM handicap_index, player_has_round, player
                        WHERE player.idplayer = ?
                          AND handicap_index.HandicapPlayerId = player.idplayer
                          AND player_has_round.InscriptionIdPlayer = player.idplayer
                          AND player_has_round.InscriptionIdRound = handicap_index.HandicapRoundId
                    )
                    SELECT * FROM selection
                        JOIN round
                            ON round.idround = selection.HandicapRoundId
                        JOIN course
                            ON course.idcourse = round.course_idcourse
                        JOIN club
                            ON club.idclub = course.club_idclub
                        ORDER BY selection.HandicapDate DESC
                        LIMIT 30
                    """;

            // ✅ try-with-resources — plus de finally/closeQuietly
            try (Connection conn              = dataSource.getConnection();
                 PreparedStatement ps         = conn.prepareStatement(query)) {

                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {

                    liste = new ArrayList<>();

                    RowMapper<Club>           clubMapper          = new ClubRowMapper();
                    RowMapper<Course>         courseMapper        = new CourseRowMapper();
                    RowMapper<Inscription>    inscriptionMapper   = new InscriptionRowMapper();
                    RowMapperRound<Round>     roundMapper         = new RoundRowMapper();
                    RowMapper<HandicapIndex>  handicapIndexMapper = new HandicapIndexRowMapper();

                    while (rs.next()) {
                        Club club = clubMapper.map(rs);
                        ECourseList ecl = ECourseList.builder()
                                .club(club)
                                .course(courseMapper.map(rs))
                                .handicapIndex(handicapIndexMapper.map(rs))
                                .inscription(inscriptionMapper.map(rs))
                                .round(roundMapper.map(rs, club))
                                .build();
                        liste.add(ecl);
                    }
                } // rs fermé ici // rs fermé ici
            } // ps + conn fermés ici // ps + conn fermés ici

            if (liste.isEmpty()) {
                LOG.warn(methodName + " - empty result list for idplayer = "
                        + player.getIdplayer());
            } else {
                LOG.debug(methodName + " - list size = " + liste.size());
            }
            return liste;

        } catch (SQLException sqle) {
            handleSQLException(sqle, methodName);
            return Collections.emptyList();             // ✅ jamais null
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();             // ✅ jamais null
        }
    } // end method

    // ========================================
    // CACHE
    // ========================================

    public List<ECourseList> getListe() {
        return liste;
    }

    public void setListe(List<ECourseList> liste) {
        this.liste = liste;
    }

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
            Player player = new Player();
            player.setIdplayer(324713);
            List<ECourseList2> li = list(player);
            LOG.debug(methodName + " - HandicapIndexList = " + li.toString());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end main
    */

} // end class

/*
import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.Player;
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
import rowmappers.InscriptionRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.HandicapIndexRowMapper;
import connection_package.DBConnection;
import entite.HandicapIndex;
import entite.composite.ECourseList2;
import utils.LCUtil;

public class HandicapIndexList {
    private static List<ECourseList2> liste = null;
    
public List<ECourseList2> list(final Player player, final Connection conn) throws SQLException, Exception{  
if(liste == null){
    final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{

 final String query ="""
  WITH selection AS (
     SELECT * from handicap_index, player_has_round, player
     WHERE player.idplayer = ?
       AND handicap_index.HandicapPlayerId = player.idplayer
       AND player_has_round.InscriptionIdPlayer = player.idplayer
       AND player_has_round.InscriptionIdRound = handicap_index.HandicapRoundId
      )
  SELECT * FROM selection
     JOIN round
         ON round.idround = selection.HandicapRoundId
     JOIN course
          ON course.idcourse = round.course_idcourse
     JOIN club
          ON club.idclub = course.club_idclub
     ORDER BY selection.HandicapDate DESC
     LIMIT 30
 """;             
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     liste = new ArrayList<>();
     RowMapper<Club> clubMapper = new ClubRowMapper();
     RowMapper<Course> courseMapper = new CourseRowMapper();
     RowMapper<Inscription> inscriptionMapper = new InscriptionRowMapper();
     RowMapperRound<Round> roundMapper = new RoundRowMapper(); // accepte rs et 
     RowMapper<HandicapIndex> handicapIndexMapper = new HandicapIndexRowMapper();
     
         
     while(rs.next()){
	  //  ECourseList ecl = new ECourseList();
            Club club = clubMapper.map(rs);
            ECourseList2 ecl = ECourseList2.builder()
               // .club(clubMapper.map(rs))  ATTENTION donne erreur Unexpected exception in rowmappers.ClubRowMapper.map:
                    //Before start of result set java.sql.SQLException: Before start of result set
                .club(club)
                .course(courseMapper.map(rs))
                .handicapIndex(handicapIndexMapper.map(rs))
                .inscription(inscriptionMapper.map(rs))
                .round(roundMapper.map(rs,club))
            .build();
            
       //     ecl.setHandicapIndex(entite.HandicapIndex.map(rs));
         //   ecl.setInscription(inscriptionMapper.map(rs));
          //  ecl.setRound(roundMapper.map(rs, ecl.club()));
          //  ecl.setClub(clubMapper.map(rs)); // new 17-04-2025
          //  ecl.setCourse(courseMapper.map(rs));
	liste.add(ecl);
      }
     if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
  //       return liste;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
    return liste;
}catch (SQLException e){
    handleSQLException(e, methodName);
    return liste;
}catch (Exception e){
    handleGenericException(e, methodName); 
    return liste;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
   //  LOG.debug("escaped to handicaplist repetition with lazy loading");
    return liste;
}
} //end method

 public static List<ECourseList2> getListe() {
        return liste;
    }

 public static void setListe(List<ECourseList2> liste) {
        HandicapIndexList.liste = liste;
    }

 void main() throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    List<ECourseList2> li = new HandicapIndexList().list(player, conn);
        LOG.debug("HandicapIndexlist = " + li.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end class
*/