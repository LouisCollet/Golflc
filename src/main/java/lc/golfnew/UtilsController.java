package lc.golfnew;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import entite.PlayingHcp;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.servlet.ServletException;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ColumnResizeEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.event.map.StateChangeEvent;
import org.primefaces.model.map.Circle;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.LatLngBounds;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Overlay;
import utils.*; 


@Named("utilsC")
@SessionScoped

public class UtilsController implements Serializable, interfaces.GolfInterface, interfaces.Log
{

private String content;
//private static String locale;
private String fmd;
private Date BirthMinDate;
private Date BirthMaxDate;
String birthmindate = "01/01/1943";
String birthmaxdate = "01/01/1993";
//private String clientIpAddress = null;
//private Set<String> localAddresses = new HashSet<>(); 
//private InetAddress ip;
private MapModel mapModel;  
  private String mapCenter;  
  private int mapZoom;  
  private String infoWindowText;  
  private Marker currentMarker;  
  private Overlay overlay;    
private final ClassLoader clo;
private final InputStream str ;
private final Properties prop1;

public UtilsController() throws IOException // constructor
{
    super();  
        this.clo = Thread.currentThread().getContextClassLoader();
        this.str = clo.getResourceAsStream("myPOM.properties"); // loaded in pom.xml via properties-maven-plugin
        this.prop1 = new Properties();
        prop1.load(str);
        mapModel = new DefaultMapModel();  
        mapZoom = 7;  
        mapCenter = "51.5, 10.49";  
        LatLng latlng = new LatLng(51.6, 10.4);  
        Marker marker = new Marker(latlng, "myMarker");  
        mapModel.addOverlay(marker);  
        Circle circle = new Circle(latlng, 50000);  
        circle.setFillColor("yellow");  
        circle.setFillOpacity(0.3);  
        mapModel.addOverlay(circle);  

}

    public void setFmd(String fmd) {
        this.fmd = fmd;
    }

    
    public void onIdle() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, 
                "No activity.", "What are you doing over there?"));
    }
 
    public void onActive() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                "Welcome Back", "Well, that's a long coffee break!"));
    }
    
public void logFile() throws IOException{
    Runtime runtime = Runtime.getRuntime();
    runtime.exec(new String[] { "C:\\Program Files\\JGsoft\\EditPadLite\\EditPadLite7.exe", "C:\\log\\golflc.log" } );
}
// end method runtime.exec(new String[] { "monappli", "un paramètre avec des espaces", "param2" } );

private int count = 20;
public int getCount()
{
	return count;
}
public void setCount(int count)
{
	this.count = count;
}
public void increment()
{
	count--;  // count down
}
//public void displayMessage(ActionEvent actionEvent) {
//		LCUtil.addMessageInfo("You said:'" + text + "'");
//	}

public void save(ActionEvent actionEvent)
{
	LCUtil.showMessageInfo("Data saved");
}

public void update(ActionEvent actionEvent)
{
	LCUtil.showMessageInfo("Data updated");
}

public void delete(ActionEvent actionEvent)
{
	LCUtil.showMessageInfo("Data deleted");
}

public void saveListener() {
        content = content.replaceAll("\\r|\\n", "");

        final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Content",
                    content.length() > 150 ? content.substring(0, 100) : content);

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
public String getContent() {
        return content;
    }

public void setContent(final String content)
    {
        this.content = content;
    }

//public Date getNow()
//{
//      return (new Date()); // used in /header.xhtml @110,52 value="#{utilsC.now}":
//   }

public void onStateChange(StateChangeEvent event){
 LatLngBounds bounds = event.getBounds();
 int zoomLevel = event.getZoomLevel();

addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Zoom Level", String.valueOf(zoomLevel)));
//  addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Center", bounds.getCenter().toString()));
 addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "NorthEast", bounds.getNorthEast().toString()));
 addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "SouthWest", bounds.getSouthWest().toString()));
 }

public void onPointSelect(PointSelectEvent event){
 LatLng latlng = event.getLatLng();

addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Point Selected",
        "Lat:" + latlng.getLat() + ", Lng:" + latlng.getLng()));
 }

public void addMessage(FacesMessage message){
 FacesContext.getCurrentInstance().addMessage(null, message);
 }

public void onResize(ColumnResizeEvent event){
        //FacesMessage msg = new FacesMessage("Column " + event.getColumn().getClientId()
        //        + " resized", "W:" + event.getWidth() + ", H:" + event.getHeight());
        addMessage(new FacesMessage("The Column " + event.getColumn().getClientId()
                + " has been resized : "
                + " W = " + event.getWidth()
                + " ,H = " + event.getHeight() ) )
                //+ event.getComponent().getFamily() ) )
                ;
      
    }
