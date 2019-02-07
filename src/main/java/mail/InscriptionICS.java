
package mail;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;
import javax.mail.MessagingException;
import static lc.golfnew.Constants.images_library;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.*;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.util.MapTimeZoneCache;
import static utils.LCUtil.asUtilDate;
import static utils.LCUtil.showMessageFatal;
public class InscriptionICS {

  public FileOutputStream createInscriptionMailICS(Player player, Player invitedBy,
          Round round, Club club, Course course, boolean isMeetingInvite ) throws MessagingException, Exception {
  try{
          LOG.info("entering createInscriptionMailICS");
    System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
  //  MemcacheService cache = MemcacheServiceFactory.getMemcacheService("calendarICS");
  
  //  https://github.com/ical4j/ical4j/issues/195#issuecomment-341607240

//  LOG.info("starting createvCard");En utilisant le fichier attaché à ce message, vous pouvez ajouter ce rendez-vous`à votre agenda
// LOG.info("club = " + club.toString());  
// LOG.info("round = " + round.toString());
//LOG.info("course = " + club.toString());  
    LOG.info("idplayer = " + player.toString()); 
    LOG.info("invitedBy = " + invitedBy.toString()); 
    LOG.info("round getPlayersString = " + round.getPlayersString());
round.getPlayers().forEach(item -> LOG.info("existing players - round.getPlayers = " + item + "/")); // java 8 lambda
    LOG.info("list invitedBy DroppedPlayers = " + invitedBy.getDroppedPlayers());
invitedBy.getDroppedPlayers().forEach(item -> LOG.info("invitedBy : List new Players " + item + "/")); // java 8 lambda
//LOG.info("line 01");

   String hostEmail = "louis.collet@skynet.be";

   // http://ical4j.github.io/docs/ical4j/api/2.0.0/net/fortuna/ical4j/model/DateTime.html
   // https://github.com/ical4j/ical4j/wiki/DateTime
     //Creating a new calendar
     CalendarBuilder builder = new CalendarBuilder();
     TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
     VTimeZone tz = registry.getTimeZone("Europe/Brussels").getVTimeZone();
     
     CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true); // new 09-12-018
// LOG.info("line 02");
     Calendar c = new Calendar();
     c.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
     c.getProperties().add(Version.VERSION_2_0);
     c.getProperties().add(CalScale.GREGORIAN);
     c.getComponents().add(tz);
// LOG.info("line 03");


    java.util.Date rd = asUtilDate(round.getRoundDate(), ZoneId.of("Europe/Brussels"));
        LOG.info("roundDate converted from localdatetime = " + rd);
    DateTime start = new DateTime(rd); // format ical4j
        LOG.info("round dte format ical4j = " + start);
    Dur duration = new Dur(0,5,0,0);   // 5 heures le temps estimé d'une partie de golf
    String AT = "";
    if(isMeetingInvite){
        AT = "Réservation";
    }else{
        AT = "Cancellation";
    }
    String eventName = "Golf Round " + AT + " : " + club.getClubName() + " - " + course.getCourseName();
    VEvent e = new VEvent(start, duration, eventName);  // also start, end, eventName
// add timezone information..
    e.getProperties().add(new Location(club.getClubName() + ", " + club.getClubAddress() + ", " + club.getClubCity()));
    e.getProperties().add(new Description(round.getRoundGame() + " " + course.getCourseName()
                            + " Les autres participants sont : " + round.getPlayersString()));
    e.getProperties().add(new Uid(UUID.randomUUID().toString()));
//  e.getProperties().add(new Organizer("MAILTO:" + hostEmail));
    e.getProperties().add(new Name("this is a new Name"));   //sert à quoi ?
    e.getProperties().add(tz.getTimeZoneId());
    
  //  e.getProperties().add(new Cn("To ... Joueur : " + player.getPlayerLastName()));

   e.getProperties().add(new Sequence("1"));
   e.getProperties().removeAll(e.getProperties(Property.STATUS)); // You can only have one status so make sure we remove any previous ones.
   if (isMeetingInvite){
     e.getProperties().add(Status.VEVENT_TENTATIVE);
   }else{
     e.getProperties().add(Status.VEVENT_CANCELLED);
 }
    
  /* boucler ici sur la table des players inscrits
    List<Player> l = invitedBy.getDroppedPlayers();
    for(int i = 0; i < l.size(); i++)
    {
         LOG.info("new player Last Name = " + l.get(i).getPlayerEmail());
     Attendee a = new Attendee(URI.create("mailto:" + l.get(i).getPlayerEmail()));
         a.getParameters().add(Role.REQ_PARTICIPANT);
         a.getParameters().add(new Cn("To ... Joueur : " + l.get(i).getPlayerLastName()));
     e.getProperties().add(a);
    }
    */
     Attendee a = new Attendee(URI.create("mailto:" + player.getPlayerEmail()));
         a.getParameters().add(Role.REQ_PARTICIPANT);
         a.getParameters().add(new Cn("To ... Joueur : " + player.getPlayerLastName()));
     e.getProperties().add(a);
    
    Organizer o = new Organizer(URI.create("mailto:" + "louis.collet@skynet.be"));
         o.getParameters().add(Role.CHAIR);
         o.getParameters().add(new Cn("Louis Collet"));
         o.getParameters().add(new SentBy(URI.create("louis.collet@skynet.be")));
    e.getProperties().add(o);

// ajout du composant au calendrier
    c.getComponents().add(e);
  //Saving an iCalendar file c'est pas bon : va être envoyé par sendEmail si mail : "INSCRIPTION"
  // à améliorer !!
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
    LOG.info("line 102");
//Attaching binary data
        FileInputStream bin = new FileInputStream(images_library + "calendar.png");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int i = fin.read(); i >= 0;) {
             bout.write(i);
             i = bin.read();
        }
        ParameterList params = new ParameterList();
        params.add(Value.BINARY);
        params.add(Encoding.BASE64);
    LOG.info("line 103");
    //    Attach attach = new Attach(params, bout.toByteArray());
        e.getProperties().add(new Attach(params, bout.toByteArray()));
    LOG.info("line 104");
    LOG.info("binary attach + "); // + attach);
 //       c = builder.build(bin);   provoque error

