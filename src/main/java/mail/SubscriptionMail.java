package mail;

import entite.Player;
import entite.Subscription;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.io.Serializable;
import utils.LCUtil;

@ApplicationScoped
public class SubscriptionMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private MailSender mailSender;

    public SubscriptionMail() { }

    public Boolean sendMail(Player player, Subscription subscription) throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            String msg =
                  " Please consider your subcription renewal at the famous GolfLC !!"
                + " <br/>Your subscription end Date is : " + subscription.getEndDate()
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/><b>Subscription for a month = </b>" + utils.LCUtil.findProperties("subscription", "month")
                + " <br/><b>Subscription for a year = </b>" + utils.LCUtil.findProperties("subscription", "year")
                + " <br/> Thank you !"
                + " <br/> The GolfLC team";

            LOG.debug("mail to be sended = " + msg);
            String sujet = "Renouvellement de votre souscription a GolfLC";
            String to = "louis.collet@skynet.be";
            byte[] pathQRC = null;
            boolean b = mailSender.sendHtmlMail(sujet, msg, to, pathQRC, player.getPlayerLanguage());
            LOG.debug("HTML Mail status = " + b);
            LCUtil.showDialogInfo("sending one subscription Renewal Mail !!");
            return b;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
