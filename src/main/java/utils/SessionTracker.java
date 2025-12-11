package utils;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

@WebListener
public class SessionTracker implements HttpSessionListener {

    private static final AtomicInteger active = new AtomicInteger(0);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        active.incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        active.decrementAndGet();
    }

    public static int getActiveSessions() {
        return Math.max(0, active.get());
    }
}