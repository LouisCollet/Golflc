
package handlers;
import events.PaymentEvent;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import qualifiers.Credit;
import qualifiers.Debit;

@SessionScoped
@Named("paymentHandler")
public class PaymentHandler implements IDebitEventObserver, ICreditEventObserver, Serializable {

    private static final long serialVersionUID = 1L;
    private BigDecimal totalPH;
    List<PaymentEvent> payments;//  = new ArrayList<>();
public PaymentHandler() // constructor 1
    {
     totalPH = BigDecimal.ZERO;
     payments = new ArrayList<>();
}
    @Produces
    @Named
    public List<PaymentEvent> getPayments() {
        return payments;
    }

    public void onCreditPaymentEvent(@Observes @Credit PaymentEvent event) {
        LOG.info("Processing the credit operation " + event);
        totalPH = totalPH.add(event.getAmount()); 
        event.setTotal(totalPH);
        payments.add(event);
    }

    public void onDebitPaymentEvent(@Observes @Debit PaymentEvent event) {
            LOG.info("Processing the debit operation " + event);
        totalPH = totalPH.subtract(event.getAmount()); 
        event.setTotal(totalPH);
        payments.add(event);
    }

    public void setPayments(List<PaymentEvent> payments) {
        LOG.info("setPayments = " + payments);
        this.payments = payments;
    }
    public void setPaymentsNull() {
   //     LOG.info("setPaymentsNull = " + payments);
        payments = new ArrayList<>();
        totalPH = BigDecimal.ZERO;
     //   PaymentBean pb = new PaymentBean();
    //lm    pb.init();
        
    }
} //end class
