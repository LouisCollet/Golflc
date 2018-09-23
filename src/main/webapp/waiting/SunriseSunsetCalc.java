
package lc.golfnew;


import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator; 
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class SunriseSunsetCalc implements interfaces.Log{
    private static SunriseSunsetCalculator calc;
//    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//    private static final DateTimeFormatter dtf_utc = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss Z");
    //                                                                            2017-04-09T04:59:02+00:00
    private static final DateTimeFormatter dtf_HHmm = DateTimeFormatter.ofPattern("HH:mm");

    public ArrayList<String> sunCalc (String date_sunrise, String date_sunset, String tz) throws ParseException 
    {
        // test en partant d'une heure de départ en en arrêtant à une heure de fin, créer des départs Golf
 //       String date = "2017-04-09T04:39:02+00:00";
            LOG.info("String date sunrise = " + date_sunrise);
        ZonedDateTime sunrise = ZonedDateTime.parse(date_sunrise, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
 //           LOG.info("ZonedDateTime with iso-offset_date_time: " + sunrise);
        Instant instant = sunrise.toInstant();
            LOG.info("instant = " + instant);
        sunrise = instant.atZone(ZoneId.of(tz));
        LOG.info("tz = " + tz);
            LOG.info ("formatted tz sunrise = " + dtf_HHmm.format(sunrise));
   //         LOG.info ("offset = " + sunrise.getOffset());
  //      sunrise = sunrise.plusMinutes(12);
    //    LOG.info("Later 12 = " + sunrise.plusMinutes(12) );
 //       LOG.info ("formatted added sunrise = " + dtf_HHmm.format(sunrise));
  //       sunrise = sunrise.plusMinutes(12);
    //    LOG.info("Later 12 = " + sunrise.plusMinutes(12) );
 //       LOG.info ("formatted added sunrise = " + dtf_HHmm.format(sunrise));      
        
   //     date = "2017-04-09T18:28:53+00:00";
           LOG.info("String date sunset = " + date_sunset);
           ZonedDateTime sunset = ZonedDateTime.parse(date_sunset, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      //     instant = sunset.toInstant();
      //     sunset = instant.atZone(ZoneId.of("Europe/Brussels"));
           sunset = sunset.toInstant().atZone(ZoneId.of(tz));
           LOG.info ("formatted tz sunset = " + dtf_HHmm.format(sunset));
        ArrayList<String> flight = new ArrayList<>();
        int i = 0;
        while(sunrise.isBefore(sunset.minusHours(2).minusMinutes(30))) // dernier départ 2 heures 30 avant sunset
        {
            i++;
            sunrise = sunrise.plusMinutes(12); // un départ toutes les 12 minutes
            LOG.info("Flight " + i + " = " + dtf_HHmm.format(sunrise));
            // Adding items to arrayList
	    flight.add(dtf_HHmm.format(sunrise) + "/available");
        }
        LOG.info("Arraylist = " + flight.toString()); //Arrays.toString(list));
        LOG.info("get12 : " + flight.get(12));
        
   //     LOG.info("louis");
    //    flight.forEach(item->System.out.println(item));
     //   LOG.info("-------------------");
        flight.forEach(System.out::println);
        // let us print all the elements available in list
        flight.forEach((n) -> {
            System.out.println("Flight = " + n);
        }); 
           return flight;
//Output : C

   //     do {
   //         sunrise = sunrise.plusMinutes(12);
   //         LOG.info ("do while added sunrise = " + dtf_HHmm.format(sunrise));      
   //     } while (sunrise.isBefore(sunset.minusMinutes(12)));
        
 /*       
        
        LocalDateTime ldt = LocalDateTime.parse(input);
        LocalDateTime ldtLater = ldt.plusMinutes(12);
        LOG.info("Later 12 = " + ldtLater);
        ldtLater = ldt.plusMinutes(12);
                LOG.info("Later 24 = " + ldtLater);        
        DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime lt = LocalTime.parse("14:10");
        LOG.info("plus 12 minutes = " + df.format(lt.plusMinutes(12)));
        LOG.info("plus 24 minutes = " + df.format(lt.plusMinutes(12)));
        
  //      start = 
   //             end = 
   //             increment = 12
        
  /*                
        String date = "2017-04-09T04:59:02+00:00";
            LOG.info("String date sunrise = " + date);
        ZonedDateTime sunrise = ZonedDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            LOG.info("ZonedDateTime with iso-offset_date_time: " + sunrise);
        Instant instant = sunrise.toInstant();
            LOG.info("instant = " + instant);
        sunrise = instant.atZone(ZoneId.of("Europe/Brussels"));
            LOG.info ("formatted brussel sunrise = " + dtf_HHmm.format(sunrise));
            LOG.info ("offfset = " + sunrise.getOffset());

           date = "2017-04-09T18:28:53+00:00";
            LOG.info("String date sunset = " + date);
           ZonedDateTime sunset = ZonedDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      //     instant = sunset.toInstant();
      //     sunset = instant.atZone(ZoneId.of("Europe/Brussels"));
           sunset = sunset.toInstant().atZone(ZoneId.of("Europe/Brussels"));
           LOG.info ("formatted brussel sunset = " + dtf_HHmm.format(sunset));
          
                  LOG.info("end of work");
        LOG.info("TimeZone : " + sunset.getZone());
        LocalDateTime  ldt1 = sunset.toLocalDateTime();
        LOG.info("LocalDate : " + ldt1);
 
   */     
       //  autre test  
 /*
 
        instant = Instant.parse("2017-04-09T04:59:02Z");
        ZonedDateTime utc = instant.atZone(ZoneId.of("UTC")); 
            LOG.info("ZonedDateTime utc : " + utc);
            LOG.info ("formatted utc sunrise = " + dtf_HHmm.format(utc));
         bxlTime2 = instant.atZone(ZoneId.of("Europe/Brussels"));
         LOG.info("bxlTime2 = " + bxlTime2);
         LOG.info ("formatted brussel sunrise = " + dtf_HHmm.format(bxlTime2));
         
           instant = Instant.parse("2017-04-09T18:28:53Z");
         utc = instant.atZone(ZoneId.of("UTC")); 
            LOG.info("ZonedDateTime utc : " + utc);
            LOG.info ("formatted utc sunrise = " + dtf_HHmm.format(utc));
         bxlTime2 = instant.atZone(ZoneId.of("Europe/Brussels"));
         LOG.info("bxlTime2 = " + bxlTime2);
         LOG.info ("formatted brussel sunset = " + dtf_HHmm.format(bxlTime2));
         
         
        
        
        
        // Z = UTC+0
        Instant instant = Instant.now();
        LOG.info("Instant : " + instant);
        // Japan = UTC+9
        ZonedDateTime jpTime = instant.atZone(ZoneId.of("Asia/Tokyo"));
        LOG.info("ZonedDateTime Japan : " + jpTime);
        LOG.info("OffSet Japan : " + jpTime.getOffset());
        
//                Instant instant = Instant.now();
        LOG.info("Instant : " + instant);
        // Japan = UTC+9
        ZonedDateTime bxlTime = instant.atZone(ZoneId.of("Europe/Brussels"));
        LOG.info("ZonedDateTime Bxl : " + bxlTime);
        LOG.info("OffSet Bxl : " + bxlTime.getOffset());
        
        
        LocalDateTime dateTime = LocalDateTime.of(2017, Month.APRIL, 18, 6, 57, 38);
        // UTC+9
         jpTime = dateTime.atZone(ZoneId.of("Asia/Tokyo"));
        LOG.info("ZonedDateTime Japan : " + jpTime);
        // Convert to instant UTC+0/Z , java.time helps to reduce 9 hours
         instant = jpTime.toInstant();
        LOG.info("Instant : " + instant);
        
        //The ‘Z’ suffix means UTC, you can convert into a java.time.instant directly, then display it with a time zone.
        
        LocalDateTime now = LocalDateTime.now();
        LOG.info("LocalDateTime = " + dtf.format(now));

        
        LocalDateTime ldt = LocalDateTime.now(); 
ZonedDateTime zdt = ldt.atZone(ZoneOffset.UTC); //you might use a different zone
//ZonedDateTime zdt = ldt.atZone(ZoneOffset.of().UTC);
String iso8601 = zdt.toString();

//Convert from ISO8601 String back to a LocalDateTime

iso8601 = "2016-02-14T18:32:04.150Z";
 zdt = ZonedDateTime.parse(iso8601);
 ldt = zdt.toLocalDateTime();
        
        
        LocalDate localDate = LocalDate.now();
        LOG.info("LocalDate = " + DateTimeFormatter.ofPattern("yyy/MM/dd").format(localDate));
  //     String dateInString = "2017-04-09T04:59:02Z";
      String dateInString =        "2017-04-09T04:59:02+00:00";  // not working
   LOG.info("dateInString with 00:00= " + dateInString);
 //     ZonedDateTime result1 = ZonedDateTime.parse(dateInString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
 //           //LOG.info("ZonedDateTime with iso-offset: " + result1);
      //onedDateTime result2 = result1.parse(dateInString, DateTimeFormatter.ISO_INSTANT);
    //         LOG.info("ZonedDateTime with iso_instant: " + result2);
            
 //     Instant i = result1.toInstant();
 //     LOG.info("Instant i: " + i);
 
         
         
 //       LOG.info("String date 2 from sunrisesunset = " + date2);
         
         
         
         
         
        LocalDateTime result = LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
        //get localdate
        LOG.info("LocalDate : " + result.toLocalDate());

        String date = "2016-08-16T10:15:30+08:00";
            LOG.info("String date to ISO_offset = " + date);
        ZonedDateTime result2 = ZonedDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            LOG.info("ZonedDateTime with iso-offset: " + result2);
        LOG.info("TimeZone : " + result2.getZone());
        LocalDateTime  ldt1 = result2.toLocalDateTime();
        LOG.info("LocalDate : " + ldt1);
        
        //Daylight Saving Time (DST)
//Paris, normally UTC+1 has DST (add one hour = UTC+2) from 27/mar to 30/Oct, 2016. 
//Review the above output, the java.time is able to calculate and handle the DST correctly.
        
      
    //    ZoneOffset nyOffSet = ZoneOffset.of("+00:00");
       
        ZonedDateTime result2 = ZonedDateTime.parse(date2, DateTimeFormatter.ISO_INSTANT);
        ZoneOffset offset = ZoneOffset.of("+00:00");
         LOG.info("zone offset of 00:00= " + offset);
  //      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        instant = Instant.parse( "2017-04-09T04:59:02+00:00" );
         LOG.info("instant sunrise : " + instant);
         
         ZonedDateTime utc = instant.atZone(ZoneId.of("UTC"));
          LOG.info("utc2: " + result2);
         ZonedDateTime bxlTime2 = instant.atZone(ZoneId.of("Europe/Brussels"));
        LOG.info("bxlTime 2: " + result2);
        ZonedDateTime utc2 = ZonedDateTime.now(ZoneOffset.UTC);
        LOG.info("ZonedDateTime 2: " + result2);
        LOG.info("TimeZone 2: " + result2.getZone());
         localDate = result2.toLocalDate();
        LOG.info("LocalDate 2: " + localDate);
        
        
        
        //get date time + timezone
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Tokyo"));
        System.out.println(zonedDateTime);

        //get date time + timezone
        ZonedDateTime zonedDateTime2 = instant.atZone(ZoneId.of("Europe/Athens"));
        System.out.println(zonedDateTime2);
        
        
        
        
        Date date1 = new Date();
        LOG.info("Date = " + sdf.format(date1));

        Calendar cal = Calendar.getInstance();
        LOG.info("Calendar= " + sdf.format(cal.getTime()));

        LocalDateTime now = LocalDateTime.now();
        LOG.info("LocalDateTime = " + dtf.format(now));

        localDate = LocalDate.now();
        LOG.info("LocalDate = " + DateTimeFormatter.ofPattern("yyy/MM/dd").format(localDate));
        
        
        
        Location location = new Location("50.826267", "4.357043"); // 50.826267,4.357043
        calc = new SunriseSunsetCalculator(location, "Europe/Brussels");

   //     DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 //       Calendar cal1 = Calendar.getInstance();
            LOG.info("civil sunset Brussels today = " +   calc.getCivilSunsetForDate(cal));
            LOG.info("civil sunrise Brussels today = " +   calc.getCivilSunriseForDate(cal));
        // https://api.sunrise-sunset.org/json?lat=50.826267&lng=4.357043&date=today
     //   {"results":{"sunrise":"5:01:12 AM","sunset":"6:27:16 PM","solar_noon":"11:44:14 AM","day_length":"13:26:04",
     //"civil_twilight_begin":"4:27:10 AM",
     //"civil_twilight_end":"7:01:17 PM","nautical_twilight_begin":"3:45:32 AM","nautical_twilight_end":"7:42:55 PM","astronomical_twilight_begin":"3:00:06 AM","astronomical_twilight_end":"8:28:22 PM"},"status":"OK"}
        
        String officialSunrise = calc.getOfficialSunriseForDate(cal);
  //      String officialSunris = calc.getOfficialSunriseForDate(now());
        
            LOG.info("officialSunrise cal = " + officialSunrise);
        Location loc = new Location("22.56", "88.36");    
            SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(loc, "Asia/Kolkata"); 

 
         Calendar calendar = Calendar.getInstance();
         calendar.set(2014, 12, 15); 
         String officialSunriseForDate = calculator.getOfficialSunriseForDate(calendar); 
         LOG.info("officialSunriseForDate = " + officialSunriseForDate);
            
            
            
            
        Calendar officialSunset = calc.getOfficialSunsetCalendarForDate(Calendar.getInstance());
            LOG.info("officialSunset = " + officialSunset);
        
        //Asia/Kuala_Lumpur +8
        ZoneId defaultZoneId = ZoneId.systemDefault();
        LOG.info("System Default TimeZone : " + defaultZoneId);

        //toString() append +8 automatically.
         Date date3 = new Date();
        LOG.info("date : " + date2);

        //1. Convert Date -> Instant
         instant = date3.toInstant();
        LOG.info("instant : " + instant); //Zone : UTC+0

        //2. Instant + system default time zone + toLocalDate() = LocalDate
         localDate = instant.atZone(defaultZoneId).toLocalDate();
        LOG.info("localDate : " + localDate);

        //3. Instant + system default time zone + toLocalDateTime() = LocalDateTime
        LocalDateTime localDateTime = instant.atZone(defaultZoneId).toLocalDateTime();
        LOG.info("localDateTime : " + localDateTime);

        //4. Instant + system default time zone = ZonedDateTime
         zonedDateTime = instant.atZone(defaultZoneId);
        LOG.info("zonedDateTime : " + zonedDateTime);
        LOG.info("zonedDateTime offset: " + zonedDateTime.getOffset());
         */

           
    }
 
  public static void main(String[] args) throws ParseException //throws IOException
     {
try{
    SunriseSunsetCalc ssc = new SunriseSunsetCalc();
    ArrayList<String> flight = ssc.sunCalc("2017-04-09T04:39:02+00:00", "2017-04-09T18:28:53+00:00","Europe/Brussels" );
 //   LatLng latlng = new GoogleGeoApiController().findLatLng(adr);
        LOG.info("after suncalc:" );
        for (String n : flight) {
            System.out.println("from main : Flight = " + n);
        } 
  //      LOG.info("LatLng rs = :"  + latlng);
  
 		//1. Create a Date from String
//		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
//		String dateInString = "22-01-2015 10:20:56";
//		Date date = sdf.parse(dateInString);
//                SunriseSunsetCalc obj = new SunriseSunsetCalc();

		//2. Test - Convert Date to Calendar
//		LOG.info("calendar = " + calendar.getTime());

		//3. Test - Convert Calendar to Date
//		Date newDate = obj.calendarToDate(calendar);
//		LOG.info("newDate = " + newDate);
   
  
    } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }
  
   } // end main//
     
     //Convert Date to Calendar
	private Calendar dateToCalendar(Date date) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;

	}

	//Convert Calendar to Date
	private Date calendarToDate(Calendar calendar) {
		return calendar.getTime();
	}
     
}  //end class
