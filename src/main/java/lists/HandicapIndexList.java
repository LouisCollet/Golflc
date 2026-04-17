package lists;

import entite.Club;
import entite.Course;
import entite.HandicapIndex;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.composite.ECourseList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.HandicapIndexRowMapper;
import rowmappers.InscriptionRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.RoundRowMapper;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;

/**
 * Liste des HandicapIndex pour un joueur
 * fix multi-user 2026-03-07 — cache supprime (donnees per-user dans singleton = fuite de donnees)
 * Migre vers GenericDAO (2026-03-18)
 */
@Named
@ApplicationScoped
public class HandicapIndexList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public HandicapIndexList() { }

    public List<ECourseList> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try {
            LOG.debug(methodName + " - idplayer = " + player.getIdplayer());

            final String query = """
                    WITH selection AS (
                        SELECT * FROM player
                            INNER JOIN handicap_index
                                ON handicap_index.HandicapPlayerId = player.idplayer
                            INNER JOIN player_has_round
                                ON player_has_round.InscriptionIdPlayer = player.idplayer
                               AND player_has_round.InscriptionIdRound = handicap_index.HandicapRoundId
                        WHERE player.idplayer = ?
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

            RowMapper<Club>          clubMapper          = new ClubRowMapper();
            RowMapper<Course>        courseMapper         = new CourseRowMapper();
            RowMapper<Inscription>   inscriptionMapper   = new InscriptionRowMapper();
            RowMapperRound<Round>    roundMapper         = new RoundRowMapper();
            RowMapper<HandicapIndex> handicapIndexMapper = new HandicapIndexRowMapper();

            List<ECourseList> result = dao.queryList(query, rs -> {
                Club club = clubMapper.map(rs);
                return ECourseList.builder()
                        .club(club)
                        .course(courseMapper.map(rs))
                        .handicapIndex(handicapIndexMapper.map(rs))
                        .inscription(inscriptionMapper.map(rs))
                        .round(roundMapper.map(rs, club))
                        .build();
            }, player.getIdplayer());

            if (result.isEmpty()) {
                LOG.warn(methodName + " - empty result list for idplayer = " + player.getIdplayer());
            } else {
                LOG.debug(methodName + " - list size = " + result.size());
            }
            return result;

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
        LOG.debug("entering {}", methodName);
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
