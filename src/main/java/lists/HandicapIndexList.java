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
 * fix multi-user 2026-03-07 — cache supprimé (données per-user dans singleton = fuite de données)
 */
@Named
@ApplicationScoped
public class HandicapIndexList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public HandicapIndexList() { }

    public List<ECourseList> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

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

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setInt(1, player.getIdplayer());
                LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {
                    List<ECourseList> result = new ArrayList<>();

                    RowMapper<Club>          clubMapper          = new ClubRowMapper();
                    RowMapper<Course>        courseMapper         = new CourseRowMapper();
                    RowMapper<Inscription>   inscriptionMapper   = new InscriptionRowMapper();
                    RowMapperRound<Round>    roundMapper         = new RoundRowMapper();
                    RowMapper<HandicapIndex> handicapIndexMapper = new HandicapIndexRowMapper();

                    while (rs.next()) {
                        Club club = clubMapper.map(rs);
                        ECourseList ecl = ECourseList.builder()
                                .club(club)
                                .course(courseMapper.map(rs))
                                .handicapIndex(handicapIndexMapper.map(rs))
                                .inscription(inscriptionMapper.map(rs))
                                .round(roundMapper.map(rs, club))
                                .build();
                        result.add(ecl);
                    }

                    if (result.isEmpty()) {
                        LOG.warn(methodName + " - empty result list for idplayer = " + player.getIdplayer());
                    } else {
                        LOG.debug(methodName + " - list size = " + result.size());
                    }
                    return result;
                }
            }

        } catch (SQLException sqle) {
            handleSQLException(sqle, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * No-op — cache removed (fix multi-user 2026-03-07)
     */
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - no-op (cache removed)");
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Player player = new Player();
            player.setIdplayer(324713);
            List<ECourseList> li = list(player);
            LOG.debug(methodName + " - HandicapIndexList = " + li.toString());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end main
    */

} // end class
