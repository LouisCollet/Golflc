package lists;

import entite.Club;
import entite.Hole;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.HoleRowMapper;
import rowmappers.InscriptionRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.ScoreStablefordRowMapper;

import static exceptions.LCException.handleGenericException;

@Named
@ApplicationScoped
public class ScoreCardList3 implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // fix multi-user 2026-03-19 — cache supprimé (données per-user dans singleton = fuite de données)

    public ScoreCardList3() { }

    public List<ECourseList> list(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
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
              JOIN inscription
                ON  InscriptionIdPlayer = player.idplayer
                AND InscriptionIdRound = round.idround
              JOIN tee
                ON course.idcourse = tee.course_idcourse
                AND inscription.InscriptionIdTee = tee.idtee
                AND tee.TeeGender = player.PlayerGender
              JOIN hole
                ON hole.tee_idtee = tee.TeeMasterTee
                AND hole.tee_course_idcourse = course.idcourse
                AND Hole.HoleNumber between roundstart and roundstart + roundholes - 1
              JOIN score
                ON score.inscription_player_idplayer = player.idplayer
                AND score.inscription_round_idround = round.idround
                AND hole.HoleNumber = score.ScoreHole
              ORDER by hole.HoleNumber
            """;

        try {
            RowMapper<Inscription> inscriptionMapper = new InscriptionRowMapper();
            RowMapper<Hole> holeMapper = new HoleRowMapper();
            RowMapper<ScoreStableford> scoreStablefordMapper = new ScoreStablefordRowMapper();
            RowMapperRound<Round> roundMapper = new RoundRowMapper();
            RowMapper<Club> clubMapper = new ClubRowMapper();
            Club club = new Club(); // default empty club for roundMapper

            List<ECourseList> result = dao.queryList(query, rs -> ECourseList.builder()
                    .inscription(inscriptionMapper.map(rs))
                    .round(roundMapper.map(rs, club))
                    .hole(holeMapper.map(rs))
                    .scoreStableford(scoreStablefordMapper.map(rs))
                    .build(),
                    player.getIdplayer(), round.getIdround());

            if (result.isEmpty()) {
                LOG.warn(methodName + " - empty result list");
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
     * No-op — cache removed (fix multi-user 2026-03-19)
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
        Round round = new Round();
        round.setIdround(636);
        // var v = list(player, round);
        // v.forEach(item -> LOG.debug("list of items =" + item));
        LOG.debug("from main, ScoreCardList3 = ");
    } // end main
    */

} // end class
