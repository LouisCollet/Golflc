package read;

import entite.Classment;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import rowmappers.ClassmentRowMapper;
import rowmappers.RowMapper;

@ApplicationScoped
public class ReadClassment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadClassment() { }

    // https://joshuaotwell.com/conditional-logic-with-sum-and-the-if-functions-in-mysql/
    public Classment read(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for player = {}", player.getIdplayer());
        LOG.debug("for round = {}", round.getIdround());
        LOG.debug("number of holes = {}", round.getRoundHoles());
        LOG.debug("start = {}", round.getRoundStart());

        final String query18Holes = """
            SELECT
                 sum(score.ScoreExtraStroke) as TotalExtraStrokes,
                 sum(score.ScorePoints) as TotalScore,
                 sum(case WHEN score.ScoreHole >= 10 THEN score.ScorePoints ELSE 0 end) as Last9,
                 sum(case WHEN score.ScoreHole >= 13 THEN score.ScorePoints ELSE 0 end) as Last6,
                 sum(case WHEN score.ScoreHole >= 16 THEN score.ScorePoints ELSE 0 end) as Last3,
                 sum(case WHEN score.ScoreHole >= 18 THEN score.ScorePoints ELSE 0 end) as Last1
              FROM score
              WHERE score.player_has_round_player_idplayer = ?
                AND score.player_has_round_round_idround = ?
              GROUP BY score.player_has_round_player_idplayer
            """;

        // new 13/04/2022
        final String query9Holes = """
            SELECT
                 sum(score.ScoreExtraStroke) as TotalExtraStrokes,
                 sum(score.ScorePoints) as TotalScore,
                 sum(case WHEN score.ScoreHole >= 1 THEN score.ScorePoints ELSE 0 end) as Last9,
                 sum(case WHEN score.ScoreHole >= 4 THEN score.ScorePoints ELSE 0 end) as Last6,
                 sum(case WHEN score.ScoreHole >= 7 THEN score.ScorePoints ELSE 0 end) as Last3,
                 sum(case WHEN score.ScoreHole >= 9 THEN score.ScorePoints ELSE 0 end) as Last1
              FROM score
              WHERE score.player_has_round_player_idplayer = ?
                AND score.player_has_round_round_idround = ?
              GROUP BY score.player_has_round_player_idplayer
            """;

        final boolean is9HolesFromStart = round.getRoundHoles() == 9 && round.getRoundStart() == 1;
        final String query = is9HolesFromStart ? query9Holes : query18Holes;
        LOG.debug("query chosen: {}", is9HolesFromStart ? "9holes" : "18holes");

        Classment result = dao.querySingle(query, new ClassmentRowMapper(), player.getIdplayer(), round.getIdround());
        if (result == null) {
            LOG.warn("empty result list for player={} round={} — returning empty Classment", player.getIdplayer(), round.getIdround());
            return new Classment(); // empty classment — all fields null → sort treats as 0
        }
        LOG.debug("returning classment = {}", result);
        return result;
    } // end method

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new ReadClassment().read(player, round, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #read(Player, Round)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(750);
        round.setRoundHoles(18);
        round.setRoundStart(0);
        Classment cl = read(player, round);
        LOG.debug("Classment = {}", cl);
    } // end main
    */

} // end class
