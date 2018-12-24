
package handlers;
import events.PaymentEvent;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import qualifiers.Credit;
import qualifiers.Debit;
/**
 *
 * @author Collet
 */
@SessionScoped
public class PaymentHandler implements IDebitEventObserver, ICreditEventObserver, Serializable {

    private static final long serialVersionUID = 1L;

    List<PaymentEvent> payments = new ArrayList<>();

    @Produces
    @Named
    public List<PaymentEvent> getPayments() {
        return payments;
    }

    public void onCreditPaymentEvent(@Observes @Credit PaymentEvent event) {

        LOG.info("Processing the credit operation " + event);
        payments.add(event);
    }

    public void onDebitPaymentEvent(@Observes @Debit PaymentEvent event) {
        LOG.info("Processing the debit operation " + event);
        payments.add(event);
    }

    public void setPayments(List<PaymentEvent> payments) {
        this.payments = payments;
    }

} //end class
