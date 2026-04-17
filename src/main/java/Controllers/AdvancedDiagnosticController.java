package Controllers;

import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import java.io.Serializable;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.primefaces.config.PrimeEnvironment; // new 04-12-2025
import org.primefaces.context.PrimeApplicationContext;
import org.primefaces.config.PrimeConfiguration;
import org.primefaces.PrimeFaces;
import static org.omnifaces.util.Faces.getResourceAsStream;
/**
 * Bean admin avancé : diagnostic, monitoring JVM, test ajax, vérif ressource, sessions, export.
 * Note: uses FacesContext — must only be called from JSF request context (XHTML pages).
 */
@Named("advancedAdmin")
@SessionScoped // migrated from @ApplicationScoped 2026-03-22 — uses FacesContext
public class AdvancedDiagnosticController implements Serializable {

    private static final long serialVersionUID = 1L;

    // Simple cache for resource checks to avoid repeated IO
    private final Map<String, Boolean> resourceExistsCache = new ConcurrentHashMap<>();

    // fix multi-user 2026-03-07 — per-user ajax test result via ThreadLocal (was shared across all users)
    private static final ThreadLocal<Map<String, String>> lastAjaxTest =
            ThreadLocal.withInitial(LinkedHashMap::new);

    @PostConstruct
    public void init() {
        lastAjaxTest.get().put("status", "no-test-yet");
        lastAjaxTest.get().put("timestamp", Instant.now().toString());
    }

    // ---------------- PRIMEFACES INFO ----------------

    public Map<String, String> getPrimefacesInfo() {
        PrimeConfiguration config = PrimeApplicationContext.getCurrentInstance(FacesContext.getCurrentInstance()).getConfig();
        Map<String, String> m = new LinkedHashMap<>();
        m.put("PrimeFaces Version", PrimeEnvironment.class.getPackage().getImplementationVersion());
        m.put("Theme", config.getTheme());
        m.put("FontAwesome Enabled", String.valueOf(config.isPrimeIconsEnabled()));
    //    m.put("Uploader", config..getUploader());
        m.put("Client Side Validation", String.valueOf(config.isClientSideValidationEnabled()));
        m.put("Partial Submit Enabled", String.valueOf(config.isPartialSubmitEnabled()));
        m.put("Move Scripts To Bottom", String.valueOf(config.isMoveScriptsToBottom()));
        m.put("HTML 5 Compliant", String.valueOf(config.getHtml5Compliance()));
        m.put("Cookies Same Site", String.valueOf(config.getCookiesSameSite()));
      //  m.put("Cookies Same Site", String.valueOf(config.isBeanValidationEnabled()));
        m.put("Bean validation", String.valueOf(config.isBeanValidationEnabled()));
        
        return m;
    }

    // ---------------- SYSTEM / SERVER ----------------

