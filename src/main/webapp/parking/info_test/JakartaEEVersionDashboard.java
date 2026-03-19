package info_test;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import java.util.Map;
import java.util.TreeMap;

@Named
@RequestScoped
public class JakartaEEVersionDashboard {

    public Map<String, String> getVersions() {
        
    Package pkg = jakarta.servlet.Servlet.class.getPackage();
    if (pkg != null) {
        LOG.debug("Specification Version: " + pkg.getSpecificationVersion());
        LOG.debug("Implementation Version: " + pkg.getImplementationVersion());
    }   
        
    Module module = jakarta.servlet.Servlet.class.getModule();
    if (module.isNamed()) {
        LOG.debug("Module name: " + module.getName());
        LOG.debug("Module descriptor: " + module.getDescriptor().version());
    }      
        Map<String, String> versions = new TreeMap<>(); // tri alphabétique

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            int major = servletContext.getMajorVersion();
            int minor = servletContext.getMinorVersion();

            versions.put("Servlet API", major + "." + minor);
        String jakartaEE = switch (major) {
            case 4 -> "Jakarta EE 8";
            case 5 -> "Jakarta EE 9/9.1";
            case 6 -> (minor == 0) ? "Jakarta EE 10" : "Jakarta EE 11";
            default -> "Unknown Jakarta EE";
        };
            versions.put("Jakarta EE", jakartaEE);
        } else {
            versions.put("Servlet API", "unknown");
            versions.put("Jakarta EE", "unknown");
        }

        // Détection dynamique
 // ne fonctionne pas       detectSpec(versions, "CDI API", "jakarta.enterprise.inject.spi");
        detectSpec(versions, "EJB API", "jakarta.ejb.EJB");
        detectSpec(versions, "JPA API", "jakarta.persistence.EntityManager");
        detectSpec(versions, "JSF API", "jakarta.faces.context.FacesContext");
        detectSpec(versions, "JAX-RS API", "jakarta.ws.rs.core.Application");
        detectSpec(versions, "Jakarta Mail API", "jakarta.mail.Session");
        detectSpec(versions, "Jakarta Batch API", "jakarta.batch.api.BatchRuntime");
        detectSpec(versions, "Jakarta WebSocket API", "jakarta.websocket.Session");
        detectSpec(versions, "Jakarta Security", "jakarta.security.enterprise.SecurityContext");
        detectSpec(versions, "Jakarta REST Client", "jakarta.ws.rs.client.Client");

        return versions;
    }

    private void detectSpec(Map<String, String> map, String name, String className) {
        try {
            Class<?> cls = Class.forName(className);
            Package pkg = cls.getPackage();
            String version = (pkg != null && pkg.getImplementationVersion() != null)
                    ? pkg.getImplementationVersion()
                    : "available";
            map.put(name, version);
        } catch (ClassNotFoundException e) {
            map.put(name, "absent");
        }
    }
}
