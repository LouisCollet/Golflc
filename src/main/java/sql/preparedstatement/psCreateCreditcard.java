package sql.preparedstatement;

import entite.Creditcard;
import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class psCreateCreditcard implements Serializable, interfaces.Log, interfaces.GolfInterface {

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final Creditcard creditcard) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setNull(1, java.sql.Types.INTEGER);                                                    // AUTO-INCREMENT
            ps.setInt(2, creditcard.getCreditCardIdPlayer());                                          // CreditcardIdPlayer
            ps.setString(3, creditcard.getCreditcardHolder());                                         // CreditcardHolder
            ps.setString(4, creditcard.getCreditcardNumber());                                         // CreditcardNumber
            ps.setTimestamp(5, Timestamp.valueOf(creditcard.getCreditCardExpirationDateLdt()));        // CreditcardExpirationDate
            ps.setString(6, creditcard.getCreditcardType());                                           // CreditcardType
            ps.setShort(7, creditcard.getCreditcardVerificationCode());                                // CreditcardVerificationCode
            ps.setTimestamp(8, Timestamp.from(Instant.now()));                                        // CreditcardModificationDate
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
