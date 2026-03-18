package Controllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.internal.build.MongoDriverVersion;
import com.mongodb.MongoException;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import info_test.GeoDetector;
import info_test.IpDetector;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.io.*;
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
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.sql.DataSource;
import org.bson.Document;
import static org.omnifaces.util.Faces.getResourceAsStream;
import org.primefaces.config.PrimeEnvironment; // new 04-12-2025

@Named("infoC")
@ApplicationScoped // mod 11/01

public class InfoController implements Serializable, interfaces.GolfInterface{
   @Inject private entite.Settings settings;        // ✅ injection CDI
    private Attributes manifestAttributes = null; // fix multi-user 2026-03-07 — was static

public InfoController() throws IOException { //  constructor ! le faire private ??
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
	LOG.debug("exception in InfoController constructor " + e);
}
} // end constructor

public void login(){ // throws IOException, SQLException { // executed via actionView in login.xhtml
        LOG.debug("entering login() coming from login.xhtml");
}


// externalContext injection removed — fix multi-user 2026-03-07 (request-scoped, must not be cached in @ApplicationScoped)
public String deployVersion() throws IOException{
    // version deployment (affichée dans login.xhtml)
  String acp = FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
//     LOG.debug("ApplicationContextPath = " + acp); //    /GolfWfly-1.0-SNAPSHOT
  Path path = Paths.get(settings.getProperty("TARGET") + acp + ".war");// converts string to path  
  FileTime fileTime = Files.getLastModifiedTime(path);
  LocalDateTime ldt = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneOffset.systemDefault());
    return ZDF_TIME.format(ldt);
}

public String getClientIpAddress() throws UnknownHostException, ServletException{
return InetAddress.getLocalHost().toString();
}

public String getClientIPv6Address() throws UnknownHostException{
    return Inet6Address.getLocalHost().getHostAddress();
}

public String getClientMacAddress() throws UnknownHostException{
try{
    
    /*
    InetAddress ip = InetAddress.getLocalHost(); //getByName("192.168.46.53");
    StringBuilder sb = new StringBuilder();

    NetworkInterface ni = NetworkInterface.getByInetAddress(ip);
    if (ni != null)    {
      byte[] mac = ni.getHardwareAddress();
      if (mac != null) {

         for (int i = 0; i < mac.length; i++){
           //             System.out.format("%02X%s",
           //                     mac[i], (i < mac.length - 1) ? "-" : "");
           sb.append(String.format("%02X%s",mac[i], (i < mac.length - 1) ? "-" : "") );
         }
      }else{
              LOG.debug("Address doesn't exist or is not accessible.");
         }
     }else{ // ni = null
          LOG.debug("Network Interface for the specified address is not found.");
      }
    */
return "fake mac";
}catch (Exception e){
	LOG.debug("UnknowHostException = " + e);
////}catch (Exception e){
//	LOG.debug("SocketException = " + e);
//}
return "yes, Louis : Mac adress";
} //end method
}

public String getGlobalAddress() throws UnknownHostException{
    return Arrays.deepToString(InetAddress.getAllByName("localhost"));
}
/*
public String getMySql() throws SQLException, Exception{
 //      LOG.debug("search version mySql");
   Connection conn = new connection_package.DBConnection().getConnection();
   String s = conn.getMetaData().getDatabaseProductName()
    + " / " + conn.getMetaData().getDatabaseProductVersion()
    + NEW_LINE + conn.getMetaData().getDriverName()
    + " / " + conn.getMetaData().getDriverVersion();
    connection_package.DBConnection.closeQuietly(conn, null, null,null);
    return s;
 }
*/
@Resource(lookup = "java:jboss/datasources/golflc")
private DataSource dataSource;

public String getMySql() throws SQLException {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);

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
}




public String getMongoDB() throws SQLException, Exception{
    Document document = null;
    //https://stackoverflow.com/questions/19274805/getting-version-of-mongo-instance-with-java-driver
    try (MongoClient mongoClient = MongoClients.create("mongodb://localhost/")){
               document = mongoClient.getDatabase("golflc").runCommand(new Document("buildInfo",1));
        }
    return "Driver version = " + MongoDriverVersion.VERSION + " - DB version = " + document.getString("version");
 }
/*
public void putMyStrings(){
    //info coming from 
    LOG.debug("entering putMyStrings");
 //  Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
   JQueryVersion = FacesContext.getCurrentInstance().getExternalContext()
           .getRequestParameterMap().get("JQueryVersion");
   LOG.debug("JQueryVersion is now = " + JQueryVersion);
}
*/
    public String getJQueryVersion() {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("JQueryVersion");
    }

public String getPrimefacesVersion(){
   // return PrimeFaces.class.getPackage().getImplementationVersion(); // mod 04-12-2025
    return PrimeEnvironment.class.getPackage().getImplementationVersion();
  //  return Primefaces.version();
}

