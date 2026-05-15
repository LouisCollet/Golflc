package sql.preparedstatement;

import entite.Course;
import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

public class psCreateHolesGlobal implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @param holeRow holesGlobal.getDataHoles()[i] — int[3]: [holenumber, par, strokeIndex]
     */
    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final int[] holeRow,
            final Tee tee,
            final Course course) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setNull     (1, Types.INTEGER);                         // idhole — auto-increment
            ps.setShort    (2, (short) holeRow[0]);                    // holenumber
            ps.setShort    (3, (short) holeRow[1]);                    // Par
            ps.setInt      (4, 0);                                     // distance — initialized to 0
            ps.setShort    (5, (short) holeRow[2]);                    // stroke index
            ps.setInt      (6, tee.getIdtee());
            ps.setInt      (7, course.getIdcourse());
            ps.setTimestamp(8, Timestamp.from(Instant.now()));         // ModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
