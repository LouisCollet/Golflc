
package course_refactoring;

import static interfaces.Log.LOG;
import jakarta.interceptor.InvocationContext;
import java.util.Arrays;


public final class CacheKeyBuilder {

    public static String build(InvocationContext ctx) {
        LOG.debug("entering buid " + ctx.toString() );
        return ctx.getTarget().getClass().getName()
                + "#" + ctx.getMethod().getName()
                + Arrays.toString(ctx.getParameters());
    }
}
