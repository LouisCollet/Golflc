package sql.preparedstatement;

import entite.LoggingUser;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class psCreateUpdateLoggingUser implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final LoggingUser logging) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setInt      (1, logging.getLoggingIdPlayer());
            ps.setInt      (2, logging.getLoggingIdRound());
            ps.setString   (3, logging.getLoggingType());
            ps.setString   (4, logging.getLoggingCalculations());
            ps.setTimestamp(5, Timestamp.from(Instant.now()));         // ModificationDate
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
            final LoggingUser logging) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setString   (1, logging.getLoggingCalculations());      // SET
            ps.setInt      (2, logging.getLoggingIdPlayer());          // WHERE
            ps.setInt      (3, logging.getLoggingIdRound());           // WHERE
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
