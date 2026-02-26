package cache;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/*
 * Thread-safe cache simple avec TTL, basé sur records.
 */
@ApplicationScoped
public class MethodCache {

    // Le cache stocke la clé (ex: "CourseService#getAllCourses()") et la valeur
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Record représentant une entrée du cache.
     * expiresAt = timestamp en millisecondes
     */
    public record CacheEntry(Object value, long expiresAt) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    /**
     * Stocke une valeur avec TTL en millisecondes.
     */
    public void put(String key, Object value, long ttlMillis) {
        long expiresAt = System.currentTimeMillis() + ttlMillis;
        cache.put(key, new CacheEntry(value, expiresAt));
    }

    /**
     * Récupère une valeur si elle n'est pas expirée, sinon retourne null.
     */
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;
        if (entry.isExpired()) {
            cache.remove(key); // nettoyage automatique
            return null;
        }
        return entry.value();
    }

    /**
     * Invalide une entrée par clé.
     */
    public void invalidate(String key) {
        cache.remove(key);
    }

    /**
     * Invalide toutes les entrées dont la clé commence par le préfixe.
     */
    public void invalidateByPrefix(String prefix) {
        cache.keySet().removeIf(k -> k.startsWith(prefix));
    }

    /**
     * Vide complètement le cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Retourne la taille actuelle du cache (utile pour test / debug).
     */
    public int size() {
        return cache.size();
    }

    /**
     * Factory pour créer un CacheEntry avec TTL.
     * Utile si tu veux manipuler directement le record.
     */
    public static CacheEntry ofTTL(Object value, long ttlMillis) {
        return new CacheEntry(value, System.currentTimeMillis() + ttlMillis);
    }
}
