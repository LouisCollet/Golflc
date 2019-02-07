
package handlers;

import events.PaymentEvent;
import javax.enterprise.event.Observes;
import qualifiers.Debit;


public interface IDebitEventObserver {
       void onDebitPaymentEvent(@Observes @Debit PaymentEvent event);
}
