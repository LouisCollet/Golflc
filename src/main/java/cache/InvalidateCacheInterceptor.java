
package cache;

import static interfaces.Log.LOG;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@InvalidateCache
public class InvalidateCacheInterceptor {

    @Inject
    MethodCache cache;

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        LOG.debug("entering intercept");
        Object result = ctx.proceed();

        InvalidateCache ic =
            ctx.getMethod().getAnnotation(InvalidateCache.class);

        if (ic == null) {
            ic = ctx.getTarget().getClass()
                .getAnnotation(InvalidateCache.class);
        }

        if (ic.all()) {
            cache.clear();
            return result;
        }

        String className = ctx.getTarget().getClass().getName();

        if (ic.keys().length > 0) {
            for (String m : ic.keys()) {
                cache.invalidateByPrefix(className + "#" + m);
            }
        } else {
            cache.invalidateByPrefix(className + "#");
        }
        return result;
    }
} //end class