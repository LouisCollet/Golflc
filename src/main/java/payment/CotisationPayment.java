
package payment;
import entite.Cotisation;
import static interfaces.Log.LOG;

public record CotisationPayment(Cotisation cotisation) implements PaymentTarget {

    @Override
    public void setPaymentReference(String reference) {
         LOG.debug("CotisationPayment.setPaymentReference = " + reference);
        cotisation.setPaymentReference(reference);
    }
}
