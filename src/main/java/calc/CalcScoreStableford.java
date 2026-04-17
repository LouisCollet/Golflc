
package calc;

import Controllers.LoggingUserController;
import Controllers.StablefordController;
import entite.Course;
import entite.LoggingUser;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Calcul des scores Stableford.
 * Version CDI-compliant. le 16/01/2026
 */
@ApplicationScoped  // changed from @RequestScoped — no mutable state 2026-02-26
public class CalcScoreStableford implements interfaces.GolfInterface, Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CLASSNAME = utils.LCUtil.getCurrentClassName();

    // ========================================
    // INJECTIONS CDI — migrated 2026-02-25
    // ========================================
    @Inject private StablefordController stablefordController;
    @Inject private read.ReadParAndStrokeIndex readParAndStrokeIndex;
    @Inject private calc.CalcStablefordCourseHandicap calcCourseHandicap;
    @Inject private calc.CalcStablefordPlayingHandicap calcPlayingHandicap;
    @Inject private calc.CalcStablefordScoreDifferential calcScoreDifferential;
    @Inject private Controllers.LoggingUserController loggingUserController;

    public CalcScoreStableford() { }

    public ScoreStableford calc(
            final Player player,
            ScoreStableford score,
            final Round round,
            final Course course,
            final Tee tee) {

        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(" with ScoreStableford = " + score);
        LOG.debug(" with Round = " + round);
        LOG.debug(" with Player = " + player);

        try {
            LoggingUserController.write(CLASSNAME + "." + methodName,"i");
            LoggingUserController.write(LocalDateTime.now().format(ZDF_TIME), "i");
            LoggingUserController.write(player.getPlayerFirstName() + " - " + player.getPlayerLastName(), "i");
            LoggingUserController.write(round.getRoundName() + " - " + round.getRoundDate().format(ZDF_TIME), "t");

            int[] arr = score.getScoreList().stream().mapToInt(i -> i.getStrokes()).toArray();
            LOG.debug("arr from score = " + Arrays.toString(arr));

            if (utils.LCUtil.isArrayAllZeroes(arr)) {
                String msg = "scoreArray is all zeroes= " + Arrays.toString(arr);
                LOG.error(msg);
                showMessageFatal(msg);
            } else {
                String msg = "scoreArray is NOT all zeroes= " + Arrays.toString(arr);
                LOG.info(msg);
            }

            if (utils.LCUtil.containsZero(arr)) {
                String msg = "There is (at least...) one result = 0 - correction needed !!";
                LOG.error(msg);
                showMessageFatal(msg);
                return score;
            }

            int[] a = new int[9];
            if (round.getRoundStart() == 10) {
                arr = IntStream.concat(IntStream.of(a), IntStream.of(arr)).toArray();
            }
            if (round.getRoundStart() == 1 && round.getRoundHoles() == 9) {
                arr = IntStream.concat(IntStream.of(arr), IntStream.of(a)).toArray();
            }
            score.setStrokeArray(arr);
            LOG.debug(" array Strokes completed from scoreList for legacy calculations = " + Arrays.toString(score.getStrokeArray()));
            LoggingUserController.write("Score Card =  " + Arrays.toString(score.getStrokeArray()));

            if (utils.LCUtil.isArrayAllZeroes(score.getParArray())) {
                LOG.debug("handling isArrayAllZeroes");
                score = readParAndStrokeIndex.read(course, score); // migrated 2026-02-25
                score = calcCourseHandicap.calc(score, player, round, tee); // migrated 2026-02-25
                LOG.debug("courseHandicap = " + score.getCourseHandicap());
                LOG.debug("HandicapIndex player = " + score.getPlayerHandicapWHS());
                int i = calcPlayingHandicap.calc(score, player, round); // migrated 2026-02-25
                score.setPlayingHandicap(i);
                LOG.debug(" playingHandicap = " + score.getPlayingHandicap());

                score.setExtraArray(stablefordController.completeWithExtraStrokesNew(score, round)); // migrated from static call 2026-03-22
                LOG.debug("ExtraArray completed with Extra strokes = " + Arrays.toString(score.getExtraArray()));
            }

            score.setHolesNotPlayed(countHolesNotPlayed(score.getStrokeArray()));
            String msg = "There are holes not played = " + score.getHolesNotPlayed();
            LOG.info(msg);
            showMessageInfo(msg);
            LoggingUserController.write(msg);

            if (score.getHolesNotPlayed() > 0) {
                score.setStrokeArray(completeHolesNotPlayed(score));
                msg = "StrokeArray completed with HolesNotPlay = " + Arrays.toString(score.getStrokeArray());
                LOG.debug(msg);
                LoggingUserController.write(msg);
            }

            score.setStart(round.getRoundStart());
            LOG.debug("start is now = " + score.getStart());
            score.setHoles(round.getRoundHoles());
            LOG.debug("holes is now = " + score.getHoles());

            score.setStrokeArray(ajustedGrossScore(score));
            msg = "ExtraArray completed with Extra strokes = " + Arrays.toString(score.getExtraArray());
            LOG.debug(msg);
            LoggingUserController.write(msg);

            score.setAdjustedGrossScore(Arrays.stream(score.getStrokeArray()).sum());
            msg = " Adjusted Score Total = " + score.getAdjustedGrossScore();
            LOG.debug(msg);
            LoggingUserController.write(msg);

            score.setPointsArray(calculatePoints(score));
            msg = "Calculated points are in score.getPointsArray :" + Arrays.toString(score.getPointsArray());
            LOG.debug(msg);
            LoggingUserController.write(msg);

            score.setStablefordResult(Arrays.stream(score.getPointsArray()).sum());
            msg = "Stableford total points : " + score.getStablefordResult();
            LOG.debug(msg);
            LoggingUserController.write(msg);

            score.setTotalStrokes(Arrays.stream(score.getStrokeArray()).sum());
            msg = "Total Strokes (calculated) = " + score.getTotalStrokes();
            LOG.debug(msg);
            LoggingUserController.write(msg);

            score.setCourseRating(tee.getTeeRating().doubleValue());
            score.setSlopeRating(tee.getTeeSlope());
            score.setScoreParCourse(tee.getTeePar());
            double sd = calcScoreDifferential.calc(score, player, round); // migrated 2026-02-25
            score.setScoreDifferential(sd);
            score.setShowCreate(true);
            score.setShowCalculate(false);
            score.setShowLineDifferential(true);

            score.setScoreList(stablefordController.completeScoreListWithStrokes(score)); // migrated 2026-02-25
            score.setScoreList(stablefordController.completeScoreListWithPoints(score));  // migrated 2026-02-25

            LOG.debug("CalcScoreStableford final = " + score);

            LoggingUser logging = new LoggingUser();
            logging.setLoggingIdPlayer(player.getIdplayer());
            logging.setLoggingIdRound(round.getIdround());
            logging.setLoggingType("R");
            boolean b = loggingUserController.createUpdateLoggingUser(logging); // migrated 2026-02-25

            return score;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return score;
        }
    } // end method

    // ========================================
    // MÉTHODES UTILITAIRES : Peuvent rester static
    // ========================================

    private static int countHolesNotPlayed(int[] arr) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == -1) {
                count++;
            }
        }
        return count;
    } // end method

    private static int[] ajustedGrossScore(ScoreStableford score) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug(" entering " + methodName + " with stroke = " + Arrays.toString(score.getStrokeArray()));
        LOG.debug("  with par = " + Arrays.toString(score.getParArray()));
        LOG.debug("  with extra = " + Arrays.toString(score.getExtraArray()));

        try {
            LoggingUserController.write("ajustedGrossScore", "t");
            int[] adj = new int[18];
            for (int i = 0; i < adj.length; i++) {
                int maximumHoleScore = score.getParArray()[i] + score.getExtraArray()[i] + 2; // fix 2026-03-28 — was [1] (always hole 2), corrected to [i]
                if (score.getStrokeArray()[i] > maximumHoleScore) {
                    LOG.debug("AdjustedGrossScore pour hole = " + (i + 1) + " , maximumHoleScore = " + maximumHoleScore);
                    adj[i] = maximumHoleScore;
                    String msg = "Adjusted Gross Score adjustment applied on hole = " + (i + 1)
                            + " adjusted score = " + adj[i] + " for par = " + score.getParArray()[i];
                    LOG.debug(msg);
                    LoggingUserController.write(msg);
                } else {
                    adj[i] = score.getStrokeArray()[i];
                }
            }
            String msg = NEW_LINE + "After ajustedGrossScore, strokes = " + Arrays.toString(adj);
            LOG.debug(msg);
            LoggingUserController.write(msg + NEW_LINE);
            return adj;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return score.getStrokeArray(); // return unmodified on error
        }
    } // end method

    private static int[] calculatePoints(ScoreStableford score) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with scoreStableford = " + score);

        try {
            int[] points = new int[18];
            for (int i = 0; i < points.length; i++) {
                points[i] = pointsStableford(
                    (score.getStrokeArray()[i] - score.getExtraArray()[i]),
                    score.getParArray()[i]
                );
            }
            LOG.debug("points calculated method = " + Arrays.toString(points));
            return points;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new int[18]; // return zeroed array on error
        }
    } // end method

    private static int[] completeHolesNotPlayed(ScoreStableford score) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("  " + methodName);
        LOG.debug("strokeArray = " + Arrays.toString(score.getStrokeArray()));
        LOG.debug("parArray    = " + Arrays.toString(score.getParArray()));
        int[] strokes = new int[18];

        try {
            for (int i = 0; i < strokes.length; i++) {
                if (score.getStrokeArray()[i] == -1) {
                    strokes[i] = score.getParArray()[i];
                    LOG.debug("hole " + (i + 1) + ", strokes completed = " + strokes[i]);
                } else {
                    strokes[i] = score.getStrokeArray()[i];
                }
            }
            LOG.debug("strokes completed after for loop = " + Arrays.toString(strokes));

            if (score.getHolesNotPlayed() == 5) {
                strokes[13] = strokes[13] + 1;
                LOG.debug("not played 5 holes, correction net bogey hole 14 " + strokes[13]);
            }
            if (score.getHolesNotPlayed() == 6) {
                strokes[12] = strokes[12] + 1;
                LOG.debug("not played 6 holes, correction net bogey hole 13 = " + strokes[12]);
            }
            return strokes;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return score.getStrokeArray(); // return unmodified on error
        } finally {
            LOG.debug("strokes completed final = " + Arrays.toString(strokes));
        }
    } // end method

    private static int pointsStableford(int net, final int par) {
        LOG.debug("entering pointsStableford");

        if (net == 0) {
            return 0;
        }

        return switch (par - net) {
            case -5, -4, -3, -2 -> 0;
            case -1 -> 1;
            case 0  -> 2;
            case 1  -> 3;
            case 2  -> 4;
            case 3  -> 5;
            default -> {
                String msg = " -- Falling in Default in pointStableford - 0 points, par = "
                           + par + " net = " + net;
                LOG.error(msg);
                showMessageFatal(msg);
                yield 0;
            }
        };
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // tests locaux
    } // end main
    */

} // end class
