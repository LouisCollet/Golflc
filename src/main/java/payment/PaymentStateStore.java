package payment;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side store for pending payment transactions.
 * Bridges the gap between JSF session state and stateless REST callbacks.
 *
 * Keyed by nonce (unique per transaction).
 * Auto-evicts expired entries on every access (lazy cleanup).
 */
@ApplicationScoped
public class PaymentStateStore implements Serializable {

    private static final long serialVersionUID = 1L;

    /** TTL = 30 minutes */
    private static final long TTL_MILLIS = 30 * 60 * 1000L;

    private final ConcurrentHashMap<String, PaymentTransaction> store = new ConcurrentHashMap<>();

    public PaymentStateStore() { } // end constructor

    /**
     * Store a new payment transaction.
     */
    public void store(String nonce, PaymentTransaction tx) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - nonce=" + nonce);
        cleanup();
        store.put(nonce, tx);
        LOG.debug(methodName + " - stored transaction, store size=" + store.size());
    } // end method

    /**
     * Retrieve a transaction by nonce. Returns null if not found or expired.
     */
    public PaymentTransaction get(String nonce) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - nonce=" + nonce);
        if (nonce == null) {
            LOG.warn(methodName + " - nonce is null");
            return null;
        }
        PaymentTransaction tx = store.get(nonce);
        if (tx == null) {
            LOG.warn(methodName + " - transaction not found for nonce=" + nonce);
            return null;
        }
        if (tx.isExpired(TTL_MILLIS)) {
            LOG.warn(methodName + " - transaction expired for nonce=" + nonce);
            store.remove(nonce);
            return null;
        }
        return tx;
    } // end method

    /**
     * Retrieve and mark as completed (one-time consume).
     * Returns null if not found, expired, or already completed.
     */
    public PaymentTransaction consume(String nonce) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - nonce=" + nonce);
        PaymentTransaction tx = get(nonce);
        if (tx == null) {
            return null;
        }
        if (tx.isCompleted()) {
            LOG.warn(methodName + " - SECURITY: transaction already consumed for nonce=" + nonce);
            return null;
        }
        tx.setCompleted(true);
        LOG.debug(methodName + " - transaction consumed for nonce=" + nonce);
        return tx;
    } // end method

    /**
     * Remove a transaction from the store.
     */
    public void remove(String nonce) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - nonce=" + nonce);
        store.remove(nonce);
    } // end method

    /**
     * Lazy cleanup — remove all expired entries.
     */
    private void cleanup() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        int before = store.size();
        store.entrySet().removeIf(entry -> entry.getValue().isExpired(TTL_MILLIS));
        int removed = before - store.size();
        if (removed > 0) {
            LOG.debug(methodName + " - cleaned up " + removed + " expired transactions");
        }
    } // end method

} // end class
