
package payment;

import entite.Subscription;
import static interfaces.Log.LOG;

public record SubscriptionPayment(Subscription subscription)implements PaymentTarget {

    @Override
    public void setPaymentReference(String reference) {
        LOG.debug("SubscriptionPayment.setPaymentReference = " + reference);
        subscription.setPaymentReference(reference);
    }
}
