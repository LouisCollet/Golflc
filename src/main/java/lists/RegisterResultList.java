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

@Named
@ApplicationScoped
public class RegisterResultList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    private List<ECourseList> liste = null;

    public RegisterResultList() { }

    public List<ECourseList> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - with player = " + player);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

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
                LIMIT 30
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Club> clubMapper = new ClubRowMapper();
                RowMapper<Course> courseMapper = new CourseRowMapper();
                RowMapper<Tee> teeMapper = new TeeRowMapper();
                RowMapper<Player> playerMapper = new PlayerRowMapper();
                RowMapperRound<Round> roundMapper = new RoundRowMapper();
                while (rs.next()) {
                    Club club = clubMapper.map(rs);
                    ECourseList ecl = ECourseList.builder()
                        .club(club)
                        .course(courseMapper.map(rs))
                        .player(playerMapper.map(rs))
                        .round(roundMapper.map(rs, club))
                        .tee(teeMapper.map(rs))
                        .build();
                    liste.add(ecl);
                }
                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result list");
                } else {
                    LOG.debug(methodName + " - list size = " + liste.size());
                    liste.forEach(item -> LOG.debug(methodName + " - " + item));
                }
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

    public List<ECourseList> getListe()                          { return liste; }
    public void              setListe(List<ECourseList> liste)   { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // Player player = new Player();
        // player.setIdplayer(324715);
        // var ecl = list(player);
        // LOG.debug("from main, ecl = " + ecl.size());
        LOG.debug("from main, RegisterResultList = ");
    } // end main
    */

} // end class
