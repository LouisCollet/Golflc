
package lists;

import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Tee;
import entite.composite.ECourseList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.InscriptionRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.RoundRowMapper;
import rowmappers.ScoreStablefordRowMapper;
import rowmappers.TeeRowMapper;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;

/**
 * Liste des rounds joues pour un joueur
 * fix multi-user 2026-03-07 — cache supprime (donnees per-user dans singleton = fuite de donnees)
 * Migre vers GenericDAO (2026-03-18)
 */
@Named
@ApplicationScoped
public class PlayedList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public PlayedList() { }

    public List<ECourseList> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try {
            LOG.debug(methodName + " - idplayer = " + player.getIdplayer());

            final String query = """
                    WITH selection AS (
                        SELECT * FROM player
                            INNER JOIN inscription
                                ON inscription.InscriptionIdPlayer = player.idplayer
                            INNER JOIN round
                                ON round.idround = inscription.InscriptionIdRound
                        WHERE player.idplayer = ?
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

            RowMapper<Club>            clubMapper            = new ClubRowMapper();
            RowMapper<Course>          courseMapper           = new CourseRowMapper();
            RowMapper<Tee>             teeMapper             = new TeeRowMapper();
            RowMapper<Inscription>     inscriptionMapper     = new InscriptionRowMapper();
            RowMapperRound<Round>      roundMapper           = new RoundRowMapper();
            RowMapper<ScoreStableford> scoreStablefordMapper = new ScoreStablefordRowMapper();

            List<ECourseList> result = dao.queryList(query, rs -> {
                Club club = clubMapper.map(rs);
                return ECourseList.builder()
                        .club(club)
                        .course(courseMapper.map(rs))
                        .inscription(inscriptionMapper.map(rs))
                        .round(roundMapper.map(rs, club))
                        .tee(teeMapper.map(rs))
                        .scoreStableford(scoreStablefordMapper.map(rs))
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
     * Kept for backward compatibility with callers
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
            player.setIdplayer(324720);
            List<ECourseList> ecl = list(player);
            for (ECourseList f : ecl) {
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
