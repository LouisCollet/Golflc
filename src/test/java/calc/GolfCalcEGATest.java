package calc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GolfCalcEGA static calculation methods.
 * Tests StablefordResult, setArrayExtraStrokes, setArrayPoints.
 *
 * Array format per hole: [hole#, par, strokeIndex, strokes, extra, points]
 */
@DisplayName("GolfCalcEGA - Legacy EGA stableford calculations")
public class GolfCalcEGATest {

    // ===================== StablefordResult tests =====================

    @Test
    @DisplayName("StablefordResult - sum of points column from 18-hole array")
    void stablefordResult_18holes() {
        // 18 holes, each with 2 points (par) = 36 total
        int[][] points = new int[18][6];
        for (int i = 0; i < 18; i++) {
            points[i][0] = i + 1; // hole
            points[i][1] = 4;     // par
            points[i][5] = 2;     // 2 points (par)
        }
        assertEquals(36, GolfCalcEGA.StablefordResult(points), "18 pars should give 36 points");
    }

    @Test
    @DisplayName("StablefordResult - mixed points")
    void stablefordResult_mixed() {
        // 9 holes with varying points
        int[][] points = new int[9][6];
        int[] expectedPoints = {0, 1, 2, 3, 2, 1, 0, 2, 3}; // total = 14
        for (int i = 0; i < 9; i++) {
            points[i][0] = i + 1;
            points[i][5] = expectedPoints[i];
        }
        assertEquals(14, GolfCalcEGA.StablefordResult(points), "Mixed points should sum to 14");
    }

    @Test
    @DisplayName("StablefordResult - all zeros")
    void stablefordResult_allZeros() {
        int[][] points = new int[18][6]; // all zeros by default
        assertEquals(0, GolfCalcEGA.StablefordResult(points), "All zeros should give 0 total");
    }

    // ===================== setArrayExtraStrokes tests =====================

    @Test
    @DisplayName("setArrayExtraStrokes - playing handicap 18 on 18 holes gives 1 extra each")
    void setArrayExtraStrokes_exactlyOnePerHole() {
        int[][] points = create18HoleArray();
        String[] result = GolfCalcEGA.setArrayExtraStrokes(points, 18);

        assertEquals("NO ERROR", result[0], "Should not produce an error");
        for (int i = 0; i < 18; i++) {
            assertEquals(1, points[i][4], "Hole " + (i + 1) + " should have 1 extra stroke");
        }
    }

    @Test
    @DisplayName("setArrayExtraStrokes - playing handicap 0 gives no extras")
    void setArrayExtraStrokes_zeroHandicap() {
        int[][] points = create18HoleArray();
        String[] result = GolfCalcEGA.setArrayExtraStrokes(points, 0);

        assertEquals("NO ERROR", result[0], "Should not produce an error");
        for (int i = 0; i < 18; i++) {
            assertEquals(0, points[i][4], "Hole " + (i + 1) + " should have 0 extra strokes");
        }
    }

    @Test
    @DisplayName("setArrayExtraStrokes - playing handicap 36 gives 2 extras each")
    void setArrayExtraStrokes_doubleAllocation() {
        int[][] points = create18HoleArray();
        String[] result = GolfCalcEGA.setArrayExtraStrokes(points, 36);

        assertEquals("NO ERROR", result[0], "Should not produce an error");
        for (int i = 0; i < 18; i++) {
            assertEquals(2, points[i][4], "Hole " + (i + 1) + " should have 2 extra strokes");
        }
    }

    // ===================== setArrayPoints tests =====================

    @Test
    @DisplayName("setArrayPoints - par round gives 2 points each hole")
    void setArrayPoints_parRound() {
        int[][] points = create18HoleArray();
        // Set strokes equal to par (4) with 0 extra -> net = 4 = par -> 2 points
        for (int i = 0; i < 18; i++) {
            points[i][3] = 4; // strokes = par
        }
        String[] result = GolfCalcEGA.setArrayPoints(points);

        assertEquals("NO ERROR", result[0], "Should not produce an error");
        for (int i = 0; i < 18; i++) {
            assertEquals(2, points[i][5], "Par on hole " + (i + 1) + " should give 2 points");
        }
    }

    @Test
    @DisplayName("setArrayPoints - birdie round gives 3 points each hole")
    void setArrayPoints_birdieRound() {
        int[][] points = create18HoleArray();
        // Set strokes = 3 with par = 4 and 0 extra -> net = 3, par - net = 1 -> birdie -> 3 points
        for (int i = 0; i < 18; i++) {
            points[i][3] = 3; // birdie on par 4
        }
        String[] result = GolfCalcEGA.setArrayPoints(points);

        assertEquals("NO ERROR", result[0], "Should not produce an error");
        for (int i = 0; i < 18; i++) {
            assertEquals(3, points[i][5], "Birdie on hole " + (i + 1) + " should give 3 points");
        }
    }

    @Test
    @DisplayName("setArrayPoints - zero strokes triggers error")
    void setArrayPoints_zeroStrokes_error() {
        int[][] points = create18HoleArray();
        // strokes remain 0 by default -> error
        String[] result = GolfCalcEGA.setArrayPoints(points);
        assertEquals("ERROR", result[0], "Zero strokes should produce an error");
    }

    // ===================== Helper methods =====================

    /**
     * Creates a standard 18-hole array with:
     * [hole#, par=4, strokeIndex=i+1, strokes=0, extra=0, points=0]
     */
    private int[][] create18HoleArray() {
        int[][] points = new int[18][6];
        for (int i = 0; i < 18; i++) {
            points[i][0] = i + 1;   // hole number
            points[i][1] = 4;       // par
            points[i][2] = i + 1;   // stroke index
            points[i][3] = 0;       // strokes
            points[i][4] = 0;       // extra
            points[i][5] = 0;       // points
        }
        return points;
    }

} // end class
