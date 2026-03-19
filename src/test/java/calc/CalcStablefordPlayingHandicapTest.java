package calc;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CalcStablefordPlayingHandicap.calc().
 * Playing Handicap = Course Handicap x Handicap Allowance
 * For STABLEFORD game: allowance = 1 (100%)
 */
@DisplayName("CalcStablefordPlayingHandicap - Playing Handicap calculation")
public class CalcStablefordPlayingHandicapTest {

    private final CalcStablefordPlayingHandicap calc = new CalcStablefordPlayingHandicap();

    @Test
    @DisplayName("Stableford game - playing handicap equals course handicap (allowance=1)")
    void playingHandicap_stableford() throws Exception {
        ScoreStableford score = new ScoreStableford();
        score.setCourseHandicap(18);

        Round round = new Round();
        round.setRoundGame("STABLEFORD");

        Player player = new Player();
        player.setIdplayer(100000);

        int result = calc.calc(score, player, round);
        assertEquals(18, result, "Stableford: playing handicap should equal course handicap");
    }

    @Test
    @DisplayName("Non-stableford game - playing handicap is zero (allowance=0)")
    void playingHandicap_nonStableford() throws Exception {
        ScoreStableford score = new ScoreStableford();
        score.setCourseHandicap(18);

        Round round = new Round();
        round.setRoundGame("MATCHPLAY");

        Player player = new Player();
        player.setIdplayer(100000);

        int result = calc.calc(score, player, round);
        assertEquals(0, result, "Non-stableford: playing handicap should be 0 (allowance=0)");
    }

    @Test
    @DisplayName("Stableford game - zero course handicap")
    void playingHandicap_zeroCourseHandicap() throws Exception {
        ScoreStableford score = new ScoreStableford();
        score.setCourseHandicap(0);

        Round round = new Round();
        round.setRoundGame("STABLEFORD");

        Player player = new Player();
        player.setIdplayer(100000);

        int result = calc.calc(score, player, round);
        assertEquals(0, result, "Zero course handicap should give zero playing handicap");
    }

    @Test
    @DisplayName("Stableford game - high course handicap (36)")
    void playingHandicap_highCourseHandicap() throws Exception {
        ScoreStableford score = new ScoreStableford();
        score.setCourseHandicap(36);

        Round round = new Round();
        round.setRoundGame("STABLEFORD");

        Player player = new Player();
        player.setIdplayer(100000);

        int result = calc.calc(score, player, round);
        assertEquals(36, result, "High course handicap should pass through for stableford");
    }

} // end class
