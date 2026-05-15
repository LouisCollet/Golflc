package sql.preparedstatement;

import entite.Player;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

public class psCreateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final Player player) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setNull     (1, Types.INTEGER);                         // AuditId — auto-increment
            ps.setInt      (2, player.getIdplayer());
            ps.setTimestamp(3, Timestamp.from(Instant.now()));         // AuditStartDate
            ps.setNull     (4, Types.TIMESTAMP);                       // AuditEndDate — NULL until logout
            ps.setTimestamp(5, Timestamp.from(Instant.now()));         // ModificationDate
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
