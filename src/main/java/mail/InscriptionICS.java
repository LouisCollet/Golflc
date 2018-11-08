
package mail;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import javax.mail.MessagingException;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.SentBy;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Name;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.MapTimeZoneCache;
import static utils.LCUtil.asUtilDate;
import static utils.LCUtil.showMessageFatal;
public class InscriptionICS {

  public FileOutputStream createInscriptionMailICS(Player player, Player invitedBy, Round round, Club club,Course course ) throws MessagingException, Exception {
  try{
          LOG.info("entering createInscriptionMailICS");
    System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
  //  MemcacheService cache = MemcacheServiceFactory.getMemcacheService("calendarICS");
  //  https://github.com/ical4j/ical4j/issues/195#issuecomment-341607240

//  LOG.info("starting createvCard");En utilisant le fichier attaché à ce message, vous pouvez ajouter ce rendez-vous`à votre agenda
// LOG.info("club = " + club.toString());  
// LOG.info("round = " + round.toString());
//LOG.info("course = " + club.toString());  
    LOG.info("idplayer = " + player.getIdplayer()); 
    LOG.info("invitedBy = " + invitedBy.getIdplayer()); 
    LOG.info("round getPlayersString = " + round.getPlayersString());
round.getPlayers().forEach(item -> LOG.info("existing players - round.getPlayers = " + item + "/")); // java 8 lambda
    LOG.info("list invitedBy DroppedPlayers = " + invitedBy.getDroppedPlayers());
invitedBy.getDroppedPlayers().forEach(item -> LOG.info("invitedBy : List new Players " + item + "/")); // java 8 lambda
//LOG.info("line 01");

   String hostEmail = "louis.collet@skynet.be";
// Create a TimeZone

 //  LOG.info("line01");
   // http://ical4j.github.io/docs/ical4j/api/2.0.0/net/fortuna/ical4j/model/DateTime.html
   // https://github.com/ical4j/ical4j/wiki/DateTime
     //Creating a new calendar
     
        CalendarBuilder builder = new CalendarBuilder();
   TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
//To add a VTimeZone definition to your calendar you would do something like this: 
VTimeZone tz = registry.getTimeZone("Europe/Brussels").getVTimeZone();
///c.getComponents().add(tz);
     
  Calendar c = new Calendar();
  c.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
  c.getProperties().add(Version.VERSION_2_0);
  c.getProperties().add(CalScale.GREGORIAN);
  c.getComponents().add(tz);

 //   LOG.info("after start generation");
  java.util.Date rd = asUtilDate(round.getRoundDate(), ZoneId.of("Europe/Brussels")); // ZoneId.of("Europe/Brussels")
    LOG.info("roundDate converted from localdatetime = " + rd);
  DateTime dt = new DateTime(rd); // format ical4j
    LOG.info("round dte format ical4j = " + dt);
  
  Dur duration = new Dur(0,5,0,0);   // 5 heures 
  String eventName = "Golf Round at " + club.getClubName();
  VEvent e = new VEvent(dt,duration, eventName);
// add timezone information..


//   TimeZoneRegistry registry = builder.getRegistry();
//   TimeZone timezone = registry.getTimeZone("Europe/Brussels");
//   VTimeZone tz = timezone.getVTimeZone();
   
// VTimeZone tz = VTimeZone.getDefault();
 //TzId tzParam = new TzId(tz.getProperties().getProperty(Property.TZID));
 //e.getProperties().getProperty(Property.DTSTART).getClass().getParameters().add(tzParam);
    
 ///   Recur recur = new Recur(Recur.WEEKLY,null);
 ///   RRule rule = new RRule();
 ///   e.getProperties().add(rule);
  e.getProperties().add(new Location(club.getClubName()));
  e.getProperties().add(new Description(round.getRoundGame() + " " + course.getCourseName()
                            + " joueurs inscrits précédemment : " + round.getPlayersString()));
  e.getProperties().add(new Uid(UUID.randomUUID().toString()));
//  e.getProperties().add(new Organizer("MAILTO:" + hostEmail));
  e.getProperties().add(new Name("this is a new Name"));   //sert à quoi ?
  e.getProperties().add(tz.getTimeZoneId());

  // boucler ici sur la table des players inscrits
 List<Player> l = invitedBy.getDroppedPlayers();
 for(int i = 0; i < l.size(); i++)
{
        LOG.info("new player email = " + l.get(i).getPlayerEmail());
    Attendee a = new Attendee(URI.create("mailto:" + l.get(i).getPlayerEmail()));
    a.getParameters().add(Role.REQ_PARTICIPANT);
    a.getParameters().add(new Cn("Joueur " + l.get(i).getPlayerLastName()));
    e.getProperties().add(a);
}
 
Organizer o = new Organizer(URI.create("mailto:" + "louis.collet@skynet.be"));
    o.getParameters().add(Role.CHAIR);
    o.getParameters().add(new Cn("louis collet"));
    o.getParameters().add(new SentBy(URI.create("louis.collet@skynet.be")));
e.getProperties().add(o);

// ajout du composant au calendrier
  c.getComponents().add(e);
  //Saving an iCalendar file
  String fcal = "c:\\aa (LC Data)\\GolfCalendar.ics";
  FileOutputStream fout = new FileOutputStream(fcal);
  CalendarOutputter outputter = new CalendarOutputter();
 //   LOG.info("before validation");
  outputter.setValidating(true);
  outputter.output(c, fout);
  //Now Parsing an iCalendar file
  FileInputStream fin = new FileInputStream(fcal);
//  CalendarBuilder builder = new CalendarBuilder();
  c = builder.build(fin);
//   LOG.info("meeting ical = " + NEW_LINE + e);
   LOG.info("calendar = " + NEW_LINE + c);    
return fout;
} catch (Exception e) {
            String msg = "£££ Exception in createInscriptionMailICS = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
} //end method 

 public static void main(String[] args) throws IOException {
  try{
      // not working error compilation 
      Player player = new Player();
      Player invitedBy = new Player();
      Club club = new Club();
      club.setClubName("this is the club name");
      Course course = new Course();
      Round round = new Round(); 
      round.setRoundDate(LocalDateTime.of(2018, Month.NOVEMBER, 17, 12, 15));
      round.setRoundGame(" this is the round game");
 //  Date date = SDF.parse("23/07/2018");
  // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
  //  LocalDate localDate = LocalDate.now();
  //  LOG.info(dtf.format(localDate)); //2016/11/16
    InscriptionICS ics = new InscriptionICS();
    ics.createInscriptionMailICS(player, invitedBy, round, club, course);
    
    
 //   DBConnection dbc = new DBConnection();
 //  Connection conn = dbc.getConnection();
   
   String tz = "Europe/Brussels";

 // ArrayList<Flight> fl = findSunriseSunset(date,"50.202764", "5.013203",tz, conn);
  //         LOG.info("response in main = :"  + fl);
   } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }
  
   } // end main//

  
  
  
} // end class
