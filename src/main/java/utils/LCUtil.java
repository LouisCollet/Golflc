package utils;

import entite.Player;
import entite.ScoreMatchplay;
import static interfaces.Log.LOG;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.imageio.ImageIO;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;
import org.primefaces.PrimeFaces;
import org.primefaces.context.RequestContext;
//import org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8;
/**
 *
 * @author Louis Collet
 */
public class LCUtil implements interfaces.GolfInterface, interfaces.Log    // constantes
{
  private static long startTime;
  private static long stopTime;


public static Object getSessionMapValue(String key)
{
   return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(key);
}

public static void setSessionMapValue(String key, Object value)
{
   FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put(key, value);
}

public static java.sql.Timestamp getCurrentTimeStamp(){
	java.util.Date today = new java.util.Date();
	return new java.sql.Timestamp(today.getTime());
}
public static String getCurrentTimeWithZoneOffset(String lzt)
{
        LOG.info("start with " + lzt);
    Instant now = Instant.now();
    ZoneId zoneId = ZoneId.of(lzt);
        LOG.info("zoneId " + zoneId);
    ZonedDateTime zdt = ZonedDateTime.ofInstant(now, zoneId);
         LOG.info("zonedDateTime " + zdt.toString());
         LOG.info("rules = " + zoneId.getRules().toString());
    String offset = zdt.getOffset().toString();
         LOG.info("Offset = " + offset);
    if (zoneId.getRules().isDaylightSavings(zdt.toInstant())) 
        {System.out.printf("  (%s daylight saving time will be in effect.)%n", zoneId);
        LOG.info("DAYLIGHT saving time will be in effect for " + zoneId);}
    else
        {System.out.printf("  (%s standard time will be in effect.)%n", zoneId);
        LOG.info("Standard time will be in effect for " + zoneId);}
    return zdt.toString();
   // return zdt.format(ZDF) + " Offset = " + offset;

}
public static java.sql.Date getCurrentDate(){
	java.util.Date today = new java.util.Date();
	return new java.sql.Date(today.getTime());
}
public static void delayLC() {// throws InterruptedException, ExecutionException{
    //http://winterbe.com/posts/2015/04/07/java8-concurrency-tutorial-thread-executor-examples/ 
    LOG.info("entering delayLC");
    
    // run this task as a background/daemon thread
    TimerTask timerTask = new schedule.SubscriptionRenewal();
    Timer timer = new Timer(true);
    int period = 1*60*1000; // 5 minutes in milliseconds
    timer.scheduleAtFixedRate(timerTask, 0, period);
//    int initialDelay = 0;
//    int period = 5;
//    timer.scheduleAtFixedRate(timerTandssk, initialDelay, TimeUnit.SECONDS*period);
 //   executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
//That last line of code runs the task every five minutes with a zero-second delay time,
// but you can also schedule a task to be run just once
    
    LOG.info(LCUtil.class.getName() + " line 88 ");
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
 //   Runnable task = () -> LOG.info("Scheduling 1: " + System.nanoTime());
    Runnable task = new schedule.SubscriptionRenewal();
    ScheduledFuture<?> future = executor.schedule(task, 3, TimeUnit.SECONDS);
      try {
          TimeUnit.MILLISECONDS.sleep(1337);
      } catch (InterruptedException ex) {
          LOG.info(LCUtil.class.getName() + " interrupted exception " + ex);
      }
    long remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
    LOG.info("Remaining Delay: %sms", remainingDelay);
    //executing tasks with a fixed time rate,
    //e.g. once every second as demonstrated in this example:
    executor = Executors.newScheduledThreadPool(1);
  //  task = () -> LOG.info("Scheduling 2: " + System.nanoTime());
    task = new schedule.SubscriptionRenewal();
    int initialDelay = 10;
    period = 3;
    executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MINUTES);;
/*
    executor = Executors.newScheduledThreadPool(1);
    task = () -> {
    try {
        TimeUnit.SECONDS.sleep(30);
        LOG.info("Scheduling 3: " + System.nanoTime());
    }catch (InterruptedException e){
        LOG.info("task interrupted");
    }
    };  //end task
    executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS); // days, hours ...
//This example schedules a task with a fixed delay of one second between
//the end of an execution and the start of the next execution.
//The initial delay is zero and the tasks duration is two seconds.
//So we end up with an execution interval of 0s, 3s, 6s, 9s and so on.
//As you can see scheduleWithFixedDelay() is handy if you cannot predict the duration of the scheduled tasks.
    ScheduledExecutorService scheduledExecutorService =
        Executors.newScheduledThreadPool(5);

    ScheduledFuture scheduledFuture = scheduledExecutorService.schedule(() -> {  // lambda expression
        LOG.info("Callable Executed!");
        return "Called!";
    }, 10, TimeUnit.SECONDS);

      try {
          LOG.info("Callable result = " + scheduledFuture.get());
      } catch (InterruptedException | ExecutionException ex) {
          LOG.info(LCUtil.class.getName() + ex);
      }
    scheduledExecutorService.shutdown();
    LOG.info(LCUtil.class.getName() + "shutdown executed");
    */
    
}
public static LocalDateTime DatetoLocalDateTime(java.util.Date date)
{
  return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
}

