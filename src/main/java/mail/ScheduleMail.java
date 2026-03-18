package mail;

// import Controllers.LanguageController; // removed — fix multi-user 2026-03-07
import entite.Club;
import entite.Creditcard;
import entite.Greenfee;
import entite.Player;
import entite.Professional;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_DAY;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import manager.PlayerManager;

@RequestScoped
public class ScheduleMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private PlayerManager playerManager;
    @Inject private MailSender mailSender;
    @Inject private read.ReadClub readClubService;

    public ScheduleMail() { }

    public void createLesson(Creditcard creditcard, Professional professional, entite.Lesson lesson)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with creditcard = " + creditcard);
        LOG.debug(" with professional = " + professional);
        LOG.debug(" with lesson = " + lesson);
        try {
            Player giver = new Player();
            giver.setIdplayer(professional.getProPlayerId());
            giver = playerManager.readPlayer(giver.getIdplayer());

            Player taker = new Player();
            taker.setIdplayer(lesson.getEventPlayerId());
            taker = playerManager.readPlayer(taker.getIdplayer());

            Club c = new Club();
            c.setIdclub(professional.getProClubId());
            c = readClubService.read(c);

            // mail 1 — to lesson taker
            String sujet = "Lesson taker : Confirmation of your reservation lesson via GolfLC";
            String mail =
                  " <br/><b>Nous avons recu le paiement suivant :</b>"
                + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant paye = " + creditcard.getTotalPrice()
                + " <br/> Titulaire  = " + creditcard.getCreditcardHolder()
                + " <br/> Communication  = " + creditcard.getCommunication()
                + " <br/> Reference  = " + creditcard.getCreditcardPaymentReference()
                + "<br/><br/><b>Concernant la lecon de golf suivante :</b>"
                + " <br/> Club = " + professional.getProClubId()
                + " <br/> Nom du Club = " + c.getClubName()
                + " <br/> Heure de debut = " + lesson.getEventStartDate().format(ZDF_TIME_HHmm)
                + " <br/> Heure de fin = " + lesson.getEventEndDate().format(ZDF_TIME_HHmm)
                + " <br/> Professional = " + professional.getProPlayerId()
                + " <br/>Nom  = " + giver.getPlayerLastName()
                + " <br/><br/> Thank you !"
                + " <br/> The GolfLC team"
                + "<br/><b>En cas d'empechement ou pour toute autre raison, vous pouvez contacter le pro </b>:"
                + " <br/>Email de votre pro" + "pro email"
                + "<br/><br/> envoye le : " + LocalDateTime.now().format(ZDF_TIME);

            String to = System.getenv("SMTP_USERNAME");
            byte[] pathQRC = null;
            mailSender.sendHtmlMailAsync(sujet, mail, to, pathQRC, taker.getPlayerLanguage());
            LOG.debug("mail async dispatched for lesson taker");

            // mail 2 — to lesson giver
            sujet = "Lesson giver : Confirmation of a lesson via GolfLC";
            mail =
                  "<br/><br/><b>Concernant la lecon de golf suivante :</b>"
                + " <br/> Club = " + professional.getProClubId()
                + " <br/> Nom du Club = " + c.getClubName()
                + " <br/> Heure de debut = " + lesson.getEventStartDate().format(ZDF_TIME_HHmm)
                + " = " + lesson.getEventStartDate().getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, Locale.of(giver.getPlayerLanguage())) // fix multi-user 2026-03-07
                + " <br/> Heure de fin = " + lesson.getEventEndDate().format(ZDF_TIME_HHmm)
                + " <br/> Professional = " + professional.getProPlayerId()
                + " <br/>Nom  = " + giver.getPlayerLastName()
                + " <br/><br/> Thank you !"
                + " <br/> The GolfLC team"
                + "<br/><b>En cas d'empechement ou pour toute autre raison, vous pouvez contacter le demandeur  </b>:"
                + taker.getPlayerEmail()
                + "<br/><br/> envoye le : " + LocalDateTime.now().format(ZDF_TIME);

            mailSender.sendHtmlMailAsync(sujet, mail, to, pathQRC, taker.getPlayerLanguage());
            LOG.debug("mail async dispatched for lesson giver");

        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public Boolean deleteLesson(Professional professional, entite.Lesson lesson)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            String sujet = "Your Subscription at GolfLC is paid !";
            Player taker = new Player();
            taker.setIdplayer(professional.getProPlayerId());
            taker = playerManager.readPlayer(taker.getIdplayer());

            String mail =
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/> Professional details  = " + professional
                + " <br/> lesson details  = " + lesson
                + " <br/> Thank you !"
                + " <br/> The GolfLC team";

            String to = System.getenv("SMTP_USERNAME");
            byte[] pathQRC = null;
            mailSender.sendHtmlMailAsync(sujet, mail, to, pathQRC, taker.getPlayerLanguage());
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean modifyLesson(Professional professional, entite.Lesson lesson)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            String sujet = "Your Cotisation via GolfLC is paid !";
            Player taker = new Player();
            taker.setIdplayer(professional.getProPlayerId());
            taker = playerManager.readPlayer(taker.getIdplayer());

            String mail =
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> professional details  = " + professional
                + " <br/> lesson details  = " + lesson
                + " <br/> Thank you !"
                + " <br/> The GolfLC team";

            String to = System.getenv("SMTP_USERNAME");
            byte[] pathQRC = null;
            mailSender.sendHtmlMailAsync(sujet, mail, to, pathQRC, taker.getPlayerLanguage());
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean sendMailGreenfee(Player player, Creditcard creditcard, Greenfee greenfee, Club club)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            String sujet = "Your Greenfee via GolfLC is paid !";
            String mail =
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant paye = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditcardNumber()
                + " <br/> Creditcard Number  secret = " + creditcard.getCreditCardNumberSecret()
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditcardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                + " <br/> credit card details  = " + creditcard.toString()
                + " <br/> Greenfee details  = " + greenfee.toString()
                + " <br/> Greenfee RoundDate  = " + greenfee.getRoundDate().format(ZDF_TIME_DAY)
                + " <br/> Club Name    = " + club.getClubName()
                + " <br/> Club City    = " + club.getAddress().getCity()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team";

            String to = System.getenv("SMTP_USERNAME");
            byte[] pathQRC = null;
            mailSender.sendHtmlMailAsync(sujet, mail, to, pathQRC, player.getPlayerLanguage());
            LOG.debug("HTML Mail async dispatched");
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
