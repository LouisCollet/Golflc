package mail;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class InscriptionMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private utils.QRCodeService qrService;
    @Inject private MailSender mailSender;
    @Inject private ical.IcalService icalService;

    public InscriptionMail() { }

    public Boolean create(Player player, Player invitedBy, Round round, Club club, Course course)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            String sujet = "Your Round Inscription via GolfLC";
            String mail =
                  " <br/>Inscription Confirmation - GolfLC!"
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
                + " <br/>En utilisant le fichier .ics attache a ce message, vous pouvez ajouter ce rendez-vous a votre agenda"
                + " <br/> Thank you !"
                + " <br/> The GolfLC team";

            String to = System.getenv("SMTP_USERNAME");
            byte[] pathICS = icalService.generateIcs(player, invitedBy, round, club, course, true);
            String content = mail.replaceAll("<br/>", "\n").replaceAll("<b>", "").replaceAll("</b>", "");
            byte[] pathQRC = qrService.generateQR(content, 200);
            LOG.debug("reponse de qrService = " + Arrays.toString(pathQRC));

            mailSender.sendHtmlMailAsync(sujet, mail, to, pathICS, pathQRC, player.getPlayerLanguage());
            String msg = "Vous allez recevoir un mail de confirmation de votre inscription ";
            LOG.info(msg);
            showMessageInfo(msg);
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean delete(Player player, Round round, Club club, Course course)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            String sujet = "Cancellation of your Round Inscription in GolfLC";
            String content =
                  " <br/>Annulation Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Round Game   = " + round.getRoundGame()
                + " <br/> Round Date   = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                + " <br/> Course Name  = " + course.getCourseName()
                + " <br/> Club Name    = " + club.getClubName()
                + " <br/> Club City    = " + club.getAddress().getCity()
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team";

            byte[] pathICS = icalService.generateIcs(player, player, round, club, course, false);
            LOG.debug("PathICS = " + pathICS);
            byte[] pathQRC = qrService.generateQR(content, 200);
            LOG.debug("reponse de qrService = " + pathQRC.toString());

            String to = System.getenv("SMTP_USERNAME");
            mailSender.sendHtmlMailAsync(sujet, content, to, pathICS, pathQRC, player.getPlayerLanguage());
            LOG.debug("HTML Mail async dispatched");
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
