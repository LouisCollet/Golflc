package mail;

import entite.Creditcard;
import entite.Lesson;
import entite.Player;
import entite.Professional;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class LessonMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private MailSender mailSender;
    @Inject private manager.PlayerManager playerManager;
    @Inject private entite.Settings settings;

    public LessonMail() { }

    public Boolean sendPaymentConfirmation(Player student, Professional professional,
            List<Lesson> lessons, Creditcard creditcard) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Player pro = playerManager.readPlayer(professional.getProPlayerId());

            String clubName = !lessons.isEmpty() ? lessons.get(0).getEventClubName() : "";
            String proName  = !lessons.isEmpty() ? lessons.get(0).getProName() : "";
            String proEmail = (pro != null && pro.getPlayerEmail() != null) ? pro.getPlayerEmail() : "";

            StringBuilder lignes = new StringBuilder();
            for (Lesson lesson : lessons) {
                String start = lesson.getEventStartDate() != null ? lesson.getEventStartDate().format(ZDF_TIME_HHmm) : "?";
                String end   = lesson.getEventEndDate()   != null ? lesson.getEventEndDate().format(ZDF_TIME_HHmm)   : "?";
                lignes.append("<tr><td>🎓 ").append(lesson.getEventTitle()).append("</td>")
                      .append("<td style='color:#555'>").append(start).append(" → ").append(end).append("</td>")
                      .append("<td align='right'>")
                      .append(String.format("%.2f %s", lesson.getLessonAmount() != null ? lesson.getLessonAmount() : 0.0,
                              creditcard.getCreditcardCurrency() != null ? creditcard.getCreditcardCurrency() : "€"))
                      .append("</td></tr>");
            }

            String mail = "<html><body style='font-family:Arial,sans-serif;max-width:600px'>"
                + "<h2>🎓 Confirmation de paiement leçon — GolfLC</h2>"
                + "<p>" + LocalDateTime.now().format(ZDF_TIME) + "</p>"
                + "<p><b>" + student.getPlayerFirstName() + " " + student.getPlayerLastName() + "</b>"
                + (student.getPlayerEmail() != null ? "<br/><span style='color:#555'>" + student.getPlayerEmail() + "</span>" : "") + "</p>"
                + "<p>Professionnel : <b>" + proName + "</b>"
                + (proEmail.isEmpty() ? "" : " — " + proEmail) + "</p>"
                + "<p>Club : <b>" + clubName + "</b></p>"
                + "<hr/>"
                + "<table style='min-width:400px;border-collapse:collapse'>"
                + lignes
                + "<tr><td colspan='3'><hr/></td></tr>"
                + "<tr><td colspan='2'><b>Total</b></td>"
                + "<td align='right'><b>" + String.format("%.2f %s", creditcard.getTotalPrice(),
                        creditcard.getCreditcardCurrency() != null ? creditcard.getCreditcardCurrency() : "€") + "</b></td></tr>"
                + "</table>"
                + "<hr/>"
                + "<p>" + creditcard.getCreditcardIssuer() + " " + creditcard.getCreditCardNumberSecret() + "</p>"
                + "<p>Référence : " + creditcard.getCreditcardPaymentReference() + "</p>"
                + "<br/><p>Merci !<br/>L'équipe GolfLC</p>"
                + "</body></html>";

            String subject = "🎓 Confirmation leçon — GolfLC";
            String to = settings.getProperty("SMTP_USERNAME");
            mailSender.sendHtmlMailAsync(subject, mail, to, null, student.getPlayerLanguage());
            LOG.info("lesson payment confirmation mail enqueued for {}", to);
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean sendProNotification(Player student, Professional professional,
            List<Lesson> lessons, Creditcard creditcard) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Player pro = playerManager.readPlayer(professional.getProPlayerId());
            if (pro == null || pro.getPlayerEmail() == null) {
                LOG.warn("pro player not found or no email for proPlayerId={}", professional.getProPlayerId());
                return false;
            }

            String clubName = !lessons.isEmpty() ? lessons.get(0).getEventClubName() : "";

            StringBuilder lignes = new StringBuilder();
            for (Lesson lesson : lessons) {
                String start = lesson.getEventStartDate() != null ? lesson.getEventStartDate().format(ZDF_TIME_HHmm) : "?";
                String end   = lesson.getEventEndDate()   != null ? lesson.getEventEndDate().format(ZDF_TIME_HHmm)   : "?";
                lignes.append("<tr><td>🎓 ").append(lesson.getEventTitle()).append("</td>")
                      .append("<td style='color:#555'>").append(start).append(" → ").append(end).append("</td></tr>");
            }

            String mail = "<html><body style='font-family:Arial,sans-serif;max-width:600px'>"
                + "<h2>📬 Nouvelle réservation de leçon — GolfLC</h2>"
                + "<p>" + LocalDateTime.now().format(ZDF_TIME) + "</p>"
                + "<p>Club : <b>" + clubName + "</b></p>"
                + "<hr/>"
                + "<p><b>Étudiant :</b> " + student.getPlayerFirstName() + " " + student.getPlayerLastName()
                + (student.getPlayerEmail() != null ? " — <a href='mailto:" + student.getPlayerEmail() + "'>" + student.getPlayerEmail() + "</a>" : "") + "</p>"
                + "<table style='min-width:360px;border-collapse:collapse'>"
                + lignes
                + "</table>"
                + "<hr/>"
                + "<p>Montant payé : <b>" + String.format("%.2f %s", creditcard.getTotalPrice(),
                        creditcard.getCreditcardCurrency() != null ? creditcard.getCreditcardCurrency() : "€") + "</b></p>"
                + "<p>Référence : " + creditcard.getCreditcardPaymentReference() + "</p>"
                + "<br/><p>L'équipe GolfLC</p>"
                + "</body></html>";

            String subject = "📬 Nouvelle leçon — " + student.getPlayerFirstName() + " " + student.getPlayerLastName();
            String to = settings.getProperty("SMTP_USERNAME");
            mailSender.sendHtmlMailAsync(subject, mail, to, null, pro.getPlayerLanguage());
            LOG.info("pro notification mail enqueued for {}", to);
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // tests locaux
    } // end main
    */

} // end class
