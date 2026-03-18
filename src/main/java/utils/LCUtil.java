package utils;

// import Controllers.LanguageController; // removed — fix multi-user 2026-03-07
import static com.google.api.client.util.Strings.isNullOrEmpty;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.sql.*;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import jakarta.enterprise.inject.spi.CDI;
import org.apache.commons.lang3.StringUtils;
import static org.omnifaces.util.Faces.getResourceAsStream;
//import org.apache.maven.shared.utils.StringUtils;
import org.primefaces.PrimeFaces;


@Named("utilC") //enlevé 16/05/2022
@RequestScoped

 public class LCUtil implements interfaces.GolfInterface{   
    
 // startTime/stopTime removed — fix multi-user 2026-03-07 (static mutable, no callers)
 // private static Locale locale — removed, fix multi-user 2026-03-07 (use local vars instead)
 // private static FacesContext context — removed, fix multi-user 2026-03-07 (use local vars instead)
  // TODOhttps://blog.stackademic.com/99-of-java-utility-classes-are-wrong-heres-the-right-way-7871fee6b8c4
//DateUtils → Date/time formatting & parsing.
//CollectionUtils → List/Map/Set helpers.
//ValidationUtils → Input validations. 


//private LCUtil(){  //enlevé 30-12-2025 pout chrcher erreur
    // Private constructor prevents instantiation new 19-12-2025
  // String msg = "Private constructor prevents instanciation - LCUtil Utility class should not be instantiated ! ";
  //  LOG.debug(msg);
  //      throw new UnsupportedOperationException("LCUtil Utility class should not be instantiated ! for : msg");
  // }



/**
 * Resolve current user locale via CDI LanguageController (session-scoped).
 * Fallback to FacesContext viewRoot, then Locale.ENGLISH.
 */
private static Locale resolveLocale() {
    try {
        Controllers.LanguageController lc = CDI.current().select(Controllers.LanguageController.class).get();
        if (lc != null && lc.getLocale() != null) {
            return lc.getLocale();
        }
    } catch (Exception ignored) {
        // outside CDI context (batch, run, tests)
    }
    FacesContext fc = FacesContext.getCurrentInstance();
    if (fc != null && fc.getViewRoot() != null) {
        return fc.getViewRoot().getLocale();
    }
    return Locale.ENGLISH;
} // end method

public static String capitalize(String input) {
        if (isNullOrEmpty(input)) { //google
            return "";
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


  // pas utilisé
  public static void executeQuery(String sql) throws SQLException{
      throw new SQLException("Syntax Error discovered by LC");
  }
  
public static Object getSessionMapValue(String key){
   return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(key);
}

public static void setSessionMapValue(String key, Object value){
   FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put(key, value);
}

public static String getCurrentTimeWithZoneOffset(String lzt){
        LOG.debug("start with " + lzt);
    Instant now = Instant.now();
    ZoneId zoneId = ZoneId.of(lzt);
        LOG.debug("zoneId " + zoneId);
    ZonedDateTime zdt = ZonedDateTime.ofInstant(now, zoneId);
         LOG.debug("zonedDateTime " + zdt.toString());
         LOG.debug("rules = " + zoneId.getRules().toString());
    String offset = zdt.getOffset().toString();
         LOG.debug("Offset = " + offset);
    if (zoneId.getRules().isDaylightSavings(zdt.toInstant())) {
        System.out.printf("  (%s daylight saving time will be in effect.)%n", zoneId);
        LOG.debug("DAYLIGHT saving time will be in effect for " + zoneId);}
    else {
        System.out.printf("  (%s standard time will be in effect.)%n", zoneId);
        LOG.debug("Standard time will be in effect for " + zoneId);}
    return zdt.toString();
   // return zdt.format(ZDF) + " Offset = " + offset;

}


public static void delayLC() {// throws InterruptedException, ExecutionException{
  
}
public static LocalDateTime DatetoLocalDateTime(java.util.Date date){
try{
   //    LOG.debug("entering DatetoLocalDateTime with Date = " + date);
       if(date == null){
          return null;
       }
        return date.toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDateTime();
 }catch(Exception e){
   String msg = "£££ Exception in DatetoLocalDateTime = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    return null;
  }
}
public static java.sql.Date LocalDateTimetoSqlDate(LocalDateTime date){
try{
   //     LOG.debug("entering LocalDateTimetoSqlDate with Date = " + date);
        if(date == null){
          return null;
        }
        return java.sql.Date.valueOf(date.toLocalDate());
 }catch(Exception e){
   String msg = "£££ Exception in LocalDateTimetoSqlDate = " + e.getMessage();
   LOG.error(msg);
    return null;
  }
}
public static LocalDate LocalDateTimetoLocalDate(LocalDateTime date){
try{
        LOG.debug("entering LocalDateTimetoLocalDate with date = " + date);
        if(date == null){
          return null;
        }
        return date.toLocalDate();
 }catch(Exception e){
    String msg = "£££ Exception in LocalDateTimetoLocalDate = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
    LOG.error(msg);
    return null;
  }
}


public static LocalDate DatetoLocalDate(java.util.Date date){
  try{
 //     LOG.debug("entering DatetoLocalDate with Date = " + date);
      if(date == null){
          return null;
      }
        return date.toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDate();
   }catch(Exception e){
      String msg = "£££ Exception in DatetoLocalDate = " + e.getMessage();
      LOG.error(msg);
      return null;
  }
}

public static java.util.Date LocalDateTimeToDate(LocalDateTime ldt){
  try{
 //     LOG.debug("entering DatetoLocalDate with Date = " + date);
      if(ldt == null){
          return null;
      }
        return java.util.Date
	      .from(ldt.atZone(ZoneId.systemDefault())
	      .toInstant());
   }catch(Exception e){
   String msg = "£££ Exception in LocalDatetoDate = " + e.getMessage();
    LOG.error(msg);
    return null;
  }
}

    public static LocalDate asLocalDate(java.util.Date date, ZoneId zone) {
        if (date == null)
            return null;
// new jdk 16
        if (date instanceof java.sql.Date dat1)
            return dat1.toLocalDate();
        else
            return Instant.ofEpochMilli(date.getTime()).atZone(zone).toLocalDate();
    }
    
    
  public static java.util.Date asUtilDate(Object date, ZoneId zone) {
        if (date == null)
            return null;
        if(date instanceof java.sql.Date || date instanceof java.sql.Timestamp)
            return new java.util.Date(((java.util.Date) date).getTime());
        if(date instanceof java.util.Date date1)
            return date1;
        if(date instanceof LocalDate localDate)
            return java.util.Date.from(localDate.atStartOfDay(zone).toInstant());
        if(date instanceof LocalDateTime localDateTime)
            return java.util.Date.from(localDateTime.atZone(zone).toInstant());
   //     if(date instanceof LocalDateTime)
   //         return java.util.Date.from(((LocalDateTime) date).atZone(zone).toInstant());
        
   //     if (date instanceof ZonedDateTime) // donne erreur enlevé 21-07-2019
//            return java.util.Date.from(((ChronoZonedDateTime<LocalDate>) date).toInstant());
   //     if (date instanceof Instant)
   //         return java.util.Date.from((Instant) date);
         if(date instanceof Instant instant)
            return java.util.Date.from(instant);
        
        throw new UnsupportedOperationException("Don't know hot to convert " + date.getClass().getName() + " to java.util.Date");
    }

public static java.sql.Date getSqlDate(java.util.Date date){
  return new java.sql.Date(date.getTime());
}
  //Then the conversion from java.util.Date to java.sql.Date is quite simple:
public static java.sql.Timestamp getSqlTimestamp(java.util.Date date){

    /*The biggest difference between java.sql.Date and java.sql.Timestamp
    is that the java.sql.Date only keeps the date, not the time,
    of the date it represents. So, for instance,
    if you create a java.sql.Date using the date and time 2009-12-24 23:20,
    then the time (23:20) would be cut off.
    If you use a java.sql.Timestamp then the time is kept.
    */
  return new java.sql.Timestamp(date.getTime());
}

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
public static String secondsToString(int time) {
    return String.format("%02d:%02d:%02d",
            time / 3600,
            (time % 3600) / 60,
            time % 60);
}
public static String secondsToString2(int time){
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

public static String formatSecsIntoHHMMSS(int secsIn){
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
return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(file.lastModified()); // fix multi-user 2026-03-07
} // end

public static Double[] doubleArrayToDoubleArray(double [] ddouble)
{ // https://stackoverflow.com/questions/1109988/how-do-i-convert-double-to-double
    if(ddouble.length != 4)    {
        LOG.debug("error using this function, array must 4 elem !");
        return null;
    }
   Double[] dDouble = new Double[] {0.0, 0.0, 0.0, 0.0};
 //   LOG.debug("Double Array = " + Arrays.deepToString(dDouble));
   for(int i=0; i < ddouble.length ; i++){
       double d = ddouble[i];
       Double dd = d;
 //      System.out.println("dObj dd = " + dd);
       dDouble[i] = dd;    
 //   System.out.println("dDouble[i] = " + dDouble[i]);
   }
//   LOG.debug("Double Array = " + Arrays.deepToString(dDouble));
   return dDouble;
}
// vezrsion généré chatgpt
public static Double[] toObjectArray(double[] input) {
    if (input == null || input.length != 4) {
        throw new IllegalArgumentException("Input array must contain exactly 4 elements.");
    }
    return Arrays.stream(input).boxed().toArray(Double[]::new);
}


  public static Double myDoubleRound(Double value, int decimalPlaces){
      try{
    if (decimalPlaces > 17)
        {return value;}
    //if (decimalPlaces < 1)
    //    {throw new UserException("decimalPlaces < 1");}
    if(value == 0){
        return Double.valueOf(0);}
    if (decimalPlaces == 0){ // fonctionne pas
          BigDecimal bd = new BigDecimal(value);
       //   bd = bd.setScale(decimalPlaces,BigDecimal.ROUND_UP);
          bd = bd.setScale(decimalPlaces,RoundingMode.UP);
          return bd.doubleValue();
        }
    final double r = (Math.round(value * Math.pow(10, decimalPlaces)))
                    / (Math.pow(10, decimalPlaces));
    return r;
 } catch (Exception e){ 
        LOG.debug("error in myDoubleRound");
        return 0.0;
    }
    
} // end function myRound


  public static int getArrayDimension(Object monTableau ){
        int dim=0;
        Class<?> cls = monTableau.getClass();
        while( cls.isArray() ){
            cls = cls.getComponentType();
            dim++;
        }
        return  dim ;
}
  public static int generatedKey (Connection conn) throws SQLException{
        try (Statement st = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID()")) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                LOG.error("error in Key returned from generatedKey()");
                return 0;
            }
        }
}

  public static String generateInsertQuery (Connection conn, String table) throws SQLException{
        // utilisé pour gestion des database, SQL requests
    int times = DBMeta.CountColumns(conn, table);
        //LOG.debug("times = " + times);
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO ")
        .append(table)
        .append(NEW_LINE)
        .append(" VALUES (");
    for(int i=0; i<times; i++){
        sb.append(TAB)
            .append("?,")
            .append(NEW_LINE); // = parameters placeholders, one par field
    }
    sb.deleteCharAt(sb.lastIndexOf(","))
            .append(");"); // delete dernière virgule
    //   LOG.debug("generated sb = " + sb);
    return sb.toString();
}
  
 public static void printArray2DInt(int[][] a){
   // int ard = getArrayDimension (a);
    LOG.debug("[ ");
    for (int row=0; row<a.length; row++) {
      if (row > 0) {
          System.out.print("  ");
          System.out.print("[");}
      for (int col=0; col<a[0].length; col++) {
        if (col > 0)
        {System.out.print(", ");
        System.out.format("%3d",a[row][col]);} // field-width = 3
      }
      LOG.debug("]");
    }
    LOG.debug("]");
  }
//}
/*

public static void printArray2DDouble (double [][] t )
{
    LOG.debug("printArray2D " + Arrays.deepToString(t));
      } // end function printArray2D


public static void printArray3DInt(int [][][] t)
{
    // start function
     LOG.debug("Start AfficheArray3D for = " + t.length + " elements");
     for (int i=0; i<t.length; i++)
     {     LOG.debug("****** start of Group " + (i+1) );
           for (int j=0; j<t[i].length; j++)
           {  LOG.debug("\n** start of Question " + (j+1) );
              for (int k=0; k<t[i][j].length; k++)
              {
                  System.out.print("\tfield = " + t[i][j][k] );
              } // end for k
              LOG.debug("\n** end of Question " + (j+1) ); //t[i][j][0]);
           } // end for j
           LOG.debug("****** end of Group " + (i+1) ); //t[i][0][0]);
     } // end for i
     LOG.debug("End of afficheArray3D");
     //LOG.debug("Array deep to string " + Arrays.deepToString(t));
} // end function afficheArray3D
//////////////// très important

/*public static File[] listFilesAsArray(File directory, FilenameFilter filter, boolean recurse)
 */


// startExecutionTimer/stopExecutionTimer/durationExecutionTimer/reset removed
// fix multi-user 2026-03-07 — static mutable fields, no callers anywhere in codebase

  public static void javaSpecs(){
   final java.util.Enumeration<?> liste = System.getProperties().propertyNames();
    String cle;
    while( liste.hasMoreElements()){
        cle = (String)liste.nextElement();
        LOG.debug( cle + " = " + System.getProperty(cle) );
    }

     SortedMap<String,Charset> charsets = Charset.availableCharsets();
        for(String nom : charsets.keySet()){
            LOG.debug("Charset "+nom);
            Charset charset = charsets.get(nom);
        for(String alias : charset.aliases()){
            System.out.print(" "+alias+",");
   }
   LOG.debug("");
}
  } // end method


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
        while (start != -1){
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
static public String dumpJavaProperties(){
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


public static <T> String[] concatArrays (String[] first, String []... rest){
  int totalLength = first.length;
  for (String[] array : rest){
    totalLength += array.length;
  }
  String[] result = Arrays.copyOf(first, totalLength);
  int offset = first.length;
  for (String[] array : rest){
    System.arraycopy(array, 0, result, offset, array.length);
    offset += array.length;
  }
  return result;
}

public static String getType(int type){
    String sType = "";
    sType = switch (type) {
         case DatabaseMetaData.procedureColumnUnknown -> "inconnu";
         case DatabaseMetaData.procedureColumnIn -> "IN";
         case DatabaseMetaData.procedureColumnInOut -> "INOUT";
         case DatabaseMetaData.procedureColumnOut -> "OUT";
         case DatabaseMetaData.procedureColumnReturn -> "valeur de retour";
         case DatabaseMetaData.procedureColumnResult -> "résultat de la requête";
         default -> "";
     }; // end switch
return sType;
}

public static long DiskSpace() {
 try { 
     long lc = java.nio.file.Files.getFileStore(Paths.get("c:/")).getUsableSpace();
  LOG.debug(" -- Usable Space (Giga )on C:\\ = " + lc);
///        double freeDiskSpace = FileSystemUtils.freeSpaceKb("C:");//calculate free disk space
///        double freeDiskSpaceGB = freeDiskSpace / 1024 / 1024; //convert the number into gigabyte
 ///       LOG.debug(" -- Free Disk Space (Giga )on C:\\ = " + freeDiskSpaceGB);
        File file = new File("C:");
           LOG.debug(" -- Free Disk Space (Bytes) on C:\\ = " + file.getFreeSpace() );
        return lc;
    } catch (IOException e)    { 
        e.printStackTrace();
        return 0;
    }
}

public static FacesContext getInstance() throws InstantiationException, FacesException{
try{
    FacesContext facesContext = FacesContext.getCurrentInstance();
    if (facesContext == null){
        LOG.error("getInstance()Object " + " cannot be created because facesContext is null");
        return null;
    }else{
        return facesContext;
    }
  }catch (Exception cv){
            String msg = "£££ Exception in getInstance = " + cv;
            LOG.error(msg);
      //      utils.LCUtil.showMessageFatal(msg);
            return null;
  }
}
// new 24-06-2024
public static String prepareMessageBean(String message, String subMessage){ 
try{
    return prepareMessageBean(message) + subMessage;

}catch (Exception ex){
            String msg = "£££ Exception in prepareMessageBean = " + ex.getMessage();
            LOG.error(msg);
      //      utils.LCUtil.showMessageFatal(msg);
            return null;
   } 
}


public static String prepareMessageBean_new(String message) {
    try {
        // files under WEB-INF/classes 
        // commencent par "messagesBean" par ex: messagesBean_fr.properties  fr = locale
        
        FacesContext context = null;
        try {
            context = FacesContext.getCurrentInstance();
        } catch (NoClassDefFoundError e) {
            // Appel depuis batch / run
            String msg = "Called from non-JSF execution with message = " + message;
            LOG.warn(msg);
            return msg;
        }

        Locale locale = resolveLocale();

        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("bundles.messagesBean", locale);
        } catch (MissingResourceException e) {
            LOG.warn("Resource bundle not found for locale " + locale + ", message=" + message, e);
            return "message not translated: " + message;
        }

        String translated;
        try {
            translated = bundle.getString(message);
            if (translated == null || translated.isBlank()) {
                translated = "???";
            }
        } catch (MissingResourceException e) {
            translated = "???";
        }

        return translated;

    } catch (Exception ex) {
        String msg = "Exception in prepareMessageBean: " + ex.getMessage();
        LOG.error(msg, ex);
        return null;
    }
}


public static String prepareMessageBean(String message){
try{
 //   LOG.debug("entering prepareMessageBean with message = " + message);
       //  https://stackoverflow.com/questions/136555 40/read-resource-bundle-properties-in-a-managed-bean
  // normally for a JSF session !
  // issue if called from a Run execution !! or batch execution ??
            FacesContext context; // fix multi-user 2026-03-07 — local var instead of static
            try{
                context = FacesContext.getCurrentInstance();
            }catch (java.lang.NoClassDefFoundError e){ // no JSF Session
               String msg = " YOU CALLED FROM A RUN execution with message = " + message;
               return msg;
            }
   Locale locale = resolveLocale();
       ResourceBundle text = ResourceBundle.getBundle("messagesBean", locale);
       if(text == null){
           return "message not translated" + message;
       }
       // files under WEB-INF/classes 
       // commencent par "messgesBean" par ex: messagesBean_fr.properties  fr = locale
 //      LOG.debug(" text language = " + text.getLocale().getLanguage());
       String someKey = text.getString(message);
       if(someKey.equals("")){
           someKey = "???";
       } 
  //   LOG.debug("bean internationalisation key found = " + someKey);
       return someKey;
}catch (Exception ex){
            String msg = "£££ Exception in prepareMessageBean = " + ex.getMessage();
            LOG.error(msg);
      //      utils.LCUtil.showMessageFatal(msg);
            return null;
   } 

} // end method
   //     }
        
public static String prepareMessageBean1(String message, Object[] arguments){ 
// essai used in createCompetitionDescription()
//https://murygin.wordpress.com/2010/04/23/parameter-substitution-in-resource-bundles/
    FacesContext context = null; // fix multi-user 2026-03-07 — local var instead of static
    try{
    try{
        context = getInstance();
    }catch (java.lang.NoClassDefFoundError e){
            String msg = " You Called from a RUN execution with message = " + message;
        //    LOG.error(msg);
            return null;
    }catch (Exception e){
            String msg = "£££ Exception = " + e;
            LOG.error(msg);
            return null;
    }
       ResourceBundle text = ResourceBundle.getBundle("/messagesBean", resolveLocale());
  // new 22-11-2020 parameter substitution in Message format
       LOG.debug(" with parameter text language = " + text.getLocale().getLanguage());
            LOG.debug(" the are arguments = " + arguments.length);
            LOG.debug("arguments = " + Arrays.toString(arguments)); //arguments[0].toString());
     //       LOG.debug("second argument = " + arguments[1].toString());
     // manipulation: MessageFormat ne connait que le format Date !!!
            for (int i = 0; i < arguments.length; i++) {
               if(arguments[i] instanceof LocalDateTime ldt){    //                LOG.info("ldt = " + i + arguments[i]);
                  arguments[i] = LocalDateTimeToDate(ldt);
                    LOG.info("arg " + i + " is now in Date format = " + arguments[i]);
               }
            } // end for
            for (int i = 0; i < arguments.length; i++) {
               if(arguments[i] instanceof Integer ldt){    //                LOG.info("ldt = " + i + arguments[i]);
                  arguments[i] = String.valueOf(ldt);
                    LOG.info("arg " + i + " is now in String format = " + arguments[i]);
               }
            }
            
         //   arguments
       String someKey = text.getString(message);
       if(someKey.equals("")){
           someKey = "???";
       } 
      return java.text.MessageFormat.format(someKey, arguments); // mod 22-11-2020
      /*StringBuilder sb = new StringBuilder();
Formatter formatter = new Formatter(sb, Locale.US);
int someNumber = 10;
String someString = "Hello";
formatter.format("Today is %tD and someNumber is %d %s", LocalDate.now(), someNumber, someString);
System.out.println(sb);
      */
 }catch (java.util.MissingResourceException mr){
            String lang = resolveLocale().getLanguage();
            String msg =  message +  " / Doesn't exists - for language = "
                    + lang + " / " + mr + " / " + arguments[0];
            LOG.debug(msg);
            utils.LCUtil.showMessageInfo(msg);
            return null;
  }catch (Exception cv){
            String msg = "£££ Exception in prepare MessageBean = " + cv;
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
            return null;
   }     
} // end method

public static void showMessageFatal(String message){
 try{
  //      LOG.debug("entering showMessage Fatal 1");
  if(message == null){ // c'est le cas pour PrepareMessageBean dans RUN execution
      LOG.debug("summary == null");
    //  return false;
  }
  FacesContext context; // fix multi-user 2026-03-07 — local var
  try{
     context = FacesContext.getCurrentInstance();
  }catch (java.lang.NoClassDefFoundError e){ // no JSF Session
      String msg = " YOU CALLED FROM A RUN execution with message = " + message;
       context = null;
       }

     if(context != null){ //JSF session,
         context.getExternalContext()
                .getFlash()
                .setKeepMessages(true);
         message = "<h2>" + message + "</h2>";
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_FATAL,message," (Application GolfLC)");
            context.addMessage(null, facesMsg);
       }
  }catch (Exception cv){
         String msg = "£££ Exception in showMessageFatal = " + cv;
           LOG.error(msg);
  //       return false;
        }     
} // end method
 
public static void showMessageFatal(String summary, String detail){
    try{
       FacesContext fc1 = FacesContext.getCurrentInstance();
       if(fc1 != null){ //JSF session, 
            fc1.getExternalContext().getFlash().setKeepMessages(true);
            summary = "<h3 style='text-align:left;'>" + summary + "</h3>";
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_FATAL,summary,detail);
            fc1.addMessage(null, facesMsg);
            showDialogFatal(facesMsg);
       }else{ //fc is null for BATCH sessions
           LOG.debug("messageFatal this is a batch execution " + summary);
       //    PrimeFaces pf = PrimeFaces.current();
            PrimeFaces.current().executeScript("{alert('Welcome user - showMessageFatal error!')}");
       }
  }catch (Exception cv){
            String msg = "£££ Exception in addMessageFatal = " + cv;
            LOG.error(msg);
 //           return false;
        }     
} // end method
public static boolean showMessageInfo(String message ){  // 1 input field
   // https://stackoverflow.com/questions/13685633/how-to-show-faces-message-in-the-redirected-page
    //FacesContext as an object is tied directly to the JSF request processing lifecycle
    //and as a result is only available during a standard JSF (user-driven) request-response process
    // donc pas disponible dans Batch Processing jsr-352 !!
    // ni dans RUN execution !
        
try{

   if(message == null){ // c'est le cas pour PrepareMessageBean dans RUN execution
      LOG.debug("summary == null");
      return false;
  }
     FacesContext context; // fix multi-user 2026-03-07 — local var
    try{
        context = getInstance();
    }catch (java.lang.NoClassDefFoundError e){
            String msg = " You Called from a RUN execution with message = " + message;
        //    LOG.debug(msg);
            return false;
     }catch (Exception e){
            String msg = "£££ Exception = " + e;
            LOG.error(msg);
      //      utils.LCUtil.showMessageFatal(msg);
            return false;
    }
    if(context != null){
        PrimeFaces.current().executeScript("window.scrollTo(0,0);"); // scroll top remonte la page après la fin de l’envoi. new 01-01-2026
        message = "<h2 style='text-align:left;'>" + message + "</h2>";  // mod 27-02-2022
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO,message," (Application GolfLC)");
        context.getExternalContext().getFlash().setKeepMessages(true); 
        context.addMessage(null, facesMsg); // new 25-05-2021 voir include/messages
            //https://stackoverflow.com/questions/15061019/primefaces-messages-not-displayed
           // https://github.com/primefaces/primefaces/blob/master/src/main/java/org/primefaces/context/RequestContext.java
    }else{   // fc is null for  BATCH sessions alert('Welcome user - omnifaces msg!'
       }
  return true;
}catch(Exception cv){
            String msg = "£££ Exception in showMessageInfo = " + cv;
            LOG.error(msg);
            return false;
        }
} //end method

public static void showMessageInfo(String summary, String detail){   // 2 inputs fieds
try{
       FacesContext fc = FacesContext.getCurrentInstance();
       if(fc != null){
            summary = "<h2>" + summary + "</h2>";
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO,summary,detail);
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, facesMsg);
       }else{   // fc is null for  BATCH sessions alert('Welcome user - omnifaces msg!'
 //          LOG.debug("MessageInfo this is a batch execution");
       }
}catch(Exception cv){
            String msg = "£££ Exception in showMessageInfo 2 input fields= " + cv;
            LOG.error(msg);
  }
} //end method
    public static void onIdleMonitor() {
        showMessageWarn("No activity.", "What are you doing over there?");
    }

    public static void onActiveMonitor() {
         showMessageWarn("Welcome Back", "Well, that's a long coffee break!");
    }

