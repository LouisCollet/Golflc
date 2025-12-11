package payments;

import jakarta.enterprise.event.Observes;

public interface IDebitEventObserver {
       void onDebitPaymentEvent(@Observes @Debit PaymentEvent event);
}
