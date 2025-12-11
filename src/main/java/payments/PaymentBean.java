
package payments;

import static payments.PaymentTypeEnum.CREDIT;
import static payments.PaymentTypeEnum.DEBIT;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import utils.LCUtil;
//https://www.javatips.net/api/javamoney-examples-master/web/javamoney-payment-cdi-event/src/main/java/org/javamoney/examples/cdi/payment/beans/PaymentBean.java
@Named("paymentBean")
@SessionScoped
public class PaymentBean implements Serializable {
    private static final long serialVersionUID = 1L;

 // Events producers
    @Inject
    @Credit
    Event<PaymentEvent> creditEventProducer;

    @Inject
    @Debit
    Event<PaymentEvent> debitEventProducer;

 //   private BigDecimal amount = new BigDecimal(0);
    private BigDecimal amount = BigDecimal.ZERO;
    private PaymentTypeEnum paymentOption = PaymentTypeEnum.DEBIT;
 
    @PostConstruct
    public void init() {
            LOG.debug("entering PostConstruct init");
   // enlevé 10-05-2020     amount = BigDecimal.ZERO; double emploi voir line 33
        paymentOption = PaymentTypeEnum.CREDIT;  //fills checkbutton
//            LOG.debug("before setPaymentsNull");
        new PaymentHandler().setPaymentsNull();
    }
 public String pay() {
 try{
            LOG.debug("entering pay");
        PaymentEvent pe = new PaymentEvent();
  //          LOG.debug("line 01 " + pe);
        pe.setType(paymentOption);
            LOG.debug("line 02 type = " + pe.getType());
        pe.setAmount(amount);
            LOG.debug("line 03 amount = " + pe.getAmount());
        pe.setDatetime(LocalDateTime.now());
            LOG.debug("line 04 date = " + pe.getDatetime());
            LOG.debug("PaymentEvent pe is now : " + pe.toString() );
//if(! pe.getAmount().equals(BigDecimal.ZERO)){ // does not work with 0.00 or 0.00000000
    if(pe.getAmount().compareTo(BigDecimal.ZERO) == 0){ 
         String msg = "nothing to do amount  = " + pe.getAmount();
         LOG.debug(msg);
         LCUtil.showMessageInfo(msg);
        }else{
           switch (pe.getType()) {
            case DEBIT -> {
                LOG.debug("this is DEBIT");
                debitEventProducer.fire(pe);   //c'est ici que cela se passe'
                    }
            case CREDIT -> creditEventProducer.fire(pe);
            default -> LOG.error("pay - invalid payment option");
        } //end switch
       }
        return "debit-credit.xhtml?faces-redirect=true";// modifié !!
  //  }
}catch (Exception ex){
    String msg = "Exception in " + "PaymentBean" + " / " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
    // Reset
}
} //end method pay
    public void reset() {
        LOG.debug("from reset");
        init();

    }

    public Event<PaymentEvent> getCreditEventLauncher() {
        return creditEventProducer;
    }

    public void setCreditEventLauncher(Event<PaymentEvent> creditEventLauncher) {
        this.creditEventProducer = creditEventLauncher;
    }

    public Event<PaymentEvent> getDebitEventLauncher() {
        return debitEventProducer;
    }

    public void setDebitEventLauncher(Event<PaymentEvent> debitEventLauncher) {
        this.debitEventProducer = debitEventLauncher;
    }

    public PaymentTypeEnum getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(PaymentTypeEnum paymentOption) {
        this.paymentOption = paymentOption;
    }

    public BigDecimal getAmount() {
        LOG.debug("getAmount = " + amount);
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        LOG.debug("setAmount = " + amount);
        this.amount = amount;
    }

}