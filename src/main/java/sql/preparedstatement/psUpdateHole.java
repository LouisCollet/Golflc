package sql.preparedstatement;

import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class psUpdateHole implements Serializable, interfaces.Log, interfaces.GolfInterface {

    /**
     * UPDATE hole SET HolePar=?, HoleDistance=?, HoleStrokeIndex=?
     *           WHERE tee_idtee=? AND HoleNumber=?
     * HoleDistance is always 0 — MySQL trigger constraint.
     */
    public static PreparedStatement psMapUpdate(
            PreparedStatement ps,
            final byte par,
            final byte strokeIndex,
            final int teeId,
            final int holeNumber) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setByte (1, par);
            ps.setShort(2, (short) 0);          // HoleDistance always 0 — trigger constraint
            ps.setByte (3, strokeIndex);
            ps.setInt  (4, teeId);
            ps.setByte (5, (byte) holeNumber);
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * INSERT INTO hole (idhole, HoleNumber, HolePar, HoleDistance, HoleStrokeIndex,
     *                   tee_idtee, tee_course_idcourse, HoleModificationDate)
     * HoleDistance is always 0 — MySQL trigger constraint.
     */
    public static PreparedStatement psMapInsert(
            PreparedStatement ps,
            final int holeNumber,
            final byte par,
            final byte strokeIndex,
            final int teeId,
            final int courseId) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setShort    (1, (short) holeNumber);
            ps.setByte     (2, par);
            ps.setShort    (3, (short) 0);      // HoleDistance always 0 — trigger constraint
            ps.setByte     (4, strokeIndex);
            ps.setInt      (5, teeId);
            ps.setInt      (6, courseId);
            ps.setTimestamp(7, Timestamp.from(Instant.now()));
            ps.getWarnings();
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