public static LocalDate DatetoLocalDate(java.util.Date date)
{
  return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
}
/**
     * Creates {@link LocalDate} from {@code java.util.Date} or it's subclasses. Null-safe.
     */
    public static LocalDate asLocalDate(java.util.Date date, ZoneId zone) {
        if (date == null)
            return null;

        if (date instanceof java.sql.Date)
            return ((java.sql.Date) date).toLocalDate();
        else
            return Instant.ofEpochMilli(date.getTime()).atZone(zone).toLocalDate();
    }
    
    
  public static java.util.Date asUtilDate(Object date, ZoneId zone) {
        if (date == null)
            return null;
        if (date instanceof java.sql.Date || date instanceof java.sql.Timestamp)
            return new java.util.Date(((java.util.Date) date).getTime());
        if (date instanceof java.util.Date)
            return (java.util.Date) date;
        if (date instanceof LocalDate)
            return java.util.Date.from(((LocalDate) date).atStartOfDay(zone).toInstant());
        if (date instanceof LocalDateTime)
            return java.util.Date.from(((LocalDateTime) date).atZone(zone).toInstant());
        if (date instanceof ZonedDateTime)
            return java.util.Date.from(((ZonedDateTime) date).toInstant());
        if (date instanceof Instant)
            return java.util.Date.from((Instant) date);
        throw new UnsupportedOperationException("Don't know hot to convert " + date.getClass().getName() + " to java.util.Date");
    }

public static java.sql.Date getSqlDate(java.util.Date dat)
{
       // LOG.debug("calendar date input = " + dat);
////    java.sql.Date d = new java.sql.Date(dat.getTime()); 

    //return d;
  return new java.sql.Date(dat.getTime());
}
  //Then the conversion from java.util.Date to java.sql.Date is quite simple:
public static java.sql.Timestamp getSqlTimestamp(java.util.Date dat) // java.sql.Date : mod 09/05/2013
{
    /*The biggest difference between java.sql.Date and java.sql.Timestamp
    is that the java.sql.Date only keeps the date, not the time,
    of the date it represents. So, for instance,
    if you create a java.sql.Date using the date and time 2009-12-24 23:20,
    then the time (23:20) would be cut off.
    If you use a java.sql.Timestamp then the time is kept.
    */
  return new java.sql.Timestamp(dat.getTime());
}
/**java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

     * Converts the given java.util.Date to java.sql.Date.
     * @param date The java.util.Date to be converted to java.sql.Date.
     * @return The converted java.sql.Date.
     */
    public static java.sql.Date toSqlDate(java.util.Date date) {
     return (date != null) ? new java.sql.Date(date.getTime()) : null;
    }

  public int getAvailableMemory() // encore à tester !!!
  {
    // obtenir le type d'environnement graphique sous lequel tourne la JVM
   GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
   // obtenir le périphérique d'affichage (carte graphique)
   GraphicsDevice device = environment.getDefaultScreenDevice();

   // calcule le nombre de Méga Octets libres dans la carte graphique
   int bytes = device.getAvailableAcceleratedMemory();
 //  int mbytes = 
   return bytes /1048576;
}


public static String secondsToString(int time){
   int seconds = (int)(time % 60);
   int minutes = (int)((time/60) % 60);
   int hours   = (int)((time/60*60) % 24);
   //int days    = (int)((time/60*60*24) % 8);
   String secondsStr = (seconds<10 ? "0" : "")+ seconds;
   String minutesStr = (minutes<10 ? "0" : "")+ minutes;
   String hoursStr   = (hours<10 ? "0" : "")  + hours;
  // String daysStr   = (days<10 ? "0" : "")  + days;
   String t = hoursStr + ":" + minutesStr + ":" + secondsStr + " seconds";
   return t;
}

public static String formatSecsIntoHHMMSS(int secsIn)
{
int hours = secsIn / 3600,  remainder = secsIn % 3600,
  minutes = remainder / 60, seconds   = remainder % 60;

return ( (hours < 10 ? "0" : "") + hours
 + ":" + (minutes < 10 ? "0" : "") + minutes
 + ":" + (seconds< 10 ? "0" : "") + seconds );

} // end function


public static String getFileLastModificationDate (String in_date)
{
// Create an instance of file object.
File file = new File(in_date);
// Get the last modification information.
    //LOG.info("Before Format : " + file.lastModified());

    //LOG.info("After Format : " + dateFormat.format(file.lastModified()));
return SDF_TIME.format(file.lastModified());
} // end

  /**
   *
   * @param value
   * @param decimalPlaces
   * @return
   */

public static Double[] doubleArrayToDoubleArray(double [] ddouble)
{ // https://stackoverflow.com/questions/1109988/how-do-i-convert-double-to-double
    if(ddouble.length != 4)
    {
        LOG.info("error using this function, array must 4 elem !");
        return null;
    }
   Double[] dDouble = new Double[] {0.0, 0.0, 0.0, 0.0};
 //   LOG.info("Double Array = " + Arrays.deepToString(dDouble));
   for(int i=0; i < ddouble.length ; i++)
   {
       double d = ddouble[i];
       Double dd = d;
 //      System.out.println("dObj dd = " + dd);
       dDouble[i] = dd;    
 //   System.out.println("dDouble[i] = " + dDouble[i]);
   }
//   LOG.info("Double Array = " + Arrays.deepToString(dDouble));
   return dDouble;
}

  public static Double myRound(Double value, int decimalPlaces)
{
    //methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
    if (decimalPlaces > 17)
        {return value;}
    //if (decimalPlaces < 1)
    //    {throw new UserException("decimalPlaces < 1");}
    if (value == 0)
        {return Double.valueOf(0);} // 18/10 find bugs
        //{return new Double(0);}
    if (decimalPlaces == 0) // fonctionne pas
        { BigDecimal bd = new BigDecimal(value);
       //   bd = bd.setScale(decimalPlaces,BigDecimal.ROUND_UP);
          bd = bd.setScale(decimalPlaces,RoundingMode.UP);
          return bd.doubleValue();
        }
    final double r = (Math.round(value.doubleValue() * Math.pow(10, decimalPlaces)))
                    / (Math.pow(10, decimalPlaces));
    return Double.valueOf(r);       // 18/10 findbugs new Double(r);
} // end function myRound

  /**
   *
   * @param monTableau
   * @return
   */
  public static int getArrayDimension(Object monTableau )
{
        int dim=0;
        Class cls = monTableau.getClass();
        while( cls.isArray() )
        {
                cls = cls.getComponentType();
                dim++;
        }
        return  dim ;
}
  public static int generatedKey (Connection conn) throws SQLException
{
        Statement st = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_UPDATABLE);
        ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID()");
        int autoIncrementKey = 0;
        if (rs.next())
            {
                autoIncrementKey = rs.getInt(1);
            }else{
           LOG.error("error in Key returned from getGeneratedKeys():" + autoIncrementKey);
            }
        return  autoIncrementKey ;
}
  /**
   *
   * @param times
   * @return
   */
  public static String generateInsertQuery (Connection conn, String table) throws SQLException
        // utilisé pour gestion des database, SQL requests