public static void showMessageWarn(String summary, String detail){   // 2 inputs fieds
try{
       FacesContext fc = FacesContext.getCurrentInstance();
       if(fc != null){
            summary = "<h2 style='text-align:left;'>" + summary + "</h2>";  // new 18-02-2019
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN,summary,detail);
            fc.getExternalContext().getFlash().setKeepMessages(true); // new 24/07/2017 forcer affichage messages si redirection e
            fc.addMessage(null, facesMsg);
       }else{   // fc is null for  BATCH sessions alert('Welcome user - omnifaces msg!'
 //          LOG.debug("MessageInfo this is a batch execution");
       }
}catch(Exception cv){
            String msg = "£££ Exception in showMessageWarn = " + cv;
            LOG.error(msg);
  }
} //end method

public static void showDialogInfo(String summary, String msg_in){
try{
       FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, msg_in);
       PrimeFaces.current().dialog().showMessageDynamic(msg, false); //  new 15-12-2021 escape - true to escape HTML content, false to display HTML content
}catch(Exception cv){
       String msg = "£££ Exception in showDialogInfo = " + cv;
            LOG.error(msg);
        }
} //end method

public static void showDialogFatal(String summary, String msg_in){
try{
       FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, summary,msg_in);
       PrimeFaces.current().dialog().showMessageDynamic(msg);
}catch(Exception cv){
       String msg = "£££ Exception in showDialogInfo = " + cv;
            LOG.error(msg);
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
try{
     String q = "select AuditStartDate, AuditEndDate,"
             + " TIMESTAMPDIFF (SECOND, AuditStartDate, AuditEndDate) as stamp from audit"
              + " where AuditPlayerId = ?";
              //+ " order by AuditStartDate desc limit 1 ";
         LOG.debug(" -- TIMESTAMPDIFF - query = " + q);

      ps.setString(1, in_player);      // Assign value to input parameter
              LOG.debug(" -- AuditPlayerId = " + in_player);
      rs = ps.executeQuery();
      int cpt = 0;
      int tot = 0;
      while (rs.next() )        // ne devrait en avoir qu'un !!!
        {   cpt ++;
            String st = rs.getString("AuditStartDate")+ " - "
                      + rs.getString("AuditEndDate")  + " = " + rs.getInt("stamp");
                //int tim = rs.getInt("stamp");
                tot+=rs.getInt("stamp");
                LOG.debug(" -- TIMESTAMPDIFF : " + cpt + " " + st);
        } // end while
      LOG.debug("Sessions    = " + cpt);
      LOG.debug("Total time  = " + tot);
      if (cpt>0)
      {         LOG.debug("Temps moyen = " + tot/cpt + " secondes");
            String t = LCUtil.formatSecsIntoHHMMSS(tot/cpt);
                LOG.debug("Time readable = " + t);
            t = LCUtil.secondsToString(tot/cpt);
                LOG.debug("Another Time readable = " + t);
      }else{
           LOG.debug("First connection for this user");
      }
}
catch(SQLException ex){
    LOG.error("-- TIMESTAMPDIFF exception ! " + ex.toString() + "/" );
    throw ex;
} // end catch
finally
{
    LOG.debug("-- passing in finally !!! ");
    rs.close();
    ps.close();
}
} //end method

