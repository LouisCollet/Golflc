
package info_test;

import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class DeployInfoProvider {

    // externalContext injection removed — fix multi-user 2026-03-07 (request-scoped, must not be cached in @ApplicationScoped)

    public String deployTime() {
        try {
            var ec = FacesContext.getCurrentInstance().getExternalContext();
            LOG.debug("Context path: " + ec.getApplicationContextPath());
            LOG.debug("Real path: " + ec.getRealPath("/"));
            LOG.debug("jboss.server.base.dir: " + System.getProperty("jboss.server.base.dir"));
            LOG.debug("jboss.server.deploy.dir: " + System.getProperty("jboss.server.deploy.dir"));

            String ctx = ec.getApplicationContextPath().substring(1);
            Path war = Paths.get(
                    System.getProperty("jboss.server.deploy.dir"),
                    ctx + ".war"
            );
            LOG.debug("war = " + war);
            return Files.getLastModifiedTime(war)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .format(ZDF_TIME);

        } catch (Exception e) {
            return "Unavailable";
        }
    }
}
