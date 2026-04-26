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
            StringBuilder sb = new StringBuilder();
            sb.append(" <br/>Lesson Payment Confirmation - GolfLC!")
              .append(" <br/>").append(LocalDateTime.now().format(ZDF_TIME))
              .append(" <br/>")
              .append(" <br/><b>Student       = </b>").append(student.getPlayerFirstName()).append(" ").append(student.getPlayerLastName())
              .append(" (").append(student.getPlayerEmail()).append(")");

            if (!lessons.isEmpty()) {
                sb.append(" <br/><b>Professional  = </b>").append(lessons.get(0).getProName());
                if (pro != null && pro.getPlayerEmail() != null) {
                    sb.append(" (").append(pro.getPlayerEmail()).append(")");
                }
                sb.append(" <br/><b>Club          = </b>").append(lessons.get(0).getEventClubName())
                  .append(" <br/>");
                for (Lesson lesson : lessons) {
                    sb.append(" <br/>  🎓 ").append(lesson.getEventTitle())
                      .append(" — ").append(lesson.getEventStartDate().format(ZDF_TIME_HHmm))
                      .append(" → ").append(lesson.getEventEndDate().format(ZDF_TIME_HHmm));
                }
            }

            sb.append(" <br/>")
              .append(" <br/><b>Amount paid   = </b>").append(creditcard.getTotalPrice()).append(" ").append(creditcard.getCreditcardCurrency())
              .append(" <br/><b>Card          = </b>").append(creditcard.getCreditCardNumberSecret())
              .append(" <br/><b>Issuer        = </b>").append(creditcard.getCreditcardIssuer())
              .append(" <br/><b>Reference     = </b>").append(creditcard.getCreditcardPaymentReference())
              .append(" <br/>")
              .append(" <br/> Thank you !")
              .append(" <br/> The GolfLC team");

            String subject = "Your Lesson at GolfLC is paid!";
            String to = settings.getProperty("SMTP_USERNAME"); // TODO: switch to student.getPlayerEmail() after testing
            mailSender.sendHtmlMailAsync(subject, sb.toString(), to, null, student.getPlayerLanguage());
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

            StringBuilder sb = new StringBuilder();
            sb.append(" <br/>New Lesson Booking - GolfLC!")
              .append(" <br/>").append(LocalDateTime.now().format(ZDF_TIME))
              .append(" <br/>");

            if (!lessons.isEmpty()) {
                sb.append(" <br/><b>Professional  = </b>").append(lessons.get(0).getProName())
                  .append(" <br/><b>Club          = </b>").append(lessons.get(0).getEventClubName())
                  .append(" <br/>");
                for (Lesson lesson : lessons) {
                    sb.append(" <br/>  🎓 ").append(lesson.getEventTitle())
                      .append(" — ").append(lesson.getEventStartDate().format(ZDF_TIME_HHmm))
                      .append(" → ").append(lesson.getEventEndDate().format(ZDF_TIME_HHmm));
                }
            }

            sb.append(" <br/>")
              .append(" <br/><b>Student       = </b>").append(student.getPlayerFirstName()).append(" ").append(student.getPlayerLastName())
              .append(" <br/><b>Student email = </b>").append(student.getPlayerEmail())
              .append(" <br/>")
              .append(" <br/><b>Amount paid   = </b>").append(creditcard.getTotalPrice()).append(" ").append(creditcard.getCreditcardCurrency())
              .append(" <br/><b>Reference     = </b>").append(creditcard.getCreditcardPaymentReference())
              .append(" <br/>")
              .append(" <br/> The GolfLC team");

            String subject = "New lesson booked by " + student.getPlayerFirstName() + " " + student.getPlayerLastName();
            String to = settings.getProperty("SMTP_USERNAME"); // TODO: switch to pro.getPlayerEmail() after testing
            mailSender.sendHtmlMailAsync(subject, sb.toString(), to, null, pro.getPlayerLanguage());
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
