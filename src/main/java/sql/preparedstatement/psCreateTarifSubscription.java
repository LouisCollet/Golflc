package sql.preparedstatement;

import entite.TarifSubscription;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

public class psCreateTarifSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final TarifSubscription tarif) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setNull     (1, Types.INTEGER);                         // TarifSubscriptionId — auto-increment
            ps.setString   (2, tarif.getCode());
            ps.setDouble   (3, tarif.getPrice());
            ps.setTimestamp(4, Timestamp.valueOf(tarif.getStartDate()));
            ps.setTimestamp(5, Timestamp.valueOf(tarif.getEndDate()));
            ps.setTimestamp(6, Timestamp.from(Instant.now()));         // ModificationDate
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