    public Map<String, String> getSystemInfo() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("JSF Version", FacesContext.class.getPackage().getImplementationVersion());
        map.put("Server Info", ec.getContext().toString());
        map.put("Request Charset", Objects.toString(ec.getRequestCharacterEncoding(), "n/a"));
        map.put("Response Charset", Objects.toString(ec.getResponseCharacterEncoding(), "n/a"));
        map.put("App Base Path", ec.getRequestContextPath());
        return map;
    }

    // ---------------- JVM / MEMORY ----------------

    public Map<String, Object> getJvmMemoryMetrics() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = mem.getHeapMemoryUsage();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("heap.init", heap.getInit());
        out.put("heap.used", heap.getUsed());
        out.put("heap.committed", heap.getCommitted());
        out.put("heap.max", heap.getMax());
        out.put("timestamp", Instant.now().toString());
        // basic runtime info
        Runtime rt = Runtime.getRuntime();
        out.put("processors", rt.availableProcessors());
        out.put("freeMemory", rt.freeMemory());
        out.put("totalMemory", rt.totalMemory());
        out.put("maxMemory", rt.maxMemory());
        
        out.put("Max Memory MB", String.valueOf(rt.maxMemory() / 1024 / 1024));
        out.put("Free Memory MB", String.valueOf(rt.freeMemory() / 1024 / 1024));
        out.put("Used Memory MB", String.valueOf((rt.totalMemory() - rt.freeMemory()) / 1024 / 1024));

        return out;
    }

    // ---------------- CONTEXT PARAMS ----------------

    public Map<String, String> getContextParameters() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> map = new TreeMap<>();
        ec.getInitParameterMap().forEach(map::put);
        return map;
    }

    // ---------------- SYSTEM PROPERTIES & ENV ----------------

    public Map<String, String> getSystemProperties() {
        Properties props = System.getProperties();
        return props.stringPropertyNames()
                .stream()
                .sorted()
                .collect(Collectors.toMap(k -> k, props::getProperty, (a,b)->b, LinkedHashMap::new));
    }

    public Map<String, String> getEnvironmentVariables() {
        return System.getenv().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue(), (a,b)->b, LinkedHashMap::new));
    }

    // ---------------- CLASSPATH ----------------

    public List<String> getClassPath() {
        return Arrays.stream(System.getProperty("java.class.path").split(System.getProperty("path.separator")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.toList());
    }

    // ---------------- RESOURCE CHECK ----------------

    /**
     * Vérifie si une ressource du webapp existe, ex: /resources/themes/saga/theme.css
     * Retourne true/false et met en cache.
     */
    public boolean checkResourceExists(String resourcePath) {
        return resourceExistsCache.computeIfAbsent(resourcePath, rp -> {
            try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                InputStream is = ec.getResourceAsStream(rp);
                if (is != null) {
                    is.close();
                    return true;
                }
            } catch (Exception ignored) {}
            return false;
        });
    }

    /**
     * Vide le cache des vérifications de ressources.
     */
    public void clearResourceCache() {
        resourceExistsCache.clear();
    }

    // ---------------- AJAX TEST ----------------

    /**
     * Action appelée depuis la page pour simuler un test AJAX.
     * Met à jour lastAjaxTest et renvoie vrai. Côté client on peut afficher un growl.
     */
 //  ne fonctionne pas bug jakarta ee 11
     public void runAjaxTest() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("serverTime", Instant.now().toString());
        result.put("thread", Thread.currentThread().getName());
        // sample small check: memory used
        result.put("heapUsed", String.valueOf(getJvmMemoryMetrics().get("heap.used")));
        lastAjaxTest.set(result);

        // if page triggers PrimeFaces.ajax, we can update UI from server
        PrimeFaces.current().ajax().update("adminForm:ajaxTestPanel");
        PrimeFaces.current().executeScript("PF('ajaxTestWV').show()"); // optional widget var modal
    }

    public Map<String, String> getLastAjaxTest() {
        return lastAjaxTest.get();
    }

    // ---------------- SESSIONS ----------------

    /**
     * Retourne le nombre de sessions actives (SessionTracker must be installed)
     */
    public int getActiveSessionCount() {
        return utils.SessionTracker.getActiveSessions();
    }

    @jakarta.inject.Inject private lists.AuditConnectionList auditConnectionList;

    /**
     * Count of users currently online (audit with endDate IS NULL).
     */
    public int getOnlineCount() {
        try {
            return auditConnectionList.countOnline();
        } catch (Exception e) {
            LOG.error("Error counting online users: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Tente de récupérer des informations sur la session courante (id + created time si disponible)
     */
    public Map<String, Object> getCurrentSessionInfo() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        Object s = ec.getSession(false);
        Map<String, Object> info = new LinkedHashMap<>();
        if (s == null) {
            info.put("hasSession", false);
        } else {
            info.put("hasSession", true);
            try {
                // HttpSession methods
                jakarta.servlet.http.HttpSession hs = (jakarta.servlet.http.HttpSession) s;
                info.put("id", hs.getId());
                info.put("creationTime", hs.getCreationTime());
                info.put("lastAccessedTime", hs.getLastAccessedTime());
                info.put("maxInactiveInterval", hs.getMaxInactiveInterval());
                info.put("attributeNames", Collections.list(hs.getAttributeNames()));
            } catch (Exception ex) {
                info.put("note", "unable to inspect session: " + ex.getMessage());
            }
        }
        return info;
    }

    // ---------------- ACTIONS ----------------

    /**
     * Simple action pour forcer UI refresh (appel AJAX)
     */
    public void reloadAll() {
        clearResourceCache();
        // update main panel(s)
        PrimeFaces.current().ajax().update("adminForm");
    }
}
