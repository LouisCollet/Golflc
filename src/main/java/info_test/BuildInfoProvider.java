
package info_test;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@ApplicationScoped
public class BuildInfoProvider {

    private final Attributes attrs;

    public BuildInfoProvider() {
        try (InputStream in = getClass()
                .getClassLoader()
                .getResourceAsStream("META-INF/MANIFEST.MF")) {

            attrs = new Manifest(in).getMainAttributes();

        } catch (Exception e) {
            throw new IllegalStateException("Cannot read MANIFEST.MF", e);
        }
    }

    public String version() {
        return attrs.getValue("Build-Version");
    }

    public String buildTime() {
        return attrs.getValue("Build-Time");
    }
}