/*
public String getSessionId() {
        FacesContext fCtx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
           LOG.info("Session Id = " + session.getId() + " - creation time = " + session.getCreationTime());
     //      LOG.info("session servlet context Path "+ session.getServletContext().getContextPath());
    return session.getId();
}

public int getSessionCount() {
        LOG.info("session count getter invoked "  + SessionCounter.getCount() + NEW_PAGE);
        
        String s = getSessionId();
   //     LOG.info("session id = " + sessionId);
        
        return SessionCounter.getCount();
    }
*/

public String getClientIpAddress() throws UnknownHostException, ServletException{
return InetAddress.getLocalHost().toString();
}

public String getClientMacAddress() throws UnknownHostException{
try{
    InetAddress ip = InetAddress.getLocalHost(); //getByName("192.168.46.53");
    StringBuilder sb = new StringBuilder();
    /*
    * Get NetworkInterface for the current host and then read
    * the hardware address.
    */
    NetworkInterface ni = NetworkInterface.getByInetAddress(ip);
    if (ni != null)    {
      byte[] mac = ni.getHardwareAddress();
      if (mac != null) {
         /*
         * Extract each array of mac address and convert it 
         * to hexa with the following format 
         * 08-00-27-DC-4A-9E.
         */
 
         for (int i = 0; i < mac.length; i++)
         {
           //             System.out.format("%02X%s",
           //                     mac[i], (i < mac.length - 1) ? "-" : "");
           sb.append(String.format("%02X%s",mac[i], (i < mac.length - 1) ? "-" : "") );
         }
      }else{
              LOG.info("Address doesn't exist or is not accessible.");
         }
     }else{ // ni = null
          LOG.info("Network Interface for the specified address is not found.");
      }
return sb.toString();
}
catch (UnknownHostException e)
{
	LOG.info("UnknowHostException = " + e);
}
catch (SocketException e)
{
	LOG.info("SocketException = " + e);
}
return "yes, Louis : Mac adress";
} //end method

public String getMySql() throws SQLException, Exception
{
    DBConnection dbc = new DBConnection();
    Connection conn = dbc.getConnection();
    DatabaseMetaData meta = conn.getMetaData();
    String s = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
    DBConnection.closeQuietly(conn, null, null,null);
    return s;
 }

public String getJsfImplementation()
{
    String version = FacesContext.class.getPackage().getImplementationVersion();
        LOG.info("JSF implementation = " + version);
    String vendor = FacesContext.class.getPackage().getImplementationVendor();
        LOG.info("JSF implementation = " + vendor);
    String title = FacesContext.class.getPackage().getImplementationTitle();
        LOG.info("JSF implementation = " + title);

    return version + " - " + vendor + " - " + title;
 }

/**
 * This method provides a convenient means of determining the JSF Implementation version.
 *
 * @return JSF Implementation version, e.g. 2.1.26
 * @since 1.5
 */
public String getImplementationVersion()
{
    return FacesContext.getCurrentInstance().getClass().getPackage().getImplementationVersion();
       //    FacesContext.getCurrentInstance().getClass().getPackage().getImplementationVersion();
}
/**
 * This method provides a convenient means of determining the JSF Specification version.
 *
 * @return JSF Specification version, e.g. 2.1
 * @since 1.5
 */
public String getSpecificationVersion()
{
    return FacesContext.getCurrentInstance().getClass().getPackage().getSpecificationVersion();
}
/**
 * This method provides a convenient means of determining the JSF Implementation Title.
 *
 * @return JSF implementation title, e.g. Mojarra.
 * @since 1.5
 */
public String getImplementationTitle()
{
    return FacesContext.getCurrentInstance().getClass().getPackage().getImplementationTitle();
}


public String getPrimefacesVersion(){
    return PrimeFaces.class.getPackage().getImplementationVersion(); //07-03-2019
}

public String getPrimefacesExtensionVersion()
{
     return "PrimeFaces Extensions " + prop1.getProperty("primefaces.extension.version");
 //   return "Running on PrimeFaces Extensions {0}", VersionProvider.getVersion();
    
   // return org.primefaces.extensions.util.Constants.LIBRARY + " broken 1.0.0.RC1 " ;
         //   + org.primefaces.extensions.util.VersionProvider.getVersion();
}
public String getGlassfishVersion()
{     return prop1.getProperty("glassfish.version");}

//public String getWildflyVersion()
//{     return prop1.getProperty("wildfly.version");}

