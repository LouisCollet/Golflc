
package payment;
import entite.Greenfee;
import static interfaces.Log.LOG;

public record GreenfeePayment(Greenfee greenfee)implements PaymentTarget {

    @Override
    public void setPaymentReference(String reference) {
         LOG.debug("GreenfeePayment.setPaymentReference = " + reference);
        greenfee.setPaymentReference(reference);
    }
}
