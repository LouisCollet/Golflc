package payment;

public interface PaymentRegistrar<T extends PaymentTarget> {
    boolean register(T payment) throws Exception;
}
