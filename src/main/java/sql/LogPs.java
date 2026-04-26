package sql;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
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

    private static String tryUnwrap(PreparedStatement ps) {
        try {
            if (ps.isWrapperFor(com.mysql.cj.jdbc.ClientPreparedStatement.class)) {
                String raw = ps.unwrap(com.mysql.cj.jdbc.ClientPreparedStatement.class).toString();
                int idx = raw.indexOf(": ");
                return idx >= 0 ? raw.substring(idx + 2) : raw;
            }
        } catch (Exception e) {
            LOG.trace("tryUnwrap : unwrap non disponible — {}", e.getMessage());
        }
        return null;
    } // end method
} // end class
