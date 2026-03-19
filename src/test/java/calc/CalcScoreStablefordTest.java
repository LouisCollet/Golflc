package calc;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CalcScoreStableford pure calculation methods.
 * Uses reflection to access private static methods: pointsStableford, countHolesNotPlayed.
 */
@DisplayName("CalcScoreStableford - Stableford points and utility calculations")
public class CalcScoreStablefordTest {

    private static final Method POINTS_STABLEFORD;
    private static final Method COUNT_HOLES_NOT_PLAYED;

    static {
        try {
            POINTS_STABLEFORD = CalcScoreStableford.class.getDeclaredMethod("pointsStableford", int.class, int.class);
            POINTS_STABLEFORD.setAccessible(true);
            COUNT_HOLES_NOT_PLAYED = CalcScoreStableford.class.getDeclaredMethod("countHolesNotPlayed", int[].class);
            COUNT_HOLES_NOT_PLAYED.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Cannot find method for testing", e);
        }
    }

    private int pointsStableford(int net, int par) throws Exception {
        return (int) POINTS_STABLEFORD.invoke(null, net, par);
    }

    private int countHolesNotPlayed(int[] arr) throws Exception {
        return (int) COUNT_HOLES_NOT_PLAYED.invoke(null, arr);
    }

    // ===================== pointsStableford tests =====================

    @Test
    @DisplayName("Par (net == par) -> 2 points")
    void pointsStableford_par() throws Exception {
        assertEquals(2, pointsStableford(4, 4), "Net equal to par should give 2 points");
        assertEquals(2, pointsStableford(3, 3), "Net equal to par 3 should give 2 points");
        assertEquals(2, pointsStableford(5, 5), "Net equal to par 5 should give 2 points");
    }

    @Test
    @DisplayName("Birdie (net == par - 1) -> 3 points")
    void pointsStableford_birdie() throws Exception {
        assertEquals(3, pointsStableford(3, 4), "Birdie on par 4 should give 3 points");
        assertEquals(3, pointsStableford(2, 3), "Birdie on par 3 should give 3 points");
    }

    @Test
    @DisplayName("Eagle (net == par - 2) -> 4 points")
    void pointsStableford_eagle() throws Exception {
        assertEquals(4, pointsStableford(2, 4), "Eagle on par 4 should give 4 points");
        assertEquals(4, pointsStableford(3, 5), "Eagle on par 5 should give 4 points");
    }

    @Test
    @DisplayName("Albatross (net == par - 3) -> 5 points")
    void pointsStableford_albatross() throws Exception {
        assertEquals(5, pointsStableford(2, 5), "Albatross on par 5 should give 5 points");
    }

    @Test
    @DisplayName("Bogey (net == par + 1) -> 1 point")
    void pointsStableford_bogey() throws Exception {
        assertEquals(1, pointsStableford(5, 4), "Bogey on par 4 should give 1 point");
        assertEquals(1, pointsStableford(4, 3), "Bogey on par 3 should give 1 point");
    }

    @Test
    @DisplayName("Double bogey or worse (net >= par + 2) -> 0 points")
    void pointsStableford_doubleBogeyOrWorse() throws Exception {
        assertEquals(0, pointsStableford(6, 4), "Double bogey should give 0 points");
        assertEquals(0, pointsStableford(7, 4), "Triple bogey should give 0 points");
        assertEquals(0, pointsStableford(8, 4), "Quad bogey should give 0 points");
        assertEquals(0, pointsStableford(9, 4), "5-over par should give 0 points");
    }

    @Test
    @DisplayName("Net zero (hole not played) -> 0 points")
    void pointsStableford_netZero() throws Exception {
        assertEquals(0, pointsStableford(0, 4), "Net zero should return 0 points");
    }

    // ===================== countHolesNotPlayed tests =====================

    @Test
    @DisplayName("No holes marked as not played")
    void countHolesNotPlayed_none() throws Exception {
        int[] arr = {4, 5, 3, 4, 5, 3, 4, 4, 5, 4, 5, 3, 4, 5, 3, 4, 4, 5};
        assertEquals(0, countHolesNotPlayed(arr), "No -1 values means 0 holes not played");
    }

    @Test
    @DisplayName("Some holes marked as not played (-1)")
    void countHolesNotPlayed_some() throws Exception {
        int[] arr = {4, -1, 3, 4, -1, 3, 4, 4, -1, 4, 5, 3, 4, 5, 3, 4, 4, 5};
        assertEquals(3, countHolesNotPlayed(arr), "Three -1 values means 3 holes not played");
    }

    @Test
    @DisplayName("All holes marked as not played (-1)")
    void countHolesNotPlayed_all() throws Exception {
        int[] arr = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        assertEquals(18, countHolesNotPlayed(arr), "All -1 values means 18 holes not played");
    }

} // end class
