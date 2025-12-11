
package payments;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@SessionScoped
@Named("paymentHandler")
public class PaymentHandler implements IDebitEventObserver, ICreditEventObserver, Serializable {

    private static final long serialVersionUID = 1L;
    private BigDecimal totalPH;
    List<PaymentEvent> payments;
public PaymentHandler(){ // constructor 1
     totalPH = BigDecimal.ZERO;
     payments = new ArrayList<>();
}
    @Produces
    @Named
    public List<PaymentEvent> getPayments() {
        return payments;
    }

    @Override
    public void onCreditPaymentEvent(@Observes @Credit PaymentEvent event) {
        LOG.debug("Processing the credit operation " + event.toString());
        totalPH = totalPH.add(event.getAmount()); 
        event.setTotal(totalPH);
        payments.add(event);
    }

    @Override
    public void onDebitPaymentEvent(@Observes @Debit PaymentEvent event) {
            LOG.debug("Processing the debit operation " + event.toString());
        totalPH = totalPH.subtract(event.getAmount()); 
        event.setTotal(totalPH);
        payments.add(event);
    }

    public void setPayments(List<PaymentEvent> payments) {
        LOG.debug("setPayments = " + payments);
        this.payments = payments;
    }
    public void setPaymentsNull() {
        LOG.debug("setPaymentsNull = " + payments);
        payments = new ArrayList<>();
        totalPH = BigDecimal.ZERO;
        
    }
} //end class
