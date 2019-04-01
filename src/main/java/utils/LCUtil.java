package utils;

import entite.ECourseList;
import entite.Player;
import entite.ScoreMatchplay;
import static interfaces.Log.LOG;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
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
import javax.faces.context.Flash;
import javax.faces.event.PhaseId;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import lc.golfnew.LanguageController;
import static org.apache.commons.lang3.StringUtils.repeat;
import org.primefaces.PrimeFaces;
//import org.primefaces.context.RequestContext;
//import org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8;
/**
 *
 * @author Louis Collet
 */
public class LCUtil implements interfaces.GolfInterface, interfaces.Log    // constantes
{
  private static long startTime;
  private static long stopTime;
 // @Inject private static FacesContext fc;
  @Inject private static Flash flash;
  @Inject private static ExternalContext ec;

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
public static LocalDateTime DatetoLocalDateTime(java.util.Date date){
try{
   //    LOG.info("entering DatetoLocalDateTime with Date = " + date);
       if(date == null){
          return null;
       }
        return date.toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDateTime();
 }catch(Exception e){
   String msg = "£££ Exception in DatetoLocalDateTime = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
}
public static java.sql.Date LocalDateTimetoSqlDate(LocalDateTime date){
try{
        LOG.info("entering LocalDateTimetoSqlDate with Date = " + date);
        if(date == null){
          return null;
        }
        return java.sql.Date.valueOf(date.toLocalDate());
 }catch(Exception e){
   String msg = "£££ Exception in LocalDateTimetoSqlDate = " + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
}
public static LocalDate LocalDateTimetoLocalDate(LocalDateTime date){
try{
        LOG.info("entering LocalDateTimetoLocalDate with date = " + date);
        if(date == null){
          return null;
        }
        return date.toLocalDate();
 }catch(Exception e){
   String msg = "£££ Exception in LocalDateTimetoLocalDate = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
}


public static LocalDate DatetoLocalDate(java.util.Date date){
  try{
      LOG.info("entering DatetoLocalDate with Date = " + date);
      if(date == null){
          return null;
      }
        return date.toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDate();
   }catch(Exception e){
   String msg = "£££ Exception in DatetoLocalDate = " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
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
               //    java.util.Date.from(date.atZone(zone).toInstant());
            return java.util.Date.from(((LocalDateTime) date).atZone(zone).toInstant());
        if (date instanceof ZonedDateTime)
            return java.util.Date.from(((ZonedDateTime) date).toInstant());
        if (date instanceof Instant)
            return java.util.Date.from((Instant) date);
        throw new UnsupportedOperationException("Don't know hot to convert " + date.getClass().getName() + " to java.util.Date");
    }

public static java.sql.Date getSqlDate(java.util.Date dat){
       // LOG.debug("calendar date input = " + dat);
////    java.sql.Date d = new java.sql.Date(dat.getTime()); 

    //return d;
  return new java.sql.Date(dat.getTime());
}
  //Then the conversion from java.util.Date to java.sql.Date is quite simple:
public static java.sql.Timestamp getSqlTimestamp(java.util.Date dat){

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
/*
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
*/

public static String secondsToString(int time){
   int seconds = (time % 60);
   int minutes = ((time/60) % 60);
   int hours   = ((time/60*60) % 24);
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

  public static Double myRound(Double value, int decimalPlaces){
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
  public static int getArrayDimension(Object monTableau ){
        int dim=0;
        Class<?> cls = monTableau.getClass();
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
  public static String generateInsertQuery_old (Connection conn, String table) throws SQLException{
        // utilisé pour gestion des database, SQL requests
// construct the SQL. Creates: CheckGameEligibility(?, ?, ?)

    int times = DBMeta.CountColumns(conn, table);
        //LOG.info("times = " + times);
    String s = "?,";
// = parameters placeholders, un par field
    StringBuilder sb = new StringBuilder(s.length()*times);
    //StringBuilder sb2 = new StringBuilder(" VALUES (");
    StringBuilder sb2 = new StringBuilder(); // new
  //  sb2 = sb2.append(table).append(" VALUES (");
    sb2.append("INSERT INTO ").append(table).append(" VALUES (");
    for(int i=0; i<times; i++){
        sb.append("?,");
    }
    //LOG.info("sb capacity = " + sb.capacity());
    sb = sb.deleteCharAt(times*2 - 1); // delete dernière virgule
    //LOG.info("sb capacity = " + sb.capacity());
   // sb2 = sb2.append("(").append(sb).append(")");
    sb2 = sb2.append(sb).append(")");
  //      LOG.info("# of question marks = " + sb2.toString());
    return sb2.toString();
}
  public static String generateInsertQuery (Connection conn, String table) throws SQLException{
        // utilisé pour gestion des database, SQL requests
    int times = DBMeta.CountColumns(conn, table);
        //LOG.info("times = " + times);
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO ").append(table).append(" VALUES (");
    for(int i=0; i<times; i++){
        sb.append("?,"); // = parameters placeholders, one par field
    }
    sb.deleteCharAt(sb.lastIndexOf(",")).append(");"); // delete dernière virgule
 //   LOG.info("generated sb = " + sb);
    return sb.toString();
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
   final java.util.Enumeration<?> liste = System.getProperties().propertyNames();
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

public static void captureScreen(String fileName) throws Exception
{
   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
   Rectangle screenRectangle = new Rectangle(screenSize);
   Robot robot = new Robot();
   BufferedImage image = robot.createScreenCapture(screenRectangle);
   ImageIO.write(image, "png", new File(fileName));
} //end captureScreen
 */

/**
 *
 * @param base
 * @param searchFor
 * @return
 */
public static int countOccurences(String base, String searchFor){
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
    final java.util.Enumeration<?> en = p.propertyNames();
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
public static long DiskSpace() {
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

public static String prepareMessageBean(String message){
try{
       //  https://stackoverflow.com/questions/13655540/read-resource-bundle-properties-in-a-managed-bean
        FacesContext context = FacesContext.getCurrentInstance();
        LOG.info("responseComplete has been called : " + context.getResponseComplete());
        if(context == null){
            LOG.info("FacesContext.getCurrentInstance() = null");
            return "***not found***";
         }

       ResourceBundle text = ResourceBundle.getBundle("/messagesBean", context.getViewRoot().getLocale());
       LOG.info(" text language = " + text.getLocale().getLanguage());
       String someKey = text.getString(message);
       if(someKey.equals("")){
           someKey = "???";
       } 
  //     LOG.info("bean internationalisation key found = " + someKey);
       
       return someKey;
 }catch (java.util.MissingResourceException mr){
            String msg =  message +  " / Doesn't exists - for language = " + LanguageController.getLanguage() + " / " + mr;
            LOG.info(msg);
            utils.LCUtil.showMessageInfo(msg);
            return null;
  }catch (Exception cv){
            String msg = "£££ Exception in prepare MessageBean = " + cv;
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
            return null;
   }     
} // end method


public static void showMessageFatal(String summary){
    try{
       FacesContext fc1 = FacesContext.getCurrentInstance();
       if(fc1 != null){ //JSF session, 
            fc1.getExternalContext().getFlash().setKeepMessages(true);
            summary = "<h3>" + summary + "</h3>";  // new 18-02-2019
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_FATAL,summary," (Application GolfLC)");
            fc1.addMessage(null, facesMsg);
         //   PrimeFaces.current().dialog().showMessageDynamic(facesMsg);
            showDialogFatal(facesMsg);
            
       }else{ //fc is null for BATCH sessions
           LOG.info("messageFatal this is a batch execution " + summary);
           PrimeFaces pf = PrimeFaces.current();
           pf.executeScript("{alert('Welcome user - showMessageFatal error!')}");
         //  PrimeFaces.current().executeScript("Welcome user - showMessageFatal error!"); // fonctionne ?
       }
  }catch (Exception cv){
            String msg = "£££ Exception in addMessageFatal = " + cv;
            LOG.error(msg);
 //           return false;
        }     
} // end method
public static void showMessageFatal2(String summary, String summary2){
    try{
       FacesContext fc1 = FacesContext.getCurrentInstance();
       if(fc1 != null){ //JSF session, 
            fc1.getExternalContext().getFlash().setKeepMessages(true);
            summary = "<h3>" + summary + "</h3>";  // new 18-02-2019
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_FATAL,summary,summary2);
            fc1.addMessage(null, facesMsg);
            showDialogFatal(facesMsg);
       }else{ //fc is null for BATCH sessions
           LOG.info("messageFatal this is a batch execution " + summary);
           PrimeFaces pf = PrimeFaces.current();
            pf.executeScript("{alert('Welcome user - showMessageFatal error!')}");
         //  PrimeFaces.current().executeScript("Welcome user - showMessageFatal error!"); // fonctionne ?
       }
  }catch (Exception cv){
            String msg = "£££ Exception in addMessageFatal = " + cv;
            LOG.error(msg);
 //           return false;
        }     
} // end method
public static void showMessageInfo(String summary)
{
   // https://stackoverflow.com/questions/13685633/how-to-show-faces-message-in-the-redirected-page
    //FacesContext as an object is tied directly to the JSF request processing lifecycle
    //and as a result is only available during a standard JSF (user-driven) request-response process
    // donc pas disponible dans Batch Processing jsr-352 !!
        
try{
       FacesContext fc = FacesContext.getCurrentInstance();
//       RequestContext rc = RequestContext.getCurrentInstance();
 // https://stackoverflow.com/questions/13685633/how-to-show-faces-message-in-the-redirected-page
  //     fc.getExternalContext().getFlash().setKeepMessages(true); // new 24/07/2017 forcer affichage messages si redirection ex: dans 
       if(fc != null){
            summary = "<h2>" + summary + "</h2>";  // new 18-02-2019
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

public static void showDialogInfo(String summary){
try{
       FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, summary," (Application GolfLC)");
       PrimeFaces.current().dialog().showMessageDynamic(msg);
}catch(Exception cv){
       String msg = "£££ Exception in showDialogInfo = " + cv;
            LOG.error(msg);
        }
} //end method

public static void showDialogFatal(FacesMessage msg){
try{
      
   //    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, summary," (Application GolfLC)");
       PrimeFaces.current().dialog().showMessageDynamic(msg);
 }catch(Exception cv){
       String err = "£££ Exception in showDialogFatal = " + cv;
            LOG.error(err);
        }
} //end method


/*
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
*/
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

public static void getAuditTimeStampDiff(String in_player) throws SQLException
{   // à modifier !!!
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
     String q = "select AuditStartDate, AuditEndDate,"
             + " TIMESTAMPDIFF (SECOND, AuditStartDate, AuditEndDate) as stamp from audit"
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
 ///       LOG.info("entering logps");     
        //avec connection pool p = org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8@10b61e60
//        org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8.
        //connection classique p = com.mysql.cj.jdbc.ClientPreparedStatement: SELECT idplayer, PlayerFirstName, PlayerLastName, PlayerCity, Play
    String p = ps.toString();
 ///   LOG.info("p toString = " + p);
    if(p.contains("WrappedPreparedStatement")){
//        LOG.info("pooled connection");
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
  LOG.info("removeNull1D input = " + Arrays.deepToString(arr1d));
ArrayList<String> items = new ArrayList<>(arr1d.length);
for(String input : arr1d) {
   if (input != null) {
      items.add(input);
   }
} // end for
String[] outputs = items.toArray(new String[items.size()]);
LOG.info("removeNull1D output = " + Arrays.deepToString(arr1d));
  return outputs;
  }
  
  
    
  
  public static String[][] removeNull2D(String[][] arr2d) {
         // https://stackoverflow.com/questions/32099750/delete-null-element-in-2d-array-in-java?lq=1
         // used with tarif 
         LOG.info("removeNull2D input = " + Arrays.deepToString(arr2d));
        ArrayList<ArrayList<String>> list2d = new ArrayList<>();
        for(String[] arr1d: arr2d){
            ArrayList<String> list1d = new ArrayList<>();
            for(String s: arr1d){
                if(s != null && s.length() > 0) {   // isEmpty() ?
                    list1d.add(s);
                }else{
      //              LOG.info("s = " + s); // new 18/02/2019
                }
            }
            // you will possibly not want empty arrays in your 2d array so I removed them
            if(list1d.size() > 0){
                list2d.add(list1d);
            }else{
      //          LOG.info("list1d.size = 0");
            }
        }
        String[][] cleanArr = new String[list2d.size()][];
        int next = 0;
        for(ArrayList<String> list1d: list2d){
            cleanArr[next++] = list1d.toArray(new String[list1d.size()]);
        }
         LOG.info("removeNull2D output = " + Arrays.deepToString(cleanArr));
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
  
public static String findProperties(String cat, String subcat) throws IOException {
try{
    LOG.info("cat : " + cat);
    LOG.info("subcat : " + subcat);
    ClassLoader clo = Thread.currentThread().getContextClassLoader(); // new 25-11-2018
     // Netbeans Files en haut à gauche /src/main/resources
     //InputStream is = clo.getResourceAsStream("subscription.properties");
       InputStream is = clo.getResourceAsStream(cat + ".properties");
       Properties p = new Properties();
       p.load(is);
       String r = p.getProperty(cat + "." + subcat);
    //     price = p.getProperty("subscription.month");
        //     price = p.getProperty("subscription.month"); //subscripton.month
       LOG.info("Property cat + e = " + r);
    return r;
    
  }catch (Exception e){
    String msg = "error findProperties = " + e ;
    LOG.error(msg);
    return null;
        }
}
public static String[] intArraytoStringArray(int[] int01){
  try{  
    String[] str01 = new String[int01.length];
    for(int i=0;i<int01.length;i++){
           str01[i] = String.valueOf(int01[i]);
       }    
    return str01;
}catch (Exception e){
    String msg = "error listallasystemproperties = " + e ;
        LOG.error("error = " + msg );
        return null;
        }
}
  
public static String fillRoundPlayersStringEcl(java.util.List<ECourseList> players) {

    ArrayList<Player> p = new ArrayList<>();   // transform player2 in list<player<    
    for(int i=0; i < players.size() ; i++)
    {
//    LOG.info("elem = " + players.get(i).Eplayer.getPlayerLastName());
    p.add(players.get(i).Eplayer);
     //       LOG.debug(" -- item in for idplayer # = " + dlPlayers.getTarget().get(i).getIdplayer() );
    }   
    return fillRoundPlayersString(p);
}

public static String fillRoundPlayersString(java.util.List<Player> players) {
   //  String s = ""; 
 if(players.isEmpty()){  // was size == 0
      LOG.info(" exiting fillRoundPlayersString with no player");
     return "";
 }
     StringBuilder sb = new StringBuilder();
     for(int i=0; i < players.size() ; i++)
     {
        sb.append(players.get(i).getPlayerLastName()).append(" (");
        sb.append(players.get(i).getIdplayer()).append("), ");
     } // end for 
      sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
 //    LOG.info(" exiting fillRoundPlayersString with = " + sb.toString());
 return sb.toString();
}

  
public static class MapToArrayExample {
    public String[] mapValuesToArray(Map <Integer,String> sourceMap) {
       Collection <String> values = sourceMap.values();
       String[] targetArray = values.toArray(new String[values.size()]);
       return targetArray;
    }
    
    }

public static String extractHHmm (String sunris) {
  try{
    LOG.info("input string = " + sunris);
      int egal1 = sunris.indexOf("=")+1; //, sunrise.indexOf(",") + 1);  // cherche 2e virgule
        LOG.info("egal 1 = " + egal1);
      int virgule1 = sunris.indexOf(",");
        LOG.info("virgule1 = " + virgule1);
      String hours = sunris.substring(egal1, virgule1);
      LOG.info("hours = " + hours);
      
      int egal2 = sunris.indexOf("=", sunris.indexOf("=") + 1);  // cherche 2e virgule
        LOG.info("egal 2 = " + egal2);
      int virgule2 = sunris.indexOf(",", sunris.indexOf(",") + 1);  // cherche 2e virgule     
        LOG.info("virgule2 = " + virgule2);
      String minutes = sunris.substring(egal2+1, virgule2);   
       LOG.info("minutes = " + minutes);
       
      int egal3 = sunris.lastIndexOf("="); //, sunris.indexOf("=") + 1);  // cherche 2e virgule
        LOG.info("egal 3 = " + egal3);
       
      String ampm = sunris.substring(egal3+1, egal3+2+1);
          LOG.info("AMPM = " + ampm);
      if(ampm.equals("PM")){
            LOG.info("hours = PM ");
          int h = Integer.parseInt(hours) + 12;
          hours = String.valueOf(h);
          LOG.info("PM hours corrected = " + hours);
      }else{
           LOG.info("hours = AM ");
      }
      LOG.info("sunrise = " + hours +"." + minutes);
    return(hours + "." + minutes);
 //   }
  }catch (Exception e){
    String msg = "error findProperties = " + e ;
    LOG.error(msg);
    return null;
        }
} // end method

 public static int calculateAgeFirstJanuary(java.util.Date birthDate){ //, LocalDate currentDate) {
 try{
           LOG.info("entering calculateAgeFirstJanuary" );
           LOG.info("entering calculateAgeFirstJanuary with birthdate = " + birthDate);
     if(birthDate != null) {
        LocalDate localDateBirth = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(birthDate) );
    //       LOG.info("localDateBirth = " + localDateBirth);
	LocalDate firstDayYear = LocalDate.of(LocalDate.now().getYear(), Month.JANUARY, 1);
   //        LOG.info("first day year = " + firstDayYear);
        return Period.between(localDateBirth,firstDayYear).getYears();
     }else{
            return 99;
     }
    }catch(Exception e){
    String msg = "Error calculateAgeFirstJanuary = " + e ;
        LOG.error("error = " + msg );
       return 99;
    }
    }//end method
 
    /**
     * Clones the provided array
     * 
     * @param src
     * @return a new clone of the provided array
     */
    public static int[][] cloneArray2D(int[][] src) {
        int length = src.length;
        int[][] target = new int[length][src[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }
        return target;
    }

  public static String[][] cloneStringArray2D(String[][] src) {
      //deep copy of non-primitive arrays type, OPPOSITE to shallow copy
    String[][] dst = new String[src.length][];
    for (int i = 0; i < src.length; i++) {
        dst[i] = Arrays.copyOf(src[i], src[i].length);
    }
    return dst;
}

    
public static void main(String[] args) throws Exception{ // throws IOException,Exception
/*
     MapToArrayExample mapToArrayExample = new MapToArrayExample();
       Map <Integer,String> sourceMap = new HashMap < > ();
        sourceMap.put(100, "ABC");
        sourceMap.put(101, "PQR");
       sourceMap.put(102, "XYZ");
       String[] targetArray = mapToArrayExample.mapValuesToArray(sourceMap);
        System.out.println(Arrays.toString(targetArray));
        */
    DBConnection dbc = new DBConnection();
    Connection conn = dbc.getConnection();
    String s = generateInsertQuery(conn, "player");
        LOG.info("string généré 1 = " + s);
 //   s = generateInsertQuery(conn, "player");
 //       LOG.info("string généré 2 = " + s);
    DBConnection.closeQuietly(conn, null, null,null); 
    
    
    
} //end class main
 public static String creditcardSecret(String creditcardNumber) {
 try{
    if(creditcardNumber == null) return null;
    return repeat("*", 12) + creditcardNumber.substring(creditcardNumber.length()-4);
    // on affiche des * sauf les 4 derniers caractères qui sont affichés
    }catch (Exception e){
    String msg = "error creditcardSecret = " + e ;
        LOG.error("error = " + msg );
        return null;
    }
}
} // end Class LCUtil
