
package mail;

import entite.Player;
import entite.Subscription;
import static interfaces.Log.LOG;
import java.nio.file.Path;
import jakarta.mail.MessagingException;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

public class SubscriptionMail {
public Boolean sendMail(Player player, Subscription subscription) throws MessagingException, Exception {
try{
    LOG.debug("entering sendSubscriptionMail");
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
                + " <br/> The GolfLC team"
                    ; 
                LOG.debug("mail to be sended = " + msg);

            //    String sujet = "Your Subscription Renewal for GolfLC";
                String sujet = "Renouvellement de votre souscription à GolfLC";
                String to = "louis.collet@skynet.be";

                Path path = null;
                boolean b = new mail.SendEmail().sendHtmlMail(sujet,msg,to,path,
                        path, player.getPlayerLanguage());
                    LOG.debug("HTML Mail status = " + b);
                    LCUtil.showDialogInfo("sending one subscription Renewal Mail !!");

return b;

}catch (Exception ex){
    String msg = "Exception in SubscriptionMail.sendMail " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return false;
}finally{  //      return false;
}
} // end method
} // end class