public static int CountRows(Connection conn, String tableName) throws SQLException{
    // count the number of rows in the table
    Statement st = null;
    ResultSet rs = null;
try{
      utils.DBMeta.validateIdentifier(tableName); // security audit 2026-03-09
      st = conn.createStatement();
      rs = st.executeQuery("SELECT COUNT(*) as count FROM " + tableName);
      // ajouter un where sur le id du player par exemple ?
      rs.first();
      return rs.getInt("count");
}catch(SQLException e){
	LOG.error("SQLException in getCountRows : " + e);
        return 0;
}catch (Exception e){
	LOG.error("Exception in getCountRows : " + e);
        return 0;
}
finally{
    rs.close();
    st.close();
}

  } // end method CountRows

public static int getCountHoles(Connection conn, int tee) throws SQLException{
    // security audit 2026-03-09 — replaced Statement with PreparedStatement
    final String query = "SELECT count(*) AS cnt FROM hole WHERE hole.tee_idtee = ?";
    try (PreparedStatement ps = conn.prepareStatement(query)) {
        ps.setInt(1, tee);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("cnt");
            }
            return 0;
        }
    } catch (SQLException e) {
        LOG.error("SQLException in getCountHoles : " + e);
        return 0;
    }

  } // end method CountRows

public static int[][] initArrayPoints2(int[][] p ){
        for(int[] subarray : p){
            Arrays.fill(subarray, 0);
        }
return p;
}

 public static int[][] initArrayPoints(int[][] p ){
      for(int i=0;i<p.length;i++) {
          for (int j=0;j<6;j++){ // why <6 = strokes, extra, ...
              p[i][j] = 0;
          }
      } //end for
        return p;
} //end method