public String getPrimefacesExtensionVersion(){
    return manifestAttributes.getValue("PFextension");
}

public String getLog4j2Version(){
    return manifestAttributes.getValue("logging");
}

public String getWeldVersion(){
    return utils.WildFlyEnvironmentInfo.getWeldVersion();
}

public String getServerVersion(){
    return utils.WildFlyEnvironmentInfo.getWildFlyVersion();
}

public String getApplicationVersion(){
    return manifestAttributes.getValue("golflc");
}
public String getNetbeansVersion() {
    return manifestAttributes.getValue("netbeans");
}

public String getPrimeflexVersion(){ //throws IOException{
    return manifestAttributes.getValue("primeflex");
}

public String getJakartaEEVersion(){// throws IOException{
    return manifestAttributes.getValue("javaEE");
}

public String getBuildVersion() {
     return manifestAttributes.getValue("buildTime");
}

public String getMojarraVersion(){
    Package jsfPackage = FacesContext.class.getPackage();
    return 
        jsfPackage.getImplementationTitle() + " " +
        jsfPackage.getImplementationVersion() + " " +
        jsfPackage.getImplementationVendor() + " " +
        jsfPackage.getSpecificationTitle() + " " +
        jsfPackage.getSpecificationVersion();
 //   String specVendor = jsfPackage.getSpecificationVendor();
  //  return implTitle + "/ " + implVersion + "/ " + implVendor + " / " + specTitle + "/ " + specVersion; // + "/" + specVendor;

}
public String getMavenVersion(){
     return manifestAttributes.getValue("Build-Tool");
}

public String getOmnifacesVersion(){
     return manifestAttributes.getValue("omnifaces");
}
//public String getJavaVersion(){
public String javaVersion(){
    return System.getProperty("java.runtime.version") + " from " + System.getProperty("java.vendor");
}

public String getJqueryVersion(){
  //  return 
//  String s = PrimeFaces.current().executeScript("jQuery().jquery");
  return "unknown";
}

public String getIpadress6(){
    return System.getProperty("java.runtime.version") + " from " + System.getProperty("java.vendor");
}

public String getOsVersion(){
    return System.getProperty("os.name") + " " + System.getProperty("os.version")
            + " " + System.getProperty("os.arch");
}
/*
public static void ListAllSystemProperties() {
    //https://docs.oracle.com/javase/tutorial/essential/environment/env.html
try{
   System.getenv().forEach((k, v) -> {
       LOG.debug("Environment Variable = " + k + TAB + v);
    });
 
   System.getProperties().entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .forEach(e -> LOG.debug("System Property " + TAB + e));
// liste.forEach(item -> LOG.debug("Flight list " + item));  // java 8 lambda

}catch (Exception e){
    String msg = "error listallasystemproperties = " + e ;
        LOG.error("error = " + msg );
        }
} // end listallsytemproperti

 public static void printManifestAttributes() {
   try {
       Manifest manifest;
       try (  InputStream in = getResourceAsStream("/META-INF/MANIFEST.MF")) { // sous /src/main/resources
           manifest = new Manifest(in);
       }
    Attributes attributes = manifest.getMainAttributes();
    
    Iterator<Object> it = attributes.keySet().iterator();
    while(it.hasNext()) {
       String key = it.next().toString();
       String value = attributes.getValue(key);
        LOG.debug("manifest attribute = " + key + " / " + value);
    }
   } catch (Exception ex) {
       LOG.debug("error printattributes" + ex);
   }
 } //end method
*/

// ========================================
// Methods merged from InfoController2 — 2026-03-07
// ========================================

public String getMySQLInfo() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
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
        LOG.error(methodName + " - MySQL retrieval timed out");
        return "MySQL version unavailable (timeout)";
    } catch (Exception e) {
        LOG.error(methodName + " - Error getting MySQL version", e);
        return "MySQL version error";
    } finally {
        executor.shutdownNow();
    }
} // end method

public String getMongoInfo() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
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
        LOG.error(methodName + " - MongoDB retrieval timed out");
        return "MongoDB version unavailable (timeout)";
    } catch (MongoException e) {
        LOG.error(methodName + " - MongoDB error", e);
        return "MongoDB info non disponible: " + e.getMessage();
    } catch (Exception e) {
        LOG.error(methodName + " - Error getting MongoDB version", e);
        return "MongoDB version error";
    } finally {
        executor.shutdownNow();
    }
} // end method

public String getIpDescription() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
            .getExternalContext().getRequest();
    String ip = IpDetector.getClientIp(request);
    LOG.info(methodName + " - IP detected: {}", ip);
    return IpDetector.getIpDescription(ip);
} // end method

public String getGeoDescription() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
            .getExternalContext().getRequest();
    String ip = IpDetector.getClientIp(request);
    return GeoDetector.getLocation(ip);
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

}// end class