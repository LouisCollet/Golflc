package lists;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import entite.Tee;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.PlayerRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.TeeRowMapper;

/**
 * fix multi-user 2026-03-07 — cache supprimé (données per-user dans singleton = fuite de données)
 */
@Named
@ApplicationScoped
public class RecentRoundList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public RecentRoundList() { }

    public List<ECourseList> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - player = " + player);

        final String query = """
            WITH selection AS (
                SELECT * FROM player
                    INNER JOIN player_has_round
                        ON player_has_round.InscriptionIdPlayer = player.idplayer
                    INNER JOIN round
                        ON round.idround = player_has_round.InscriptionIdRound
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

        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapper<Tee> teeMapper = new TeeRowMapper();
        RowMapperRound<Round> roundMapper = new RoundRowMapper();
        RowMapper<Player> playerMapper = new PlayerRowMapper();

        List<ECourseList> result = dao.queryList(query, rs -> {
            Club club = clubMapper.map(rs);
            return ECourseList.builder()
                    .club(club)
                    .course(courseMapper.map(rs))
                    .player(playerMapper.map(rs))
                    .round(roundMapper.map(rs, club))
                    .tee(teeMapper.map(rs))
                    .build();
        }, player.getIdplayer());

        if (result.isEmpty()) {
            LOG.warn(methodName + " - empty result list for player=" + player.getIdplayer());
        } else {
            LOG.debug(methodName + " - list size = " + result.size());
        }
        return result;
    } // end method

    /**
     * No-op — cache removed (fix multi-user 2026-03-07)
     */
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - no-op (cache removed)");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        List<ECourseList> ec = list(player);
        LOG.debug("from main, ec = " + ec.size());
    } // end main
    */

} // end class