public static void deleteDir(String dir){
 File directory = new File(dir);
    LOG.debug(" -- directory to be cleaned = " + directory);

// Get all files in directory
File[] files = directory.listFiles();
for (File file : files){
    // Delete each file
    if (!file.delete())
        // Failed to delete file
        { long t = file.lastModified();
          LOG.debug(" -- Failed to delete = " +
                file + " last modified = " + t );
        }

} // end for
} // end method

private static final Map<String, String> DRIVER_MARKERS = Map.of(
    "ClientPreparedStatement",  "Connector/J",
    "WrappedPreparedStatement", "IronJacamar pooled"
);

public static void logps(PreparedStatement ps) {
    if (ps == null) {
        LOG.warn("logps : PreparedStatement est null");
        return;
    }
    try {
        // IronJacamar wraps the real driver PS.
        // unwrap(PreparedStatement.class) returns the Connector/J ClientPreparedStatement
        // whose toString() = "com.mysql.cj.jdbc.ClientPreparedStatement: SELECT ... actual values ..."
        PreparedStatement realPs = ps;
        try {
            if (ps.isWrapperFor(PreparedStatement.class)) {
                PreparedStatement unwrapped = ps.unwrap(PreparedStatement.class);
                if (unwrapped != ps) {
                    realPs = unwrapped;
                }
            }
        } catch (Exception ignored) {
            // unwrap not supported — use original
        }

        String psString    = realPs.toString();
        int    colonIndex  = psString.indexOf(": ");

        if (colonIndex >= 0) {
            // Connector/J format: "com.mysql.cj.jdbc.ClientPreparedStatement: SELECT ..."
            LOG.debug("SQL :{}{}", NEW_LINE, psString.substring(colonIndex + 2));
        } else {
            LOG.debug("SQL : {}", psString);
        }

    } catch (Exception e) {
        LOG.error("logps : erreur inattendue sur [{}] : {}", ps.getClass().getSimpleName(), e.getMessage(), e);
    }
}



