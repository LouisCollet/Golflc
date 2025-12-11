
package payments;
import jakarta.enterprise.event.Observes;

public interface ICreditEventObserver {
    void onCreditPaymentEvent(@Observes @Credit PaymentEvent event);
}