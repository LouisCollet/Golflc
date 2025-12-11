
package Controllers;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("resourceChecker")
@RequestScoped
public class ResourceCheckerController {
    private String path;
    private String lastResult;

    @Inject
    private AdvancedDiagnosticController advancedAdmin;

    public void check() {
        boolean ok = advancedAdmin.checkResourceExists(path);
        lastResult = path + " -> " + (ok ? "FOUND" : "MISSING");
    }

    // getters/setters
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getLastResult() { return lastResult; }

    public java.util.Map<String, Boolean> getCommonChecks() {
        java.util.Map<String, Boolean> m = new java.util.LinkedHashMap<>();
        m.put("/resources/themes/saga/theme.css", advancedAdmin.checkResourceExists("/resources/themes/saga/theme.css"));
        m.put("/resources/primeicons/primeicons.css", advancedAdmin.checkResourceExists("/resources/primeicons/primeicons.css"));
        m.put("/resources/primefaces/core/core.css", advancedAdmin.checkResourceExists("/resources/primefaces/core/core.css"));
        return m;
    }
}
