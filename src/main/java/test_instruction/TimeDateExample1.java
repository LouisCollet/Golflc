package test_instruction;
//import net.aksingh.owmjapis.core.OpenWeatherMap;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import static java.util.Comparator.comparingInt;
import net.aksingh.owmjapis.api.APIException;

public class TimeDateExample1 {
    public static void main(String[] args) throws APIException {
        
   try{
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

//time = LocalTime.of(15, 30); // 15:30:00

int hour = lt1530.getHour(); // 15
System.out.println("hour of day = " + lt1530.getHour());
int second = lt1530.getSecond(); // 0

int minute = lt1530.getMinute(); // 30
System.out.println("minute of day = " + lt1530.getMinute());
int secondOfDay = lt1530.toSecondOfDay(); // 55800      
       
    //   2014, Month.FEBRUARY, 10
       LocalDate a = LocalDate.of(2018, Month.JANUARY, 14);
       ZoneId brussels = ZoneId.of("Europe/Brussels");
       LocalDateTime localDateTime = LocalDateTime.now(brussels);
       LocalTime lt1 = LocalTime.now(brussels).minusHours(0);
//LocalTime lt1 = localDateTime.toLocalTime().minusHours(0);

if(lt1.isBefore(LocalTime.of(8, 30, 00, 000000001))){
    System.out.println("tarif 1 - localtime before lt0830 !! , lt = " + lt0830 + " /lt0830 = " + lt1);
}else if (lt1.isBefore(LocalTime.of(12, 30, 00, 000000001))){
    System.out.println("tarif 2 - localtime before lt1230 !! , lt = " + lt1 + " /lt1230 = " + lt1230);
}else if (lt1.isBefore(LocalTime.of(15, 30, 00, 000000001))){
    System.out.println("tarif 3 - localtime before lt1530 !! , lt = " + lt1 + " /lt1530 = " + lt1530);
}
else{
        System.out.println("tarif 4 - localtime after lt1530 !! , lt = " + lt1 + " /lt1530 = " + lt1530);
}



 System.out.println("hour of day = " + localDateTime.getHour());
 System.out.println("minute of day = " + localDateTime.getMinute());

DayOfWeek dayOfWeek = a.getDayOfWeek();
 System.out.println("dayOfWeek Name = " + dayOfWeek.name());
 int dayOfWeekIntValue = dayOfWeek.getValue(); // 6
 System.out.println("dayOfWeekIntValue = " + dayOfWeekIntValue);
 
int dayNum = localDateTime.getDayOfWeek().getValue(); //.name();
 System.out.println("value Num = " + dayNum); //a.getDayOfWeek().name());
String dayString = localDateTime.getDayOfWeek().name();
System.out.println("value String = " + dayString); 
if (dayOfWeek.equals("FRIDAY"))
{
    
}
 
 
//boolean isWeekend = (dayNum == DateConstants.SATURDAY || dayNum == DateTimeConstants.SUNDAY);
    
    System.out.println(a.getDayOfWeek().name());
       
       LocalDate firstDate = LocalDate.of(2010, 5, 17); // 2010-05-17 
		LocalDate secondDate = LocalDate.of(2015, 3, 7); // 2015-03-07  		Period period = Period.between(firstDate, secondDate); 

 Period period = Period.between(firstDate, secondDate);
 		int days = period.getDays(); // 18 
 		int months = period.getMonths(); // 9 
 		int years = period.getYears(); // 4 
 		boolean isNegative = period.isNegative(); // false 
  
 		Period twoMonthsAndFiveDays = Period.ofMonths(2).plusDays(5); 
		LocalDate sixthOfJanuary = LocalDate.of(2014, 1, 6); 
  
 		// add two months and five days to 2014-01-06, result is 2014-03-11 
 		LocalDate eleventhOfMarch = sixthOfJanuary.plus(twoMonthsAndFiveDays); 

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
      System.out.println("date1 is before date" + date3.isBefore(date4));  
 LocalDate today = LocalDate.now();
int year = today.getYear();
int month = today.getMonthValue();
int day = today.getDayOfMonth();
System.out.printf("Year : %d  Month : %d  day : %d \t %n", year, month, day);

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
     System.out.println("OWM exception by LC =  = " + e);
  //          e.printStackTrace();
    }
   
   
    } // end method main
    
    public static void isWeekend(LocalDate dt) {
    
}
    
    
} // end class