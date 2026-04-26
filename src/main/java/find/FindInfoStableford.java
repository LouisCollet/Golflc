package find;

import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.Tee;
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
import java.util.ArrayList;
import java.util.List;
import utils.LCUtil;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.InscriptionRowMapper;
import rowmappers.PlayerRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.TeeRowMapper;

@ApplicationScoped
public class FindInfoStableford implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    public FindInfoStableford() { }

    public ECourseList find(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (liste != null) {
            LOG.debug(methodName + " - escaped thanks to lazy loading");
            return liste.get(0);
        }

        LOG.debug(methodName + " - for player = " + player);
        LOG.debug(methodName + " - for round = " + round);

        final String query = """
            SELECT *
            FROM round
            JOIN player
               ON player.idplayer = ?
            JOIN course
             ON round.course_idcourse = course.idcourse
             AND round.idround = ?
            JOIN club
             ON club.idclub = course.club_idclub
            JOIN inscription
             ON InscriptionIdRound = round.idround
             AND InscriptionIdPlayer = player.idplayer
            JOIN tee
             ON tee.course_idcourse = course.idcourse
             AND inscription.InscriptionIdTee = tee.idtee
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setInt(2, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();

                RowMapper<Club> clubMapper = new ClubRowMapper();
                RowMapper<Course> courseMapper = new CourseRowMapper();
                RowMapper<Tee> teeMapper = new TeeRowMapper();
                RowMapper<Player> playerMapper = new PlayerRowMapper();
                RowMapper<Inscription> inscriptionMapper = new InscriptionRowMapper();
                RowMapperRound<Round> roundMapper = new RoundRowMapper();

                if (rs.next()) {
                    Club club = clubMapper.map(rs);
                    ECourseList ecl = ECourseList.builder()
                            .club(club)
                            .course(courseMapper.map(rs))
                            .player(playerMapper.map(rs))
                            .inscription(inscriptionMapper.map(rs))
                            .round(roundMapper.map(rs, club))
                            .tee(teeMapper.map(rs))
                            .build();
                    liste.add(ecl);
                }

                if (liste.isEmpty()) {
                    String msg = "Empty result in " + methodName + " for player = " + player.getIdplayer();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return null;
                }

                if (liste.size() > 1) {
                    String msg = "Result > 1 = " + liste.size() + " in " + methodName
                            + " for player = " + player.getIdplayer();
                    LOG.error(msg);
                }

                LOG.debug(methodName + " - ResultSet has " + liste.size() + " lines");
                ECourseList ecl = liste.get(0);
                LOG.debug(methodName + " - exiting with ECourseList = " + ecl);
                return ecl;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()                 { return liste; }
    public void setListe(List<ECourseList> liste)       { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(636);
        // ECourseList ecl = find(player, round);
        // LOG.debug("main - after res = " + ecl);
        LOG.debug("from main, FindInfoStableford = ");
    } // end main
    */

} // end class
