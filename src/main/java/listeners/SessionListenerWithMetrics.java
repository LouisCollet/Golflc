
package listeners;
import com.codahale.metrics.Counter;
import static interfaces.Log.LOG;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;
//import listeners.MetricRegistrySingleton;
// new 11-03-2024
// ne fonctionne pas !!
// enlevé 31-08-2025 @WebListener()
public class SessionListenerWithMetrics implements HttpSessionListener {

    private final Counter counterOfActiveSessions;
    private final AtomicInteger activeSessions;
    
    public SessionListenerWithMetrics() {
        super();
        activeSessions = new AtomicInteger();
        counterOfActiveSessions = MetricRegistrySingleton.metrics.counter("web.sessions.active.count"); // c'st où ??
    }

    public void sessionCreated(final HttpSessionEvent event) {
            LOG.debug("counterOfActiveSessions = " + counterOfActiveSessions.getCount());
        counterOfActiveSessions.inc();
            LOG.debug("counterOfActiveSessions = " + counterOfActiveSessions.getCount());
    }
    public void sessionDestroyed(final HttpSessionEvent event) {
        counterOfActiveSessions.dec();
    }
}
