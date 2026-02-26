
package lists;

import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Tee;
import entite.composite.ECourseList;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.InscriptionRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.RoundRowMapper;
import rowmappers.ScoreStablefordRowMapper;
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

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;

/**
 * Liste des rounds joués pour un joueur
 * ✅ @ApplicationScoped — stateless, cache partagé
 * ✅ @Resource DataSource — plus de Connection en paramètre
 * ✅ try-with-resources — plus de finally/closeQuietly
 * ✅ Collections.emptyList() — jamais null
 * ✅ liste — champ d'instance (plus static)
 */
@Named
@ApplicationScoped
public class PlayedList implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ DataSource injecté — standard golflc
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
            LOG.debug(methodName + " - player   = " + player.toString());

            final String query = """
                    WITH selection AS (
                        SELECT * FROM player_has_round, round, player
                        WHERE player.idplayer = ?
                          AND player_has_round.InscriptionIdPlayer = player.idplayer
                          AND player_has_round.InscriptionIdRound = round.idround
                    )
                    SELECT * FROM selection
                        JOIN tee
                            ON tee.idtee = selection.InscriptionIdTee
                        JOIN course
                            ON course.idcourse = selection.course_idcourse
                        JOIN club
                            ON club.idclub = course.club_idclub
                        ORDER BY selection.RoundDate DESC
                        LIMIT 30
                    """;

            // ✅ try-with-resources — plus de finally/closeQuietly
            try (Connection        conn = dataSource.getConnection();
                 PreparedStatement ps   = conn.prepareStatement(query)) {

                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {

                    liste = new ArrayList<>();

                    RowMapper<Club>            clubMapper             = new ClubRowMapper();
                    RowMapper<Course>          courseMapper           = new CourseRowMapper();
                    RowMapper<Tee>             teeMapper              = new TeeRowMapper();
                    RowMapper<Inscription>     inscriptionMapper      = new InscriptionRowMapper();
                    RowMapperRound<Round>      roundMapper            = new RoundRowMapper();
                    RowMapper<ScoreStableford> scoreStablefordMapper  = new ScoreStablefordRowMapper();

                    while (rs.next()) {
                        Club club = clubMapper.map(rs);
                        ECourseList ecl = ECourseList.builder()
                                .club(club)
                                .course(courseMapper.map(rs))
                                .inscription(inscriptionMapper.map(rs))
                                .round(roundMapper.map(rs, club))
                                .tee(teeMapper.map(rs))
                                .scoreStableford(scoreStablefordMapper.map(rs))
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
            player.setIdplayer(324720);
            List<ECourseList2> ecl = list(player);
            for (ECourseList2 f : ecl) {
                if (f.round().getIdround() == 688) {
                    LOG.debug(methodName + " - found = " + f);
                }
            }
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
import rowmappers.InscriptionRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.TeeRowMapper;
import rowmappers.ScoreStablefordRowMapper;
import connection_package.DBConnection;
import entite.ScoreStableford;
import entite.composite.ECourseList2;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static utils.LCUtil.showMessageFatal;

public class PlayedList {
     private static List<ECourseList2> liste = null;
    
public List<ECourseList2> list(final Player player, final Connection conn) throws SQLException, Exception{

    final String methodName = utils.LCUtil.getCurrentMethodName(); 
if(liste == null){
        LOG.debug("starting PlayedList(), Player = {}", player.getIdplayer());
        LOG.debug("with player = " + player.toString());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{

 String query = """
WITH selection AS (
      SELECT * from player_has_round, round, player
      WHERE player.idplayer = ?
        AND player_has_round.InscriptionIdPlayer = player.idplayer
        AND player_has_round.InscriptionIdRound = round.idround
     )
 SELECT * FROM selection
    JOIN tee
        ON tee.idtee = selection.InscriptionIdTee
    JOIN course
        ON course.idcourse = selection.course_idcourse
    JOIN club
        ON club.idclub = course.club_idclub
    ORDER BY selection.RoundDate DESC
    LIMIT 30;
""";
 
//154 2024-01-19T09:23:11,223 215614  ERROR lists.PlayedList . list 75 :
//SQL Exception in getPlayedList() = java.sql.SQLSyntaxErrorException: Expression #45 of 
//SELECT list is not in GROU P BY clause and contains nonaggregated column 'golflc.tee.TeeGender' which is not functionally dependent on columns in GROU P BY clause;
//this is incompatible with sql_mode=only_full_group_by, SQLState = 42000, ErrorCode = 1055 

        ps = conn.prepareStatement(query);
        ps.setInt(1, player.getIdplayer());
        utils.LCUtil.logps(ps);
	rs =  ps.executeQuery();
        liste = new ArrayList<>();
        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapper<Tee> teeMapper = new TeeRowMapper();
        RowMapper<Inscription> inscriptionMapper = new InscriptionRowMapper();
        RowMapperRound<Round> roundMapper = new RoundRowMapper();
        RowMapper<ScoreStableford> scoreStablefordMapper = new ScoreStablefordRowMapper();
	while(rs.next()){
       //   ECourseList ecl = new ECourseList();
                Club club = clubMapper.map(rs);
                ECourseList2 ecl = ECourseList2.builder()
                    .club(club)
                    .course(courseMapper.map(rs))
                 //   .handicapIndex(handicapIndexMapper.map(rs))
                    .inscription(inscriptionMapper.map(rs))
                    .round(roundMapper.map(rs,club))
                    .tee(teeMapper.map(rs))
                    .scoreStableford(scoreStablefordMapper.map(rs))
                .build();
          
       //   ecl.setClub(clubMapper.map(rs));
        //  ecl.setCourse(courseMapper.map(rs));
       //   ecl.setRound(new entite.Round().dtoMapper(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
       //   ecl.setRound(roundMapper.map(rs, ecl.getClub()));
      //    ecl.setTee(teeMapper.map(rs));
        //  ecl.setInscription(inscriptionMapper.map(rs));
      //    ecl.setScoreStableford(entite.ScoreStableford.map(rs));
	liste.add(ecl);
	} //end while
     if(liste.isEmpty()){
         String msg = "££ Empty Result Table in " + methodName;
         LOG.error(msg);
         showMessageFatal(msg);
  //       return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
     //     liste.forEach(item -> LOG.debug("PlayedList " + item));  // java 8 lambda
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
   //      LOG.debug("escaped to listPlayed repetition with lazy loading");
    return liste;  //plusieurs fois ??
}

} //end method

    public static List<ECourseList2> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList2> liste) {
        PlayedList.liste = liste;
    }
    
    void main() throws SQLException, Exception {
      Connection conn = new DBConnection().getConnection(); 
  try{
    // player n'est plus utilisé au 19-09-2021 pour enregistrement des résultats de flights dans lequel LC ne se trouve pas
    Player player = new Player();
    player.setIdplayer(324720);
 //   player = new load.LoadPlayer().load(player, conn);
    List<ECourseList2> ecl = new PlayedList().list(player, conn);
    for(ECourseList2 f : ecl) {
        if(f.round().getIdround() == 688) {
            LOG.debug(NEW_LINE + "Main ecl found = " + f); //.toString());
   //         LOG.debug("Round found = " + customer.getRound()); //.toString());
        }
        
    }
 //       LOG.debug("from main, after lp = " + lp);
   }catch (Exception e){
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end Class
*/