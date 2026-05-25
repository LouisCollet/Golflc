package Controllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.internal.build.MongoDriverVersion;
import com.mongodb.MongoException;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import info_test.IpDetector;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.sql.DataSource;
import org.bson.Document;
import static org.omnifaces.util.Faces.getResourceAsStream;
import org.primefaces.config.PrimeEnvironment; // new 04-12-2025

@Named("infoC")
@SessionScoped // migrated from @ApplicationScoped 2026-03-22 — uses FacesContext

public class InfoController implements Serializable, interfaces.GolfInterface{
   @Inject private entite.Settings settings;        // ✅ injection CDI
   @Inject private info_test.GeoDetector geoDetector; // migrated from static 2026-03-22
    private Attributes manifestAttributes = null; // fix multi-user 2026-03-07 — was static

public InfoController() {
    //  LOG.debug("entering Infocontroller constructor");
       // voir pom.xml manifestEntries pour les fields !!
try{
   try (InputStream inputStream = getResourceAsStream("/META-INF/MANIFEST.MF")){
    if(inputStream != null){
        manifestAttributes = new Manifest(inputStream).getMainAttributes();
   //      LOG.debug("closing inputStream"); 
 //       inputStream.close(); // mod 24-04-2024 new 10-03-2024
    }else{
        LOG.debug("infoController : META-INF/MANIFEST.MF is null");
    }
   } // end try2
}catch (Exception e){
	LOG.debug("exception in InfoController constructor {}", e);
}
} // end constructor

public void login(){ // executed via actionView in login.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
} // end method


// externalContext injection removed — fix multi-user 2026-03-07 (request-scoped, must not be cached in @ApplicationScoped)
public String deployVersion() throws IOException{
    // version deployment (affichée dans login.xhtml + footer.xhtml)
  String acp = FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
  Path path = Paths.get(settings.getProperty("TARGET") + acp + ".war");
  try {
      FileTime fileTime = Files.getLastModifiedTime(path);
      LocalDateTime ldt = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneOffset.systemDefault());
      return ZDF_TIME.format(ldt);
  } catch (java.nio.file.NoSuchFileException e) {
      // WAR supprimé pendant mvn clean — pas d'erreur fatale
      LOG.debug("deployVersion - WAR not found (build in progress): {}", path);
      return "build...";
  }
} // end method

public String getClientIpAddress() throws UnknownHostException, ServletException{
return InetAddress.getLocalHost().toString();
} // end method

public String getClientIPv6Address() throws UnknownHostException{
    return Inet6Address.getLocalHost().getHostAddress();
} // end method

public String getClientMacAddress() throws UnknownHostException{
try{
return "fake mac";
}catch (Exception e){
	LOG.debug("UnknowHostException = {}", e);
return "yes, Louis : Mac adress";
}
} // end method

public String getGlobalAddress() throws UnknownHostException{
    return Arrays.deepToString(InetAddress.getAllByName("localhost"));
} // end method
@Resource(lookup = "java:jboss/datasources/golflc")
private DataSource dataSource;

public String getMySql() throws SQLException {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);

    try (Connection conn = dataSource.getConnection()) {            // ✅ try-with-resources
        return conn.getMetaData().getDatabaseProductName()
                + " / " + conn.getMetaData().getDatabaseProductVersion()
                + NEW_LINE + conn.getMetaData().getDriverName()
                + " / " + conn.getMetaData().getDriverVersion();

    } catch (SQLException e) {
        handleSQLException(e, methodName);
        return "";                                                  // ✅ jamais null
    } catch (Exception e) {
        handleGenericException(e, methodName);
        return "";                                                  // ✅ jamais null
    }
} // end method




public String getMongoDB() throws SQLException, Exception{
    Document document = null;
    //https://stackoverflow.com/questions/19274805/getting-version-of-mongo-instance-with-java-driver
    try (MongoClient mongoClient = MongoClients.create("mongodb://localhost/")){
               document = mongoClient.getDatabase("golflc").runCommand(new Document("buildInfo",1));
        }
    return "Driver version = " + MongoDriverVersion.VERSION + " - DB version = " + document.getString("version");
 } // end method
    public String getJQueryVersion() {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("JQueryVersion");
    } // end method

public String getPrimefacesVersion(){
    return PrimeEnvironment.class.getPackage().getImplementationVersion();
} // end method

