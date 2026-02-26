package inscriptionlist;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import static interfaces.Log.LOG;

/**
 * Repository thread-safe avec cache TTL pour tout type d'entité.
 * T doit être un DTO (ECourseList, Player, etc.)
 */
public abstract class AbstractCachedRepository<T> {

    // 🔐 Cache immuable pour sécurité thread
    private volatile List<T> cache = List.of();
    private volatile long cacheTimestamp = 0L;

    // 🕰 TTL en millisecondes
    private volatile long ttlMillis;

    // 🔒 Lock pour double-checked locking
    private final ReentrantLock lock = new ReentrantLock();

    // 🔁 Sliding TTL (réinitialise le timestamp à chaque accès si true)
    private volatile boolean slidingTtl = false;

    protected AbstractCachedRepository(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    /**
     * Retourne le cache, recharge si TTL expiré
     */
    public List<T> list(Connection conn) throws SQLException {
        // Fast path (sans lock)
        if (!isExpired()) {
            if (slidingTtl) {
                cacheTimestamp = System.currentTimeMillis();
            }
            return cache;
        }

        // Slow path (double-check avec lock)
        lock.lock();
        try {
            if (!isExpired()) {
                if (slidingTtl) {
                    cacheTimestamp = System.currentTimeMillis();
                }
                return cache;
            }

            LOG.debug("Reloading cache (TTL expired)");
            List<T> loaded = loadFromDatabase(conn);

            // Publication atomique
            cache = List.copyOf(loaded);
            cacheTimestamp = System.currentTimeMillis();

            return cache;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Force la purge du cache
     */
    public void purge() {
        lock.lock();
        try {
            cache = List.of();
            cacheTimestamp = 0L;
            LOG.debug("Cache purged manually");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Définit dynamiquement un nouveau TTL
     */
    public void setTtlMillis(long ttlMillis) {
        this.ttlMillis = ttlMillis;
        LOG.debug("TTL updated dynamically to " + ttlMillis + " ms");
    }

    /**
     * Active ou désactive le sliding TTL
     */
    public void setSlidingTtl(boolean sliding) {
        this.slidingTtl = sliding;
        LOG.debug("Sliding TTL set to " + sliding);
    }

    /**
     * Vérifie si le cache est expiré
     */
    private boolean isExpired() {
        long now = System.currentTimeMillis();
        
        var v = (now - cacheTimestamp) > ttlMillis;
           LOG.debug("is expired with v = " + v);
       // return (now - cacheTimestamp) > ttlMillis;
        return v;
    }

    /**
     * Méthode abstraite à implémenter pour charger la liste depuis la DB
     */
    protected abstract List<T> loadFromDatabase(Connection conn) throws SQLException;
}
