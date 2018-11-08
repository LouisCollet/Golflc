/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author Collet
 */
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.GregorianCalendar;
import java.util.UUID;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.MapTimeZoneCache;
import net.fortuna.ical4j.validate.ValidationException;

public class ICalendarExample2 {

 public static void main(String[] args) throws IOException, ValidationException, ParserException {
  System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
  String calFile = "mycalendar.ics";
  
  //Creating a new calendar
  net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
  calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
  calendar.getProperties().add(Version.VERSION_2_0);
  calendar.getProperties().add(CalScale.GREGORIAN);
  
  //Creating an event
  java.util.Calendar cal = java.util.Calendar.getInstance();
  cal.set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER);
  cal.set(java.util.Calendar.DAY_OF_MONTH, 25);

  VEvent christmas = new VEvent(new Date(cal.getTime()), "Christmas Day");
  // initialise as an all-day event..
//  christmas.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE);
  christmas.getProperties().add(new Uid(UUID.randomUUID().toString()));
//  UidGenerator uidGenerator = new UidGenerator("1");
//  christmas.getProperties().add(uidGenerator.generateUid());

  calendar.getComponents().add(christmas);

  //Saving an iCalendar file
  FileOutputStream fout = new FileOutputStream(calFile);

  CalendarOutputter outputter = new CalendarOutputter();
  outputter.setValidating(true);
  outputter.output(calendar, fout);
  
  //Now Parsing an iCalendar file
  FileInputStream fin = new FileInputStream(calFile);

  CalendarBuilder builder = new CalendarBuilder();

  calendar = builder.build(fin);
  
     //Iterating over a Calendar
     for (Component component : calendar.getComponents()) {
         LOG.info("Component [" + component.getName() + "]");
         
         for (Property property : component.getProperties()) {
             LOG.info("Property [" + property.getName() + ", " + property.getValue() + "]");
         }
         LOG.info("meeting ical = " + NEW_LINE + christmas);
         LOG.info("calendar = " + NEW_LINE + calendar);
     } //for
 }
 
 public boolean createvCard(final String sujet, String texte, final String  to) 
{try{
    System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
  //  MemcacheService cache = MemcacheServiceFactory.getMemcacheService("calendarICS");
  //  https://github.com/ical4j/ical4j/issues/195#issuecomment-341607240

  LOG.info("starting createvCard");
// Create a TimeZone

   CalendarBuilder builder = new CalendarBuilder();
   TimeZoneRegistry registry = builder.getRegistry();
   TimeZone timezone = registry.getTimeZone("Europe/Brussels");
   VTimeZone tz = timezone.getVTimeZone();
// LOG.info("line 02");
 // Start Date is on: April 1, 2008, 9:00 am
    java.util.Calendar startDate = new GregorianCalendar();
    startDate.setTimeZone(timezone);
    startDate.set(java.util.Calendar.MONTH, java.util.Calendar.NOVEMBER);
    startDate.set(java.util.Calendar.DAY_OF_MONTH, 5);
    startDate.set(java.util.Calendar.YEAR, 2018);
    startDate.set(java.util.Calendar.HOUR_OF_DAY, 9);
    startDate.set(java.util.Calendar.MINUTE, 0);
    startDate.set(java.util.Calendar.SECOND, 0);
// LOG.info("line 03");
 // End Date is on: April 1, 2008, 13:00
    java.util.Calendar endDate = new GregorianCalendar();
    endDate.setTimeZone(timezone);
    endDate.set(java.util.Calendar.MONTH, java.util.Calendar.NOVEMBER);
    endDate.set(java.util.Calendar.DAY_OF_MONTH, 5);
    endDate.set(java.util.Calendar.YEAR, 2018);
    endDate.set(java.util.Calendar.HOUR_OF_DAY, 12);
    endDate.set(java.util.Calendar.MINUTE, 0);	
    endDate.set(java.util.Calendar.SECOND, 0);
 //LOG.info("line 04");
// Create the event
    String eventName = "Progress Meeting";
    DateTime start = new DateTime(startDate.getTime());
    DateTime end = new DateTime(endDate.getTime());
    VEvent meeting = new VEvent(start, end, eventName);
 //LOG.info("line 05");
// add timezone info..
    meeting.getProperties().add(tz.getTimeZoneId());
 LOG.info("line 05a");
// generate unique identifier..
 //   UidGenerator ug = new UidGenerator("uidGen"); //cannot be instanciated !!
     LOG.info("line 5b");
     meeting.getProperties().add(new Uid(UUID.randomUUID().toString()));
     LOG.info("line 05c");
  //  meeting.getProperties().add(uid);
 LOG.info("line 06");
// add attendees..
    Attendee dev1 = new Attendee(URI.create("mailto:dev1@mycompany.com"));
    dev1.getParameters().add(Role.REQ_PARTICIPANT);
    dev1.getParameters().add(new Cn("Developer 1"));
    meeting.getProperties().add(dev1);
 //LOG.info("line 07");
    Attendee dev2 = new Attendee(URI.create("mailto:dev2@mycompany.com"));
    dev2.getParameters().add(Role.OPT_PARTICIPANT);
    dev2.getParameters().add(new Cn("Developer 2"));
    meeting.getProperties().add(dev2);
 //LOG.info("line 08");
// Create a calendar
    net.fortuna.ical4j.model.Calendar icsCalendar = new net.fortuna.ical4j.model.Calendar();
    icsCalendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
    icsCalendar.getProperties().add(CalScale.GREGORIAN);

 //LOG.info("line 09");
// Add the event and print
    icsCalendar.getComponents().add(meeting);
        LOG.info("icsCalendar = " + NEW_LINE + icsCalendar);
 CalendarOutputter outputter = new CalendarOutputter();
  outputter.setValidating(true);
}catch (Exception e){
    LOG.error("Error createvCard = " + e.getMessage(), e);
    throw e;
}
     
    
   // VCard vcard = new VCard(props);
    return true;
}

 
}