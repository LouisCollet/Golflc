
package rowmappers;

import entite.Creditcard;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class CreditcardRowMapper extends AbstractRowMapper<Creditcard>{

    private static final String CLASSNAME =
            utils.LCUtil.getCurrentClassName();

    private static final DateTimeFormatter EXPIRY_FORMAT =
            DateTimeFormatter.ofPattern("MM/yy");

    @Override
    public Creditcard map(ResultSet rs) throws SQLException {
        final String methodName =
                utils.LCUtil.getCurrentMethodName();

        try {
            var creditcard = new Creditcard();

            creditcard.setCreditCardIdPlayer(getInteger(rs,"CreditcardIdPlayer"));
            creditcard.setCreditcardHolder(getString(rs, "CreditcardHolder"));
            creditcard.setCreditcardNumber(getString(rs, "CreditcardNumber"));
            creditcard.setCreditcardVerificationCode(getShort(rs,"CreditcardVerificationCode"));
            creditcard.setCreditcardType(enumeration.CreditcardBrand.valueOf(getString(rs, "CreditcardBrand")));

            // Expiration date (LocalDateTime)
            var expirationLdt = getLocalDateTime(rs, "CreditcardExpirationDate");

            creditcard.setCreditCardExpirationDateLdt(expirationLdt);

            if (expirationLdt != null) {
                // YearMonth
                var yearMonth = YearMonth.from(expirationLdt);
                creditcard.setExpirationDateYearMonth(yearMonth);

                // String MM/yy
                creditcard.setCreditCardExpirationDateString(yearMonth.format(EXPIRY_FORMAT)
                );
            }

            return creditcard;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
} // end class
