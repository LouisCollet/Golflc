
package cache;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache centralisé thread-safe avec TTL.
 * Typage sécurisé par méthodes dédiées.
 */
@ApplicationScoped
public class GenericMethodCache {

    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    /**
     * Stocke une valeur dans le cache, avec TTL en millisecondes.
     */
    public void put(String key, Object value, long ttlMillis) {
        LOG.debug("we put the key = " + key);
        if (value != null) {
            store.put(key, CacheEntry.withTTL(value, ttlMillis));
        }
    }

    /**
     * Récupère un objet typé depuis le cache.
     */
    public <T> T get(String key, Class<T> type) {
        LOG.debug("we get the key = " + key);
        CacheEntry entry = store.get(key);

        if (entry == null || entry.isExpired()) {
            store.remove(key);
            return null;
        }

        Object value = entry.value();
        return type.isInstance(value) ? type.cast(value) : null;
    }

    /**
     * Récupère une liste typée depuis le cache.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        CacheEntry entry = store.get(key);

        if (entry == null || entry.isExpired()) {
            store.remove(key);
            return null;
        }

        Object value = entry.value();
        if (value instanceof List<?>) {
            return (List<T>) value;
        }
        return null;
    }

    /** Invalide une entrée par clé. */
    public void invalidate(String key) {
         LOG.debug("we invalidate the key = " + key);
        store.remove(key);
    }

    /** Invalide toutes les clés dont le préfixe commence par prefix. */
    public void invalidateByPrefix(String prefix) {
        store.keySet().removeIf(k -> k.startsWith(prefix));
    }

    /** Vide entièrement le cache. */
    public void clear() {
         LOG.debug("we clear the cache ! ");
        store.clear();
    }

    /* =========================
       CacheEntry interne
       ========================= */

    /**
     * Une entrée de cache immuable avec date d’expiration.
     */
    public static final class CacheEntry {

        private final Object value;
        private final long expiresAt;

        private CacheEntry(Object value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        public static CacheEntry withTTL(Object value, long ttlMillis) {
            return new CacheEntry(value, System.currentTimeMillis() + ttlMillis);
        }

        public Object value() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
