package calc;

import entite.Player;
import entite.Round;
import entite.ScoreMatchplay;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Arrays;

@Named
@ApplicationScoped
public class CalculateScoreMatchplay implements Serializable {

    private static final long serialVersionUID = 1L;

    public CalculateScoreMatchplay() { }

    public ScoreMatchplay calc(final Player player, ScoreMatchplay score, final Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - with Round = " + round);
        LOG.debug(methodName + " - with Player = " + player);
        LOG.debug(methodName + " - with ScoreMatchplay = " + score);
        try {
            var v = result(score.getstrokesEur(), score.getstrokesUsa());
            score.setResult(v);
            return score;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return score;
        }
    } // end method

    public String[] result(int[] teamA, int[] teamB) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug(methodName + " - entering calc A " + Arrays.toString(teamA));
            LOG.debug(methodName + " - entering calc B " + Arrays.toString(teamB));
            int totA = 0;
            int totB = 0;
            int j = 0;
            int max = teamA.length;
            String[] result = {"", ""};
            int i = 0;
            for (i = 0; i < max; i++) {
                j = i;
                if (teamA[i] == 0 || teamB[i] == 0) {
                    break;
                }
                if (teamA[i] == teamB[i]) {
                    LOG.debug((i + 1) + " A/S");
                }
                if (teamA[i] < teamB[i]) {
                    totA++;
                    LOG.debug((i + 1) + " A < B donc A+1, score A = " + totA);
                }
                if (teamB[i] < teamA[i]) {
                    totB++;
                    LOG.debug((i + 1) + " B < A  donc B+1, score B = " + totB);
                }
            }
            LOG.debug(methodName + " - j = " + j);
            int holes = max - j;
            LOG.debug(methodName + " - holes still to play = " + holes);
            LOG.debug(methodName + " - holes still to play max-j+1= " + (max - j + 1));
            LOG.debug(methodName + " - total A = " + totA);
            LOG.debug(methodName + " - total B = " + totB);
            if (totA == totB) {
                result[0] = "HALVED";
                result[1] = "HALVED";
                return result;
            }
            if (totB > totA) {
                LOG.debug(methodName + " - case = B > A");
                int d = totB - totA;
                LOG.debug(methodName + " - B-A ou d = " + d);
                LOG.debug(methodName + " - i = " + i);
                if (i == 18 && d == 2) {
                    LOG.debug(methodName + " - hole 17 special case 1 for difference = " + d);
                    result[1] = "B-2Up";
                    return result;
                }
                if (i == 17 && d == 1) {
                    LOG.debug(methodName + " - hole 18 special case 2 for difference = " + d);
                    result[1] = "B-1Up";
                    return result;
                }
                if (d > holes) {
                    result[1] = "B-" + d + "&" + holes;
                    return result;
                } else {
                    result[1] = "B-" + d + "Up";
                    return result;
                }
            }
            if (totA > totB) {
                LOG.debug(methodName + " - case = A > B");
                int d = totA - totB;
                LOG.debug(methodName + " - A-B ou d = " + d);
                if (i == 18 && d == 2) {
                    LOG.debug(methodName + " - hole 17 special case 3 for difference = " + d);
                    result[0] = "A-2Up";
                    return result;
                }
                if (i == 17 && d == 1) {
                    LOG.debug(methodName + " - hole 17 2e special case 4 for difference = " + d);
                    result[0] = "A-1Up";
                    return result;
                }
                if (d > holes) {
                    result[0] = "A-" + d + "&" + holes;
                    return result;
                } else {
                    result[0] = "A-" + d + "Up";
                    return result;
                }
            }
            return result;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new String[]{"", ""};
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