public String getLog4j2Version()
{ return prop1.getProperty("log4j2.version");  }

public String getNetbeansVersion()
{  return prop1.getProperty("netbeans.version"); }

public String getMojarraVersion()
{
    Package jsfPackage = FacesContext.class.getPackage();
    String implTitle = jsfPackage.getImplementationTitle();
    String implVersion = jsfPackage.getImplementationVersion();
    String implVendor = jsfPackage.getImplementationVendor();
    String specTitle = jsfPackage.getSpecificationTitle();
    String specVersion = jsfPackage.getSpecificationVersion();
    String specVendor = jsfPackage.getSpecificationVendor();
    return implTitle + " " + implVersion + " " + implVendor + specTitle + " " + specVersion + " " + specVendor;
    
   //  return prop1.getProperty("mojarra.version");  // mod 14/04/2017
}
public String getMavenVersion()
{
     return prop1.getProperty("maven.version");
}
public String getHibernateVersion(){
 //   LOG.info(org.hibernate.Version.getVersionString());
 //   String hibernateVersion = org.hibernate.annotations.common.Version.VERSION;
 //       System.out.println("Hibernate Version: "+ hibernateVersion);
     return "Hibernate Validator " + prop1.getProperty("hibernate.version");
}

public String getOmnifacesVersion(){
     return prop1.getProperty("omnifaces.version");
}

public String getJavaVersion(){
    String javaVersion = System.getProperty("java.runtime.version");
    return javaVersion + " from " + System.getProperty("java.vendor");
}

public String getIpadress6(){
 //   String hostAddress = java.net.Inet6Address.getHostAddress();
    String javaVersion = System.getProperty("java.runtime.version");
    return javaVersion + " from " + System.getProperty("java.vendor");
}

public String getOsVersion(){
    return System.getProperty("os.name") + " " + System.getProperty("os.version")
            + " " + System.getProperty("os.arch");
}
public void preProcessPDF(Object document) {
      Document pdf = (Document) document;
      pdf.setPageSize(PageSize.A4.rotate());
      pdf.open();
    }
    
public static int getElem(PlayingHcp playingHcp){
    // used in ??
    // calcule le nombre de players (de 1/2 à 4 ?)
    int counter_players = 0;
    Double hcp[] = playingHcp.getHcpScr();
    for (Double hcp1 : hcp) {
        if (hcp1 != 0.0) {
            counter_players ++;
            LOG.info("Scramble Hcp = " + hcp1);
        }
    }
            LOG.info("Scramble Hcp number of players  = " + counter_players );
            
return counter_players;
}

public static void printResultSet(ResultSet rs) throws SQLException{
    ResultSetMetaData rsmd = rs.getMetaData();
    System.out.println("querying SELECT * FROM XXX");
    int columnsNumber = rsmd.getColumnCount();
    while (rs.next()) {
        for (int i = 1; i <= columnsNumber; i++) {
            if (i > 1) LOG.info(",  ");
            String columnValue = rs.getString(i);  // à supposer que ce sont tous des string ??
            LOG.info(rsmd.getColumnName(i) + " = " + columnValue);
        }
        System.out.println("");
    }
            LOG.info("Scramble Hcp number of players  = "  );

}

public double getSum(PlayingHcp playingHcp)   //To find the sum of array elements
{ 
          double sum=0;
          Double hcp[] = playingHcp.getHcpScr();
          for(Double i:hcp)
          {
              sum += i;
              LOG.info("Scramble Hcp - The sum is : " + sum); 
          }
          LOG.info("Scramble Hcp - The FINAL sum is : " + sum); 

return sum;
}

    public MapModel getMapModel() {
        return mapModel;
    }

    public void setMapModel(MapModel mapModel) {
        this.mapModel = mapModel;
    }

    public String getMapCenter() {
        return mapCenter;
    }

    public void setMapCenter(String mapCenter) {
        this.mapCenter = mapCenter;
    }

    public int getMapZoom() {
        return mapZoom;
    }

    public void setMapZoom(int mapZoom) {
        this.mapZoom = mapZoom;
    }

    public String getInfoWindowText() {
        return infoWindowText;
    }

    public void setInfoWindowText(String infoWindowText) {
        this.infoWindowText = infoWindowText;
    }

    public Marker getCurrentMarker() {
        return currentMarker;
    }

    public void setCurrentMarker(Marker currentMarker) {
        this.currentMarker = currentMarker;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
    }
public void onMarkerSelect(OverlaySelectEvent event){  
        LOG.debug("onMarkerSelect: " + event.getOverlay().getClass().getName());  
        infoWindowText = "blabla";  
    } 

}// end class