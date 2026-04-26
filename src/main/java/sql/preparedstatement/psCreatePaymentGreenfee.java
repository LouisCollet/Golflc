package sql.preparedstatement;

import entite.Greenfee;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class psCreatePaymentGreenfee implements Serializable, interfaces.Log, interfaces.GolfInterface {

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final Player player,
            final Greenfee greenfee) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setNull(1, java.sql.Types.INTEGER);                              // AUTO-INCREMENT
            ps.setInt(2, greenfee.getIdclub());                                  // GreenfeeIdClub
            ps.setInt(3, player.getIdplayer());                                  // GreenfeeIdPlayer
            ps.setInt(4, greenfee.getIdround());                                 // GreenfeeIdRound
            ps.setTimestamp(5, Timestamp.valueOf(greenfee.getRoundDate()));      // GreenfeeRoundDate
            ps.setString(6, greenfee.getPaymentReference());                     // GreenfeePaymentReference
            ps.setString(7, greenfee.getCommunication());                        // GreenfeeCommunication
            ps.setString(8, greenfee.getItems());                                // GreenfeeItems
            ps.setString(9, greenfee.getStatus());                               // GreenfeeStatus
            ps.setDouble(10, greenfee.getPrice());                               // GreenfeeAmount
            ps.setString(11, greenfee.getCurrency());                            // GreenfeeCurrency
            ps.setTimestamp(12, Timestamp.from(Instant.now()));                 // GreenfeeModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);// TarifModificationDate
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
