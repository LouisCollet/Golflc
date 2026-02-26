package Controllers;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.internal.build.MongoDriverVersion;
import info_test.GeoDetector;
import info_test.IpDetector;
//import info_test.IpDetector;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.concurrent.StructuredTaskScope.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.bson.Document;
import org.omnifaces.util.Faces;
import org.primefaces.config.PrimeEnvironment; // new 04-12-2025
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

@Named("infoC2")
@ApplicationScoped
public class InfoController2 implements Serializable {

    @Inject private entite.Settings settings;        // ✅ injection CDI
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource; // migrated 2026-02-26

    private static final long serialVersionUID = 1L;

    private Attributes manifest;
     @Inject
    HttpServletRequest request;
     @Inject
    private ExternalContext externalContext;

    @PostConstruct
    public void init() {
        try (InputStream in = Faces.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (in != null) {
                manifest = new Manifest(in).getMainAttributes();
                LOG.debug("Manifest loaded successfully");
            } else {
                LOG.warn("MANIFEST.MF not found");
            }
        } catch (IOException e) {
            LOG.error("Failed to load MANIFEST.MF", e);
        }
    }

    /* ===================== APPLICATION ===================== */

    public String getApplicationVersion() {
        return manifestValue("golflc");
    }

    public String getBuildVersion() {
        return manifestValue("buildT");
    }

    public String getDeployVersion() throws IOException {
        String contextPath = externalContext.getApplicationContextPath();
     //   Path war = Paths.get(Settings.getProperty("TARGET"), contextPath + ".war");
// après migration CDI 
        Path war = Paths.get(settings.getProperty("TARGET"), contextPath + ".war");
        FileTime time = Files.getLastModifiedTime(war);
        return ZDF_TIME.format(LocalDateTime.ofInstant(time.toInstant(), ZoneOffset.systemDefault())
        );
    }

    
    /* ===================== SERVER ===================== */

    public String getWildFlyVersion() {
        return utils.WildFlyEnvironmentInfo.getWildFlyVersion();
    }

    public String getWeldVersion() {
        return utils.WildFlyEnvironmentInfo.getWeldVersion();
    }

    public String getJavaVersion() {
        return System.getProperty("java.runtime.version") +
               " (" + System.getProperty("java.vendor") + ")";
    }

    public String getOsVersion() {
        return System.getProperty("os.name") + " " +
               System.getProperty("os.version") + " (" +
               System.getProperty("os.arch") + ")";
    }

    /* ===================== FRONTEND ===================== */

    public String getPrimeFacesVersion() {
        return PrimeEnvironment.class.getPackage().getImplementationVersion();
    }

    public String getPrimeFlexVersion() {
        return manifestValue("primeflex");
    }

    public String getMojarraVersion() {
        Package p = FacesContext.class.getPackage();
        return p.getImplementationTitle() + " " + p.getImplementationVersion();
    }

    public String getOmniFacesVersion() {
        return manifestValue("omnifaces");
    }

    /* ===================== DATABASE ===================== 

    public String getMySqlInfo() throws SQLException, Exception {
        try (Connection c = new DBConnection().getConnection()) {
            return c.getMetaData().getDatabaseProductName() + " " +
                   c.getMetaData().getDatabaseProductVersion() +
                   " / " + c.getMetaData().getDriverName() + " " +
                   c.getMetaData().getDriverVersion();
        }
    }*/

public String getMySQLInfo() {
    LOG.debug("entering getMySQLInfo");
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
        Future<String> future = executor.submit(() -> getMySQLInfo2());
        return future.get(2, TimeUnit.SECONDS); 
    } catch (TimeoutException e) {
        LOG.error("MySQL retrieval timed out");
        return "MySQL version unavailable (timeout)";
    } catch (Exception e) {
        LOG.error("Error getting MySQL version", e);
        return "MySQL version error";
    } finally {
        executor.shutdownNow();
    }
}

public String getMySQLInfo2() {
  //  String url = "jdbc:mysql://localhost:3306/your_database";
  //  String user = "your_user";
  //  String password = "your_password";
    
    // connection_package.DBConnection2.getConnection()
    try (Connection conn = dataSource.getConnection()) { // migrated 2026-02-26
        DatabaseMetaData metaData = conn.getMetaData();
        String driverVersion = metaData.getDriverVersion();
        String dbVersion = metaData.getDatabaseProductVersion();
        return "Driver " + driverVersion + " / DB " + dbVersion;
    } catch (SQLException e) {
        LOG.error("Erreur lors de la récupération des infos MySQL", e);
        return "MySQL info non disponible: " + e.getMessage();
    } catch (Exception e) {
        LOG.error("Erreur inattendue", e);
        return "Erreur: " + e.getMessage();
    }
}

    public String getMongoInfo2() {
    try (MongoClient client = MongoClients.create("mongodb://localhost")) {
        Document info = client.getDatabase("golflc").runCommand(new Document("buildInfo", 1));
        return "Driver " + MongoDriverVersion.VERSION + " / DB " + info.getString("version");
    } catch (MongoException e) {
        LOG.error("Erreur lors de la récupération des infos MongoDB", e);
        return "MongoDB info non disponible: " + e.getMessage();
    } catch (Exception e) {
        LOG.error("Erreur inattendue", e);
        return "Erreur: " + e.getMessage();
    }
}
     public String getMongoInfo() {
        LOG.debug("entering getMongoInfo");
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
        Future<String> future = executor.submit(() -> getMongoInfo2());
        return future.get(2, TimeUnit.SECONDS); 
    } catch (TimeoutException e) {
        LOG.error("MongoDB retrieval timed out");
        return "MongoDB version unavailable (timeout)";
    } catch (Exception e) {
        LOG.error("Error getting MongoDB version", e);
        return "MongoDB version error";
    } finally {
        executor.shutdownNow();
    }
    }//end method
    
    /* ===================== CLIENT ===================== */

 public String getIpDescription() {
        String ip = IpDetector.getClientIp(request);
        LOG.info("IP détectée: {}", ip);
        // Description
        String description = IpDetector.getIpDescription(ip);
        LOG.info("Description: {}", description);
        return description;
    }
public String getGeoDescription() {
        String ip = IpDetector.getClientIp(request);
        String location = GeoDetector.getLocation(ip);
        LOG.debug("Vous êtes à: " + location); // Résultat: "Vous êtes à: Paris, France"
        return location;
 }
  //  @Inject
//    private HttpServletRequest request;

    public String getClientAddress() {
        return request.getRemoteAddr();
    }
       
  public String jvmStartup() {
    long startTimeMillis = ManagementFactory.getRuntimeMXBean().getStartTime();
    return Instant.ofEpochMilli(startTimeMillis)
            .atZone(ZoneId.systemDefault())
            .format(ZDF_TIME);
    }
  public String jvmUptime() {
  // Pour obtenir l'uptime (temps écoulé depuis le démarrage) :
    long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        LOG.debug("Serveur démarré depuis : " + uptime + " ms");
    return "JVM démarré depuis : " + uptime / 1000 + " secs";
    }

    public String getUserAgent() {
        return FacesContext.getCurrentInstance()
                           .getExternalContext()
                           .getRequestHeaderMap()
                           .get("user-agent");
    }

    /* ===================== UTILS ===================== */

    private String manifestValue(String key) {
        return manifest != null ? manifest.getValue(key) : "n/a";
    }
}
