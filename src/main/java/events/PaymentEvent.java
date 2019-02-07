package events;

import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.Log.NEW_LINE;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.Digits;

public class PaymentEvent {

    private PaymentTypeEnum type; // credit or debit
    @Digits(integer = 10, fraction = 2, message = "Invalid value for Amount")
    private BigDecimal amount;
    private Date datetime;
    private BigDecimal total;
public PaymentEvent() // constructor 1
    {
     amount = BigDecimal.ZERO;
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

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String toString() {
        return "PaymentEvent : " 
                + NEW_LINE + " Date = " + SDF_TIME.format(getDatetime())
                + NEW_LINE + " Montant = " + getAmount()
                + NEW_LINE + " Mouvement = " + getType()
                + NEW_LINE + " Total : " + getTotal();
    } 

} //end class