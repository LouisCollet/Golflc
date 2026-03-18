
package ical;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.util.MapTimeZoneCache;

public class IcalTest {    // version 4
    private static final String DEFAULT_TZ_CACHE_IMPL = "net.fortuna.ical4j.util.JCacheTimeZoneCache";
public Path create(){
  try{
        System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
     TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
     VTimeZone tz = registry.getTimeZone("Europe/Brussels").getVTimeZone(); 
     Calendar calendar = new Calendar();
   ///  calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
     calendar.add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
 ///    calendar.getProperties().add(Version.VERSION_2_0);
 ///    calendar.getProperties().add(CalScale.GREGORIAN);
     calendar.add(ImmutableVersion.VERSION_2_0);
     calendar.add(ImmutableCalScale.GREGORIAN);
     calendar.getComponents().add(tz);
     LocalDateTime start = LocalDateTime.of(2021, Month.OCTOBER, 17, 12, 15);
     String eventName = "Golf Round ";
//     VEvent e = new VEvent(start, Duration.ofHours(5), eventName);
 //    e.getProperties().add(new Location("clubname"));
 //    calendar.getComponents().add(e);
     Path temp = Files.createTempFile("Ical", ".ics");
     try (
           FileOutputStream fout = new FileOutputStream(temp.toFile()) //
     ) {
          CalendarOutputter outputter = new CalendarOutputter();
          outputter.output(calendar, fout);
     }
    return temp;
} catch (Exception e) {
            String msg = "£££ Exception in IcalGenerator.create = " + e.getMessage();
            LOG.error(msg);
            return null;
 }finally{
    System.setProperty("net.fortuna.ical4j.timezone.cache.impl", null);
}
} //end method 

 void main() throws IOException {
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
      p.add(player2);
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

    //  boolean bo = new IcalGenerator().create(player, invitedBy, round, club, course, false);  // true = tentative, false = cancel
       Path path = new IcalTest().create();
         LOG.debug("Path bo = " + path.toString());
         long bytes = Files.size(path);
            LOG.debug(String.format("%,d bytes", bytes));
            LOG.debug(String.format("%,d kilobytes", bytes / 1024));
/*
    String to = System.getenv("SMTP_USERNAME");
    Path path = null;
    boolean b = new mail.SendEmail().sendHtmlMail("sujet de test from main","message du mail",to,"INSCRIPTION",
            path, "en");
       LOG.debug("HTML Mail status = " + b);
   */    
       
   } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }
   } // end main//
} // end class