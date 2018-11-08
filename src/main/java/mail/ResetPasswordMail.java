
package mail;

import entite.Player;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.Log.LOG;
import javax.mail.MessagingException;
import utils.LCUtil;

public class ResetPasswordMail {

    public Boolean sendResetPasswordMail(Player player, String href) throws MessagingException, Exception {
   
{
      LOG.info("entering sendResetPasswordMail");
      LOG.info("** href for activation = " + href);   
    String msg =
                  " <br/>Welcome to GolfLC! at "
                + SDF_TIME.format(new java.util.Date() )
                + " <br/> You hase ask the reset of your password !"
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getPlayerCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Please click this link to reset your password: "
                + "<b><font color='red';size='12'> WITHIN THE 10 MINUTES</b></font>"
                + " <br/> <a href=" + href + "> "
              
                + "Click here to Reset your password</a>"
                + " <br/> See you soon back !"
                + " <br/> The GolfLC team"
                    ; 
//Confirm Your Email Adress
 //       A confirmation email has been sent to ???
            //Click on the confirmation link in the email to activate your account
                LOG.info(msg);

            // new 10/02/2013
            String sujet = "Forgot Your Password for GolfLC";
            String to = "louis.collet@skynet.be";
            utils.SendEmail sm = new utils.SendEmail();
            boolean b = sm.sendHtmlMail(sujet,msg,to,"PASSWORD");
            
            msg = "-- We just send you an mail for resetting your Password, please use it within the 10 minutes ... = " ;//+ player.getIdplayer()
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            
                LOG.info("HTML Mail status = " + b);

return b;
}
    }
} // end class