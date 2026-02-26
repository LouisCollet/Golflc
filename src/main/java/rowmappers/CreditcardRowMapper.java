
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
            creditcard.setCreditcardType(getString(rs, "CreditcardType"));

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
}


/*
 import entite.Creditcard;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class CreditcardRowMapper implements RowMapper<Creditcard> {
 
    @Override
   public Creditcard map(ResultSet rs) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();  
   try{  
           //LOG.debug("entering map for method = " + methodName);
            Creditcard creditcard = new Creditcard();
            creditcard.setCreditCardIdPlayer(rs.getInt("CreditcardIdPlayer"));
            creditcard.setCreditcardHolder(rs.getString("CreditcardHolder"));
            creditcard.setCreditcardNumber(rs.getString("CreditcardNumber"));
            creditcard.setCreditCardExpirationDateLdt(rs.getTimestamp("CreditcardExpirationDate").toLocalDateTime());
            creditcard.setCreditcardVerificationCode(rs.getShort("CreditcardVerificationCode"));
            creditcard.setCreditcardType(rs.getString("CreditcardType"));
            // new 31-07-2025 compléter date version String
            creditcard.setCreditCardExpirationDateString(
                    creditcard.getCreditCardExpirationDateLdt().getMonthValue() + "/" +   // if you just wanted the two-digit number,
                            creditcard.getCreditCardExpirationDateLdt().getYear()% 100
            );
             LOG.debug("expiration dateString = " +  creditcard.getCreditCardExpirationDateString());
       //     LOG.debug("expirationDateYearMonth fixed = " +  YearMonth.of(2025,02));  // expirationDateYearMonth = 2025-02
             // Create a YearMonth object
        YearMonth thisYearMonth =
                YearMonth.of(
                creditcard.getCreditCardExpirationDateLdt().getYear(),
                creditcard.getCreditCardExpirationDateLdt().getMonthValue());
        // Create a DateTimeFormatter string
    //    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM");  // MM/yy
        // Format this year-month
        LOG.debug("expirationDateYearMonth formatted from Ldt = " + thisYearMonth.format(DateTimeFormatter.ofPattern("MM/yy")));
            creditcard.setExpirationDateYearMonth(thisYearMonth);
         return creditcard;
 }catch(Exception e){
        handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class */