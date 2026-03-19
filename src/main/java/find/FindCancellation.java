package find;

import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.UnavailablePeriod;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.InscriptionRowMapper;
import rowmappers.PlayerRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;

@ApplicationScoped
public class FindCancellation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindCancellation() { }

    public List<ECourseList> find(final UnavailablePeriod unavailable, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        try (Connection conn = dao.getConnection()) {

            String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
            String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
            String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
            String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
            String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");

            final String query =
                "SELECT " + cl + "," + co + "," + ro + "," + pl + "," + ph +
                "     FROM player" +
                "     JOIN player_has_round" +
                "         ON InscriptionIdPlayer = player.idplayer" +
                "     AND player_has_round.InscriptionFinalResult = 0" +
                "      JOIN round" +
                "         ON InscriptionIdRound = round.idround" +
                "           AND DATE(round.RoundDate) >= DATE(?) " +
                "           AND DATE(round.RoundDate) <= DATE(?)" +
                "           AND DATE(round.RoundDate) > NOW()" +
                "      JOIN course" +
                "         ON course.idcourse = round.course_idcourse" +
                "      JOIN club" +
                "       ON club.idclub = course.club_idclub" +
                "      ORDER by date(RoundDate) DESC";

            try (PreparedStatement ps = conn.prepareStatement(query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

                ps.setTimestamp(1, Timestamp.valueOf(unavailable.getStartDate()));
                ps.setTimestamp(2, Timestamp.valueOf(unavailable.getEndDate()));
                utils.LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.last();
                    LOG.debug("ResultSet FindCancellation has " + rs.getRow() + " lines.");
                    rs.beforeFirst();

                    List<ECourseList> liste = new ArrayList<>();
                    RowMapper<Club> clubMapper = new ClubRowMapper();
                    RowMapper<Course> courseMapper = new CourseRowMapper();
                    RowMapper<Player> playerMapper = new PlayerRowMapper();
                    RowMapper<Inscription> inscriptionMapper = new InscriptionRowMapper();
                    RowMapperRound<Round> roundMapper = new RoundRowMapper();

                    while (rs.next()) {
                        Club club = clubMapper.map(rs);
                        ECourseList ecl = ECourseList.builder()
                                .club(club)
                                .course(courseMapper.map(rs))
                                .player(playerMapper.map(rs))
                                .inscription(inscriptionMapper.map(rs))
                                .round(roundMapper.map(rs, club))
                                .build();
                        liste.add(ecl);
                    }

                    liste.forEach(item -> LOG.debug("Cancellation list " + item + "/"));
                    return liste;
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

/*
void main() throws SQLException, Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    // tests locaux
} // end main
*/

} // end class
