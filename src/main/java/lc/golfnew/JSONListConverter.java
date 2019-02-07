package lc.golfnew;

import entite.TarifGreenfee;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
//@Named("ItemReaderInscription")
@Named("jSONListConverter")
@SessionScoped
public class JSONListConverter implements Serializable, interfaces.GolfInterface
{
    static Connection conn = null; 
    public void main( String[] args ) throws IOException, ParseException, SQLException
    {
        testTarif();
    }
        // http://www.appsdeveloperblog.com/java-into-json-json-into-java-all-possible-examples/
        
        //  deserialisez dates   http://www.baeldung.com/jackson-serialize-dates
        
        // oui : http://www.makeinjava.com/convert-object-date-json-jackson-objectmapper-example/
        // https://github.com/amitshekhariitbhu/Fast-Android-Networking/issues/88
        
        //Convert Array of String into JSON
        // Configure gson
        //http://www.baeldung.com/jackson-annotations
  public static void testTarif() throws SQLException     //  throws SQLException, Exception
    {       
 try{
  /*   
     int[][] multi = new int[][]{
  { 1, 2, 3, 0, 0, 0, 0, 0, 0, 0 },
  { 21, 22, 23, 0, 0, 0, 0, 0, 0, 0 },
  { 0, 0, 3, 0, 0, 0, 0, 0, 0, 0 },
  { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
  { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
};
     LOG.info("0 0 = "+ multi[0][0]); 
     LOG.info("1 0 = "+ multi[1][0]); 
     LOG.info("0 1 = "+ multi[0][1]); 
     
     LOG.info("multi = " + Arrays.deepToString(multi)); 
    */

// http://www.baeldung.com/jackson-jsonmappingexception
    	//Define map which will be converted to JSON
        
 //       SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
 //       sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
   //  sdf.setTimeZone(zone);
    //   Date [][]datesSeason = new Date[20][3];
       String [][]datesSeason = new String[20][3];
       // fill 1st row
       datesSeason[0][0] = ("01/01/2018");
       datesSeason[0][1] = ("28/02/2018");
       datesSeason[0][2] = ("M");
       // fil 2nd row
       datesSeason[1][0] = ("01/03/2018");
       datesSeason[1][1] = ("31/05/2018");
       datesSeason[1][2] = ("H");
       // fil 3rd row
       datesSeason[2][0] = ("01/06/2018");
       datesSeason[2][1] = ("30/06/2018");
       datesSeason[2][2] = ("M");
       
       datesSeason[3][0] = ("01/07/2018");
       datesSeason[3][1] = ("31/08/2018");
       datesSeason[3][2] = ("L");
       
       datesSeason[4][0] = ("01/09/2018");
       datesSeason[4][1] = ("30/09/2018");
       datesSeason[4][2] = ("M");
       
       datesSeason[5][0] = ("01/10/2018");
       datesSeason[5][1] = ("30/11/2018");
       datesSeason[5][2] = ("H");
       
       datesSeason[6][0] = ("01/12/2018");
       datesSeason[6][1] = ("31/12/2018");
       datesSeason[6][2] = ("L");
   
       LocalDateTime dround_in = LocalDateTime.of(2018, Month.JUNE, 29, 12, 15);
       
       
       LOG.info("LocalDateTime dround = " + dround_in);
       LocalDate ddeb = LocalDate.parse(datesSeason[0][0],ZDF_DAY);
       LOG.info("lOCalDate ddeb = " + ddeb);
       LocalDate dfin = LocalDate.parse(datesSeason[0][1],ZDF_DAY);
        LOG.info("Local Date dfin = " + dfin);
       LocalDate dround = dround_in.toLocalDate();
        LOG.info("LocalDate dround = " + dround);
       
        LocalTime tround = dround_in.toLocalTime();
        LOG.info("LocalTime tround = " + tround);
        

  // LOG.info("dround = " + dround);
       if (dround.isEqual(ddeb)
       || dround.isEqual(dfin)
       || (dround.isAfter(ddeb) && (dround.isBefore(dfin)))){
            LOG.info("getit !!= " + dround);
            // alors on a la période code prix !!H M L
         }
  String season = "";
    //   LocalDate date = LocalDate.of(2014, 2, 15);
        LOG.info("datesSeason = " + Arrays.deepToString(datesSeason));

        
        
        
     for (String[] datesSeason1 : datesSeason) {
         //          LOG.info("length row = " + datesSeason.length); //[col]);
         //   for (int col = 0; col < datesSeason[row].length; col++)
         //  {
         //               LOG.info("length col = " + datesSeason[row].length);
         //   datesSeason[row][col] = row * col;
         //   LOG.info("col = " + col + "row = " + row);
         if (datesSeason1[0] == null) {
             break;
         } else {
             ddeb = LocalDate.parse(datesSeason1[0], ZDF_DAY);
             dfin = LocalDate.parse(datesSeason1[1], ZDF_DAY);
             if (dround.isEqual(ddeb)
                     || dround.isEqual(dfin)
                     || (dround.isAfter(ddeb) && (dround.isBefore(dfin)))) {
                 LOG.info("Trouvé dans datesSeason for !!= " + dround);
                 // alors on a la période code prix !!H M L 
                 season = datesSeason1[2];
                 LOG.info("season = " + season);
                 LOG.info("elem deb = " + ddeb);
                 LOG.info("elem fin = " + dfin);
                 break;
             }
         } // end else
     } // end for
    
    String [][]teeTimes = new String[10][5];
       // fill 1st row
       teeTimes[0][0] = ("00:00");
       teeTimes[0][1] = ("07:59");
       teeTimes[0][2] = ("0");
       teeTimes[0][3] = ("0");
       teeTimes[0][4] = ("0");
       // fil 2nd row
       teeTimes[1][0] = ("08:00");
       teeTimes[1][1] = ("08:59");
       teeTimes[1][2] = ("76"); // High
       teeTimes[1][3] = ("74"); // Medium
       teeTimes[1][4] = ("74"); // Low
       // fil 3rd row
       teeTimes[2][0] = ("09:00");
       teeTimes[2][1] = ("11:59");
       teeTimes[2][2] = ("90");
       teeTimes[2][3] = ("84.5");
       teeTimes[2][4] = ("74");
      
       teeTimes[3][0] = ("12:00");
       teeTimes[3][1] = ("13:59");
       teeTimes[3][2] = ("111");
       teeTimes[3][3] = ("90");
       teeTimes[3][4] = ("74");
    
       teeTimes[4][0] = ("14:00");
       teeTimes[4][1] = ("16:59");
       teeTimes[4][2] = ("90");
       teeTimes[4][3] = ("84.5");
       teeTimes[4][4] = ("74");
    
       teeTimes[5][0] = ("17:00");
       teeTimes[5][1] = ("23:59");
       teeTimes[5][2] = ("65");
       teeTimes[5][3] = ("60");
       teeTimes[5][4] = ("55");
    
    LOG.info("teeTimes = " + Arrays.deepToString(teeTimes));
    
    
    LocalTime tdeb = null;//dround_in.toLocalTime();
    LocalTime tfin = null;//dround_in.toLocalTime();
    double price = 0;
     for (String[] teeTime : teeTimes) {
         if (teeTime[0] == null) {
             break;
         } else {
             LOG.info("row = " + Arrays.deepToString(teeTime));
             LOG.info("HR deb = " + teeTime[0]);
             LOG.info("HR fin = " + teeTime[1]);
             LOG.info("Prix High = " + teeTime[2]);
             LOG.info("Prix Medium = " + teeTime[3]);
             LOG.info("Prix Low = " + teeTime[4]);
         } // endif
     } // end for
    
    LOG.info("before search teeTimes " );
     // 0 = deb
     // 1 = dfin
     // 2 = price H
     // 3 = price M
     // 4 = price L
     for (String[] teeTime : teeTimes) {
         if (teeTime[0] == null) {
             break;
         } else {
             tdeb = LocalTime.parse(teeTime[0], ZDF_HOURS);
             tfin = LocalTime.parse(teeTime[1], ZDF_HOURS);
             if (tround.equals(tdeb)
                     || tround.equals(tfin)
                     || (tround.isAfter(tdeb) && (tround.isBefore(tfin)))) {
                 LOG.info("Trouvé dans teeTimes for !!= " + tround);
                 LOG.info("elem tdeb = " + tdeb);
                 LOG.info("elem tfin = " + tfin);
                 switch (season) {
                     case "H":
                         price = Double.valueOf(teeTime[2]);
                         LOG.info("case H");
                         break;
                     case "M":
                         price = Double.valueOf(teeTime[3]);
                         LOG.info("case M");
                         break;
                     case "L":
                         price = Double.valueOf(teeTime[4]);
                         LOG.info("case L");
                         break;
                     default:
                         LOG.info("price not found !!!= ");
                 } //end switch
             } // end if
         } // end else
     } // end for
   LOG.info("at the end the price is = " + price);
   
      TarifGreenfee t = new TarifGreenfee(datesSeason, teeTimes); 
      t.RemoveNull();
   // trouver la première date 
   LOG.info("première date = " + datesSeason[0][0]);
   LOG.info("dernière date = " + datesSeason[datesSeason.length][1]);
   LocalDate dd = LocalDate.parse(datesSeason[0][0], ZDF_DAY);
   LOG.info("dd début = " + dd);
   
   
 // et maintenant, jour de la semaine !! -----------------------------------------
    DayOfWeek dayOfWeek = dround.getDayOfWeek();
    LOG.info("dayOfWeek Name = " + dayOfWeek.name());
    int dayOfWeekIntValue = dayOfWeek.getValue(); // 6
    LOG.info("dayOfWeekIntValue = " + dayOfWeekIntValue);
 
       switch(dayOfWeek) {
                    case FRIDAY:
                         LOG.info("tarif for Friday ");
                        break;
                    case SATURDAY:
                         LOG.info("tarif for Saturday - weekend");
                        break;
                    case SUNDAY:
                         LOG.info("tarif for Sunday - weekend");
                        break;
                    default:
                        LOG.info("default tarif for week : !!= " + dayOfWeek.name());
       }
       
   //    dround.getYear();
               utils.LCUtil.EasterSundayDate(2017);
          //     utils.Holidays.EasterSundayDate(2018);
               utils.LCUtil.EasterSundayDate(2019);
               utils.LCUtil.EasterSundayDate(dround.getYear());
               utils.LCUtil.EasterSundayDate(2024);
               utils.LCUtil.EasterSundayDate(2027);
               utils.LCUtil.EasterSundayDate(2029);
               utils.Holidays ho = new utils.Holidays();
               boolean b = ho.CountryHolidays(dround,"BE");
               // true = holiday

/*
        
        Tarif t = new Tarif(datesSeason, teeTimes); 
        
        Course course = new Course();
        course.setIdcourse(102);  // santana = course 102
        conn = DBConnection.getConnection();
        
        
       boolean b1 = create.CreateTarif.createTarif(t, course, conn);
       LOG.info("after create tarif " + b1);
        
       Tarif ta = find.FindTarifData.findCourseTarif(course, conn);
        LOG.info("YES YES after find tarif " + NEW_LINE + ta.toString());
 */       
        
        
        } catch (Exception e) {
     LOG.info("jackson exception by LC =  = " + e);
  //          e.printStackTrace();
    }

        
    }
  
  
  
    private static List<List<Object>> compact(Object[][] array) {
        //https://stackoverflow.com/questions/41727238/java-multidimensional-array-remove-empty-null-elements
    return Stream.of(array).parallel()
      // Ignore null objects of the outer array
      .filter(Objects::nonNull)
      // Process all inner arrays
      .map(inner -> Stream.of(inner).parallel()
        // Ignore null objects or empty strings
        .filter(element -> element != null && !"".equals(element))
        // Collect what is left
        .collect(Collectors.toList()))
      // From the result above, ignore all empty arrays
      .filter(inner -> !inner.isEmpty())
      // Collect what is left
      .collect(Collectors.toList());
  }
    


} //end class