    LOG.info("line 105");    
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
      player.setIdplayer(456783);  // muntingh
      player.setPlayerLastName("Muntingh");
      player.setPlayerEmail("theo.muntingh@skynet.be");
      Player player2 = new Player();
      player2.setIdplayer(2014101);  // muntingh
      Player player3 = new Player();
      player3.setIdplayer(2014102);  
      ArrayList<Player> p = new ArrayList<>();   // transform player2 in list<player<    
      p.add(player2); //.setDroppedPlayers(player)).;
      p.add(player3);
      player.setDroppedPlayers(p);
      
      Player invitedBy = new Player();
      invitedBy.setIdplayer(324713);
      player.setPlayerLastName("Collet");
      Club club = new Club();
      club.setClubName("Cabopino");
      Course course = new Course();
      Round round = new Round(); 
      round.setRoundDate(LocalDateTime.of(2018, Month.NOVEMBER, 17, 12, 15));
      round.setRoundGame("round game : STABLEFORD");
      round.setPlayersString("inscrits précédemment : Corstjens, Bauer");

    InscriptionICS ics = new InscriptionICS();
    ics.createInscriptionMailICS(player, invitedBy, round, club, course, false);  // true = tentative, false = cancel
    
    String to = "louis.collet@skynet.be";
    utils.SendEmail sm = new utils.SendEmail();
    boolean b = sm.sendHtmlMail("sujet de test","message du mail",to,"INSCRIPTION");
       LOG.info("HTML Mail status = " + b);
 //   DBConnection dbc = new DBConnection();
 //  Connection conn = dbc.getConnection();
   
//   String tz = "Europe/Brussels";

 // ArrayList<Flight> fl = findSunriseSunset(date,"50.202764", "5.013203",tz, conn);
  //         LOG.info("response in main = :"  + fl);
   } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }
  
   } // end main//

  
  
  
} // end class
