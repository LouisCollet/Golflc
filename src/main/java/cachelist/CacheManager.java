package cachelist;

import static interfaces.Log.LOG;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire de cache générique thread-safe pour les listes
 * Supporte les metrics, TTL, et invalidation
 *
 * @param <T> Type des éléments dans la liste cachée
 * @author Votre nom
 * @version 1.0
 * @since 2025-01-18
 */
public class CacheManager<T> {

    private final String cacheName;
    private volatile List<T> cachedList = null;
    private final CacheMetrics metrics = new CacheMetrics();
    private final CacheConfig config;

    /**
     * Configuration du cache
     */
    public static class CacheConfig {
        private final long ttlMillis;
        private final boolean enableMetrics;

        public CacheConfig(long ttlMillis, boolean enableMetrics) {
            this.ttlMillis = ttlMillis;
            this.enableMetrics = enableMetrics;
        }

        public static CacheConfig defaultConfig() {
            return new CacheConfig(
                Duration.ofHours(1).toMillis(), // TTL 1 heure par défaut
                true // Metrics activées
            );
        }

        public static CacheConfig withTTL(Duration ttl) {
            return new CacheConfig(ttl.toMillis(), true);
        }

        public static CacheConfig noExpiration() {
            return new CacheConfig(Long.MAX_VALUE, true);
        }
    }

    /**
     * Classe pour les statistiques du cache
     */
    public static class CacheMetrics {
        private volatile Instant lastLoadTime;
        private volatile Instant cacheExpirationTime;
        private volatile long loadDurationMs;
        private volatile int loadCount;
        private volatile int hitCount;
        private volatile int missCount;
        private volatile int invalidationCount;

        public Instant getLastLoadTime() { return lastLoadTime; }
        public Instant getCacheExpirationTime() { return cacheExpirationTime; }
        public long getLoadDurationMs() { return loadDurationMs; }
        public int getLoadCount() { return loadCount; }
        public int getHitCount() { return hitCount; }
        public int getMissCount() { return missCount; }
        public int getInvalidationCount() { return invalidationCount; }

        public double getHitRate() {
            int total = hitCount + missCount;
            return total > 0 ? (double) hitCount / total * 100 : 0.0;
        }

        public boolean isExpired() {
            return cacheExpirationTime != null && 
                   Instant.now().isAfter(cacheExpirationTime);
        }

        synchronized void recordLoad(long durationMs, long ttlMillis) {
            this.lastLoadTime = Instant.now();
            this.cacheExpirationTime = lastLoadTime.plusMillis(ttlMillis);
            this.loadDurationMs = durationMs;
            this.loadCount++;
        }

        synchronized void recordHit() { this.hitCount++; }
        synchronized void recordMiss() { this.missCount++; }
        synchronized void recordInvalidation() { this.invalidationCount++; }

        synchronized void reset() {
            lastLoadTime = null;
            cacheExpirationTime = null;
            loadDurationMs = 0;
            loadCount = 0;
            hitCount = 0;
            missCount = 0;
            invalidationCount = 0;
        }

        @Override
        public String toString() {
            return String.format(
                "CacheMetrics[loads=%d, hits=%d, misses=%d, invalidations=%d, hitRate=%.2f%%, " +
                "lastLoad=%s, loadTime=%dms, expires=%s, expired=%s]",
                loadCount, hitCount, missCount, invalidationCount, getHitRate(),
                lastLoadTime != null ? lastLoadTime : "never",
                loadDurationMs,
                cacheExpirationTime != null ? cacheExpirationTime : "never",
                isExpired()
            );
        }
    }

    /**
     * Constructeur
     *
     * @param cacheName Nom du cache pour le logging
     * @param config Configuration du cache
     */
    public CacheManager(String cacheName, CacheConfig config) {
        this.cacheName = cacheName;
        this.config = config;
    }

    /**
     * Constructeur avec configuration par défaut
     */
    public CacheManager(String cacheName) {
        this(cacheName, CacheConfig.defaultConfig());
    }

    /**
     * Récupère les données du cache ou les charge si nécessaire
     *
     * @param loader Fonction de chargement des données (peut lancer des exceptions)
     * @return Liste immutable des données
     * @throws Exception si le chargement échoue
     */
    public List<T> get(ThrowingSupplier<List<T>> loader) throws Exception {
        // Vérifier si le cache est valide
        if (isCacheValid()) {
            if (config.enableMetrics) metrics.recordHit();
            LOG.debug("[{}] Cache hit - {} items (hit rate: {:.2f}%)",
                cacheName, cachedList.size(), metrics.getHitRate());
            return List.copyOf(cachedList);
        }

        // Cache miss ou expiré
        if (config.enableMetrics) metrics.recordMiss();
        LOG.debug("[{}] Cache miss - Loading from source", cacheName);

        return loadAndCache(loader);
    }

    /**
     * Vérifie si le cache est valide (non null et non expiré)
     */
    private boolean isCacheValid() {
        return cachedList != null && !metrics.isExpired();
    }

    /**
     * Charge les données et met en cache
     */
    private synchronized List<T> loadAndCache(ThrowingSupplier<List<T>> loader) throws Exception {
        // Double-check locking
        if (isCacheValid()) {
            return List.copyOf(cachedList);
        }

        Instant startTime = Instant.now();

        try {
            List<T> loadedData = loader.get();

            if (loadedData == null) {
                throw new IllegalStateException("Loader returned null");
            }

            long duration = Duration.between(startTime, Instant.now()).toMillis();

            cachedList = List.copyOf(loadedData);

            if (config.enableMetrics) {
                metrics.recordLoad(duration, config.ttlMillis);
            }

            LOG.info("[{}] Loaded {} items in {}ms", cacheName, loadedData.size(), duration);

            return List.copyOf(cachedList);

        } catch (Exception e) {
            LOG.error("[{}] Failed to load data", cacheName, e);
            throw e;
        }
    }

    /**
     * Invalide le cache
     */
    public synchronized void invalidate() {
        LOG.debug("[{}] Cache invalidated", cacheName);
        cachedList = null;
        if (config.enableMetrics) {
            metrics.recordInvalidation();
        }
    }

    /**
     * Réinitialise le cache et les metrics
     */
    public synchronized void reset() {
        LOG.info("[{}] Cache reset", cacheName);
        cachedList = null;
        metrics.reset();
    }

    /**
     * Récupère le cache actuel sans recharger
     */
    public Optional<List<T>> getCached() {
        List<T> cached = cachedList;
        return cached != null && !metrics.isExpired() 
            ? Optional.of(List.copyOf(cached)) 
            : Optional.empty();
    }

    /**
     * Vérifie si le cache est chargé et valide
     */
    public boolean isLoaded() {
        return isCacheValid();
    }

    /**
     * Récupère la taille du cache
     */
    public int size() {
        List<T> cached = cachedList;
        return cached != null ? cached.size() : 0;
    }

    /**
     * Récupère les metrics
     */
    public CacheMetrics getMetrics() {
        return metrics;
    }

    /**
     * Récupère le nom du cache
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * Récupère la configuration du cache
     */
    public CacheConfig getConfig() {
        return config;
    }
}