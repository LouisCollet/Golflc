/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import static java.lang.System.out;
/**
 *
 * @author Louis Collet
 */
public class TestDateFormatter {
    static DateTimeFormatter mongo_formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);  // format = Wed Nov 23 11:09:18 CET 2022  
  void main() {
  //      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy");
try{
    
        String date = "Sat Dec 31 12:23:59 UTC 2022";
        out.println("Date = " + date);
        LocalDateTime ldt = LocalDateTime.parse(date, mongo_formatter); // result in UTC
        out.println("localDateTime = " + ldt);  //default, print ISO_LOCAL_DATE
        out.println("localDateTime minus 1 = " +  ldt.minusHours(1));  //default, print ISO_LOCAL_DATE
        out.println("localDateTime UTC = " + ldt.atZone(ZoneId.of("UTC")));  //default, print ISO_LOCAL_DATE
// convert LocalDateTime to ZonedDateTime, with default system zone id
      ZonedDateTime offSetNegative5 = ldt.atOffset(ZoneOffset.of("-01:00")).toZonedDateTime();
      out.println("negative 1 = " + offSetNegative5.toLocalDateTime());
   //   offSetNegative5.toLocalDate();

      ZonedDateTime zonedDateTime = ldt.atZone(ZoneId.systemDefault());
      out.println("zonedDateTime = " + zonedDateTime);
        
        // convert LocalDateTime to ZonedDateTime, with default system zone id
  //    ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault());

      // convert LocalDateTime to ZonedDateTime, with specified zoneId
      ZonedDateTime europeDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Europe/Brussels"));
      System.out.println(europeDateTime);

      // convert LocalDateTime to ZonedDateTime, with specified off set
     

      // display all zone ids
      //ZoneId.getAvailableZoneIds().forEach(System.out::println);
        
        
        
        
        
     //   System.out.println("formatted date = " + mongo_formatter.format(ldt)); // print formatter date
    //    LocalDateTime ldt = LocalDateTime.parse(s, mongo_formatter); // format UTC
     //   System.out.println("localDateTime formatted = " + ldt);
    //    System.out.println(" systemDefault = " + ZoneId.systemDefault());
        System.out.println("localDateTime at systemDefault = " + ldt.atZone(ZoneId.systemDefault()));
           
        
        
   } catch (Exception me) {
        System.err.println("An error occurred in main: " + me);
   }
 } // end main
} // end class