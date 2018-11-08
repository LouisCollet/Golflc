
package utils;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.MapTimeZoneCache;
import net.fortuna.ical4j.validate.ValidationException;

public class ICalendarExample {

 public static void main(String[] args) {
     
  System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
  //Initilize values
  String calFile = "c:\\aa (LC Data)\\TestCalendar.ics";
    /*
 java.util.Calendar cal = java.util.Calendar.getInstance();
 cal.set(java.util.Calendar.YEAR, now.getYear());
  cal.set(java.util.Calendar.MONTH, now.getMonthValue());
  cal.set(java.util.Calendar.DAY_OF_MONTH,now.getDayOfMonth());
  cal.set(java.util.Calendar.HOUR, now.getHour());
  cal.set(java.util.Calendar.MINUTE,now.getMinute());
   LOG.info("calendar cal = " + cal.toString());
  startCal.set(year,month, day, hour, minute);
  cal.set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER);
   */ 
  //start time
  java.util.Calendar startCal = java.util.Calendar.getInstance();
  startCal.set(2018, 11, 23, 20, 00);
  //end time
  java.util.Calendar endCal = java.util.Calendar.getInstance();
  endCal.set(2018, 11, 23, 20, 30);
  
  String subject = "Meeting Subject";
  String location = "Location - Brussels";
  String description = "This goes in decription section of the meeting like agenda etc.";
    String hostEmail = "admin@javaxp.com";
  //Creating a new calendar
  net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
  calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
  calendar.getProperties().add(Version.VERSION_2_0);
  calendar.getProperties().add(CalScale.GREGORIAN);
  
  SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMdd'T'hhmmss'Z'");
  String strDate = sdFormat.format(startCal.getTime());
  
  net.fortuna.ical4j.model.Date startDt = null;
  try {
   startDt = new net.fortuna.ical4j.model.Date(strDate,"yyyyMMdd'T'hhmmss'Z'");
  } catch (ParseException e) {
    e.printStackTrace();
  }
  
  long diff = endCal.getTimeInMillis() - startCal.getTimeInMillis();
  int min = (int)(diff / (1000 * 60));
  
  Dur dur = new Dur(0,0,min,0);
  
  //Creating a meeting event
  VEvent meeting = new VEvent(startDt,dur,subject);
  meeting.getProperties().add(new Location(location));
  meeting.getProperties().add(new Description(description));/// was an error
  meeting.getProperties().add(new Uid(UUID.randomUUID().toString()));
  try {
//  meeting.getProperties().getProperty(Property.DESCRIPTION).setValue(description));
  } catch (Exception e) {
   e.printStackTrace();
//  } catch (URISyntaxException e) {
//   e.printStackTrace();
//  } catch (ParseException e) {
//   e.printStackTrace();
  }
  
  try {
   meeting.getProperties().add(new Organizer("MAILTO:"+hostEmail));
  } catch (URISyntaxException e) {
   e.printStackTrace();
  }
    calendar.getComponents().add(meeting);
    
    FileOutputStream fout = null;
 try {
   fout = new FileOutputStream(calFile);
  } catch (FileNotFoundException e) {
   e.printStackTrace();
  }
  
  CalendarOutputter outputter = new CalendarOutputter();
  outputter.setValidating(true);
  
 try {
   outputter.output(calendar, fout);
  } catch (IOException e) {
   e.printStackTrace();
  } catch (ValidationException e) {
   e.printStackTrace();
  }
  
  LOG.info("meeting ical = " + NEW_LINE + meeting);
  LOG.info("calendar = " + NEW_LINE + calendar);
 }
}