/*
public static void logps(PreparedStatement ps){
try{
 ///       LOG.debug("entering logps");
 // from ironjacamar
        //avec connection pool p = org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8@10b61e60
//        org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8.
        //connection classique p = com.mysql.cj.jdbc.ClientPreparedStatement: SELECT idplayer, PlayerFirstName, PlayerLastName, PlayerCity, Play
        
  //  LOG.debug("fetchsize " + ps.getFetchSize());  // donne result = 0 ??    
    String p = ps.toString();
 //   LOG.debug("p toString = " + p);
 //   LOG.debug("ps.getClass = " + ps.getClass()); // class org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8 

    if(p.contains("ClientPreparedStatement")){ // connector-j 8xxx
        LOG.debug("Prepared Statement after bind variables set = "
                + NEW_LINE.repeat(2)
                + p.substring(p.indexOf(":")+2 , p.length() ));
    }else{
      //   LOG.debug("pooled connection for ");
    }
   }catch (Exception e){
        LOG.error("logps Exception " + e);
      }
}
*/
public static void logRs(ResultSet rs){
try{
        LOG.debug("entering logRs");     
        //avec connection pool p = org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8@10b61e60
//        org.jboss.jca.adapters.jdbc.jdk8.WrappedPreparedStatementJDK8.
        //connection classique p = com.mysql.cj.jdbc.ClientPreparedStatement: SELECT idplayer, PlayerFirstName, PlayerLastName, PlayerCity, Play
        
    LOG.debug("fetchsize " + rs.getFetchSize());  // donne result = 0 ??    
 //  String p = rs.toString();
    ResultSetMetaData resultSetMetaData = rs.getMetaData();
   // resultSetMetaData.getTableName(4);
    String tableName = resultSetMetaData.getTableName(4);
     LOG.debug("Name of the table : "+ tableName);
    
    
 /*//   LOG.debug("p toString = " + p);
    if(p.contains("WrappedPreparedStatement")){ // connector-j 8xxx

    }else{
        LOG.debug("Prepared Statement after bind variables set = "
                + NEW_LINE.repeat(2)
                + p.substring(p.indexOf(":")+2 , p.length() ));
    }
     */
   }catch (Exception e){
        LOG.error("logps Exception " + e);
      }
}

public static void logMap(Map<String, Object> map){
  try{
        LOG.debug("entering logMap");     

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
            LOG.debug("SessionMap Key : " + key  +" // " + "SessionMap Value : " + value);
            });
    /*   
       map.entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .filter(!getKey().equals("facelets.ui.DebugOutput"))  // avoid printing .xhtml files   
            .forEach(e -> LOG.debug("SessionMap one shot " + e));
       */
   }catch (Exception e){
        LOG.error("logps Exception " + e);
      }
}

public static void LCstartup() throws Exception{ //throws SQLException //throws SQLException
try{   
        LOG.debug("we are starting LCstartup !!");
    ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
 //       LOG.debug("Startup with ServletContext = " + sc);
        LOG.debug("server Info = " + sc.getServerInfo());
    }catch (Exception e){
        LOG.error("There is maybe no database available ??? " + e);
      }
    
   // DBMeta.listMetaData(conn);
   // DBMeta.listMetaData(PostStartupBean.getConn());
 ////  DBConnection.getPooledConnection(); enlevé 01/121/2019
   ListAllSystemProperties();
} //end method


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
    /// version 2
    try {
    Enumeration<URL> resources = Thread.currentThread()
        .getContextClassLoader()
        .getResources("META-INF/MANIFEST.MF");
    
    while (resources.hasMoreElements()) {
        Manifest manifest2 = new Manifest(resources.nextElement().openStream());
        Attributes attrs = manifest2.getMainAttributes();
        String specVersion = attrs.getValue("Specification-Version");
        String implVersion = attrs.getValue("Implementation-Version");
      //  String name = attrs.entrySet();
        
        if (specVersion != null) {
            LOG.debug("Spec Version: " + specVersion + "Impl Version: " + implVersion);
        }
    }
} catch (IOException e) {
    e.printStackTrace();
}
    
    
    
    
   } catch (Exception ex) {
       LOG.debug("error printattributes" + ex);
   }
 } //end method