// construct the SQL. Creates: CheckGameEligibility(?, ?, ?)
{
    int times = DBMeta.getCountColumns(conn, table);
        //LOG.info("times = " + times);
    String s = "?,";
// = parameters placeholders, un par field
    StringBuilder sb = new StringBuilder (s.length()*times);
    //StringBuilder sb2 = new StringBuilder(" VALUES (");
    StringBuilder sb2 = new StringBuilder("INSERT INTO "); // new
    sb2 = sb2.append(table).append(" VALUES (");
    for (int i=0; i<times; i++)
    {
        sb.append(s);
    }
    //LOG.info("sb capacity = " + sb.capacity());
    sb = sb.deleteCharAt(times*2 - 1); // delete dernière virgule
    //LOG.info("sb capacity = " + sb.capacity());
   // sb2 = sb2.append("(").append(sb).append(")");
    sb2 = sb2.append(sb).append(")");
  //      LOG.info("# of question marks = " + sb2.toString());
    return sb2.toString();
}

public static void printArray2DInt(int[][] a)
{
    int ard = getArrayDimension (a);
    System.out.print("[ ");
    for (int row=0; row<a.length; row++)
    {
      if (row > 0)
        {System.out.print("  ");
        System.out.print("[");}
      for (int col=0; col<a[0].length; col++) {
        if (col > 0)
        {System.out.print(", ");
        System.out.format("%3d",a[row][col]);} // field-width = 3
      }
      LOG.info("]");
    }
    LOG.info("]");
  }
//}
/**

public static void printArray2DDouble (double [][] t )
{
    LOG.info("printArray2D " + Arrays.deepToString(t));
      } // end function printArray2D


public static void printArray3DInt(int [][][] t)
{
    // start function
     LOG.info("Start AfficheArray3D for = " + t.length + " elements");
     for (int i=0; i<t.length; i++)
     {     LOG.info("****** start of Group " + (i+1) );
           for (int j=0; j<t[i].length; j++)
           {  LOG.info("\n** start of Question " + (j+1) );
              for (int k=0; k<t[i][j].length; k++)
              {
                  System.out.print("\tfield = " + t[i][j][k] );
              } // end for k
              LOG.info("\n** end of Question " + (j+1) ); //t[i][j][0]);
           } // end for j
           LOG.info("****** end of Group " + (i+1) ); //t[i][0][0]);
     } // end for i
     LOG.info("End of afficheArray3D");
     //LOG.info("Array deep to string " + Arrays.deepToString(t));
} // end function afficheArray3D
//////////////// très important

/*public static File[] listFilesAsArray(File directory, FilenameFilter filter, boolean recurse)
 */

/**
 *
 */
public static void startExecutionTimer()
  {
      reset();
   //   startTime = System.currentTimeMillis();
      Clock clock = Clock.systemDefaultZone();
      startTime = clock.millis();
  }

/**
 *
 */
public static void stopExecutionTimer()
  {
      Clock clock = Clock.systemDefaultZone();
  //  stopTime = System.currentTimeMillis();
      stopTime = clock.millis();
  }

/**
 *
 * @return
 */
public static long durationExecutionTimer()
  {
    return (stopTime - startTime);
  }

/**
 *
 */
public static void reset() {
    startTime = 0;
    stopTime  = 0;
  }
  /**
   *
   */
  public static void javaSpecs()
{
   final java.util.Enumeration liste = System.getProperties().propertyNames();
    String cle;
    while( liste.hasMoreElements() )
    {
        cle = (String)liste.nextElement();
        LOG.info( cle + " = " + System.getProperty(cle) );
    }

     SortedMap<String,Charset> charsets = Charset.availableCharsets();
        for(String nom : charsets.keySet()){
            LOG.info("Charset "+nom);
            Charset charset = charsets.get(nom);
        for(String alias : charset.aliases()){
            System.out.print(" "+alias+",");
   }
   LOG.info("");
}
  } // end method

/**
 *
 * @param fileName
 * @throws Exception
 */
public static void captureScreen(String fileName) throws Exception
{
   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
   Rectangle screenRectangle = new Rectangle(screenSize);
   Robot robot = new Robot();
   BufferedImage image = robot.createScreenCapture(screenRectangle);
   ImageIO.write(image, "png", new File(fileName));
} //end captureScreen


