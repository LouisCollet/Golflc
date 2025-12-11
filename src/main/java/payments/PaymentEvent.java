package payments;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Digits;

public class PaymentEvent {

    private PaymentTypeEnum type; // credit or debit
    @Digits(integer = 10, fraction = 2, message = "Invalid value for Amount")
    private BigDecimal amount;
    private LocalDateTime datetime;
    private BigDecimal total;
public PaymentEvent(){ // constructor 1;
    LOG.debug("from constructor PaymentEvent");
     amount = BigDecimal.ZERO;
     LOG.debug("from constructor PaymentEvent amount = " + amount);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentTypeEnum getType() {
        return type;
    }

    public void setType(PaymentTypeEnum type) {
        this.type = type;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Override
    public String toString() {
        LOG.debug("starting toString of PaymentEvent");
        
        return "PaymentEvent : " 
                + NEW_LINE + " Date = " + getDatetime() //.format(ZDF_TIME)  
     //           + ZDF_TIME.format(datetime) npe si date = null !
                + " Amount = " + getAmount()
                + " Movement = " + getType()
                + " Total : " + getTotal();
    } 

} //end class