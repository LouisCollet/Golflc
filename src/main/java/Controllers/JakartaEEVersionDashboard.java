package Controllers;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Retourne la liste des spécifications Jakarta EE détectées dans le classpath.
 * Utilisé dans technical_info.xhtml pour afficher le tableau des versions.
 */
@Named("jakartaEEVersionDashboard")
@ApplicationScoped
public class JakartaEEVersionDashboard implements Serializable {

    private static final long serialVersionUID = 1L;

    public JakartaEEVersionDashboard() { }

    public Map<String, String> getVersions() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        Map<String, String> m = new LinkedHashMap<>();
        m.put("Jakarta Faces (Mojarra)", specVersion("jakarta.faces.FacesException"));
        m.put("Jakarta CDI",             specVersion("jakarta.enterprise.inject.spi.CDI"));
        m.put("Jakarta Persistence",     specVersion("jakarta.persistence.Entity"));
        m.put("Jakarta EJB",             specVersion("jakarta.ejb.EJB"));
        m.put("Jakarta Servlet",         specVersion("jakarta.servlet.Servlet"));
        m.put("Jakarta REST (JAX-RS)",   specVersion("jakarta.ws.rs.Path"));
        m.put("Jakarta Bean Validation", specVersion("jakarta.validation.Validator"));
        m.put("Jakarta Transaction",     specVersion("jakarta.transaction.Transactional"));
        m.put("Jakarta Security",        specVersion("jakarta.security.enterprise.SecurityContext"));
        m.put("Jakarta Mail",            specVersion("jakarta.mail.Session"));
        m.put("Jakarta Batch",           specVersion("jakarta.batch.api.Batchlet"));
        m.put("Jakarta JSON Binding",    specVersion("jakarta.json.bind.Jsonb"));
        m.put("Jakarta JSON Processing", specVersion("jakarta.json.Json"));
        m.put("Jakarta WebSocket",       specVersion("jakarta.websocket.Session"));
        m.put("Jakarta Concurrency",     specVersion("jakarta.enterprise.concurrent.ManagedExecutorService"));
        return m;
    } // end method

    /**
     * Détecte si une classe est présente dans le classpath et retourne la version du package.
     * Retourne "absent" si la classe n'est pas trouvée.
     */
    private String specVersion(String className) {
        try {
            Package pkg = Class.forName(className).getPackage();
            if (pkg == null) { return "présent (version inconnue)"; }
            String v = pkg.getSpecificationVersion();
            String impl = pkg.getImplementationVersion();
            if (v != null)    { return v; }
            if (impl != null) { return impl; }
            return "présent (version inconnue)";
        } catch (ClassNotFoundException e) {
            return "absent";
        }
    } // end method

} // end class
