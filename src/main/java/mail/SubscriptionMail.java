
package mail;

import entite.Player;
import entite.Subscription;
import static interfaces.Log.LOG;
import javax.mail.MessagingException;
import utils.LCUtil;

public class SubscriptionMail {

    public Boolean sendMail(Player player, Subscription subscription) throws MessagingException, Exception {
{
        LOG.info("entering sendSubscriptionMail");
       String msg =
                  " Please consider your subcription renewal at the famous GolfLC !!" 
                + " <br/>Your subscription end Date is : " + subscription.getEndDate()
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getPlayerCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/><b>Subscription for a month = </b>" + utils.LCUtil.findProperties("subscription", "month")
                + " <br/><b>Subscription for a year = </b>" + utils.LCUtil.findProperties("subscription", "year")
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 
                LOG.info("mail to be sended = " + msg);

                String sujet = "Your Subscription Renewal for GolfLC";
                String to = "louis.collet@skynet.be";
                utils.SendEmail sm = new utils.SendEmail();
                boolean b = sm.sendHtmlMail(sujet,msg,to,"RENEWAL");
                    LOG.info("HTML Mail status = " + b);
                    LCUtil.showDialogInfo("sending one subscription Renewal Mail !!");

return b;
}

    
    }
} // end class
