
package payment;
public class PaymentAlreadyRegisteredException
        extends RuntimeException {

    public PaymentAlreadyRegisteredException(String message) {
        super(message);
    }
}
