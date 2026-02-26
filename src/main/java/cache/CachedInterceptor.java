package cache;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import static interfaces.Log.LOG;

@Interceptor
@Cached // ou @InvalidateCache si tu veux que l'interceptor soit appelé pour les deux annotations
public class CachedInterceptor {
        
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
  
    @AroundInvoke
    public Object around(InvocationContext ctx) throws Exception {
         LOG.debug("entering CachedInterceptor.around");
        // 1️⃣ Vérifier si la méthode est annotée @InvalidateCache
        InvalidateCache invalidate = ctx.getMethod().getAnnotation(InvalidateCache.class);
        if (invalidate != null) {
            if (invalidate.all()) {
                LOG.debug("cache cleared !");
                cache.clear();
                
            } else if (invalidate.keys().length > 0) {
                String className = ctx.getTarget().getClass().getName();
                for (String key : invalidate.keys()) {
                    LOG.debug("CachedInterceptor removed = " + className + "#" + key);
                    cache.remove(className + "#" + key);
                }
            }
            return ctx.proceed();
        }

        // 2️⃣ Vérifier si la méthode est annotée @Cached
        Cached cachedAnnotation = ctx.getMethod().getAnnotation(Cached.class);
        if (cachedAnnotation == null) {
            return ctx.proceed(); // rien à faire
        }

        // Calcul TTL en millisecondes
        long ttl = TimeUnit.MILLISECONDS.convert(cachedAnnotation.ttl(), cachedAnnotation.unit());

        // Construire la clé du cache
        String key = ctx.getTarget().getClass().getName() + "#" + ctx.getMethod().getName();

        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.value();
        }

        // Exécuter la méthode
        Object result = ctx.proceed();

        // Stocker dans le cache si non null
        if (result != null) {
            cache.put(key, new CacheEntry(result, ttl));
        }

        return result;
    }
} //end class