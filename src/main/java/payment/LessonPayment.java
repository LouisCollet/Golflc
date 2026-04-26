package payment;

import entite.Lesson;
import static interfaces.Log.LOG;
import java.util.List;

/**
 * PaymentTarget wrapping a list of lessons to be paid.
 * NOTE: entite.LessonPayment is the DB record; this is the payment-flow target.
 */
public record LessonPayment(List<Lesson> lessons) implements PaymentTarget {

    @Override
    public void setPaymentReference(String reference) {
        LOG.debug("LessonPayment.setPaymentReference = " + reference);
        // Reference is stored on the Creditcard; no per-lesson update needed here
    }

} // end record
