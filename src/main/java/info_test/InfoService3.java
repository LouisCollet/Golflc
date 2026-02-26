
package info_test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class InfoService3 {

    @Inject BuildInfoProvider build;
    @Inject DeployInfoProvider deploy;
    @Inject RuntimeInfoProvider runtime;

    public Map<String, String> technicalInfo() {

        Map<String, String> info = new LinkedHashMap<>();

        info.put("Application version", build.version());
        info.put("Build date", build.buildTime());
        info.put("Deploy date", deploy.deployTime());
        info.put("Server startup", runtime.jvmStartup());
        info.put("Java", runtime.java());
        info.put("OS", runtime.os());
        info.put("WildFly", runtime.server());

        return info;
    }
}
