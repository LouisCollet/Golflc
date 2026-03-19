package calc;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CalcStablefordScoreDifferential.calc().
 * Formula (18 holes): SD = (113 / SlopeRating) x (AGS - CourseRating)
 * Formula (9 holes):  SD = (113 / SlopeRating) x (AGS - CourseRating) + expectedSD9Holes
 *   where expectedSD9Holes = (playerHandicapWHS / 2) + 1.5
 */
@DisplayName("CalcStablefordScoreDifferential - Score Differential calculation")
public class CalcStablefordScoreDifferentialTest {

    private final CalcStablefordScoreDifferential calc = new CalcStablefordScoreDifferential();

    private ScoreStableford createScore(short slope, double courseRating, int ags, double playerHandicapWHS) {
        ScoreStableford score = new ScoreStableford();
        score.setSlopeRating(slope);
        score.setCourseRating(courseRating);
        score.setAdjustedGrossScore(ags);
        score.setPlayerHandicapWHS(playerHandicapWHS);
        return score;
    }

    @Test
    @DisplayName("18 holes - typical round (Slope=125, CR=71.2, AGS=85)")
    void scoreDifferential_18holes_typical() {
        // SD = (113/125) * (85 - 71.2) = 0.904 * 13.8 = 12.4752 -> rounded 12.5
        ScoreStableford score = createScore((short) 125, 71.2, 85, 15.0);
        Round round = new Round();
        round.setRoundHoles((short) 18);
        Player player = new Player();

        double result = calc.calc(score, player, round);
        assertEquals(12.5, result, 0.1, "Typical 18-hole score differential");
    }

    @Test
    @DisplayName("18 holes - standard slope (Slope=113, CR=72.0, AGS=72)")
    void scoreDifferential_18holes_parRound() {
        // SD = (113/113) * (72 - 72.0) = 1.0 * 0.0 = 0.0
        ScoreStableford score = createScore((short) 113, 72.0, 72, 0.0);
        Round round = new Round();
        round.setRoundHoles((short) 18);
        Player player = new Player();

        double result = calc.calc(score, player, round);
        assertEquals(0.0, result, 0.01, "Par round on standard slope should give SD=0");
    }

    @Test
    @DisplayName("18 holes - below par round")
    void scoreDifferential_18holes_belowPar() {
        // SD = (113/130) * (68 - 72.5) = 0.86923 * (-4.5) = -3.9115 -> rounded -3.9
        ScoreStableford score = createScore((short) 130, 72.5, 68, 0.0);
        Round round = new Round();
        round.setRoundHoles((short) 18);
        Player player = new Player();

        double result = calc.calc(score, player, round);
        assertTrue(result < 0, "Below-par round should give negative score differential");
        assertEquals(-3.9, result, 0.15, "Below-par score differential value");
    }

    @Test
    @DisplayName("9 holes - includes expected SD adjustment")
    void scoreDifferential_9holes() {
        // base SD = (113/120) * (42 - 35.5) = 0.94167 * 6.5 = 6.1208
        // expectedSD9Holes = (20.0 / 2) + 1.5 = 11.5
        // total SD = 6.1208 + 11.5 = 17.6208 -> rounded 17.6
        ScoreStableford score = createScore((short) 120, 35.5, 42, 20.0);
        Round round = new Round();
        round.setRoundHoles((short) 9);
        Player player = new Player();

        double result = calc.calc(score, player, round);
        assertEquals(17.6, result, 0.15, "9-hole SD should include expected SD adjustment");
    }

    @Test
    @DisplayName("18 holes - high handicap player with high AGS")
    void scoreDifferential_18holes_highHandicap() {
        // SD = (113/113) * (108 - 70.0) = 1.0 * 38.0 = 38.0
        ScoreStableford score = createScore((short) 113, 70.0, 108, 36.0);
        Round round = new Round();
        round.setRoundHoles((short) 18);
        Player player = new Player();

        double result = calc.calc(score, player, round);
        assertEquals(38.0, result, 0.1, "High handicap player score differential");
    }

} // end class
