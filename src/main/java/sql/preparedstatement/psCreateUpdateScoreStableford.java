package sql.preparedstatement;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class psCreateUpdateScoreStableford implements Serializable, interfaces.Log, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    /**
     * INSERT score — all 15 columns, fairway/green/putts/bunker/penalty set to 0 on creation.
     */
    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final ScoreStableford.Score sco,
            final Player player,
            final Round round) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setNull     (1,  java.sql.Types.INTEGER);         // AUTO-INCREMENT
            ps.setInt      (2,  sco.getHole());                  // ScoreHole
            ps.setInt      (3,  sco.getStrokes());               // ScoreStrokes
            ps.setInt      (4,  sco.getExtra());                 // ScoreExtra
            ps.setInt      (5,  sco.getPoints());                // ScorePoints
            ps.setInt      (6,  sco.getPar());                   // ScorePar
            ps.setInt      (7,  sco.getIndex());                 // ScoreIndex
            ps.setInt      (8,  0);                              // ScoreFairway
            ps.setInt      (9,  0);                              // ScoreGreen
            ps.setInt      (10, 0);                              // ScorePutts
            ps.setInt      (11, 0);                              // ScoreBunker
            ps.setInt      (12, 0);                              // ScorePenalty
            ps.setInt      (13, player.getIdplayer());           // ScoreIdPlayer
            ps.setInt      (14, round.getIdround());             // ScoreIdRound
            ps.setTimestamp(15, Timestamp.from(Instant.now())); // ScoreModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * UPDATE score SET ScoreStroke=?, ScorePoints=?, ScoreExtraStroke=?
     *            WHERE ScoreHole=? AND inscription_player_idplayer=? AND inscription_round_idround=?
     */
    public static PreparedStatement psMapUpdate(
            PreparedStatement ps,
            final ScoreStableford.Score sco,
            final Player player,
            final Round round) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setInt(1, sco.getStrokes());
            ps.setInt(2, sco.getPoints());
            ps.setInt(3, sco.getExtra());
            ps.setInt(4, sco.getHole());
            ps.setInt(5, player.getIdplayer());
            ps.setInt(6, round.getIdround());
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method
} // end class
