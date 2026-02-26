package utils;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.BeanManager;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Optional;

import static interfaces.Log.LOG;

/**
 * Détection de l'environnement CDI / WildFly
 * Compatible WildFly 10 → 39+
 * Aucune dépendance WildFly spécifique
 */
public final class WildFlyEnvironmentInfo {

    private WildFlyEnvironmentInfo() {
        throw new UnsupportedOperationException("Utility class");
    }

    /* =======================
       PUBLIC API
       ======================= */

    public static String report() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n╔════════════════════════════════════════════╗\n");
        sb.append("║     WildFly CDI / Runtime Environment      ║\n");
        sb.append("╚════════════════════════════════════════════╝\n\n");

        sb.append("🏢 Server        : ").append(getWildFlyVersion()).append("\n");
        sb.append("📦 Jakarta EE   : ").append(getJakartaEELevel()).append("\n");
        sb.append("🔧 CDI API      : ").append(getCDIApiVersion()).append("\n");
        sb.append("🧩 CDI Impl     : Weld (intégré WildFly)\n");
        sb.append("📌 Weld Version : ").append(getWeldVersion()).append("\n");
        sb.append("☕ Java         : ").append(System.getProperty("java.version")).append("\n");
        sb.append("🔍 BeanManager  : ").append(getBeanManagerInfo()).append("\n");

        sb.append("\n╚════════════════════════════════════════════╝");

        LOG.info(sb.toString());
        return sb.toString();
    }

    /* =======================
       WILDFLY (MBean)
       ======================= */

    public static String getWildFlyVersion() {
        return readAttribute(
                "jboss.as:management-root=server",
                "productName",
                "productVersion"
        ).orElse("WildFly (version inconnue)");
    }

    /* =======================
       JAKARTA EE / CDI
       ======================= */

    public static String getCDIApiVersion() {
        Package pkg = CDI.class.getPackage();
        return pkg != null && pkg.getSpecificationVersion() != null
                ? pkg.getSpecificationVersion()
                : "Inconnue";
    }

    public static String getBeanManagerInfo() {
        try {
            BeanManager bm = CDI.current().getBeanManager();
            return bm.getClass().getName();
        } catch (Exception e) {
            return "CDI non actif";
        }
    }

    /* =======================
       WELD
       ======================= */

    public static String getWeldVersion() {
        return getVersionFromManifest("org.jboss.weld.Container")
                .or(() -> getVersionFromManifest("org.jboss.weld.bootstrap.WeldBootstrap"))
                .orElse("Intégré au serveur (non exposée)");
    }

    /* =======================
       JAKARTA EE LEVEL
       ======================= */

    public static String getJakartaEELevel() {
        String wf = getWildFlyVersion();
        if (wf.contains("39") || wf.contains("38")) return "Jakarta EE 11";
        if (wf.contains("37") || wf.contains("36")) return "Jakarta EE 10";
        if (wf.contains("33") || wf.contains("34") || wf.contains("35")) return "Jakarta EE 10";
        if (wf.contains("31") || wf.contains("32")) return "Jakarta EE 10";
        return "Jakarta EE (niveau inconnu)";
    }

    /* =======================
       INTERNAL HELPERS
       ======================= */

    private static Optional<String> readAttribute(String objectName, String... attributes) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName(objectName);

            StringBuilder sb = new StringBuilder();
            for (String attr : attributes) {
                Object value = server.getAttribute(name, attr);
                if (value != null) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(value.toString());
                }
            }
            return Optional.of(sb.toString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<String> getVersionFromManifest(String className) {
        try {
            Class<?> c = Class.forName(className);
            Package pkg = c.getPackage();
            if (pkg != null && pkg.getImplementationVersion() != null) {
                return Optional.of(pkg.getImplementationVersion());
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