/**
 *
 * @param base
 * @param searchFor
 * @return
 */
public static int countOccurences(String base, String searchFor)
{
    int len = searchFor.length();
    int result = 0;

    if (len > 0) {  // search only if there is something
        int start = base.indexOf(searchFor);
        while (start != -1)
        {
            result++;
            start = base.indexOf(searchFor, start+len);
        }
    }
    return result;
}
/**
 *
 * @return
 */
static public String dumpJavaProperties()
{
    final StringBuffer sb = new StringBuffer();
    final Runtime rt = Runtime.getRuntime();
    final long freeMemory = rt.freeMemory();
    final long totalMemory = rt.totalMemory();
        sb.append("free memory=").append(freeMemory);
    sb.append("\n");
        sb.append("total memory=").append(totalMemory);
    sb.append("\n");
    java.util.Properties p = null;

    try {
      p = System.getProperties();
    }
    catch(Exception e) {
      e.printStackTrace();
      return "";
    }
    final java.util.Enumeration en = p.propertyNames();
    while (en.hasMoreElements())
    {
      final String s = (String) en.nextElement();
      final String strValue= p.getProperty(s);
      sb.append(s).append("=<").append(strValue).append(">");
      sb.append("\n");
    }
    // result to a string
    return sb.toString();
} // end dump Java properties

/**
 *
 * @param <T>
 * @param first
 * @param second
 * @return
 */
//public static <T> String[] concat2Arrays (String[] first, String[] second)
//{
//  String[] result = Arrays.copyOf(first, first.length + second.length);
//  System.arraycopy(second, 0, result, first.length, second.length);
//  return result;
//}

/**
 *
 * @param <T>
 * @param first
 * @param rest
 * @return
 */
public static <T> String[] concatArrays (String[] first, String []... rest)
{
  int totalLength = first.length;
  for (String[] array : rest)
  {
    totalLength += array.length;
  }
  String[] result = Arrays.copyOf(first, totalLength);
  int offset = first.length;
  for (String[] array : rest)
  {
    System.arraycopy(array, 0, result, offset, array.length);
    offset += array.length;
  }
  return result;
}

/**
 *
 * @param meta
 */


/**
 *
 * @param meta
 */

/**
 *
 * @param type
 * @return
 */
public static String getType(int type){
    String sType = "";
switch(type)
{
case DatabaseMetaData.procedureColumnUnknown :
    sType = "inconnu";
    break;
case DatabaseMetaData.procedureColumnIn :
    sType = "IN";
    break;
case DatabaseMetaData.procedureColumnInOut :
    sType = "INOUT";
    break;
case DatabaseMetaData.procedureColumnOut :
    sType = "OUT";
    break;
case DatabaseMetaData.procedureColumnReturn :
    sType = "valeur de retour";
    break;
case DatabaseMetaData.procedureColumnResult :
    sType = "résultat de la requête";
    break;
default : sType = "";
    break;
} // end switch
return sType;
}

    /**
     * Returns the current memory use.
     *
     * @return the current memory use
     */
/*
public static long getMemoryUse()
{
        garbageCollect();
        garbageCollect();
        garbageCollect();
        garbageCollect();
        long totalMemory = Runtime.getRuntime().totalMemory();
        garbageCollect();
        garbageCollect();
        long freeMemory = Runtime.getRuntime().freeMemory();
        return (totalMemory - freeMemory);
}
*/
    /**
     * Makes sure all garbage is cleared from the memory.
     */


//public static void DiskSpace()
public static long DiskSpace() 
{
 try
 { 
     long lc = java.nio.file.Files.getFileStore(Paths.get("c:/")).getUsableSpace();
  LOG.info(" -- Usable Space (Giga )on C:\\ = " + lc);
///        double freeDiskSpace = FileSystemUtils.freeSpaceKb("C:");//calculate free disk space
///        double freeDiskSpaceGB = freeDiskSpace / 1024 / 1024; //convert the number into gigabyte
 ///       LOG.info(" -- Free Disk Space (Giga )on C:\\ = " + freeDiskSpaceGB);
        File file = new File("C:");
        LOG.info(" -- Free Disk Space (Bytes) on C:\\ = " + file.getFreeSpace() );
        return lc;
    } catch (IOException e)    { 
        e.printStackTrace();
        return 0;
    }
}

public static String prepareMessageBean(String message) // new 17/06/2017
{     
try{
       //  https://stackoverflow.com/questions/13655540/read-resource-bundle-properties-in-a-managed-bean
       FacesContext context = FacesContext.getCurrentInstance();
       ResourceBundle text = ResourceBundle.getBundle("/messagesBean", context.getViewRoot().getLocale());
       String someKey = text.getString(message);
       if(someKey.equals(""))
       {
           someKey = "???";
       }    LOG.info("bean internationalisation = " + someKey);
       return someKey;
  }catch (Exception cv){
            String msg = "£££ Exception in prepare MessageBean = " + cv;
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
            return null;
        }     
} // end method