public static String firstPartUrl(){
try{
  FacesContext context; // fix multi-user 2026-03-07 — local var
  try{
     context = FacesContext.getCurrentInstance();
  }catch (java.lang.NoClassDefFoundError e){ // no JSF Session
      String msg = " YOU CALLED FROM A RUN execution, hard coded returned !";
      LOG.debug(msg);
      return "http://localhost:8080/GolfWfly-1.0-SNAPSHOT";
  }
       ExternalContext ec = context.getExternalContext();
     //       LOG.debug("** ExternalContext = " + ec.toString());   
        String host = ec.getRequestServerName();
   //       LOG.debug("** application host = " + host);   
        int port = ec.getRequestServerPort();
  //       LOG.debug("** port = " + port);   
        String uri = ec.getRequestContextPath();
         // Return the portion of the request URI that identifies the web application context for this request.     
        return "http://" + host + ":" + port + uri;      // exemple http://localhost:8080/GolfWfly-1.0-SNAPSHOT/login.xhtml
    //    return href;
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
        LOG.debug("Uncompress.Input String length : " + str.length());
        //final InputStream is = new GZIPInputStream( new FileInputStream( file ), 65536 
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str.getBytes("ISO-8859-1")),512);
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "ISO-8859-1"));
        String outStr = "";
        String line;
        while ((line=bf.readLine())!=null) {
          outStr += line;
        }
        LOG.debug("Uncompress.Output String length : " + outStr.length());
        LOG.debug("Uncompress.Output String  : " + outStr);
        return outStr;
     } // end method decompress
  
 // http://www.softraction.com/2011/10/compressing-and-decompressing-string-in.html
  public static String compress(String str)  throws IOException {
        if (str == null || str.length() == 0)
            {  return str;  }
        long start = System.nanoTime();
            LOG.debug("Compress.Input String length : " + str.length()); // 336
        ByteArrayOutputStream out = new ByteArrayOutputStream();
     try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
           gzip.write(str.getBytes());
       }
        String outStr = out.toString("ISO-8859-1");
        LOG.debug("Compress.Output String length : " + outStr.length()); //72
 //       System.out.println("Compress.String compressed : " + outStr);
        LOG.debug("Compress.ratio : " + outStr.length() / (str.length()/100) + " % ");
        LOG.debug("Compress.time : " + Long.toString(System.nanoTime() - start) );
    LOG.debug("ending compress " );
        return outStr;
 } // end method compress
  
  public static String array2DToString(String [][] input){
  try{
      return Arrays.deepToString(input);
  //        LOG.debug("array2DToString.input = " + s);
   //    return s;
  }catch (Exception e){
     String msg = "array2DToString.Exception in string2D = " + e ;
        LOG.debug("error = " + msg );
     return null;}
}  //end method 
  
  public static String[][] array2DoubleToString(Double[][] boardD){
  try{
//         LOG.debug("array2DToString.input = " + Arrays.deepToString(boardD));
      int n = boardD.length;
      int m = boardD[0].length;
  //        LOG.debug("length n = " + n);
 //         LOG.debug("length m = " + m);
      String[][] boardS = new String[n][m];
      IntStream.range(0, m).forEach(i->{  // Rows (n)
        IntStream.range(0, n).forEach(j->{
            boardS[j][i] = String.valueOf(boardD[j][i]);
            });
      });
 //   LOG.debug("boardS String printed = " + Arrays.deepToString(boardS));
       return boardS;
 
  }catch (Exception e){
     String msg = "array2DToString.Exception in string2D = " + e ;
        LOG.debug("error = " + msg );
     return null;}
}  //end method 
  
  
public static int[] extractFrom2D(int[][] orig, int pos) {
  try{
    int[] ret = new int[orig.length];
    for(int i = 0; i < ret.length; i++) {
        ret[i] = orig[i][pos];
    }
    return ret;
  }catch (Exception ex){
            String msg = "Exception !  in extractFrom2D = " + ex.getMessage();
            LOG.debug(msg);
            return null;
  
    } //end method 
}
 public static String[][] ModifyMembersBase(String[][] base) {
    try{
      String[][] copy = Arrays.stream(base).map(String[]::clone).toArray(String[][]::new);
 //       LOG.debug("array base copy = " + Arrays.deepToString(copy));
      int n = base.length;
      int m = base[0].length;
 //       LOG.debug("length n = " + n);
 //       LOG.debug("length m = " + m);
          for (int i = 0; i < n; i++) {
 //             LOG.debug("i =" + i);
              String[] item = base[i];
 //             LOG.debug("dst = " + Arrays.toString(dst));
 //             LOG.debug("elem1 = " + dst[1]); // amount
              String age = item[2];
              age = age.replace("00-00"," ");
 //             LOG.debug("age = " + age);
              Double price = Double.valueOf(item[1]);
        /* bug dans programme : on force US et puis on corrige ...
              Locale locale = new Locale("en", "US");
           NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
           nf.setMaximumFractionDigits(1);
   //        nf.setGroupingUsed(true);
           String amountFormatted = nf.format(price);
   //         LOG.debug("result = " + result); 
            amountFormatted = amountFormatted.replace(".", ";")
                    .replace(",",".")
                    .replace(";",",")
                    .replace("$","€ ");// bug system
 //           LOG.debug("bug correction = " + amountFormatted);
  */     String amountFormatted = priceFormatted(price, 1);
      // copie des éléments modifiés
            copy[i][1] = amountFormatted;
            copy[i][2] = age;
          }
  return copy;
//LOG.debug("array base modified = " + Arrays.deepToString(copy));
  }catch (Exception ex){
            String msg = "Exception !  in amountFormatted= " + ex.getMessage();
            LOG.debug(msg);
            return null;
        }
    } //end method 
  
  
  
 public static int[][] array2DStringToInt(String[][] boardD){
  try{
//         LOG.debug("array2DToString.input = " + Arrays.deepToString(boardD));
      int n = boardD.length;
      int m = boardD[0].length;
  //        LOG.debug("length n = " + n);
 //         LOG.debug("length m = " + m);
      int[][] boardS = new int[n][m];
      IntStream.range(0, m).forEach(i->{  // Rows (n)
        IntStream.range(0, n).forEach(j->{
            boardS[j][i] = Integer.parseInt(boardD[j][i]);
            });
      });
 //   LOG.debug("boardS String printed = " + Arrays.deepToString(boardS));
       return boardS;
 
  }catch (Exception e){
     String msg = "array2DToString.Exception in string2D = " + e ;
        LOG.debug("error = " + msg );
     return null;}
}  //end method 
  
  
 public static int[][] baseModified(String[][] boardD){
  try{
//         LOG.debug("array2DToString.input = " + Arrays.deepToString(boardD));
      int n = boardD.length;
      int m = boardD[0].length;
  //        LOG.debug("length n = " + n);
 //         LOG.debug("length m = " + m);
      int[][] boardS = new int[n][m];
      IntStream.range(0, m).forEach(i->{  // Rows (n)
        IntStream.range(0, n).forEach(j->{
            boardS[j][i] = Integer.parseInt(boardD[j][i]);
            });
      });
 //   LOG.debug("boardS String printed = " + Arrays.deepToString(boardS));
       return boardS;
 
  }catch (Exception e){
     String msg = "array2DToString.Exception in string2D = " + e ;
        LOG.debug("error = " + msg );
     return null;}
}  //end method 
  
 
public static int[] stringArrayToIntArray(String[] input){
try{
 //       LOG.debug("stringArrayToIntArray input String = " + Arrays.toString(input));
     int[] integers = new int[input.length];
// Creates the integer array.
     for (int i=0; i<input.length; i++){
        integers[i] = Integer.parseInt(input[i]); 
     //Parses the integer for each string.
     }
 //     LOG.debug("stringArrayToIntArray output int =  " + Arrays.toString(integers));
   return integers;
}catch (Exception e){
    String msg = "Exception in stringArrayToIntArray = " + e ;
        LOG.debug(msg );
    return null;}
}  //end rmethod

  public static int[] stringArrayToIntArray2(String[] input) {
    if (input == null) {
        throw new IllegalArgumentException("Input array cannot be null.");
    }

    return Arrays.stream(input)
                 .mapToInt(Integer::parseInt)
                 .toArray();
}

  public static String[] stringToArray1D(String input){
  try{      // one delimiter = ","
          LOG.debug("stringToArray1D. input = " + input);
        input = input.replace("[", "").replace("]", "").replace(", ", ",");
            LOG.debug("str stripped = " + input);
        String[] matrix = input.split(",");
            LOG.debug("array = " + Arrays.toString(matrix));
  //      System.out.println("stringToArray2D.matrix = " + Arrays.deepToString(matrix));
    return matrix;
  }catch (Exception e){
    String msg = "stringToArray1D.Exception in string1D = " + e ;
        LOG.debug("error = " + msg );
        return null;}
}  //end method
    
  
  
  public static String[][] stringToArray2D(String input){
  try{      // two delimiters = ";" et ensuite ","
          LOG.debug("stringToArray2D. input = " + input);
    String lines[] = input.split(";");
        LOG.debug("lines = " + Arrays.deepToString(lines));
    int width = lines.length;
        LOG.debug("width = " + width);
    String[][] matrix = new String[width][]; 
 // Enhanced For-Loop 
    int r = 0;
    for (String row : lines) {
        matrix[r++] = row.split(",") ;
       // System.out.println(" * matrix = " + (r) + " = " + matrix[r]);
    }
        LOG.debug("stringToArray2D.matrix = " + Arrays.deepToString(matrix));
    return matrix;
  }catch (Exception e){
    String msg = "stringToArray2D.Exception in string2D = " + e ;
        LOG.error("error = " + msg );
        return null;}
}  //end method 

  public static void printCurrentPhaseID(){
      PhaseId currentPhaseId = FacesContext.getCurrentInstance().getCurrentPhaseId();
        LOG.debug("currentPhaseId = " + currentPhaseId);
  }
  
  
public static String[] removeNull1D( String[] arr1d) {
 // LOG.debug("removeNull1D input = " + Arrays.deepToString(arr1d));
ArrayList<String> items = new ArrayList<>(arr1d.length);
for(String input : arr1d) {
   if(input != null) {
      items.add(input);
   }
} // end for
 // return items.toArray(new String[items.size()]); // mod 23-09-2024
  return items.toArray(String[]::new);
  }

public static Boolean[] removeNull1DBoolean( Boolean[] arr1d) {
 // LOG.debug("removeNull1D input = " + Arrays.deepToString(arr1d));
ArrayList<Boolean> items = new ArrayList<>(arr1d.length);
for(Boolean input : arr1d) {
   if(input != null) {
      items.add(input);
   }
} // end for
  return items.toArray(new Boolean[items.size()]);
  }




  public static String[][] removeNull2D(String[][] arr2d) {
   // https://stackoverflow.com/questions/32099750/delete-null-element-in-2d-array-in-java?lq=1
 //        LOG.debug("removeNull2D input = " + Arrays.deepToString(arr2d));
        ArrayList<ArrayList<String>> list2d = new ArrayList<>();
        for(String[] arr1d: arr2d){
            ArrayList<String> list1d = new ArrayList<>();
            for(String s: arr1d){
                if(s != null && s.length() > 0) {   // isEmpty() ?
                    list1d.add(s);
                }else{
      //              LOG.debug("s = " + s); // new 18/02/2019
                }
            }
            // you will possibly not want empty arrays in your 2d array so I removed them
            if(!list1d.isEmpty()){
                list2d.add(list1d);
            }else{
      //          LOG.debug("list1d.size = 0");
            }
        }
        String[][] cleanArr = new String[list2d.size()][];
        int next = 0;
        for(ArrayList<String> list1d: list2d){
            cleanArr[next++] = list1d.toArray(new String[list1d.size()]);
        }
//         LOG.debug("removeNull2D output = " + Arrays.deepToString(cleanArr));
     return cleanArr;
  }  //end removeNull2D
  
  public static LocalDate EasterSundayDate(int year){  // oudin's algorithm
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
        LOG.debug("In the year " + year + " Easter with fall on day " + day + " of month " + month + " / " + ld);
      //  LOG.debug("p = " + p);
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
  public static void printMap(Map<String, Object> map){
 LOG.debug("entering printMap ...");
  //    Stream<String, Object> stream = map.values().stream();
   //     stream.forEach((value) -> {LOG.debug(value);
   //   });
  
      map.forEach((key, value) -> LOG.debug(key + ":" + value));
 // } // end method
   ////  for (Map.Entry entry: objectSet.entrySet()){
   //    System.out.println("key: " + entry.getKey() + "; value: " + entry.getValue());
   //   }
   for(Map.Entry<String, Object> entry : map.entrySet()){
    LOG.debug("key = " + entry.getKey());
    LOG.debug("value = " + entry.getValue());
}
  }
  
    public static void printProperties(String settings) throws IOException {
    //   ClassLoader clo = Thread.currentThread().getContextClassLoader();
       LOG.debug("Printing Properties = " + settings);
      try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(settings)) { // close included
          Properties properties = new Properties();
          properties.load(inputStream);
          Enumeration<Object> keys = properties.keys();
          while (keys.hasMoreElements()) {
              String key = (String)keys.nextElement();
              String value = (String)properties.get(key);
              LOG.debug(settings + " = " + key + ": " + value);
          }
      }
  } // end method


    public static String findProperties(String cat, String subcat) throws IOException {
try{
    LOG.debug("cat : " + cat);
    LOG.debug("subcat : " + subcat);
    ClassLoader clo = Thread.currentThread().getContextClassLoader(); // new 25-11-2018
     // Netbeans Files en haut à gauche /src/main/resources
     //InputStream is = clo.getResourceAsStream("subscription.properties");
       InputStream is = clo.getResourceAsStream(cat + ".properties");
       Properties p = new Properties();
       p.load(is);
       String r = p.getProperty(cat + "." + subcat);
    //     price = p.getProperty("subscription.month");
        //     price = p.getProperty("subscription.month"); //subscripton.month
       LOG.debug("Property cat + e = " + r);
    return r;
    
  }catch (Exception e){
    String msg = "error findProperties = " + e ;
    LOG.error(msg);
    return null;
        }
}
    
