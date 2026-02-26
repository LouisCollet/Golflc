package connection_package;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

@ApplicationScoped
public class DatasourceStats {

    private static final String DATASOURCE = "MySqlDS"; //java:jboss/datasources/golflc"; //MySqlDS";  // pool-name

    private ObjectName getPoolObjectName() throws Exception {
        return new ObjectName(
            "jboss.as:subsystem=datasources,data-source=" + DATASOURCE
        );
    }

    private Object readAttribute(String attr) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            LOG.debug("server = " + server.toString());
              LOG.debug("server = " + server.getDefaultDomain());
   
            return server.getAttribute(getPoolObjectName(), attr);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lecture stats datasource", e);
        }
    }

    public int getActiveCount() {
        LOG.debug("entering getActiveCount");
        return (Integer) readAttribute("ActiveCount");
    }

    public int getAvailableCount() {
        return (Integer) readAttribute("AvailableCount");
    }

    public int getInUseCount() {
        return (Integer) readAttribute("InUseCount");
    }

    public int getCreatedCount() {
        return (Integer) readAttribute("CreatedCount");
    }
}
