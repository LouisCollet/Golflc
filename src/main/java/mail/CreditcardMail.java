package mail;

import entite.Club;
import entite.Cotisation;
import entite.Creditcard;
import entite.Greenfee;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.Subscription;
import entite.TarifGreenfee;
import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_DAY;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.io.Serializable;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreditcardMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private MailSender mailSender;
    @Inject private entite.Settings settings;

    public CreditcardMail() { }

    public Boolean sendMailGreenfee(Player player, Creditcard creditcard, TarifGreenfee tarif, Round round,
            Inscription inscription) throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String sujet = "Your creditcard payment for your Round Inscription via GolfLC";
            String mail =
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant paye = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditcardNumber()
                + " <br/> Creditcard Number  secret = " + creditcard.getCreditCardNumberSecret()
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditcardIssuer()
                + " <br/> Tarif details  = " + tarif.toString()
                + " <br/> credit card details  = " + creditcard.toString()
                + " <br/> round details  = " + round.toString()
                + " <br/> inscription details  = " + inscription.toString()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team";

            String to = settings.getProperty("SMTP_USERNAME");
            byte[] pathICS = null;
            mailSender.sendHtmlMailAsync(sujet, mail, to, pathICS, player.getPlayerLanguage());
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean sendMailSubscription(Player player, Creditcard creditcard, Subscription subscription)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String sujet = "Your Subscription at GolfLC is paid !";
            String mail =
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant paye = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditcardNumber()
                + " <br/> Creditcard Number  secret = " + creditcard.getCreditCardNumberSecret()
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditcardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                + " <br/> credit card details  = " + creditcard.toString()
                + " <br/> subscription details  = " + subscription.toString()
                + " <br/> Subscription EndDate  = " + subscription.getEndDate().format(ZDF_TIME_DAY)
                + " <br/> Thank you !"
                + " <br/> The GolfLC team";

            String to = settings.getProperty("SMTP_USERNAME");
            byte[] pathICS = null;
            mailSender.sendHtmlMailAsync(sujet, mail, to, pathICS, player.getPlayerLanguage());
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean sendMailCotisation(Player player, Creditcard creditcard, Cotisation cotisation,
            Club club, TarifMember tarifMember) throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String sujet = "Your Cotisation via GolfLC is paid !";
            String mail =
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant paye = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditcardNumber()
                + " <br/> Creditcard Number  secret = " + creditcard.getCreditCardNumberSecret()
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditcardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                + " <br/> credit card details  = " + creditcard.toString()
                + " <br/> cotisation details  = " + cotisation.toString()
                + " <br/> cotisation EndDate  = " + cotisation.getCotisationEndDate().format(ZDF_TIME_DAY)
                + " <br/> Club Name    = " + club.getClubName()
                + " <br/> Club City    = " + club.getAddress().getCity()
                + " <br/><b>Tarif Member         = </b>" + tarifMember.toString()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team";

            String to = settings.getProperty("SMTP_USERNAME");
            byte[] pathICS = null;
            mailSender.sendHtmlMailAsync(sujet, mail, to, pathICS, player.getPlayerLanguage());
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean sendMailGreenfee(Player player, Creditcard creditcard, Greenfee greenfee, Club club)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
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

            String to = settings.getProperty("SMTP_USERNAME");
            byte[] pathICS = null;
            mailSender.sendHtmlMailAsync(sujet, mail, to, pathICS, player.getPlayerLanguage());
            LOG.debug("HTML Mail async dispatched");
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

/*
void main() throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    // tests locaux
} // end main
*/

} // end class
