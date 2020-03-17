package test_instruction;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import static java.util.Comparator.comparingInt;
import net.aksingh.owmjapis.api.APIException;
public class ZonedDateTimeExample {

    public static void main(String[] args) throws APIException {
   try{
       
       System.out.println("system default ZoneId = " + ZoneId.systemDefault());
       
       ZoneId zid = ZoneId.systemDefault();
      System.out.printf("Zone Id = %s%n", zid);
      System.out.printf("Rules = %s%n", zid.getRules());
      System.out.printf("DST in effect: %b%n",zid.getRules().isDaylightSavings(Instant.now()));

      zid = ZoneId.of("Europe/Paris");
      System.out.printf("Zone Id = %s%n", zid);

      ZoneOffset zoffset = ZoneOffset.of("+06:00");
      System.out.printf("Zone Offset = %s%n", zoffset);
      System.out.printf("Total seconds = %d%n", zoffset.getTotalSeconds());

      ZonedDateTime zonedDateTime = ZonedDateTime.now();
      System.out.printf("Zoned date and time = %s%n", zonedDateTime);
      System.out.printf("Zone = %s%n", zonedDateTime.getZone());

      zoffset = ZoneOffset.from(zonedDateTime);
      System.out.printf("Zone Offset = %s%n", zoffset);
 
      OffsetDateTime offsetDateTime = OffsetDateTime.now();
      System.out.printf("Offset date and time = %s%n", offsetDateTime);
 //     System.out.printf("Offset date and time = %s%n", 
  //                      offsetDateTime.with(Adjusters.lastDayOfMonth()));

      zonedDateTime = ZonedDateTime.of(2013, 11, 2, 3, 00, 0, 0, 
                                       ZoneId.of("America/Chicago"));
      System.out.printf("Zoned date and time = %s%n", zonedDateTime);


      offsetDateTime = OffsetDateTime.of(2013, 11, 2, 3, 00, 0, 0, zoffset);
      System.out.printf("Offset date and time = %s%n", offsetDateTime);

      offsetDateTime = OffsetDateTime.of(2013, 11, 3, 3, 00, 0, 0, zoffset);
      System.out.printf("Offset date and time = %s%n", offsetDateTime);
       
       
       
        // create a ZonedDateTime object 
        ZonedDateTime zonedDT 
            = ZonedDateTime 
                  .parse( 
                      "2018-12-06T19:21:12.123+05:30[Asia/Calcutta]"); 
  
        // print ZonedDateTime 
        System.out.println("ZonedDateTime of Calcutta: "
                           + zonedDT); 
  
        // apply withZoneSameInstant() 
        ZonedDateTime zonedDT2 
            = zonedDT 
                  .withZoneSameInstant( 
                      ZoneId.of("Pacific/Fiji")); 
  
        // print ZonedDateTime after withZoneSameInstant() 
        System.out.println("ZonedDateTime of Fuji: "
                           + zonedDT2); 
       
       // create a ZonedDateTime object 
         zonedDT 
            = ZonedDateTime 
                  .parse( 
                      "2018-10-25T23:12:31.123+02:00[Europe/Paris]"); 
  
        // print ZonedDateTime 
        System.out.println("ZonedDateTime of Calcutta: "
                           + zonedDT); 
  
        // apply withZoneSameInstant() 
         zonedDT2 
            = zonedDT 
                  .withZoneSameInstant( 
                      ZoneId.of("Canada/Yukon")); 
  
        // print ZonedDateTime after withZoneSameInstant() 
        System.out.println("ZonedDateTime of yukon: "
                           + zonedDT2); 
       
       
       ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
       System.out.println("now = " + now);
 zonedDateTime = now.withZoneSameInstant(ZoneId.of("America/Chicago"));
 System.out.println("zdt = " + zonedDateTime);
       
       
       
       Instant inst = LocalDateTime.of(2016, Month.MARCH, 12, 20, 45)
            .atZone(ZoneId.of("America/Chicago"))
            .toInstant();
    System.out.println("Instant = " + inst); //2016-03-13T02:45:00Z
       Timestamp ts = Timestamp.from(inst);
    System.out.println("timestamp = " + ts); //2016-03-12 20:45:00.0
       
     ZonedDateTime ldt = 
       LocalDateTime.parse( "2014-02-14T06:04:00" )    // Parse a string lacking any indicator of time zone or offset-from-UTC. *Not* a specific point on the timeline.
             .atOffset( ZoneOffset.UTC )           // Apply UTC as we are certain that offset-from-UTC of zero was intended by the supplier of that input string. Returns a `OffsetDateTime` object.
             .atZoneSameInstant(                   // Adjust into another time zone. The `sameInstant` part means the same moment, different wall-clock time. 
                 ZoneId.of( "Africa/Tunis" )       // Specify the particular zone of interest to you.
             );                                     // Returns a `ZonedDateTime` object.
       System.out.println("zdt " + ldt);   
       
       
       LocalDateTime localDateTime = LocalDateTime.of(2018, 10, 25, 12, 00, 00);  //October 25th at 12:00pm
ZonedDateTime zonedDateTimeInUTC = localDateTime.atZone(ZoneId.of("UTC")); 
ZonedDateTime zonedDateTimeInEST = zonedDateTimeInUTC.withZoneSameInstant(ZoneId.of("America/New_York")); 

System.out.println(localDateTime.toString()); // 2018-10-25T12:00
System.out.println(zonedDateTimeInUTC.toString()); // 2018-10-25T12:00Z[UTC]
System.out.println(zonedDateTimeInEST.toString()); // 2018-10-25T08:00-04:00[America/New_York]
       
 ZonedDateTime z = ZonedDateTime.of(LocalDate.now().atTime(11, 30), ZoneOffset.UTC);
System.out.println(z.withZoneSameInstant(ZoneId.of("US/Central")));  

ZonedDateTime y1 = ZonedDateTime.of(LocalDate.now().atTime(11, 30), ZoneOffset.UTC);
System.out.println("y1 = " + y1);  
ZoneId y2 = ZoneId.of("US/Central");
System.out.println("y2 = " + y2);  
LocalDateTime l = LocalDateTime.ofInstant(y1.toInstant(), y2);
System.out.println("l = " + l);  
 
/* https://stackoverflow.com/questions/31909006/save-zoneddatetime-in-mysql-and-glassfish

 https://stackoverflow.com/questions/38063851/mysql-datetime-and-timestamp-to-java-sql-timestamp-to-zoneddatetime
Timestamp t = resultSet.getTimestamp(timestampColumnId);
ZoneId zoneId = ZoneId.of(resultSet.getString(zoneColumnId), ZoneId.SHORT_IDS);
ZonedDateTime d = ZonedDateTime.ofInstant(t.toInstant(), zoneId);
Or, you could just store the DATETIME as a TIMESTAMP in the database as ZZ Coder suggests in his answer stated above. But, you could just use the ZoneId you have hard-coded as such:
Timestamp t = resultSet.getTimestamp(timestampColumnId);
ZonedDateTime d = ZonedDateTime.ofInstant(t.toInstant(), zoneId);
*/
       LocalDate date = LocalDate.of(2014, 2, 15); // 2014-06-15
       LocalDateTime startOfDay = date.atStartOfDay(); // 2014-02-15 00:00
//LocalDateTime d = LocalDateTime.parse("2017-02-03T12:30:30");
// time information
LocalDateTime d = LocalDateTime.of(2014, 02, 20, 12, 0);
LocalTime t = d.toLocalTime();
System.out.println("LocalTime = " + t);

LocalTime lt0830 = LocalTime.of(8, 30, 00, 000000001); // 15:30:00 15,30
LocalTime lt1230 = LocalTime.of(12, 30, 00, 000000001); // 15:30:00 15,30
LocalTime lt1530 = LocalTime.of(15, 30, 00, 000000001); // 15:30:00 15,30

System.out.println("LocalTime1530 = " + lt1530);


       
    //   2014, Month.FEBRUARY, 10
       LocalDate a = LocalDate.of(2018, Month.JANUARY, 14);
       ZoneId brussels = ZoneId.of("Europe/Brussels");
        localDateTime = LocalDateTime.now(brussels);
       LocalTime lt1 = LocalTime.now(brussels).minusHours(0);
//LocalTime lt1 = localDateTime.toLocalTime().minusHours(0);

 LocalDate d1 = LocalDate.of(2019, 3, 23);
    LocalTime t1 = LocalTime.of(9, 57, 0, 0);
    System.out.println("resultat = " + LocalDateTime.of(d1,t1));
    


 // loaldate : avant 08:00 heures, entre 12:00 et 15:00 

 LocalDateTime dateTime = LocalDateTime.of(2014, Month.APRIL, 1, 10, 45); //10:45
 ZoneId berlin = ZoneId.of("Europe/Berlin"); 

 
 	// 2014-02-20 12:00 
 	LocalDateTime dateTime2 = LocalDateTime.of(2014, 02, 20, 12, 0); 
 	// 2014-02-20 12:00, Europe/Berlin (+01:00) 
 	ZonedDateTime berlinDateTime = ZonedDateTime.of(dateTime2, berlin); 
// https://gist.github.com/mscharhag/9195718 
      LocalDateTime date4 = LocalDateTime.parse("2017-02-03T12:30:30");
      LocalDateTime date3 = LocalDateTime.parse("2017-03-03T12:30:30");

Instant instant = Instant.now();
ZonedDateTime current = instant.atZone(ZoneId.systemDefault());
System.out.printf("Current time is %s%n%n", current);

System.out.printf("%10s %20s %13s%n", "Offset", "ZoneId", "Time");
ZoneId.getAvailableZoneIds().stream()
        .map(ZoneId::of)
        .filter(zoneId -> {
            ZoneOffset offset = instant.atZone(zoneId).getOffset();
            return offset.getTotalSeconds() % (60 * 60) != 0;
        })
        .sorted(comparingInt(zoneId ->instant.atZone(zoneId).getOffset().getTotalSeconds()))
        .forEach(zoneId -> {
            ZonedDateTime zdt = current.withZoneSameInstant(zoneId);
            System.out.printf("%10s %25s %10s%n",
                zdt.getOffset(), zoneId,
                zdt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
        });


//Read more: http://javarevisited.blogspot.com/2015/03/20-examples-of-date-and-time-api-from-Java8.html#ixzz544gC9oiZ

       
  
 } catch (Exception e) {
     System.out.println("ZonedDateTimeExample exception by LC =  = " + e);
  //          e.printStackTrace();
    }
   
   
    } // end method main

} // end class