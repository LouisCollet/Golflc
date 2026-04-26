package context;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import java.io.Serializable;

@Named("deploymentInfo")
@ApplicationScoped
public class DeploymentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ServletContext servletContext;

    public DeploymentInfo() { }

    public boolean isRecentDeploy() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Long deploymentTime = (Long) servletContext.getAttribute("deploymentTime");
        if (deploymentTime == null) {
            return false;
        }
        int sessionTimeoutMinutes = servletContext.getSessionTimeout(); // from web.xml
        long sessionTimeoutMs = sessionTimeoutMinutes * 60 * 1000L;
        boolean recent = (System.currentTimeMillis() - deploymentTime) < sessionTimeoutMs;
        LOG.debug("recentDeploy = {}", recent);
        return recent;
    } // end method

} // end class