public static void showMessageFatal(String summary)
{            
    try{
 ////       LOG.info("entering showMessageFatal " + FacesContext.getCurrentInstance() );
        if(RequestContext.getCurrentInstance() == null){
            LOG.info("RequestContext.getCurrentInstance() == null");   
    //        RequestContext.getCurrentInstance().execute("{alert('Welcome user - showMessageFatal error!')}");
        }
  
       FacesContext fc = FacesContext.getCurrentInstance();
       fc.getExternalContext().getFlash().setKeepMessages(true); // afficher message si redirection redirect=true
//        LOG.info("face context batch" + FacesContext.getCurrentInstance().getApplication());
       RequestContext rc = RequestContext.getCurrentInstance();
       if(fc != null) //JSF session, fc is null for  BATCH sessions
       {
            fc.getExternalContext().getFlash().setKeepMessages(true);
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_FATAL,summary," (Application GolfLC)");
            fc.addMessage(null, facesMsg);
//         rc.showMessageInDialog(facesMsg); // new 20/07/2015
            PrimeFaces.current().dialog().showMessageDynamic(facesMsg);
       }else{   // fc is null for  BATCH sessions alert('Welcome user - omnifaces msg!'
//           LOG.info("messageFatal this is a batch execution " + summary);
      //    rc.execute("PrimeFaces.info('Hello from the Backing Bean');");
           PrimeFaces.current().executeScript("Welcome user - showMessageFatal error!"); // fonctionne ?
       }
  }
catch (Exception cv)
        {
            String msg = "£££ Exception in addMessageFatal = " + cv;
            LOG.error(msg);
 //           return false;
        }     
} // end method

public static void showMessageInfo(String summary)
{
   // FacesContext as an object is tied directly to the JSF request processing lifecycle
    //and as a result is only available during a standard JSF (user-driven) request-response process
    // donc pas disponible dans Batch Processing jsr-352 !!
        
try{
       FacesContext fc = FacesContext.getCurrentInstance();
       RequestContext rc = RequestContext.getCurrentInstance();
 // https://stackoverflow.com/questions/13685633/how-to-show-faces-message-in-the-redirected-page
  //     fc.getExternalContext().getFlash().setKeepMessages(true); // new 24/07/2017 forcer affichage messages si redirection ex: dans 
       if(fc != null)
       {
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO,summary," (Application GolfLC)");
            fc.getExternalContext().getFlash().setKeepMessages(true); // new 24/07/2017 forcer affichage messages si redirection e
            fc.addMessage(null, facesMsg);
     //       rc.showMessageInDialog(facesMsg); // new 20/07/2015
 ////           PrimeFaces.current().dialog().showMessageDynamic(facesMsg);
           // https://github.com/primefaces/primefaces/blob/master/src/main/java/org/primefaces/context/RequestContext.java
       }else{   // fc is null for  BATCH sessions alert('Welcome user - omnifaces msg!'
 //          LOG.info("MessageInfo this is a batch execution");
       }
}catch(Exception cv){
            String msg = "£££ Exception in showMessageInfo = " + cv;
            LOG.error(msg);
    //        return null;
        }
} //end method

public static void showDialogInfo(String summary)
{
try{
       RequestContext rc = RequestContext.getCurrentInstance();
       FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, summary," (Application GolfLC)");
    //   rc.showMessageInDialog(msg);  // deprecated
       PrimeFaces.current().dialog().showMessageDynamic(msg);
}catch(Exception cv){
       String msg = "£££ Exception in showDialogInfo = " + cv;
            LOG.error(msg);
        }
} //end method

public static void showDialogFatal(String summary)
{
try{
       RequestContext rc = RequestContext.getCurrentInstance();
       FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, summary," (Application GolfLC)");
//       rc.showMessageInDialog(msg);  // deprecated
       PrimeFaces.current().dialog().showMessageDynamic(msg);
}catch(Exception cv){
       String msg = "£££ Exception in showDialogFatal = " + cv;
            LOG.error(msg);
        }
} //end method



public static boolean isValidEmailAddress(String email) {
   boolean result = true;
   try {
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
   } catch (AddressException ex)   {
      result = false;
   }
   return result;
}
/*
public static PreparedStatement prepareStatement(Connection connection, String sql, Object... values) throws SQLException {
    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    for (int i = 0; i < values.length; i++) {
        preparedStatement.setObject(i + 1, values[i]);
    }
    LOG.debug(sql + " " + Arrays.asList(values));
    return preparedStatement;
}
*/

public static void getTimeStampDiff(String in_player) throws SQLException
{   // à modifier !!!
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
     String q = "select AuditStartDate, AuditEndDate,"
             + " TIMESTAMPDIFF (SECOND, AuditStartDate, AuditEndDate) as stamp from audit_in_out"
              + " where AuditPlayerId = ?";
              //+ " order by AuditStartDate desc limit 1 ";
         LOG.info(" -- TIMESTAMPDIFF - query = " + q);

      ps.setString(1, in_player);      // Assign value to input parameter
              LOG.info(" -- AuditPlayerId = " + in_player);
      rs = ps.executeQuery();
      int cpt = 0;
      int tot = 0;
      while (rs.next() )        // ne devrait en avoir qu'un !!!
        {   cpt ++;
            String st = rs.getString("AuditStartDate")+ " - "
                      + rs.getString("AuditEndDate")  + " = " + rs.getInt("stamp");
                //int tim = rs.getInt("stamp");
                tot+=rs.getInt("stamp");
                LOG.info(" -- TIMESTAMPDIFF : " + cpt + " " + st);
        } // end while
      LOG.info("Sessions    = " + cpt);
      LOG.info("Total time  = " + tot);
      if (cpt>0)
      {         LOG.info("Temps moyen = " + tot/cpt + " secondes");
            String t = LCUtil.formatSecsIntoHHMMSS(tot/cpt);
                LOG.info("Time readable = " + t);
            t = LCUtil.secondsToString(tot/cpt);
                LOG.info("Another Time readable = " + t);
      }else{
           LOG.info("First connection for this user");
      }
}
catch(SQLException ex)
{
    LOG.error("-- TIMESTAMPDIFF exception ! " + ex.toString() + "/" );
    throw ex;
} // end catch
finally
{
    LOG.info("-- passing in finally !!! ");
    rs.close();
    ps.close();
}
} //end method

