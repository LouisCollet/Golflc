package calc;

import Controllers.UtilsController;
import entite.PlayingHandicap;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;

@ApplicationScoped
public class CalcScramblePlayingHandicap implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private UtilsController utilsController;
 public CalcScramblePlayingHandicap() { }

    public int getScramblePlayingHcp(final PlayingHandicap playingHcp, final int players) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - with PlayingHcp = " + playingHcp.toString());
        LOG.debug(methodName + " - with number of players = " + players);
        try {
            double sum = utilsController.getSum(playingHcp);
            LOG.debug(methodName + " - sum = " + sum);
            PlayingHandicap ph = new PlayingHandicap();
            if (players == 0) {
                ph.setPlayingHandicap(0);
            }
            if (players == 2) {
                ph.setPlayingHandicap((int) (sum * .25));
            }
            if (players == 3) {
                ph.setPlayingHandicap((int) (sum * .20));
            }
            if (players == 4) {
                ph.setPlayingHandicap((int) (sum * .10));
            }
            LOG.debug(methodName + " - playing handicap calculated = " + ph.getPlayingHandicap());
            return ph.getPlayingHandicap(); // fix 2026-03-28 — was: playingHcp.getPlayingHandicap() (always returned original, ph was discarded)
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return 0;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
