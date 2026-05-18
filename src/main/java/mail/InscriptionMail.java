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
    @Inject private entite.Settings settings;

    public InscriptionMail() { }

    public Boolean create(Player player, Player invitedBy, Round round, Club club, Course course)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String sujet = "✅ Confirmation d'inscription — " + club.getClubName();
            String dateStr    = round.getRoundDate() != null ? round.getRoundDate().format(ZDF_TIME_HHmm) : "?";
            String courseName = course.getCourseName() != null ? course.getCourseName() : "";
            String game       = round.getRoundGame()   != null ? round.getRoundGame()   : "";
            String players    = round.getPlayersString() != null && !round.getPlayersString().isBlank()
                                ? round.getPlayersString() : "—";
            String invitedRow = (invitedBy != null && invitedBy.getIdplayer() != player.getIdplayer())
                                ? "<tr><td>Invité par</td><td><b>"
                                  + invitedBy.getPlayerFirstName() + " " + invitedBy.getPlayerLastName()
                                  + "</b></td></tr>"
                                : "";

            String mail = "<html><body style='font-family:Arial,sans-serif;max-width:600px'>"
                + "<h2>✅ Confirmation d'inscription — GolfLC</h2>"
                + "<p>" + LocalDateTime.now().format(ZDF_TIME) + "</p>"
                + "<p><b>" + player.getPlayerFirstName() + " " + player.getPlayerLastName() + "</b><br/>"
                + club.getClubName() + " — " + club.getAddress().getCity() + "</p>"
                + "<hr/>"
                + "<table style='min-width:320px;border-collapse:collapse;line-height:1.8'>"
                + "<tr><td>Date</td><td><b>" + dateStr + "</b></td></tr>"
                + "<tr><td>Parcours</td><td><b>" + courseName + "</b></td></tr>"
                + "<tr><td>Format de jeu</td><td><b>" + game + "</b></td></tr>"
                + "<tr><td>Autres participants</td><td>" + players + "</td></tr>"
                + invitedRow
                + "</table>"
                + "<hr/>"
                + "<p style='font-size:0.9em;color:#555'>Le fichier .ics joint vous permet d'ajouter ce rendez-vous à votre agenda.</p>"
                + "<br/><p>Merci !<br/>L'équipe GolfLC</p>"
                + "</body></html>";

            String to = settings.getProperty("SMTP_USERNAME");
            byte[] pathICS = icalService.generateIcs(player, invitedBy, round, club, course, true);
            String qrContent = player.getPlayerFirstName() + " " + player.getPlayerLastName()
                + "\n" + dateStr + " — " + courseName + "\n" + club.getClubName();
            byte[] pathQRC = qrService.generateQR(qrContent, 200);
            LOG.debug("reponse de qrService = " + Arrays.toString(pathQRC));

            mailSender.sendHtmlMailAsync(sujet, mail, to, pathICS, pathQRC, player.getPlayerLanguage());
            String msg = "Vous allez recevoir un mail de confirmation de votre inscription";
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
        LOG.debug("entering {}", methodName);
        try {
            String sujet  = "❌ Annulation d'inscription — " + club.getClubName();
            String dateStr    = round.getRoundDate() != null ? round.getRoundDate().format(ZDF_TIME_HHmm) : "?";
            String courseName = course.getCourseName() != null ? course.getCourseName() : "";
            String game       = round.getRoundGame()   != null ? round.getRoundGame()   : "";

            String content = "<html><body style='font-family:Arial,sans-serif;max-width:600px'>"
                + "<h2>❌ Annulation d'inscription — GolfLC</h2>"
                + "<p>" + LocalDateTime.now().format(ZDF_TIME) + "</p>"
                + "<p><b>" + player.getPlayerFirstName() + " " + player.getPlayerLastName() + "</b><br/>"
                + club.getClubName() + " — " + club.getAddress().getCity() + "</p>"
                + "<hr/>"
                + "<table style='min-width:320px;border-collapse:collapse;line-height:1.8'>"
                + "<tr><td>Date annulée</td><td><b>" + dateStr + "</b></td></tr>"
                + "<tr><td>Parcours</td><td><b>" + courseName + "</b></td></tr>"
                + "<tr><td>Format de jeu</td><td><b>" + game + "</b></td></tr>"
                + "</table>"
                + "<hr/>"
                + "<br/><p>L'équipe GolfLC</p>"
                + "</body></html>";

            byte[] pathICS = icalService.generateIcs(player, player, round, club, course, false);
            LOG.debug("PathICS = {}", pathICS != null ? pathICS.length : 0);
            String qrContent = player.getPlayerFirstName() + " " + player.getPlayerLastName()
                + "\n" + dateStr + " — " + courseName + "\n" + club.getClubName();
            byte[] pathQRC = qrService.generateQR(qrContent, 200);

            String to = settings.getProperty("SMTP_USERNAME");
            mailSender.sendHtmlMailAsync(sujet, content, to, pathICS, pathQRC, player.getPlayerLanguage());
            LOG.debug("HTML Mail async dispatched");
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