public static Timestamp getLastAuditLogin(Player player, Connection conn) throws SQLException
{
    LOG.info("starting getLastAuditLogin - player = " + player.getIdplayer());
    PreparedStatement ps = null;
    ResultSet rs = null;
    java.sql.Timestamp dbSqlTimestamp = null;
try
{
     String query = "SELECT AuditStartDate, AuditPlayerId from audit_in_out"
              + " where AuditPlayerId = ?"
              + " order by AuditStartDate desc limit 1 ";
      ps = conn.prepareStatement(query);
      ps.setInt(1, player.getIdplayer());      // Assign value to input parameter
      rs = ps.executeQuery();       // Get the result table from the query  3
         //    String p = ps.toString();
         utils.LCUtil.logps(ps);
        if(rs.first())
            {LOG.info("this is a returning connection for : " + player.getIdplayer());
            dbSqlTimestamp = rs.getTimestamp("AuditStartDate");
         //   String s = ;
            LOG.info("last connection string = " + SDF.format(dbSqlTimestamp));
            
        }else{
             LOG.info("this is the first connection for : " + player.getIdplayer());
       //        String text = ;
       //     dbSqlTimestamp = null;   
            dbSqlTimestamp = Timestamp.valueOf("2000-01-01 00:00:00.123456");  // fake date
        }
    return dbSqlTimestamp;
}catch (Exception ex){
    String msg = "Exception in getLastAuditLogin() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
        DBConnection.closeQuietly(null, null, rs, ps);
}

} // end method 



public static int getCountRows(Connection conn, String tableName) throws SQLException
{
    // count the number of rows in the table
    Statement st = null;
    ResultSet rs = null;
try
    {
      st = conn.createStatement();
      rs = st.executeQuery("SELECT COUNT(*) FROM " + tableName);
      // get the number of rows from the result set
      rs.last();
      int rowCount = rs.getRow();

      return rowCount;
    }
catch (SQLException e)
{
	LOG.error("SQLException in getCountRows : " + e);
        return 0;
}
finally
{
    rs.close();
    st.close();
}

  } // end method CountRows

public static int getCountHoles(Connection conn, int tee) throws SQLException
{
    // count the number of rows in the table
    Statement st = null;
    ResultSet rs = null;
try
    {
      st = conn.createStatement();
      rs = st.executeQuery("select count(*) from hole where hole.tee_idtee = " + tee);
      // get the number of rows from the result set
      rs.last();
 //     int rowCount = rs.getRow(); réponses es n imbécile !
      return rs.getRow();
    }
catch (SQLException e)
{
	LOG.error("SQLException in getCountRows : " + e);
        return 0;
}
finally
{
    rs.close();
    st.close();
}

  } // end method CountRows


  public static int[][] initArrayPoints(int[][] p )
{
      for(int i=0;i<p.length;i++)
      {
          for (int j=0;j<6;j++) // why <6 = strokes, extra, ...
          {
              p[i][j] = 0;
          }
      }
        return p;
} //end method

public static void deleteDir(String dir)
{

 File directory = new File(dir);
 LOG.info(" -- directory to be cleaned = " + directory);

// Get all files in directory
File[] files = directory.listFiles();
for (File file : files)
{
    // Delete each file
    if (!file.delete())
        // Failed to delete file
        { long t = file.lastModified();
          LOG.info(" -- Failed to delete = " +
                file + " last modified = " + t );
        }

} // end for
} // end method


public static void logps(PreparedStatement ps) 
{
  try{
        LOG.info("entering logps");     
        //avec connection pool p = org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8@10b61e60
//        org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8.
        //connection classique p = com.mysql.cj.jdbc.ClientPreparedStatement: SELECT idplayer, PlayerFirstName, PlayerLastName, PlayerCity, Play
    String p = ps.toString();
    LOG.info("p toString = " + p);
    if(p.contains("WrappedPreparedStatement")){
        LOG.info("pooled connection");
    }else{
      //  LOG.info("p 0 = " + p.substring(0));
        LOG.debug("Prepared Statement after bind variables set= " + p.substring(p.indexOf(":"), p.length() ));
    }
   }catch (Exception e){
        LOG.error("logpMap Exception " + e);
      }
}

