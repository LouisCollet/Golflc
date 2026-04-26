package sql.preparedstatement;

import entite.Cotisation;
import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class psCreatePaymentCotisation implements Serializable, interfaces.Log, interfaces.GolfInterface {

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final Cotisation cotisation) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setNull(1, java.sql.Types.INTEGER);                                       // AUTO-INCREMENT
            ps.setInt(2, cotisation.getIdclub());                                         // CotisationIdClub
            ps.setInt(3, cotisation.getIdplayer());                                       // CotisationIdPlayer
            ps.setTimestamp(4, Timestamp.valueOf(cotisation.getCotisationStartDate()));   // CotisationStartDate
            ps.setTimestamp(5, Timestamp.valueOf(cotisation.getCotisationEndDate()));     // CotisationEndDate
            ps.setString(6, cotisation.getPaymentReference());                            // CotisationPaymentReference
            ps.setString(7, cotisation.getCommunication());                               // CotisationCommunication
            ps.setString(8, cotisation.getItems());                                       // CotisationItems
            ps.setString(9, cotisation.getStatus());                                      // CotisationStatus
            ps.setDouble(10, cotisation.getPrice());                                      // CotisationAmount
            ps.setTimestamp(11, Timestamp.from(Instant.now()));                           // CotisationModificationDate
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
