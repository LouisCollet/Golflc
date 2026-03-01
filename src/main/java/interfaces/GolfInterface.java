package interfaces;

import java.text.*;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public interface GolfInterface{ 
 //  lso, the variables declared in an interface are public, static & final by default 
String EMPTY_STRING = "";
 String NULL_STRING = "null";
 String ARRAY_START = "{";
 String ARRAY_END = "}";
 String EMPTY_ARRAY = ARRAY_START + ARRAY_END;
 String ARRAY_ELEMENT_SEPARATOR = ", ";
 String PREFIX_SEPARATOR = ":";
 String FOLDER_SEPARATOR = "/";
 String WINDOWS_FOLDER_SEPARATOR = "\\";
 String TOP_PATH = "..";
 String CURRENT_PATH = ".";
 String DOT_SEPARATOR = ".";
 String DOT_SEPARATOR_SPLIT_REGEX = "\\.";    
 //String GoogleApiKey = removed — now in env var GOOGLE_MAPS_API_KEY
 /*
 String RESET = "\u001B[0m";
 String RED = "\u001B[31m";
 String RED_BOLD = "\u001B[1;31m"; // 1; = bold 
 String RED_BOLD_UNDERLINED = "\u001B[1;4;31m"; // 1; = bold 4;=underline
 String RED_BACKGROUND = "\u001B[41m"; // [4 au lieu de [3
 */
  String SYSTEM_DRIVE = System.getProperty("user.home").substring(0,1);   // on extrait la 1ere lettre
//static final int [][]PAR_5  = { {1,6},{2,5},{3,4},{4,3},{5,2},{6,1},{7,0} };
//static final int [][]PAR_4  = { {1,5},{2,4},{3,3},{4,2},{5,1},{6,0} };
//static final int [][]PAR_3  = { {1,4},{2,3},{3,2},{4,1},{5,0}};
// EGA Exact handicap entre col 1 et col2
// buffer zone = entre col 3 et col 4
// ajustement à la hausse : col 5
// ajustement à la baisse : col 6
double [][] BUFFER_ZONE =    // avant 2014
    {
    {1.0,0.0,  4.4, 35, 36,0.1,0.1},
    {2.0,4.5, 11.4, 34, 36,0.1,0.2},
    {3.0,11.5,18.4, 33, 36,0.1,0.3},
    {4.0,18.5,26.4, 32, 36,0.1,0.4},
    {5.0,26.5,36.0, 31, 36,0.2,0.5},
    };


// EGA Exact handicap entre col2 et col3
// buffer zone = entre col4 et col5
// ajustement à la hausse : col 6
// ajustement à la baisse : col 7
//   1   2     3   4  5    6    7
double [][] BUFFER_ZONE_2014 = {   // à partir de 2014
    {1, 0.0,  4.4, 33,36, 0.1, 0.1},
    {2, 4.5, 11.4, 32,36, 0.1, 0.2},
    {3, 11.5,18.4, 31,36, 0.1, 0.3},
    {4, 18.5,26.4, 30,36, 0.1, 0.4},
    {5, 26.5,36.0, 30,36, 0.2, 0.5},
 //   {6.0, 36.1,54.0, 30,36, 0.2, 0.5},  // new category 6 on 01/05/2019
    {6.0, 37,54, 30,36, 0.0, 1},  // new category 6 on 01/05/2019
    };

int[][] FEWER_THAN_20SD = {   // WHS, à partir de 11/2020
    {1, 1, -2},     {2, 1, -2},     {3, 1, -2},
    {4, 1, -1},     {5, 1, 0},     {6, 2, -1},
    {7, 2, 0},     {8, 2, 0},     {9, 3, 0},
    {10, 3, 0},    {11, 3, 0},    {12, 4, 0},
    {13, 4, 0},    {14, 4, 0},    {15, 5, 0},
    {16, 5, 0},    {17, 6, 0},    {18, 6, 0},
    {19, 7, 0},
    {20, 8, 0}, // mod 26-07-2020
};
//static final int ADDITIONAL_STABLEFORD_POINTS = 17;
// static final BigDecimal ADDITIONAL_STABLEFORD_POINTS = BigDecimal.valueOf(17);

// String DATE_BEGIN_COURSE = "2010-01-01"; old
//String DATE_END_COURSE = "2099-12-31";
// mod 03-12-2025
LocalDateTime DATE_BEGIN_COURSE = LocalDateTime.parse("2010-01-01T00:00:00");
LocalDateTime DATE_END_COURSE   = LocalDateTime.parse("2099-12-31T23:59:59");
String DATE_END_HANDICAP = "2099-12-31";
// if(round.getRoundDate().isBefore(LocalDateTime.of(2014,Month.MAY,01,0,0))){
// in Belgium !
LocalDateTime START_DATE_WHS = LocalDateTime.of(2020,Month.NOVEMBER,03,0,0);

java.text.DateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");
java.text.DateFormat SDF_HH_MM = new SimpleDateFormat("HH:mm");
java.text.DateFormat SDF_YYYY = new SimpleDateFormat("yyyy/MM/dd");
java.text.DateFormat SDF_YYYY_MM = new SimpleDateFormat("yyyy/MM");
java.text.DateFormat SDF_TIME = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.getDefault());
java.text.DateFormat sdf_timeHHmm  = new SimpleDateFormat("dd/MM/yyyy HH:mm");
java.text.DateFormat sdf_timeHHmmss  = new SimpleDateFormat("dd/MM/yyyy HH:mm:SSS");
DateTimeFormatter ZDF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm a");
DateTimeFormatter ZDF_DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
DateTimeFormatter ZDF_YEAR_MONTH_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
DateTimeFormatter ZDF_FILE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-SSS"); // insert in file name
DateTimeFormatter ZDF_YEAR = DateTimeFormatter.ofPattern("yyyy");
DateTimeFormatter ZDF_HOURS = DateTimeFormatter.ofPattern("HH:mm");
DateTimeFormatter ZDF_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
DateTimeFormatter ZDF_TIME2 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
DateTimeFormatter mongo_formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US); 
                                                            // format = Wed Nov 23 11:09:18 CET 2022    yyyy-MM-dd'T'HH:mm:ss'Z'
// https://help.gooddata.com/cloudconnect/manual/date-and-time-format.html  "

//TemporalAdjuster temporalFirst = TemporalAdjusters.firstDayOfYear();
//TemporalAdjuster temporalLast = TemporalAdjusters.lastDayOfYear();
DateTimeFormatter ZDF_YY_MM = DateTimeFormatter.ofPattern("yy-MM").withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
DateTimeFormatter ZDF_TIME_HHmm = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
DateTimeFormatter ZDF_TIME_DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy").withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
//static DateTimeFormatter ZDF_TIME_HHMM = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
// static final String NEW_LINE = System.getProperty( "line.separator" );
final String OWM_KEY_LC = "65b6810c7fb377fb322b6a7486bfb87a"; /* YOUR OWM API KEY HERE */ //key LC
//static final String TAB = "\t";
final String NEW_PAGE = "\f";
String NEW_LINE_XML = "&#10;";
String DESTINATION = "C:\\Users\\collet\\Documents\\NetBeansProjects\\GolfWfly\\upload\\ "; // mod 03-09-2023
} // end interface