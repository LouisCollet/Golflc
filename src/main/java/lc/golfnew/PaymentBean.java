
package lc.golfnew;

import events.PaymentEvent;
import events.PaymentTypeEnum;
import static events.PaymentTypeEnum.CREDIT;
import static events.PaymentTypeEnum.DEBIT;
import handlers.PaymentHandler;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import qualifiers.Credit;
import qualifiers.Debit;

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

    private BigDecimal amount = new BigDecimal(0);
    private PaymentTypeEnum paymentOption; // = PaymentTypeEnum.DEBIT;

    @PostConstruct
    public void init() {
            LOG.info("entering init");
        amount = BigDecimal.ZERO;
        paymentOption = PaymentTypeEnum.CREDIT;  //fills checkbutton
        PaymentHandler ph = new PaymentHandler();
            LOG.info("before setPaymentsNull");
        ph.setPaymentsNull();

    }

    // Pay Action
    public String pay() {
            LOG.info("entering pay");
        PaymentEvent pe = new PaymentEvent();
        pe.setType(paymentOption);
        pe.setAmount(amount);
        pe.setDatetime(new Date());
            LOG.info("PaymentEvent pe is now : " + pe.toString() );

        switch (pe.getType()) {
            case DEBIT:
                 LOG.info("this is DEBIT");
                debitEventProducer.fire(pe);   //c'est ici que cela se passe'
                break;
            case CREDIT:
                creditEventProducer.fire(pe);
                break;
            default:
                LOG.error("pay - invalid payment option");
                break;
        }
        // paymentAction

        return "debit-credit.xhtml?faces-redirect=true";// modifié !!
    }

    // Reset Action
    public void reset() {
        LOG.info("from reset");
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
        LOG.info("getAmount = " + amount);
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        LOG.info("setAmount = " + amount);
        this.amount = amount;
    }

}