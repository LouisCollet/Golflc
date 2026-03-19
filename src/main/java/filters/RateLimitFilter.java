package filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static interfaces.Log.LOG;

/**
 * Rate limiting filter — limits requests per IP per time window.
 * Security audit 2026-03-18
 *
 * Configured in web.xml with url-patterns:
 *   /login.xhtml      → 10 requests / minute
 *   /rest/*            → 60 requests / minute
 *   (default)          → 120 requests / minute
 */
public class RateLimitFilter implements Filter {

    private static final int LOGIN_LIMIT   = 10;   // per minute per IP
    private static final int REST_LIMIT    = 60;
    private static final int DEFAULT_LIMIT = 120;
    private static final long WINDOW_MS    = 60_000L; // 1 minute

    private final ConcurrentHashMap<String, RequestCounter> counters = new ConcurrentHashMap<>();
    private volatile long lastCleanup = System.currentTimeMillis();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    } // end method

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ip   = getClientIp(req);
        String uri  = req.getRequestURI();

        // ✅ Skip static resources — CSS, JS, fonts, images served by JSF/PrimeFaces
        if (isStaticResource(uri)) {
            chain.doFilter(request, response);
            return;
        }

        int    limit = resolveLimit(uri);
        String key   = ip + "|" + resolveCategory(uri);

        // Cleanup old entries every 5 minutes
        long now = System.currentTimeMillis();
        if (now - lastCleanup > 300_000L) {
            lastCleanup = now;
            counters.entrySet().removeIf(e -> e.getValue().isExpired(now));
        }

        RequestCounter counter = counters.computeIfAbsent(key, k -> new RequestCounter());

        if (counter.incrementAndCheck(limit, now)) {
            // ✅ Within limit — add standard rate limit headers
            resp.setIntHeader("X-RateLimit-Limit", limit);
            resp.setIntHeader("X-RateLimit-Remaining", Math.max(0, limit - counter.getCount()));
            chain.doFilter(request, response);
        } else {
            // ❌ Rate limit exceeded
            LOG.warn("Rate limit exceeded for IP=" + ip + " on " + uri);
            resp.setIntHeader("X-RateLimit-Limit", limit);
            resp.setIntHeader("X-RateLimit-Remaining", 0);
            resp.setIntHeader("Retry-After", 60);
            resp.setStatus(429); // Too Many Requests
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write("Too many requests. Please try again later.");
        }
    } // end method

    @Override
    public void destroy() {
        counters.clear();
    } // end method

    // ========================================
    // HELPERS
    // ========================================

    private boolean isStaticResource(String uri) {
        return uri.contains("jakarta.faces.resource")
            || uri.contains("/resources/")
            || uri.endsWith(".css")
            || uri.endsWith(".js")
            || uri.endsWith(".woff")
            || uri.endsWith(".woff2")
            || uri.endsWith(".ttf")
            || uri.endsWith(".eot")
            || uri.endsWith(".svg")
            || uri.endsWith(".png")
            || uri.endsWith(".jpg")
            || uri.endsWith(".jpeg")
            || uri.endsWith(".gif")
            || uri.endsWith(".ico");
    } // end method

    private int resolveLimit(String uri) {
        if (uri.contains("login.xhtml"))  return LOGIN_LIMIT;
        if (uri.contains("/rest/"))       return REST_LIMIT;
        return DEFAULT_LIMIT;
    } // end method

    private String resolveCategory(String uri) {
        if (uri.contains("login.xhtml"))  return "login";
        if (uri.contains("/rest/"))       return "rest";
        return "default";
    } // end method

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();  // first IP = real client
        }
        return req.getRemoteAddr();
    } // end method

    // ========================================
    // INNER CLASS — per-key counter with time window
    // ========================================

    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        /** Returns true if request is allowed, false if limit exceeded. */
        boolean incrementAndCheck(int limit, long now) {
            if (now - windowStart > WINDOW_MS) {
                // Window expired — reset
                synchronized (this) {
                    if (now - windowStart > WINDOW_MS) {
                        count.set(0);
                        windowStart = now;
                    }
                }
            }
            return count.incrementAndGet() <= limit;
        } // end method

        int getCount() {
            return count.get();
        } // end method

        boolean isExpired(long now) {
            return now - windowStart > WINDOW_MS * 5;  // cleanup after 5 minutes idle
        } // end method
    } // end class

} // end class
