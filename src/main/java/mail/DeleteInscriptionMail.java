package mail;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import java.io.FileOutputStream;
import javax.mail.MessagingException;

public class DeleteInscriptionMail {

    public Boolean sendMail(Player player, Round round, Club club, Course course) throws MessagingException, Exception {
        LOG.info("entering sendInscriptionMail");
    String sujet = "Cancellation of your Round Inscription in GolfLC";
    String mail =
                  " <br/>Annulation Confirmation - GolfLC!"
                + " <br/>" + SDF_TIME.format(new java.util.Date() )
                + " <br/> Round Game   = " + round.getRoundGame()
                + " <br/> Round Date   = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                + " <br/> Course Name  = " + course.getCourseName()
                + " <br/> Club Name    = " + club.getClubName()
                + " <br/> Club City    = " + club.getClubCity()
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getPlayerCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 

    mail.InscriptionICS im = new mail.InscriptionICS();
    FileOutputStream fos = im.createInscriptionMailICS(player, player, round, club, course, false); // 2e player remplace invitedBy
        LOG.info("fileoutputstrean fos = " + fos.toString());

    String to = "louis.collet@skynet.be";
    utils.SendEmail sm = new utils.SendEmail();
    boolean b = sm.sendHtmlMail(sujet,mail,to,"INSCRIPTION");
       LOG.info("HTML Mail status = " + b);
return b;
} //en method
} // end class