public static void logMap(Map<String, Object> map){
  try{
        LOG.info("entering logMap");     

     /*   // 4. Java 8 - using Stream.forEach()
		map.entrySet()
			.stream()
                        .filter(x -> !x.getKey().equals("facelets.ui.DebugOutput"))
			.forEach(System.out::println);
        */
        
       Map<String, Object> collect = map.entrySet()
                .stream()
		.filter(x -> !x.getKey().equals("facelets.ui.DebugOutput"))  // avoid printing .xhtml files
	//	.collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
       collect.forEach((String key, Object value) -> {
            LOG.info("SessionMap Key : " + key  +" // " + "SessionMap Value : " + value);
            });
    /*   
       map.entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .filter(!getKey().equals("facelets.ui.DebugOutput"))  // avoid printing .xhtml files   
            .forEach(e -> LOG.info("SessionMap one shot " + e));
       
       
       */
   }catch (Exception e){
        LOG.error("logps Exception " + e);
      }
}
public static void LCstartup() throws Exception //throws SQLException //throws SQLException
{
//   Connection conn = null;
try{   
        LOG.info("we are starting LCstartup !!");
    ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
 //       LOG.debug("Startup with ServletContext = " + sc);
        LOG.debug("server Info = " + sc.getServerInfo());
        
 //   FacesContext fc = FacesContext.getCurrentInstance();
 //       LOG.info("facescontext Application = " + fc.getApplication());
                
 //       conn = PostStartupBean.getConn(); // new 20/04/2018
        
        
 /// conn = utils.DBConnection.getConnection();
 
 
    }catch (Exception e){
        LOG.error("There is maybe no database available ??? " + e);
      }
    
   // DBMeta.listMetaData(conn);
   // DBMeta.listMetaData(PostStartupBean.getConn());
   DBConnection.getPooledConnection();
   ListAllSystemProperties();
   
 //  DBConnection.closeQuietly(conn, null, null, null);
   
 //  InputStream in = null;
 /*   
   try{
       LOG.info("starting properties files");
    File confDir = new File(System.getProperty("jboss.server.config.dir"));
    File fileProp = new File(confDir, "myLC.properties");
    //teste fileProp.exists etc.
    in = new FileInputStream(fileProp);
    Properties prop = new Properties();
    // load a properties file
    prop.load(in);
    // get the property value and print it out
        LOG.info("database = " + prop.getProperty("database"));
        LOG.info("dbuser = " + prop.getProperty("dbuser"));
        LOG.info("dbpassword = " + prop.getProperty("dbpassword"));
        Enumeration<?> e = prop.propertyNames();
	while (e.hasMoreElements()) {
		String key = (String) e.nextElement();
		String value = prop.getProperty(key);
		LOG.info("Key : " + key + ", Value : " + value);
	}
    }catch (FileNotFoundException e){
        LOG.error("File not found file myLC.properties : " + e);
   }catch (Exception e){
        LOG.error("Unable to load properties file my.properties : " + e);
        
   }finally{
        in.close();
   }
    */
//}catch (SQLException e){
//	LOG.error("error SQL in DBMeta first step : " + e);
    //    throw e;
// } finally {
           // DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
       //     DBConnection.closeQuietly(conn, null, null, null); // new 14/8/2014
//}

} //end method