public static String[] intArrayToStringArray(int[] int01){
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

/*
public static String fillRoundPlayersStringEcl(java.util.List<ECourseList> players) {
    ArrayList<Player> p = new ArrayList<>();   // transform player2 in list<player<    
    for(int i=0; i < players.size() ; i++){
//    LOG.debug("elem = " + players.get(i).Eplayer.getPlayerLastName());
       p.add(players.get(i).getPlayer());
     //       LOG.debug(" -- item in for idplayer # = " + dlPlayers.getTarget().get(i).getIdplayer() );
    }   
    return fillRoundPlayersString(p); // next method
}
*/


/*
public static String fillRoundPlayersString(List<Player> players) { // from
 if(players.isEmpty()){  // was size == 0
      LOG.debug(" exiting fillRoundPlayersString with no player");
     return "";
 }
     StringBuilder sb = new StringBuilder();
     for(int i=0; i < players.size() ; i++){
        sb.append(players.get(i).getPlayerLastName()).append(" (");
        sb.append(players.get(i).getIdplayer()).append("), ");
     } // end for 
      sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
 //    LOG.debug(" exiting fillRoundPlayersString with = " + sb.toString());
 return sb.toString();
}
*/
  
public static class MapToArrayExample {
    public String[] mapValuesToArray(Map <Integer,String> sourceMap) {
       Collection <String> values = sourceMap.values();
       String[] targetArray = values.toArray(new String[values.size()]);
       return targetArray;
    }
    
    }

public static String extractHHmm (String sunris) {
  try{
    LOG.debug("input string = " + sunris);
      int egal1 = sunris.indexOf("=")+1; //, sunrise.indexOf(",") + 1);  // cherche 2e virgule
        LOG.debug("egal 1 = " + egal1);
      int virgule1 = sunris.indexOf(",");
        LOG.debug("virgule1 = " + virgule1);
      String hours = sunris.substring(egal1, virgule1);
      LOG.debug("hours = " + hours);
      
      int egal2 = sunris.indexOf("=", sunris.indexOf("=") + 1);  // cherche 2e virgule
        LOG.debug("egal 2 = " + egal2);
      int virgule2 = sunris.indexOf(",", sunris.indexOf(",") + 1);  // cherche 2e virgule     
        LOG.debug("virgule2 = " + virgule2);
      String minutes = sunris.substring(egal2+1, virgule2);   
       LOG.debug("minutes = " + minutes);
       
      int egal3 = sunris.lastIndexOf("="); //, sunris.indexOf("=") + 1);  // cherche 2e virgule
        LOG.debug("egal 3 = " + egal3);
       
      String ampm = sunris.substring(egal3+1, egal3+2+1);
          LOG.debug("AMPM = " + ampm);
      if(ampm.equals("PM")){
            LOG.debug("hours = PM ");
          int h = Integer.parseInt(hours) + 12;
          hours = String.valueOf(h);
          LOG.debug("PM hours corrected = " + hours);
      }else{
           LOG.debug("hours = AM ");
      }
      LOG.debug("sunrise = " + hours +"." + minutes);
    return(hours + "." + minutes);
 //   }
  }catch (Exception e){
    String msg = "error findProperties = " + e ;
    LOG.error(msg);
    return null;
        }
} // end method

 public static int calculateAgeFirstJanuary(LocalDateTime birthDate){
 try{
//           LOG.debug("entering calculateAgeFirstJanuary" );
 //          LOG.debug("entering calculateAgeFirstJanuary with birthdate = " + birthDate);
     if(birthDate != null) {
        LocalDate localDateBirth = birthDate.toLocalDate();
        LocalDate firstDayYear = Year.of(LocalDate.now().getYear()).atMonth(Month.JANUARY).atDay(1);
        return localDateBirth.until(firstDayYear).getYears();
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
  public static int[][] cloneArray2D(int[][] src) {  // shallow copy
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
 //   String[] copiedArray = Arrays.stream(strArray).toArray(String[]::new);
 //we need to do a deep copy of an array of non-primitive types. 
 // Employee[] copiedArray = SerializationUtils.clone(employees);
    for (int i = 0; i < src.length; i++) {
        dst[i] = Arrays.copyOf(src[i], src[i].length);
    }
    return dst;
}
 /* 
public static String[][] concatenateTwoStringArrays(String[][]a, String[][] b) { 
  // Function to merge two arrays of same type 
  //https://www.geeksforgeeks.org/merge-arrays-into-a-new-object-array-in-java/amp/
        return Stream.concat(Arrays.stream(a), Arrays.stream(b))
                     .toArray(String[][]::new); 
    } 
  */

  // https://stackoverflow.com/questions/17185880/how-to-concatenate-two-dimensional-arrays-of-string-in-java
public static String[][] concatenateTwoStringArrays(String[][] a, String[][] b) {
       LOG.debug("arrays a = " + Arrays.deepToString(b));
       LOG.debug("arrays b = " + Arrays.deepToString(b));
    String[][] result = new String[a.length + b.length][];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
       LOG.debug("Utils arrays concatenated = " +  Arrays.deepToString(result));
    return result;
}


 public void main(String[] args) throws Exception {
  try{
  //    LCUtil lcu  = ; //;
///      utils.LCUtil.join2arrays();//LCUtil().join2arrays();
      //      LOG.debug("from main, dateYear =" +  p);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         
   }
   } // end main//
    
public static void printSQLException(SQLException ex) {
// new 13-05-2019 see : https://docs.oracle.com/javase/tutorial/jdbc/basics/sqlexception.html
    for (Throwable e : ex) {
        if (e instanceof SQLException) {
            if (ignoreSQLException(
                ((SQLException)e).
                getSQLState()) == false) {

                e.printStackTrace(System.err);
                LOG.error("SQLState: " +
                    ((SQLException)e).getSQLState());

                LOG.error("Error Code: " +
                    ((SQLException)e).getErrorCode());

                LOG.error("Message: " + e.getMessage());

                Throwable t = ex.getCause();
                while(t != null) {
                    LOG.error("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}
 
 
 
 public static boolean ignoreSQLException(String sqlState) {
    if (sqlState == null) {
        LOG.debug("The SQL state is not defined!");
        return false;
    }

    // X0Y32: Jar file already exists in schema
    if (sqlState.equalsIgnoreCase("X0Y32"))
        return true;

    // 42Y55: Table already exists in schema
    if (sqlState.equalsIgnoreCase("42Y55"))
        return true;
    return false;
} // end method
 public static void getWarningsFromResultSet(ResultSet rs) throws SQLException {
     printWarnings(rs.getWarnings());
}

public static void getWarningsFromStatement(Statement stmt)throws SQLException {
    printWarnings(stmt.getWarnings());
}

public static void printWarnings(SQLWarning warning) throws SQLException {
    if (warning != null) {
        LOG.debug("\n---Warning---\n");

    while (warning != null) {
        LOG.debug("Message: " + warning.getMessage());
        LOG.debug("SQLState: " + warning.getSQLState());
        LOG.debug("Vendor error code: ");
        LOG.debug(warning.getErrorCode());
        LOG.debug("");
        warning = warning.getNextWarning();
    }
}
} // end method
// utilisé pour extraire les arrays Scorestableford de arrayGlobal
 public static int[] cloneIntFromArray2D(int[][]src, int x) {
   try{
       if(src == null){
           return null;
       };
      int[] dst = new int[src.length];
      for (int i = 0; i < src.length; i++) {
         dst[i] = src[i][x];
  //       LOG.debug("dst = " + dst[i]);
      }
 //     LOG.debug("destination = " + Arrays.toString(dst));
    return dst;
  }catch (Exception e){
        String msg = "Exception in cloneIntFromArray2D = " + e ;
        LOG.debug(msg );
        return null;
  }
    
    
 } // end method 
 public static boolean isNegative(BigDecimal b){
     return b.signum() == -1;
    }
    
 public static double roundDouble(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();
    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
}
 
 
     /*
  private static Hashtable<String, String> getManifestAttributes(Manifest manifest) {
   Hashtable<String, String> h = new Hashtable<String, String>();
   try {
     Attributes attrs = manifest.getMainAttributes();
     Iterator it = attrs.keySet().iterator();
     while (it.hasNext()) {
       String key = it.next().toString();
       h.put(key, attrs.getValue(key));
     }
   } catch (Exception ignore) {
   }
   return h;
 }
    */
 // 27-08-2020 more efficient !! 
 //https://www.logicbig.com/how-to/code-snippets/jcode-java-stack-walking-get-current-method.html
 //public static String getCurrentMethodName(String classe) {
 //   StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
 //   StackWalker.StackFrame frame = 
 //           stackWalker.walk(stream1 -> stream1.skip(1)
  //                                     .findFirst()
  //                                     .orElse(null));
  //  return frame == null ? "caller: null" : classe + "." + frame.getMethodName() + " - ";
  //  }
 
     // ✅ Instance statique réutilisable (thread-safe)
    private static final StackWalker STACK_WALKER = 
        StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    
    /**
     * Récupère le nom de la méthode appelante (classe.méthode)
     * Optimisé avec StackWalker réutilisable
     */
    public static String getCurrentMethodName() {
        return STACK_WALKER.walk(frames -> 
            frames.skip(1)  // Skip getCurrentMethodName()
                  .findFirst()
                  .map(frame -> frame.getClassName() + "." + frame.getMethodName())
                  .orElse("unknown.method")
        );
    }
 
     /**
     * Version avec classe fournie (pour compatibilité)
     * @deprecated Utilisez getCurrentMethodName() sans paramètre
     */
    @Deprecated
    public static String getCurrentMethodName(String classe) {
        return STACK_WALKER.walk(frames -> 
            frames.skip(1)
                  .findFirst()
                  .map(frame -> classe + "." + frame.getMethodName())
                  .orElse(classe + ".unknown")
        );
    }
 /**
     * Récupère nom complet avec infos de ligne (debug)
     */
    public static String getCurrentMethodNameDetailed() {
        return STACK_WALKER.walk(frames -> 
            frames.skip(1)
                  .findFirst()
                  .map(frame -> String.format("%s.%s():%d",
                      frame.getClassName(),
                      frame.getMethodName(),
                      frame.getLineNumber()
                  ))
                  .orElse("unknown")
        );
    }
 
 public static String getCurrentClassName() {
   return StackWalker.getInstance(java.lang.StackWalker.
           Option.RETAIN_CLASS_REFERENCE)
           .getCallerClass()
           .getName();
 }
   public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	} 

public static String priceFormatted (Double price, int i) {
    Locale local = resolveLocale();
      LOG.debug("locale for format = " + local);
 //       LOG.debug("double d = " + price);
    if(local.getLanguage().equals("fr")){  // bug java 16 !!  fr ne fonctionne pas !! donne des ? pour séparateur thousands
       local = Locale.of("nl","BE");
   }
    NumberFormat cf = NumberFormat.getCurrencyInstance(local);
    cf.setMaximumFractionDigits(i);
       LOG.debug("version 5 with 1 digit " + cf.format(price));
    return cf.format(price);
}

 public static  List<String> capitalizeAllWords(List<String> words) {
   return words.stream()
     //  .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
       .map(word -> StringUtils.capitalize(word))    
       .collect(Collectors.toList());
}
   
public static List<Integer> getNumbersUsingIntStreamRangeClosed(int start, int end) {
    return IntStream.rangeClosed(start, end)
      .boxed()
      .collect(Collectors.toList());
}
public static boolean isArrayAllZeroes(int[] arr){
 //   boolean b = false;
    for(int i=0;i<arr.length;i++){
      if(arr[i] > 0){
          return false;
      }
    }
      return true;
} // end method 
/*
public static boolean isArrayOneZero(int[] arr){
    for(int i=0;i<arr.length;i++){
 //       LOG.debug("array one zero ? " + arr[i] );
 //       LOG.debug("array one zero index =  " + i );
      if(arr[i] == 0){
          return true;
      }
    }
 return false;
} // end
*/
public static boolean containsZero(int[] arr) {
    if (arr == null || arr.length == 0) {
        return false;
    }
    for (int value : arr) {
        if (value == 0) {
            return true;
        }
    }
    return false;
}




//Convert int[] to Integer[]
public static Integer[] intToInteger(int[] intArray) {
	Integer[] result = new Integer[intArray.length];
	for (int i = 0; i < intArray.length; i++) {
            result[i] = intArray[i];
	}
	return result;
}

//2. Convert Integer[] to int[]
	public static int[] toPrimitive(Integer[] IntegerArray) {
		int[] result = new int[IntegerArray.length];
		for (int i = 0; i < IntegerArray.length; i++) {
                    result[i] = IntegerArray[i];
		}
		return result;
	}


//user defined function that finds the sslice of an specified array   
public static int[] findSlice(int[] array, int startIndex, int endIndex) { 
//getting the slice of an array and storing it in array slcarray[]  
//the range() method converts the elements into stream  
//getting the elements of the int stream using lambda expression  
//converting the mapped elements into sliced array using the toArray() method   
int[] slcarray = IntStream.range(startIndex, endIndex)
        .map(i -> array[i]).
        toArray();
//returns the slice of array  
return slcarray;   
}   
//https://www.baeldung.com/java-filename-without-extension
public static String removeFileExtension(String filename, boolean removeAllExtensions) {
    if (filename == null || filename.isEmpty()) {
        return filename;
    }
    String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
    return filename.replaceAll(extPattern, "");
}
// new 02-12-2025 used in entite Creditcard
public static String mask(String input, int visible) {
  if (input == null || input.length() <= visible) return input;
  int maskLength = input.length() - visible;
  return "*".repeat(maskLength) + input.substring(maskLength);
}
// -------------------------
    // CONTEXT-PARAMS (web.xml)
    // -------------------------
    public static Map<String, String> getContextParams() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> params = new TreeMap<>();
        for (String name : ec.getInitParameterMap().keySet()) {
            params.put(name, ec.getInitParameter(name));
        }
        return params;
    }

    // -------------------------
    // CLASSPATH ANALYSIS
    // -------------------------
    public static List<String> getClassPath() {
        return System.getProperty("java.class.path")
                .lines().sorted()
                .collect(Collectors.toList());
    }

public void listAllProperties() {
    System.getProperties().forEach((key, value) -> {
        if (key.toString().contains("jboss") || key.toString().contains("wildfly")) {
            System.out.println(key + " = " + value);
        }
    });
}

}// end Class LCUtil

/*
   public static String readString(){ // contains TypePayment for creditcard
    try{
        Path path = Path.of("C:/log/savetofile.txt");;
        String strR = Files.readString(path);
           LOG.debug("String readed = " + strR);
        return strR;
        }catch (IOException ex) {
            LOG.error("Invalid Path in readString() ");
            return null;
        }
    } //end method read
 
 public static void writeString(String in){
        Path path = Paths.get("C:/log/savetofile.txt"); // // contains TypePayment for creditcard
         try {
            Files.writeString(path, in, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING); // efface contenu précédent
               LOG.debug("String written to file = " + path + " = " + in);
        }catch (IOException ex) {
            LOG.error("Invalid Path in writeString()");
        }
    }   // end method write
 */ //   long kilobytes = Runtime.getRuntime().freeMemory() / 1024;
 //   long megabytes = kilobytes / 1024;
 //   long gigabytes = megabytes / 1024;
 //   LOG.debug("freememory KB : " + kilobytes + "/ mega : " + megabytes + "/ giga : " + gigabytes);