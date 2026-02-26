package lists;

import entite.Club;
import entite.Hole;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
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
import rowmappers.HoleRowMapper;
import rowmappers.InscriptionRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.ScoreStablefordRowMapper;

@Named
@ApplicationScoped
public class ScoreCardList3 implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    public ScoreCardList3() { }

    public List<ECourseList> list(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            liste.forEach(item -> LOG.debug("hole = " + item.scoreStableford().getScoreHole()
                    + " / points = " + item.scoreStableford().getScorePoints()));
            LOG.debug("total points = " + liste.stream()
                    .mapToInt(o -> o.scoreStableford().getScorePoints())
                    .sum());
            return liste;
        }

        LOG.debug(methodName + " with Player = " + player);
        LOG.debug(methodName + " with Round = " + round);

        final String query = """
            SELECT *
             FROM course
             JOIN player
                 ON player.idplayer = ?
             JOIN round
                 ON round.idround = ?
                 AND round.course_idcourse = course.idcourse
              JOIN player_has_round
                ON  InscriptionIdPlayer = player.idplayer
                AND InscriptionIdRound = round.idround
              JOIN tee
                ON course.idcourse = tee.course_idcourse
                AND player_has_round.InscriptionIdTee = tee.idtee
                AND tee.TeeGender = player.PlayerGender
              JOIN hole
                ON hole.tee_idtee = tee.TeeMasterTee
                AND hole.tee_course_idcourse = course.idcourse
                AND Hole.HoleNumber between roundstart and roundstart + roundholes - 1
              JOIN score
                ON score.player_has_round_player_idplayer = player.idplayer
                AND score.player_has_round_round_idround = round.idround
                AND hole.HoleNumber = score.ScoreHole
              ORDER by hole.HoleNumber
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setInt(2, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Inscription> inscriptionMapper = new InscriptionRowMapper();
                RowMapper<Hole> holeMapper = new HoleRowMapper();
                RowMapper<ScoreStableford> scoreStablefordMapper = new ScoreStablefordRowMapper();
                RowMapperRound<Round> roundMapper = new RoundRowMapper();
                RowMapper<Club> clubMapper = new ClubRowMapper();
                Club club = new Club();
                club = clubMapper.map(rs); // ne va rien donner ?
                while (rs.next()) {
                    ECourseList ecl = ECourseList.builder()
                            .inscription(inscriptionMapper.map(rs))
                            .round(roundMapper.map(rs, club))
                            .hole(holeMapper.map(rs))
                            .scoreStableford(scoreStablefordMapper.map(rs))
                            .build();
                    liste.add(ecl);
                }
            }

            if (liste.isEmpty()) {
                LOG.warn(methodName + " - empty result list");
            } else {
                LOG.debug(methodName + " - list size = " + liste.size());
            }
            return liste;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()                 { return liste; }
    public void setListe(List<ECourseList> liste)       { this.liste = liste; }

    // ✅ Invalidation explicite
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
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(636);
        // var v = list(player, round);
        // v.forEach(item -> LOG.debug("list of items =" + item));
        LOG.debug("from main, ScoreCardList3 = ");
    } // end main
    */

} // end class
