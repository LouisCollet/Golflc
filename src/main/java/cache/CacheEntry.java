
package cache;

import static interfaces.Log.LOG;

public record CacheEntry(Object value, long expiresAt) {

    public static CacheEntry ofTTL(Object value, long ttlMillis) {
        LOG.debug("cacheentry for value = " + value);
        return new CacheEntry(value, System.currentTimeMillis() + ttlMillis);
    }

    public boolean isExpired() {
        LOG.debug("cacheentry is expired " + expiresAt);
        return System.currentTimeMillis() > expiresAt;
    }
}