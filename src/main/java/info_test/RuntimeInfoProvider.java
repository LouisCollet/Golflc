
package info_test;

import static interfaces.GolfInterface.ZDF_TIME;
import jakarta.enterprise.context.ApplicationScoped;
import java.lang.management.ManagementFactory;
import java.time.*;

@ApplicationScoped
public class RuntimeInfoProvider {

    public String jvmStartup() {
    long startTimeMillis = ManagementFactory.getRuntimeMXBean().getStartTime();
    return Instant.ofEpochMilli(startTimeMillis)
            .atZone(ZoneId.systemDefault())
            .format(ZDF_TIME);
    }

    public String java() {
        return System.getProperty("java.runtime.version")
             + " (" + System.getProperty("java.vendor") + ")";
    }

    public String os() {
        return System.getProperty("os.name") + " "
             + System.getProperty("os.version") + " "
             + System.getProperty("os.arch");
    }

public String server() {
    String version = System.getProperty("jboss.as.version");
    
    if (version == null) {
        version = System.getProperty("wildfly.version");
    }
    
    if (version == null) {
        version = "Version server inconnue";
    }
    
    return version;
}
}
