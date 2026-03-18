package lists;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import entite.Tee;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
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

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public RecentRoundList() { }

    public List<ECourseList> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - player = " + player);

        final String query = """
            WITH selection AS (
                SELECT * FROM player, player_has_round, round
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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<ECourseList> result = new ArrayList<>();
                RowMapper<Club> clubMapper = new ClubRowMapper();
                RowMapper<Course> courseMapper = new CourseRowMapper();
                RowMapper<Tee> teeMapper = new TeeRowMapper();
                RowMapperRound<Round> roundMapper = new RoundRowMapper();
                RowMapper<Player> playerMapper = new PlayerRowMapper();
                while (rs.next()) {
                    Club club = clubMapper.map(rs);
                    ECourseList ecl = ECourseList.builder()
                            .club(club)
                            .course(courseMapper.map(rs))
                            .player(playerMapper.map(rs))
                            .round(roundMapper.map(rs, club))
                            .tee(teeMapper.map(rs))
                            .build();
                    result.add(ecl);
                }
                if (result.isEmpty()) {
                    LOG.warn(methodName + " - empty result list for player=" + player.getIdplayer());
                } else {
                    LOG.debug(methodName + " - list size = " + result.size());
                }
                return result;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
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
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        List<ECourseList> ec = list(player);
        LOG.debug("from main, ec = " + ec.size());
    } // end main
    */

} // end class
