package read;

import entite.Classment;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import rowmappers.ClassmentRowMapper;
import rowmappers.RowMapper;
import java.sql.Connection;

@ApplicationScoped
public class ReadClassment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ReadClassment() { }

    // https://joshuaotwell.com/conditional-logic-with-sum-and-the-if-functions-in-mysql/
    public Classment read(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for player = " + player.getIdplayer());
        LOG.debug("for round = " + round.getIdround());
        LOG.debug("number of holes = " + round.getRoundHoles());
        LOG.debug("start = " + round.getRoundStart());

        String query = """
            SELECT
                 score.player_has_round_player_idplayer,
                 sum(score.ScoreExtraStroke) as TotalExtraStrokes,
                 sum(score.ScorePoints) as TotalScore,
                 sum(case WHEN score.ScoreHole > 9  THEN ScorePoints ELSE 0 end) as Last9,
                 sum(case WHEN score.ScoreHole > 12 THEN ScorePoints ELSE 0 end) as Last6,
                 sum(case WHEN score.ScoreHole > 15 THEN ScorePoints ELSE 0 end) as Last3,
                 sum(case WHEN score.ScoreHole > 17 THEN ScorePoints ELSE 0 end) as Last1
              FROM score
                 WHERE score.player_has_round_player_idplayer = ?
                 AND score.player_has_round_round_idround = ?;
            """;

        final String query9Holes = """
            SELECT
                 score.player_has_round_player_idplayer,
                 sum(score.ScoreExtraStroke) as TotalExtraStrokes,
                 sum(score.ScorePoints) as TotalScore,
                 sum( IF (score.ScoreHole > 0,ScorePoints,0)) as Last9,
                 sum( IF (score.ScoreHole > 3,ScorePoints,0)) as Last6,
                 sum( IF (score.ScoreHole > 6,ScorePoints,0)) as Last3,
                 sum( IF (score.ScoreHole > 8,ScorePoints,0)) as Last1
              FROM score
                 WHERE score.player_has_round_player_idplayer = ?
                 AND score.player_has_round_round_idround = ?;
            """;

        // new 13/04/2022
        if (round.getRoundHoles() == 9 && round.getRoundStart() == 1) {
            query = query9Holes;
            LOG.debug("query9Holes chosen!");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setInt(2, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<Classment> liste = new ArrayList<>();
                RowMapper<Classment> classmentMapper = new ClassmentRowMapper();
                while (rs.next()) {
                    liste.add(classmentMapper.map(rs));
                }
                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result list for player=" + player.getIdplayer()
                            + " round=" + round.getIdround());
                    return null;
                }
                LOG.debug(methodName + " - returning classment = " + liste.getFirst());
                return liste.getFirst();
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new ReadClassment().read(player, round, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #read(Player, Round)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(750);
        round.setRoundHoles(18);
        round.setRoundStart(0);
        Classment cl = read(player, round);
        LOG.debug("Classment = " + cl);
    } // end main
    */

} // end class
