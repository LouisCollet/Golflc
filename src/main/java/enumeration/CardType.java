package enumeration;

import static interfaces.Log.LOG;
import java.util.regex.Pattern;
// https://github.com/sualeh/creditcardnumber/blob/main/src/main/java/us/fatehi/creditcardnumber/CardBrand.java

// 22-08-2025 remplacé par CarBrand, version plus récente 
public enum CardType {
    UNKNOWN,
    VISA("^4[0-9]{12}(?:[0-9]{3}){0,2}$"),
    //Visa("^4[0-9]{3,}$", new LengthCheck(13, 16, 19)),
    MASTERCARD("^(?:5[1-5]|2(?!2([01]|20)|7(2[1-9]|3))[2-7])\\d{14}$"),
    AMERICAN_EXPRESS("^3[47][0-9]{13}$"),
    DINERS_CLUB("^3(?:0[0-5]\\d|095|6\\d{0,2}|[89]\\d{2})\\d{12,15}$"),
    DISCOVER("^6(?:011|[45][0-9]{2})[0-9]{12}$"),
    JCB("^(?:2131|1800|35\\d{3})\\d{11}$"),
    CHINA_UNION_PAY("^62[0-9]{14,17}$");

    private Pattern pattern;

    CardType() {
        this.pattern = null;
    }

    CardType(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public static CardType detect(String cardNumber) {
    //   LOG.debug("entering detect with cardNumber = " + cardNumber);
        for (CardType cardType : CardType.values()) {
            if (null == cardType.pattern) continue;
            if (cardType.pattern.matcher(cardNumber).matches()){
    //            LOG.debug("cardType detected = " + cardType);
                return cardType;
            }
        }
   //     LOG.debug("cardType detected = " + UNKNOWN);
        return UNKNOWN;
    }
}