package sql.preparedstatement;

import entite.HandicapIndex;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

public class psCreateUpdateHandicapIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final HandicapIndex handicapIndex) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setNull      (1,  Types.INTEGER);                                       // HandicapId — auto-increment
            ps.setInt       (2,  handicapIndex.getHandicapPlayerId());
            ps.setInt       (3,  handicapIndex.getHandicapRoundId());
            ps.setBigDecimal(4,  handicapIndex.getHandicapScoreDifferential());
            ps.setTimestamp (5,  Timestamp.valueOf(handicapIndex.getHandicapDate()));
            ps.setBigDecimal(6,  handicapIndex.getHandicapWHS());
            ps.setInt       (7,  0);                                                   // HandicapExceptionalScoreReduction
            ps.setString    (8,  "0");                                                 // HandicapSoftHardCap
            ps.setString    (9,  handicapIndex.getHandicapComment());
            ps.setInt       (10, handicapIndex.getHandicapPlayedStrokes());
            ps.setDouble    (11, 0.0);                                                 // LowHandicapIndex
            ps.setDouble    (12, handicapIndex.getHandicapExpectedSD9Holes());
            ps.setShort     (13, handicapIndex.getHandicapHolesNotPlayed());
            ps.setTimestamp (14, Timestamp.from(Instant.now()));                       // ModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public static PreparedStatement psMapUpdate(
            PreparedStatement ps,
            final HandicapIndex handicapIndex) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setBigDecimal(1, handicapIndex.getHandicapWHS());
            ps.setString    (2, "UPD-" + handicapIndex.getHandicapComment());
            ps.setString    (3, handicapIndex.getHandicapSoftHardCap());
            ps.setShort     (4, handicapIndex.getHandicapExceptionalScoreReduction());
            ps.setDouble    (5, handicapIndex.getLowHandicapIndex());
            ps.setInt       (6, handicapIndex.getHandicapId());                        // WHERE
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
