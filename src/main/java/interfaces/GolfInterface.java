package interfaces;

import java.text.*;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public interface GolfInterface
{
    String SYSTEM_DRIVE = System.getProperty("user.home").substring(0,1);   // on extrait la 1ere lettre

//static final int [][]PAR_5  = { {1,6},{2,5},{3,4},{4,3},{5,2},{6,1},{7,0} };
//static final int [][]PAR_4  = { {1,5},{2,4},{3,3},{4,2},{5,1},{6,0} };
//static final int [][]PAR_3  = { {1,4},{2,3},{3,2},{4,1},{5,0}};
// EGA Exact handicap entre col 1 et col2
// buffer zone = entre col 3 et col 4
// ajustement à la hausse : col 5
// ajustement à la baisse : col 6
static final double [][] BUFFER_ZONE =    // avant 2014
    {
    {1.0,0.0,  4.4, 35, 36,0.1,0.1},
    {2.0,4.5, 11.4, 34, 36,0.1,0.2},
    {3.0,11.5,18.4, 33, 36,0.1,0.3},
    {4.0,18.5,26.4, 32, 36,0.1,0.4},
    {5.0,26.5,36.0, 31, 36,0.2,0.5},
    };

static final double [][] BUFFER_ZONE_2014 =    // à partir de 2014
    {
    {1.0, 0.0,  4.4, 33,36, 0.1, 0.1},
    {2.0, 4.5, 11.4, 32,36, 0.1, 0.2},
    {3.0, 11.5,18.4, 31,36, 0.1, 0.3},
    {4.0, 18.5,26.4, 30,36, 0.1, 0.4},
    {5.0, 26.5,36.0, 30,36, 0.2, 0.5},
    };

/* modification 11-09-2018 : utilisation de jdbc.properties dans DBConnection
static final String DB_DRIVER = "com.mysql.jdbc.Driver()";
driver deleted on 11-09-2018 chargé automatiquement par woldfly car jdbc4 compliant
 static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver()";  // changed 05-10-2017 pour Connector/J 8
static final String DB_NAME = "golflc";

static final String DB_HOST_57 = "localhost:3306";  // new 02-09-2018 passage à MySQL Server version 8 (le 3306 était utilisé par version 5.7
static final String DB_CONNECTION_V5_7 =   // mod 21/07/2015 5 = mysql version 5.7, access with Connector /J 8.12 on 05-08-2018
  //      "jdbc:mysql://localhost:3306/golflc?AllowMultiQueries=true?AllowUserVariables=true";
    //    "jdbc:mysql://localhost:3306/" + DB_NAME
          "jdbc:mysql://" + DB_HOST_57 + "/" + DB_NAME
                + "?AllowMultiQueries=true"
                + "&AllowUserVariables=true"
                + "&useSSL=false"
                + "&nullNamePatternMatchesAll=true"; // add 11/12/2017 Connector/J 8
static final String DB_HOST_80 = "localhost:3307"; 
static final String DB_CONNECTION_V8_0 =  
          "jdbc:mysql://" + DB_HOST_80 +" /" + DB_NAME
                + "?AllowMultiQueries=true"
                + "&AllowUserVariables=true"
                + "&useSSL=false"
                + "&nullNamePatternMatchesAll=true"; // add 11/12/2017 Connector/J 8
//nullNamePatternMatchesAll=true"
//"Properties that have their default values changed:" nullNamePatternMatchesAll is now false by default
// So one way to fix it is by appending ?nullNamePatternMatchesAll=true to the connection string 
// replaced on 11-09-2018 by jdbc.properties

static final String DB_USER = "LouisCollet";
static final String DB_PASSWORD = "lc1lc2";
*/
static final String DATE_BEGIN_COURSE = "2010-01-01";
static final String DATE_END_COURSE = "2099-12-31";
static final String DATE_END_HANDICAP = "2099-12-31";

static java.text.DateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");
static java.text.DateFormat SDF_YYYY = new SimpleDateFormat("yyyy/MM/dd");
static java.text.DateFormat SDF_MM = new SimpleDateFormat("yyyy/MM");
static java.text.DateFormat SDF_TIME = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
static java.text.DateFormat sdf_timeHHmm  = new SimpleDateFormat("dd/MM/yyyy HH:mm");
static java.text.DateFormat sdf_timeHHmmss  = new SimpleDateFormat("dd/MM/yyyy HH:mm:SSS");
static DateTimeFormatter ZDF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm a");
static DateTimeFormatter ZDF_DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
static DateTimeFormatter ZDF_HOURS = DateTimeFormatter.ofPattern("HH:mm");
static DateTimeFormatter ZDF_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
static DateTimeFormatter ZDF_TIME_HHmm = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
static DateTimeFormatter ZDF_TIME_DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy").withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
//static DateTimeFormatter ZDF_TIME_HHMM = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
//final  static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
static final String NEWLINE = System.getProperty( "line.separator" );

static final String OWM_KEY_LC = "65b6810c7fb377fb322b6a7486bfb87a"; /* YOUR OWM API KEY HERE */ //key LC
//static final String TAB = "\t";

static final String NEW_PAGE = "\f";

//static final String NEW_LINE = "\n";

static final String NEW_LINE_XML = "&#10;";

static final String destination = "C:\\Users\\collet\\Documents\\NetBeansProjects\\GolfWfly\\upload\\ ";

// see also in web.xml        <file.upload>E:\FileUpload</file.upload>

//static final String GOOGLE_API_KEY = "AIzaSyACXDPdyVSXu-qCcvegAyoL2ykdbahQ3Lc"; transéré to Constants
} // end interface