package sql;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.Map;

public class LogPs implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Map<String, String> DRIVER_MARKERS = Map.of(
        "ClientPreparedStatement",     "Connector/J",          // MySQL standard
        "ServerPreparedStatement",     "Connector/J server",   // MySQL server-side
        "WrappedPreparedStatement",    "IronJacamar pooled",   // WildFly / JBoss pool
        "HikariProxyPreparedStatement","HikariCP pooled",      // HikariCP pool
        "ProxyPreparedStatement",      "C3P0 pooled",          // C3P0 pool
        "LoggingPreparedStatement",    "P6Spy logging"         // P6Spy interceptor
    );

    public static void log(PreparedStatement ps) {
        if (ps == null) {
            LOG.warn("logps : PreparedStatement est null");
            return;
        }
        if (!LOG.isDebugEnabled()) return;

        try {
            String psString   = ps.toString();
            int    colonIndex = psString.indexOf(": ");

            String driverName = DRIVER_MARKERS.entrySet().stream()
                .filter(e -> psString.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("driver inconnu [" + ps.getClass().getSimpleName() + "]");

            if (colonIndex >= 0) {
                LOG.debug("PreparedStatement [{}] :{}{}",
                    driverName, NEW_LINE, psString.substring(colonIndex + 2));
            } else {
                String query = tryUnwrap(ps);
                if (query != null) {
                    LOG.debug("PreparedStatement [{}] (unwrapped) :{}{}", driverName, NEW_LINE, query);
                } else {
                    LOG.debug("PreparedStatement [{}] : requête non disponible — toString = {}",
                        driverName, psString);
                }
            }
        } catch (Exception e) {
            LOG.error("logps : erreur inattendue sur [{}] : {}",
                ps.getClass().getSimpleName(), e.getMessage(), e);
        }
    } // end method

    private static String trimDriverPrefix(String raw) {
        int idx = raw.indexOf(": ");
        return idx >= 0 ? raw.substring(idx + 2) : raw;
    } // end method

    private static volatile Field cachedWrappedField;

    private static String tryUnwrap(PreparedStatement ps) {
        return tryUnwrap(ps, 3);
    } // end method

    private static String tryUnwrap(PreparedStatement ps, int depth) {
        if (depth == 0) return trimDriverPrefix(ps.toString());

        // 1. IronJacamar public API — getUnderlyingStatement() est publique, pas de setAccessible
        try {
            java.lang.reflect.Method m = ps.getClass().getMethod("getUnderlyingStatement");
            Object inner = m.invoke(ps);
            if (inner instanceof PreparedStatement innerPs && innerPs != ps)
                return tryUnwrap(innerPs, depth - 1);
        } catch (NoSuchMethodException ignored) {
            // pas IronJacamar
        } catch (Exception e) {
            LOG.trace("getUnderlyingStatement : {}", e.getMessage());
        }

        // 2. unwrap JDBC standard — charge la classe depuis le classloader du driver
        try {
            ClassLoader cl = ps.getClass().getClassLoader();
            for (String name : new String[]{
                    "com.mysql.cj.jdbc.ClientPreparedStatement",
                    "com.mysql.cj.jdbc.ServerPreparedStatement"}) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends PreparedStatement> clazz =
                        (Class<? extends PreparedStatement>) Class.forName(name, false, cl);
                    if (ps.isWrapperFor(clazz))
                        return trimDriverPrefix(ps.unwrap(clazz).toString());
                } catch (ClassNotFoundException ignored) { }
            }
        } catch (Exception e) {
            LOG.trace("unwrap dynamic : {}", e.getMessage());
        }

        // 3. scan complet de la hiérarchie (dernier recours, JDK < 26)
        try {
            Class<?> c = ps.getClass();
            while (c != null) {
                for (var field : c.getDeclaredFields()) {
                    if (!field.trySetAccessible()) continue;
                    Object value = field.get(ps);
                    if (value instanceof PreparedStatement inner && inner != ps)
                        return tryUnwrap(inner, depth - 1);
                }
                c = c.getSuperclass();
            }
        } catch (Exception e) {
            LOG.trace("reflection scan : {}", e.getMessage());
        }

        return null;
    } // end method

} // end class
