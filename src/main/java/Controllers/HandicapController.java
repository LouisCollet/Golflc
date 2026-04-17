
package Controllers;

import entite.HandicapIndex;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;

@ApplicationScoped
public class HandicapController implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject
    private update.UpdateHandicapIndex updateHandicapIndex;   // migrated 2026-02-24

    @Inject
    private create.CreateOrModifyHandicapIndex createOrModifyHandicapIndex;    // migrated 2026-02-24
    @Inject
    private calc.CalculateHandicapIndex calculateHandicapIndex;                // migrated 2026-02-24
    @Inject
    private create.CreateHandicapIndex createHandicapIndex;                    // migrated 2026-02-24

    public HandicapController() { }

    // mod 18-04-2025
    public HandicapIndex create(final ScoreStableford scoreStableford, final Player player, final Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            HandicapIndex handicapIndex = new HandicapIndex();
            handicapIndex.setHandicapScoreDifferential(BigDecimal.valueOf(scoreStableford.getScoreDifferential()));
            handicapIndex.setHandicapPlayerId(player.getIdplayer());
            handicapIndex.setHandicapRoundId(round.getIdround());
            handicapIndex.setHandicapDate(round.getRoundDate());
            handicapIndex.setHandicapPlayedStrokes((short) scoreStableford.getTotalStrokes());
            handicapIndex.setHandicapHolesNotPlayed((short) scoreStableford.getHolesNotPlayed());
            handicapIndex.setHandicapExpectedSD9Holes(scoreStableford.getExpectedSD9Holes());
            LOG.debug("for HandicapIndex completed = {}", handicapIndex);

            int handicapId = createOrModifyHandicapIndex.status(handicapIndex);
            if (handicapId == 0) {
                handicapIndex = createHandicapIndex.create(handicapIndex);
                LOG.debug("handicapIndex created = {}", handicapIndex);
                LOG.debug("key for later modify and insert HandicapWHS = {}", handicapIndex.getHandicapId());
            } else {
                handicapIndex.setHandicapId(handicapId);
                LOG.debug("HandicapIndex existe déjà - modification de score = {}", handicapIndex);
            }

            handicapIndex = calculateHandicapIndex.calc(handicapIndex);
            if (handicapIndex == null) {
                LOG.debug("after calculatedHandicapWHS, handicapIndex = null : {}", handicapIndex);
                return handicapIndex;
            }
            LOG.debug("after calculatedHandicapWHS, handicapIndex = {}", handicapIndex);
            LOG.debug("HandicapIndex before modification = {}", handicapIndex);

            if (updateHandicapIndex.update(handicapIndex)) {
                LOG.debug("HandicapIndex after modification = {}", handicapIndex);
                LOG.debug("status execution modifyHandicapIndex = OK");
            } else {
                LOG.debug("status execution modifyHandicapIndex = NOT OK - null returned");
            }
            return handicapIndex;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(437);
    } // end main
    */

} // end class
