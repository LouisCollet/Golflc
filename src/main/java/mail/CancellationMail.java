package mail;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import utils.LCUtil;

@ApplicationScoped
public class CancellationMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private MailSender mailSender;
    @Inject private ical.IcalService icalService;
    @Inject private entite.Settings settings;

    public CancellationMail() { }

    public Boolean dispatch(List<ECourseList> ecl) throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("nombre de cancellations = " + ecl.size());
            for (int i = 0; i < ecl.size(); i++) {
                LOG.debug("i = " + i);
                String clubName = ecl.get(i).club().getClubName();
                LOG.debug("clubName  = " + clubName);
                String courseName = ecl.get(i).course().getCourseName();
                LOG.debug("courseName  = " + courseName);
                String roundDate = ecl.get(i).round().getRoundDate().toString();
                LOG.debug("roundDate  = " + roundDate);
                String playerName = ecl.get(i).player().getPlayerLastName();
                LOG.debug("playerName  = " + playerName);
                sendMail(ecl.get(i).player(), ecl.get(i).player(), ecl.get(i).round(),
                        ecl.get(i).club(), ecl.get(i).course());
            }
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean sendMail(Player player, Player invitedBy, Round round, Club club, Course course)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
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
                + " <br/>En utilisant le fichier .ics attache a ce message, vous pouvez ajouter ce rendez-vous a votre agenda"
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME);

            String to = settings.getProperty("SMTP_USERNAME");
            byte[] pathICS = icalService.generateIcs(player, invitedBy, round, club, course, true);
            LOG.debug("pathICS = " + pathICS);
            mailSender.sendHtmlMailAsync(sujet, Smail, to, pathICS, player.getPlayerLanguage());
            LOG.debug("HTML Mail async dispatched");
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
