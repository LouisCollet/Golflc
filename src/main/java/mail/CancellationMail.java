
package mail;

import entite.Club;
import entite.Course;
import entite.composite.ECourseList;
import entite.Player;
import entite.Round;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.mail.MessagingException;
import utils.LCUtil;

public class CancellationMail {
    final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 public Boolean dispatch(List<ECourseList> ecl) throws MessagingException, Exception {
         
 try{
     LOG.debug("entering ... " + CLASSNAME );
     // boucler sur une liste
     LOG.debug("nombe de cancellations = " + ecl.size());
         for(int i = 0; i < ecl.size(); i++) {
                LOG.debug("i = " + i);
            String clubName = ecl.get(i).getClub().getClubName();
                LOG.debug("courseName  = " + clubName);    
            String courseName = ecl.get(i).getCourse().getCourseName();
                LOG.debug("courseName  = " + courseName);
            String roundDate = ecl.get(i).getRound().getRoundDate().toString();
                LOG.debug("roundDate  = " + roundDate);
            String playerName = ecl.get(i).getPlayer().getPlayerLastName();
                LOG.debug("playerName  = " + playerName);
            // à compléter invitedby (second usage of player)
            sendMail(ecl.get(i).getPlayer(), ecl.get(i).getPlayer(), ecl.get(i).getRound(),
                    ecl.get(i).getClub(),ecl.get(i).getCourse());
        }
                  return true;       
}catch (Exception ex){
    String msg = "Exception in " + CLASSNAME + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}
} //end method
    public Boolean sendMail(Player player, Player invitedBy, Round round, Club club,Course course ) throws MessagingException, Exception {
{
        LOG.debug("entering sendMail in CancellattionMail");
        String sujet = "Your Round Cancellation via GolfLC";
                String Smail = 
                  " <br/>Cancellation notification - GolfLC!"
            
                + " <br/> Round Game   = " + round.getRoundGame()
                + " <br/> Round Date   = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                + " <br/> Autres participants connus  : " + round.getPlayersString()
                + " <br/> Course Name  = " + course.getCourseName()
                + " <br/> Club Name    = " + club.getClubName()
                + " <br/> Club City    = " + club.getAddress().getCity()
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/><b>Invited by     = </b>" + invitedBy.getPlayerLastName() + ", " + invitedBy.getPlayerFirstName()
 + " <br/>En utilisant le fichier .ics attaché à ce message, vous pouvez ajouter ce rendez-vous à votre agenda"
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                    ; 
                String to = "louis.collet@skynet.be";
         // insérer ici le ics
                  Path pathICS = new utils.IcalGenerator().create(player, invitedBy, round, club, course, true);
                    LOG.debug("fileoutputstrean fos = " + pathICS);
         // a faire tester si pas null ??
                Path pathQRC = null;
                boolean b = new mail.SendEmail().sendHtmlMail(sujet,Smail,to,
                        pathICS, pathQRC,
                        player.getPlayerLanguage());
                    LOG.debug("HTML Mail status = " + b);
return b;
}
    }
} // end class