public static String firstPartUrl(){
 //   String href = "####";
try{   
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
     //    **uri = /GolfNew-1.0-SNAPSHOT 
        String host = ec.getRequestServerName();
   //       LOG.info("** application host = " + host);   
        int port = ec.getRequestServerPort();
  //       LOG.info("** port = " + port);   
        String uri = ec.getRequestContextPath();
         // Return the portion of the request URI that identifies the web application context for this request.     
         String href = "http://" + host + ":" + port + uri;
         //http://localhost:8080/GolfWfly-1.0-SNAPSHOT/login.xhtml
        return href;
}catch (Exception e){
	LOG.error("error in firstPartUrl : " + e);
        return null;
 } finally {
       
 }

} //end method
/*
public static String getFileNameFromPart(Part part)
{
        final String partHeader = part.getHeader("content-disposition");
        for (String content : partHeader.split(";")) {
            if (content.trim().startsWith("filename")) {
                String fileName = content.substring(content.indexOf('=') + 1)
                        .trim().replace("\"", "");
                return fileName;
            }
        }
        return null;
    }
*/
 public static String uncompress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        LOG.info("Uncompress.Input String length : " + str.length());
        //final InputStream is = new GZIPInputStream( new FileInputStream( file ), 65536 
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str.getBytes("ISO-8859-1")),512);
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "ISO-8859-1"));
        String outStr = "";
        String line;
        while ((line=bf.readLine())!=null) {
          outStr += line;
        }
        LOG.info("Uncompress.Output String length : " + outStr.length());
        LOG.info("Uncompress.Output String  : " + outStr);
        return outStr;
     } // end method decompress
  
 // http://www.softraction.com/2011/10/compressing-and-decompressing-string-in.html
  public static String compress(String str)  throws IOException {
        if (str == null || str.length() == 0)
            {  return str;  }
        long start = System.nanoTime();
            LOG.info("Compress.Input String length : " + str.length()); // 336
        ByteArrayOutputStream out = new ByteArrayOutputStream();
     try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
           gzip.write(str.getBytes());
       }
        String outStr = out.toString("ISO-8859-1");
        LOG.info("Compress.Output String length : " + outStr.length()); //72
 //       System.out.println("Compress.String compressed : " + outStr);
        LOG.info("Compress.ratio : " + outStr.length() / (str.length()/100) + " % ");
        LOG.info("Compress.time : " + Long.toString(System.nanoTime() - start) );
    LOG.info("ending compress " );
        return outStr;
 } // end method compress
  
  public static String array2DToString(String [][] input){
  try{
      String s = Arrays.deepToString(input);
          LOG.info("array2DToString.input = " + s);
       return s;
  }catch (Exception e){
     String msg = "array2DToString.Exception in string2D = " + e ;
        LOG.info("error = " + msg );
     return null;}
}  //end method 
  
  public static String[] stringToArray1D(String input){
  try{      // one delimiter = ","
          LOG.info("stringToArray1D. input = " + input);
        input = input.replace("[", "").replace("]", "").replace(", ", ",");
            LOG.info("str stripped = " + input);
        String[] matrix = input.split(",");
            LOG.info("array = " + Arrays.toString(matrix));
  //      System.out.println("stringToArray2D.matrix = " + Arrays.deepToString(matrix));
    return matrix;
  }catch (Exception e){
    String msg = "stringToArray1D.Exception in string1D = " + e ;
        LOG.info("error = " + msg );
        return null;}
}  //end method
    
  
  
  public static String[][] stringToArray2D(String input){
  try{      // two delimiters = ";" et ensuite ","
          LOG.info("stringToArray2D. input = " + input);
    String lines[] = input.split(";");
        LOG.info("lines = " + Arrays.deepToString(lines));
    int width = lines.length;
        LOG.info("width = " + width);
    String[][] matrix = new String[width][]; 
 // Enhanced For-Loop 
    int r = 0;
    for (String row : lines) {
        matrix[r++] = row.split(",") ;
       // System.out.println(" * matrix = " + (r) + " = " + matrix[r]);
    }
        LOG.info("stringToArray2D.matrix = " + Arrays.deepToString(matrix));
    return matrix;
  }catch (Exception e){
    String msg = "stringToArray2D.Exception in string2D = " + e ;
        LOG.error("error = " + msg );
        return null;}
}  //end method 
  // new 28/01/2017
  
        
  public static String[] extractSubarray(final ScoreMatchplay src, final int xtr)
{
        int ii=src.getScoreMP4()[0].length;
        String[] dest = new String[ii];
        for (int i=0; i<ii; i++)
        { 
            if(i == xtr)
            {
 //               LOG.info("i = {} en xtr = {}",i ,xtr);
 //               LOG.info("player = " + score.getPlayers()[i] );
                for (int j=0; j<ii; j++)
                {
                  dest[j] = src.getScoreMP4()[i][j];
                }
             } // end if
        } // end for
        
 //           LOG.info("extracted subarray = " + Arrays.deepToString(dest) );
 //           LOG.info("player = " + score.getPlayers()[xtr]);
            return dest;
} // end method
  
  public static void printCurrentPhaseID(){
      PhaseId currentPhaseId = FacesContext.getCurrentInstance().getCurrentPhaseId();
        LOG.info("currentPhaseId = " + currentPhaseId);
  }
  
  
  public static String[] removeNull1D( String[] arr1d) {
  //String[] inputs;
ArrayList<String> items = new ArrayList<>(arr1d.length);
for(String input : arr1d) {
   if (input != null) {
      items.add(input);
   }
} // end for
String[] outputs = items.toArray(new String[items.size()]);
  return outputs;
  }
  
  
    
  
  public static String[][] removeNull2D( String[][] arr2d) {
         // https://stackoverflow.com/questions/32099750/delete-null-element-in-2d-array-in-java?lq=1
         // used with tarif 
        ArrayList<ArrayList<String>> list2d = new ArrayList<>();
        for(String[] arr1d: arr2d){
            ArrayList<String> list1d = new ArrayList<>();
            for(String s: arr1d){
                if(s != null && s.length() > 0) {   // isEmpty() ?
                    list1d.add(s);
                }
            }
            // you will possibly not want empty arrays in your 2d array so I removed them
            if(list1d.size() > 0){
                list2d.add(list1d);
            }
        }
        String[][] cleanArr = new String[list2d.size()][];
        int next = 0;
        for(ArrayList<String> list1d: list2d){
            cleanArr[next++] = list1d.toArray(new String[list1d.size()]);
        }
     return cleanArr;
  }  //end removeNull2D
  
  public static LocalDate EasterSundayDate(int year)
    {  // oudin's algorithm
        //
        int a = year % 19,
            b = year / 100,
            c = year % 100,
            d = b / 4,
            e = b % 4,
            g = (8 * b + 13) / 25,
            h = (19 * a + b - d - g + 15) % 30,
            j = c / 4,
            k = c % 4,
            m = (a + 11 * h) / 319,
            r = (2 * e + 2 * j - k - h + m + 32) % 7,
            month  = (h - m + r + 90) / 25,
            day = (h - m + r + month + 19) % 32;

       LocalDate ld = LocalDate.of(year, month, day);
        LOG.info("In the year " + year + " Easter with fall on day " + day + " of month " + month + " / " + ld);
      //  LOG.info("p = " + p);
return ld;
} //end EasterSundayDate
  
  public static boolean isValidTimeZone(final String timeZone) {
    final String DEFAULT_GMT_TIMEZONE = "GMT";
    
    if (timeZone == null){
        LOG.fatal("timeZone is null");
        return false;
    }
    if (timeZone.equals(DEFAULT_GMT_TIMEZONE)) {
        return true;
    } else {
        // if custom time zone is invalid, time zone id returned is always "GMT" by default
        String id = TimeZone.getTimeZone(timeZone).getID();
        if (!id.equals(DEFAULT_GMT_TIMEZONE)) {
            return true;
        }
    }
    return false;
}
  
  public static void ListAllSystemProperties() {
try{
    /*
      LOG.info("entering listallsystemproperties");
        Properties systemProperties = System.getProperties();
        Enumeration enuProp = systemProperties.propertyNames();
        while (enuProp.hasMoreElements()) {
            String propertyName = (String) enuProp.nextElement();
            String propertyValue = systemProperties.getProperty(propertyName);
            LOG.debug("System property Name = " + propertyName + " Value = " + propertyValue);
        }
 */
   System.getenv().forEach((k, v) -> {
    LOG.info("getenv() = "+ k + ":" + v);
    });
 
   System.getProperties().entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .forEach(e -> LOG.info("System Property " + e));
// liste.forEach(item -> LOG.info("Flight list " + item));  // java 8 lambda

}catch (Exception e){
    String msg = "error listallasystemproperties = " + e ;
        LOG.error("error = " + msg );
        }
}
  

public static void main(String[] args) // throws IOException,Exception
{
} //end class main


} // end Class ApexUtil
