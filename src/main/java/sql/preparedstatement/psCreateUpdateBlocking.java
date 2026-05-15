package sql.preparedstatement;

import entite.Blocking;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

public class psCreateUpdateBlocking implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final Player player) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setInt      (1, player.getIdplayer());
            ps.setTimestamp(2, Timestamp.from(Instant.now()));         // BlockingLastAttempt
            ps.setInt      (3, 1);                                     // BlockingAttempts — initial value
            ps.setTimestamp(4, Timestamp.from(Instant.now()));         // BlockingRetryTime
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
            final Blocking blocking) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));         // BlockingLastAttempt
            ps.setShort    (2, blocking.getBlockingAttempts());
            if (blocking.getBlockingAttempts() > 2) {
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
            } else {
                ps.setTimestamp(3, Timestamp.from(Instant.now()));
            }
            ps.setInt      (4, blocking.getBlockingPlayerId());        // WHERE
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
