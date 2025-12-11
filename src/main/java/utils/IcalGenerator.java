
package utils;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import entite.Settings;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.*;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class IcalGenerator {    // version 4.0.0 on 28-06-2024

public Path create(Player player,
          Player invitedBy,
          Round round,
          Club club,
          Course course, 
          boolean isMeetingInvite ){ //throws MessagingException, Exception {
  try{
          LOG.debug("entering IcalGeneratorv4.create using version 4.0.0");
// https://ical4j.github.io/2024/06/19/ical4j-v4-released.html 
// https://javadoc.io/doc/org.mnode.ical4j/ical4j/latest/ical4j.core/module-summary.html

// enlevé 23-11-2024     System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());  pouir ancienne version
  //  https://github.com/ical4j/ical4j/issues/195#issuecomment-341607240
// https://www.programcreek.com/java-api-examples/?api=net.fortuna.ical4j.model.property.Status
// https://ical4j.github.io/2019/06/18/ical4j-4-preview.html
  //  LOG.debug("club = " + club.toString());  
  //  LOG.debug("round = " + round.toString());
  //  LOG.debug("course = " + club.toString());  
  //  LOG.debug("player = " + player); 
  //  LOG.debug("invitedBy = " + invitedBy); 
  //  LOG.debug("isMeetingInvite = " + isMeetingInvite); 
    
   round.getPlayers().forEach(item -> LOG.debug("existing players - round.getPlayers = " + item + "/"));
    LOG.debug("list invitedBy DroppedPlayers = " + invitedBy.getDroppedPlayers());
// invitedBy.getDroppedPlayers().forEach(item -> LOG.debug("invitedBy : List new Players " + item + "/"));

// Date endDate = new Date(Date.from(LocalDateTime.from(startDate.toInstant().atZone(ZoneId.of("UTC"))).plusDays(1).toInstant(ZoneOffset.UTC)));
   String hostEmail = "louis.collet@skynet.be";
    // https://github.com/ical4j/ical4j/wiki/DateTime
   // http://ical4j.github.io/docs/ical4j/api/3.0.4/

   /*
   VEvent meeting = new VEvent(start, end, eventName)
    .withProperty(tz.getTimeZoneId())
    .withProperty(ug.generateUid())
    .withProperty(
        new Attendee(URI.create("mailto:dev1@mycompany.com"))
            .withParameter(Role.REQ_PARTICIPANT)
            .withParameter(new Cn("Developer 1").getFluentTarget())
        .getFluentTarget())
    .withProperty(
        new Attendee(URI.create("mailto:dev2@mycompany.com"))
            .withParameter(Role.OPT_PARTICIPANT)
            .withParameter(new Cn("Developer 2").getFluentTarget())
        .getFluentTarget())
    .getFluentTarget();

net.fortuna.ical4j.model.Calendar icsCalendar = new net.fortuna.ical4j.model.Calendar()
    .withProdId("-//Events Calendar//iCal4j 1.0//EN")
    .withDefaults()
    .withComponent(meeting)
    .getFluentTarget();

System.out.println(icsCalendar);
   
   */
     CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true); // new 09-12-2018
     Calendar calendar = new Calendar();
     calendar.add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
     calendar.add(ImmutableVersion.VERSION_2_0);
     calendar.add(ImmutableCalScale.GREGORIAN);
     TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
     VTimeZone tz = registry.getTimeZone(club.getAddress().getZoneId()).getVTimeZone(); // new 12-11-2024
     calendar.add(tz);
     String AT = "";
    if(isMeetingInvite){
        AT = "Réservation";
    }else{
        AT = "Cancellation";
    }
    String eventName = "Golf Round " + AT + " : " + club.getClubName() + " - " + course.getCourseName();
    VEvent event = new VEvent(round.getRoundDate(), Duration.ofHours(5), eventName); // 5 heures = le temps estimé d'une partie de golf
        event.add(new Location(club.getClubName()       
               + ", " 
               + club.getAddress().getStreet()
               + ", "
               + club.getAddress().getCity()));
        event.add(new Description(round.getRoundGame() + " "     
               + course.getCourseName()
               + NEW_LINE + "Les autres joueurs du flight sont : "
               + round.getPlayersString()));

    UidGenerator ug = new RandomUidGenerator();
        event.add(ug.generateUid());
        event.add(new Priority(5));
        event.add(new Sequence("1"));  // sert à quoi ??
        
    if(isMeetingInvite){
         event.add(new Status.Factory().createProperty(Status.VALUE_TENTATIVE));  // mod 23-11-2024
    }else{
         event.add(new Status.Factory().createProperty(Status.VALUE_CANCELLED)) ;
    }
  /* boucler ici sur la table des players inscrits
    List<Player> l = invitedBy.getDroppedPlayers();
    for(int i = 0; i < l.size(); i++){
         LOG.debug("new player Last Name = " + l.get(i).getPlayerEmail());
     Attendee a = new Attendee(URI.create("mailto:" + l.get(i).getPlayerEmail()));
         a.getParameters().add(Role.REQ_PARTICIPANT);
         a.getParameters().add(new Cn("To ... Joueur : " + l.get(i).getPlayerLastName()));
     e.getProperties().add(a);
    }
  */
    Attendee a = new Attendee(URI.create("mailto:" + player.getPlayerEmail()));
         a.add(Role.REQ_PARTICIPANT);
         a.add(PartStat.NEEDS_ACTION);
         a.add(Rsvp.TRUE);
         a.add(new Cn("To ... Joueur : " + player.getPlayerLastName()));
    event.add(a);
/* test purpose only
       Attendee a1 = new Attendee(URI.create("mailto:jigarp@vervesys.com"));
         a1.getParameters().add(Role.OPT_PARTICIPANT);
         a1.getParameters().add(new Cn("For Jigar P"));
        e.getProperties().add(a1);
*/
     Organizer o = new Organizer(URI.create("mailto:" + hostEmail));
         o.add(Role.CHAIR);
         o.add(new Cn("Louis Collet"));
         o.add(new SentBy(URI.create(hostEmail)));
     event.add(o);
     
     /* new 25-11-2024
     /Outlook uses a custom property to display HTML called the X-ALT-DESC property

    ParameterList htmlParameters = new ParameterList();
    XParameter fmtTypeParameter = new XParameter("FMTTYPE", "text/html");
    htmlParameters.add(fmtTypeParameter);
     
     String html = "<font color=#ff0000>You will this description instead of the text version if you have Outlook!</font>";
     XProperty htmlProp = new XProperty("X-ALT-DESC", htmlParameters, html);
     event.add(htmlProp);

    /*  Attaching binary data
    FileInputStream fin = new FileInputStream("c:/golf_image.jpg"); //test only
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    for (int i = fin.read(); i >= 0;) {
      bout.write(i);
      i = fin.read();
    }
    ParameterList params = new ParameterList();
    params.add(Value.BINARY);
    params.add(Encoding.BASE64);
    Attach attach = new Attach(params, bout.toByteArray()); 
    event.add(attach);
     
     LOG.debug("attach done !");
*/
     calendar.add(event);
  // http://tutorials.jenkov.com/java-io/fileoutputstream.html
//    String tmpdir = System.getProperty("java.io.tmpdir");
    Path temp = Files.createTempFile("Ical", ".ics");  // préfixe, suffixe = format Ical5357271663881144425.ics 
        LOG.debug(".ics file = " + temp);
    CalendarOutputter outputter = new CalendarOutputter();
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
          outputter.output(calendar, baos); 
          Files.write(temp, baos.toByteArray());
          //        LOG.debug("calendar file = " + calendar);
          // new 23-11-2024
          //      LOG.debug(String.format("%,d bytes", Files.size(temp)));
          //      LOG.debug(String.format("%,d kilobytes", Files.size(temp) / 1024));
      }
        LOG.debug("exiting icalGenerator with ics file = " + temp.toString());    
    return temp;
} catch (Exception e) {
            String msg = "£££ Exception in IcalGenerator.create = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
} //end method 

 public static void main(String args[]) throws Exception, SQLException {
      Connection conn = new DBConnection().getConnection();
  try{
      Player player = new Player();
      player.setIdplayer(456783);  // muntingh
      player.setPlayerLastName("Muntingh");
      player.setPlayerLanguage("fr");
      player.setPlayerEmail("theo.muntingh@skynet.be");
      Player player2 = new Player();
      player2.setIdplayer(2014101);  // muntingh
      Player player3 = new Player();
      player3.setIdplayer(2014102);  
      ArrayList<Player> p = new ArrayList<>();   // transform player2 in list<player<    
      p.add(player2);
      p.add(player3);
      player.setDroppedPlayers(p);
 
      Player invitedBy = new Player();
      invitedBy.setIdplayer(324713);
      player.setPlayerLastName("Collet");

      Club club = new Club();
      club.setIdclub(108);  //rigenée
      club = new read.ReadClub().read(club, conn);
      Course course = new Course();
      Round round = new Round(); 
      round.setRoundDate(LocalDateTime.of(2018, Month.NOVEMBER, 17, 12, 15));
      round.setRoundGame("round game : STABLEFORD");
      round.setPlayersString("inscrits précédemment : Corstjens, Bauer");
      Path icsFile = new IcalGenerator().create(player, invitedBy, round, club, course, true);  // true = STATUS:TENTATIVE, false = cancel
         LOG.debug("icsFile = " + icsFile.toString());
         long bytes = Files.size(icsFile);
            LOG.debug(String.format("%,d bytes", bytes));
            LOG.debug(String.format("%,d kilobytes", bytes / 1024));

    String to = "louis.collet@skynet.be";
    Path pathQR = null;
    // not working needs Settings !
 //   Settings.init();
    if(new mail.SendEmail().sendHtmlMail("this is the subjet","this is the mail",to,icsFile,pathQR,player.getPlayerLanguage())){
   //           LOG.debug("HTML Mail status = " + b);
             String msg = "Vous allez recevoir un mail de confirmation de votre inscription ";
             LOG.info(msg);
             showMessageInfo(msg);
       //      return true;
          }else{
            String msg = "mail de confirmation NOT sent !!";
            LOG.error(msg);
            showMessageFatal(msg);
    }
   } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main//
} // end class