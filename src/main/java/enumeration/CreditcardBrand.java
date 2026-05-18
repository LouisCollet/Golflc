package enumeration;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.regex.Pattern;

public enum CreditcardBrand {

    VISA(
        "^4[0-9]{3,}$",
        new LengthCheck(13, 16, 19)),

    MAESTRO(
        "^(?:5018|5020|5038|5893|6304|6759|676[1-3])[0-9]*$",
        new LengthRangeCheck(12, 19)),

    MASTERCARD(
        "^(?:5[1-5][0-9]{2}|222[1-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]*$",
        new LengthCheck(16)),

    JCB(
        "^(?:2131|1800|35[0-9]{2})[0-9]*$",
        new LengthRangeCheck(15, 19)),

    UPI(null, null),   // user selection only — no card number pattern

    DINERS_CLUB(
        "^3(?:0[0-5]|[68][0-9])[0-9]{1,}$",
        new LengthRangeCheck(14, 19)),

    AMERICAN_EXPRESS(
        "^3[47][0-9]{2,}$",
        new LengthCheck(15)),

    DISCOVER(
        "^6(?:011|5[0-9]{3})[0-9]*$",
        new LengthCheck(16, 19)),

    CHINA_UNION_PAY(
        "^62[0-9]{2,}$",
        new LengthRangeCheck(16, 19));

    private static final class LengthCheck implements Predicate<Integer> {
        private final int[] validLengths;
        LengthCheck(final int... validLengths) { this.validLengths = validLengths; }
        @Override
        public boolean test(final Integer length) {
            for (final int validLength : validLengths) {
                if (length == validLength) return true;
            }
            return false;
        }
    }

    private static final class LengthRangeCheck implements Predicate<Integer> {
        private final int minLength;
        private final int maxLength;
        LengthRangeCheck(final int minLength, final int maxLength) {
            this.minLength = minLength;
            this.maxLength = maxLength;
        }
        @Override
        public boolean test(final Integer length) {
            return length >= minLength && length <= maxLength;
        }
    }

    private interface Predicate<T> {
        boolean test(T t);
    }

    public static CreditcardBrand from(final String accountNumber) {
        if (isBlank(accountNumber)) return null;
        for (final CreditcardBrand brand : values()) {
            if (brand.pattern == null) continue;
            if (brand.pattern.matcher(accountNumber).matches()) return brand;
        }
        return null;
    }

    public boolean isLengthValid(final int accountNumberLength) {
        return lengthCheck != null && lengthCheck.test(accountNumberLength);
    }

    private final Pattern pattern;
    private final Predicate<Integer> lengthCheck;

    CreditcardBrand(final String patternRegEx, final Predicate<Integer> lengthCheck) {
        this.pattern     = patternRegEx != null ? Pattern.compile(patternRegEx) : null;
        this.lengthCheck = lengthCheck;
    }

} // end enum