public String getPrimefacesExtensionVersion(){
    return manifestAttributes.getValue("PFextension");
} // end method

public String getLog4j2Version(){
    return manifestAttributes.getValue("logging");
} // end method

public String getWeldVersion(){
    return utils.WildFlyEnvironmentInfo.getWeldVersion();
} // end method

public String getServerVersion(){
    return utils.WildFlyEnvironmentInfo.getWildFlyVersion();
} // end method

public String getApplicationVersion(){
    return manifestAttributes.getValue("golflc");
} // end method
public String getNetbeansVersion() {
    return manifestAttributes.getValue("netbeans");
} // end method

public String getPrimeflexVersion(){ //throws IOException{
    return manifestAttributes.getValue("primeflex");
} // end method

public String getJakartaEEVersion(){// throws IOException{
    return manifestAttributes.getValue("javaEE");
} // end method

public String getBuildVersion() {
     return manifestAttributes.getValue("buildTime");
} // end method

public String getMojarraVersion(){
    Package jsfPackage = FacesContext.class.getPackage();
    return 
        jsfPackage.getImplementationTitle() + " " +
        jsfPackage.getImplementationVersion() + " " +
        jsfPackage.getImplementationVendor() + " " +
        jsfPackage.getSpecificationTitle() + " " +
        jsfPackage.getSpecificationVersion();
} // end method
public String getMavenVersion(){
     return manifestAttributes.getValue("Build-Tool");
} // end method

public String getOmnifacesVersion(){
     return manifestAttributes.getValue("omnifaces");
} // end method
public String javaVersion(){
    return System.getProperty("java.runtime.version") + " from " + System.getProperty("java.vendor");
} // end method

public String getJqueryVersion(){
  return "unknown";
} // end method

public String getIpadress6(){
    return System.getProperty("java.runtime.version") + " from " + System.getProperty("java.vendor");
} // end method

public String getOsVersion(){
    return System.getProperty("os.name") + " " + System.getProperty("os.version")
            + " " + System.getProperty("os.arch");
} // end method

public String getMySQLInfo() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
        Future<String> future = executor.submit(() -> {
            try (Connection conn = dataSource.getConnection()) {
                java.sql.DatabaseMetaData metaData = conn.getMetaData();
                return "Driver " + metaData.getDriverVersion() + " / DB " + metaData.getDatabaseProductVersion();
            }
        });
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
} // end method

public String getMongoInfo() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
        Future<String> future = executor.submit(() -> {
            try (MongoClient mongoClient = MongoClients.create("mongodb://localhost/")) {
                Document info = mongoClient.getDatabase("golflc").runCommand(new Document("buildInfo", 1));
                return "Driver " + MongoDriverVersion.VERSION + " / DB " + info.getString("version");
            }
        });
        return future.get(2, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
        LOG.error("MongoDB retrieval timed out");
        return "MongoDB version unavailable (timeout)";
    } catch (MongoException e) {
        LOG.error("MongoDB error", e);
        return "MongoDB info non disponible: " + e.getMessage();
    } catch (Exception e) {
        LOG.error("Error getting MongoDB version", e);
        return "MongoDB version error";
    } finally {
        executor.shutdownNow();
    }
} // end method

public String getIpDescription() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
            .getExternalContext().getRequest();
    String ip = IpDetector.getClientIp(request);
    LOG.info("IP detected: {}", ip);
    return IpDetector.getIpDescription(ip);
} // end method

public String getGeoDescription() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
            .getExternalContext().getRequest();
    String ip = IpDetector.getClientIp(request);
    return geoDetector.getLocation(ip); // migrated from static 2026-03-22
} // end method

public String getClientAddress() {
    HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
            .getExternalContext().getRequest();
    return request.getRemoteAddr();
} // end method

public String jvmStartup() {
    long startTimeMillis = ManagementFactory.getRuntimeMXBean().getStartTime();
    return Instant.ofEpochMilli(startTimeMillis)
            .atZone(ZoneId.systemDefault())
            .format(ZDF_TIME);
} // end method

public String jvmUptime() {
    long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
    return "JVM up since: " + uptime / 1000 + " secs";
} // end method

public String getUserAgent() {
    return FacesContext.getCurrentInstance()
            .getExternalContext()
            .getRequestHeaderMap()
            .get("user-agent");
} // end method

public String getPrimeflexVersion2() {
    return manifestAttributes.getValue("primeflex");
} // end method

} // end class