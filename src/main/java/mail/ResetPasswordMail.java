package mail;

import entite.Player;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.io.Serializable;
import java.time.LocalDateTime;

@ApplicationScoped
public class ResetPasswordMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private MailSender mailSender;
    @Inject private entite.Settings settings;

    public ResetPasswordMail() { }

    public Boolean send(Player player, String href) throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String sujet = "You Forgot Your Password for the Application GolfLC";
            String msg =
                  " <br/>Welcome to GolfLC! at " + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> You have ask the reset of your password !"
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Please click the following link to reset your password: "
                + "<b><font color='red';size='12'> WITHIN THE 10 MINUTES</b></font>"
                + " <br/> <a href=" + href + "> "
                + "Click here to Reset your password</a>"
                + " <br/> See you soon back !"
                + " <br/> The GolfLC team";

            LOG.debug("mail to be sended = " + msg);
            String to = settings.getProperty("SMTP_USERNAME");
            byte[] pathQRC = null;
            mailSender.sendHtmlMailAsync(sujet, msg, to, null, pathQRC, player.getPlayerLanguage());
            LOG.debug("HTML Mail async sent (fire-and-forget)");
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean sendMailResetOK(Player player) throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String sujet = "Successfull password reset to Golflc !!!";
            String href = utils.LCUtil.firstPartUrl() + "/login.xhtml";
            String msg =
                  " <br/>Welcome to GolfLC! at " + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Your password is reseted !"
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Please click the following link to connect again to the Application Golflc: "
                + " <br/> <a href=" + href + "> "
                + "Click here to Connect</a>"
                + " <br/> We hope to see you back soon!"
                + " <br/> The GolfLC team";

            String to = settings.getProperty("SMTP_USERNAME");
            byte[] pathQRC = null;
            mailSender.sendHtmlMailAsync(sujet, msg, to, pathQRC, player.getPlayerLanguage());
            LOG.debug("sendMailResetOK async dispatched");
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
