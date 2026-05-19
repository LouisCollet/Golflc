package payment;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PaymentStateStore implements Serializable {

    private static final long serialVersionUID = 1L;

    /** TTL = 30 minutes */
    private static final long TTL_MILLIS = 30 * 60 * 1000L;

    private final ConcurrentHashMap<String, PaymentTransaction> store = new ConcurrentHashMap<>();

    public PaymentStateStore() { } // end constructor

    public void store(String nonce, PaymentTransaction tx) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cleanup();
        store.put(nonce, tx);
        LOG.debug("stored transaction size={}", store.size());
    } // end method

    public PaymentTransaction get(String nonce) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (nonce == null) {
            LOG.warn("nonce is null");
            return null;
        }
        PaymentTransaction tx = store.get(nonce);
        if (tx == null) {
            LOG.warn("transaction not found nonce={}", nonce);
            return null;
        }
        if (tx.isExpired(TTL_MILLIS)) {
            LOG.warn("transaction expired nonce={}", nonce);
            store.remove(nonce);
            return null;
        }
        return tx;
    } // end method

    public PaymentTransaction consume(String nonce) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        PaymentTransaction tx = get(nonce);
        if (tx == null) {
            return null;
        }
        if (tx.isCompleted()) {
            LOG.warn("SECURITY: transaction already consumed nonce={}", nonce);
            return null;
        }
        tx.setCompleted(true);
        LOG.debug("transaction consumed nonce={}", nonce);
        return tx;
    } // end method

    public void remove(String nonce) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        store.remove(nonce);
    } // end method

    private void cleanup() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        int before = store.size();
        store.entrySet().removeIf(entry -> entry.getValue().isExpired(TTL_MILLIS));
        int removed = before - store.size();
        if (removed > 0) {
            LOG.debug("cleaned up {} expired transactions", removed);
        }
    } // end method

} // end class
