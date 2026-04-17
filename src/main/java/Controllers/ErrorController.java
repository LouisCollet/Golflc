package Controllers;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
@Named("errorC")
@RequestScoped
public class ErrorController {

    private final HttpServletRequest request;

    // Mode DEV ou PROD (peut être injecté via config ou env variable)via pom.xml dans maven-wildfly configuaration
    private final boolean devMode = Boolean.parseBoolean(System.getProperty("app.devMode", "false"));
        
    public ErrorController() {
        request = (HttpServletRequest)
                FacesContext.getCurrentInstance()
                            .getExternalContext()
                            .getRequest();

        logError();
    }

    private void logError() {
        Integer status = (Integer) request.getAttribute("jakarta.servlet.error.status_code");

        Throwable ex = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

        if (status != null) {
            if (status >= 500) {
                LOG.debug("status > 500, HTTP {0}", status);
                if (ex != null) {
                    LOG.debug(ex.getMessage(), ex);
                    notifyAdmin(ex);
                }
            } else if (status >= 400) {
                LOG.debug("status > 400,HTTP {0}", status);
            } else {
                LOG.debug("HTTP {0}", status);
            }
        } else if (ex != null) {
            LOG.debug("Exception non mappée", ex);
            notifyAdmin(ex);
        }
    }

    private void notifyAdmin(Throwable ex) {
        // TODO: implémenter notification réelle (mail, webhook, Slack)
    }

    // ================= GETTERS =================

    public Integer getStatus() {
        return (Integer) request.getAttribute(
                "jakarta.servlet.error.status_code");
    }

    public String getMessage() {
        if (devMode) {
            // Affiche message complet en DEV
            return (String) request.getAttribute("jakarta.servlet.error.message");
        } else {
            // En PROD, message générique pour sécurité
            return "Une erreur est survenue.";
        }
    }

    public String getStackTrace() {
        LOG.debug("entering getStack trace ith devMode = {}", devMode);
        if (devMode) {
            Throwable ex = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
            if (ex != null) {
                StringBuilder sb = new StringBuilder();
                for (StackTraceElement ste : ex.getStackTrace()) {
                    sb.append("at ").append(ste).append("\n");
                }
                return sb.toString();
            }
        }
        return null; // PROD = pas de stack trace
    }
}
