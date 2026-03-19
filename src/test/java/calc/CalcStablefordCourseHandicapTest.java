package calc;

import entite.HandicapIndex;
import entite.Round;
import entite.Tee;
import java.math.BigDecimal;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CalcStablefordCourseHandicap.courseHandicap().
 * Formula: Course Handicap = HI x (Slope / 113) + (CR - Par)
 * For 9 holes: Course Handicap = (HI / 2) x (Slope / 113) + (CR - Par)
 */
@DisplayName("CalcStablefordCourseHandicap - Course Handicap calculation")
public class CalcStablefordCourseHandicapTest {

    private final CalcStablefordCourseHandicap calc = new CalcStablefordCourseHandicap();

    private Tee createTee(int slope, double rating, short par) {
        Tee tee = new Tee();
        tee.setTeeSlope((short) slope);
        tee.setTeeRating(BigDecimal.valueOf(rating));
        tee.setTeePar(par);
        return tee;
    }

    private Round createRound(int holes) {
        Round round = new Round();
        round.setRoundHoles((short) holes);
        round.setCourseIdcourse(1);
        return round;
    }

    private HandicapIndex createHandicapIndex(double whs) {
        HandicapIndex hi = new HandicapIndex();
        hi.setHandicapWHS(BigDecimal.valueOf(whs));
        return hi;
    }

    @Test
    @DisplayName("18 holes - typical mid-handicap player (HI=15.0, Slope=125, CR=71.2, Par=72)")
    void courseHandicap18Holes_midHandicap() throws Exception {
        // CH = 15.0 * (125/113) + (71.2 - 72) = 15.0 * 1.10619 + (-0.8) = 16.593 - 0.8 = 15.793 -> rounded 16
        HandicapIndex hi = createHandicapIndex(15.0);
        Tee tee = createTee(125, 71.2, (short) 72);
        Round round = createRound(18);

        BigDecimal result = calc.courseHandicap(hi, tee, round);
        assertEquals(16, result.intValue(), "Mid-handicap 18 holes should round to 16");
    }

    @Test
    @DisplayName("18 holes - scratch player (HI=0.0, Slope=130, CR=72.5, Par=72)")
    void courseHandicap18Holes_scratchPlayer() throws Exception {
        // CH = 0.0 * (130/113) + (72.5 - 72) = 0 + 0.5 = 0.5 -> rounded 0 (HALF_EVEN)
        HandicapIndex hi = createHandicapIndex(0.0);
        Tee tee = createTee(130, 72.5, (short) 72);
        Round round = createRound(18);

        BigDecimal result = calc.courseHandicap(hi, tee, round);
        assertEquals(0, result.intValue(), "Scratch player should have course handicap near 0");
    }

    @Test
    @DisplayName("18 holes - high handicap player (HI=36.0, Slope=113, CR=70.0, Par=72)")
    void courseHandicap18Holes_highHandicap() throws Exception {
        // CH = 36.0 * (113/113) + (70.0 - 72) = 36.0 * 1.0 + (-2.0) = 34.0
        HandicapIndex hi = createHandicapIndex(36.0);
        Tee tee = createTee(113, 70.0, (short) 72);
        Round round = createRound(18);

        BigDecimal result = calc.courseHandicap(hi, tee, round);
        assertEquals(34, result.intValue(), "High handicap on standard slope should be HI + (CR-Par)");
    }

    @Test
    @DisplayName("9 holes - typical player (HI=20.0, Slope=120, CR=35.5, Par=36)")
    void courseHandicap9Holes_typical() throws Exception {
        // CH = (20.0/2) * (120/113) + (35.5 - 36) = 10.0 * 1.06195 + (-0.5) = 10.6195 - 0.5 = 10.1195 -> rounded 10
        HandicapIndex hi = createHandicapIndex(20.0);
        Tee tee = createTee(120, 35.5, (short) 36);
        Round round = createRound(9);

        BigDecimal result = calc.courseHandicap(hi, tee, round);
        assertEquals(10, result.intValue(), "9-hole course handicap should use HI/2");
    }

    @Test
    @DisplayName("Invalid number of holes throws SQLException")
    void courseHandicap_invalidHoles_throwsException() {
        HandicapIndex hi = createHandicapIndex(15.0);
        Tee tee = createTee(125, 71.2, (short) 72);
        Round round = createRound(12); // invalid

        assertThrows(SQLException.class, () -> calc.courseHandicap(hi, tee, round),
                "Should throw SQLException for invalid number of holes");
    }

} // end class
