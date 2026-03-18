package mail;

import entite.Player;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.LocalDateTime;

@ApplicationScoped
public class ActivationMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private mail.MailSender mailSender; // migrated 2026-02-26

    public ActivationMail() { }

    public Boolean sendMailAccountCreated(Player player, String href) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " for player = " + player);
        LOG.debug("** href/url for activation = " + href);
     String msg =
                  " <br/>Welcome to GolfLC! at "
                + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Thanks for signing up!"
                + " <br/> Your account has been created, but before you can login with the following credentials, you have to activate your account."
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Please click here to activate your account: "
                + " <a href=" + href + "> to activate your account</a>"
                + " <br/> Thank you !"
                + "<b><font color='red';size='12'> WITHIN THE 10 MINUTES</b></font>"

                + " <br/> The GolfLC team"
                    ;
            String sujet = "Activate Your Account for GolfLC";
            String to = System.getenv("SMTP_USERNAME");
            byte[] pathICS = null;
            // ✅ async — ne bloque plus le thread HTTP (fire-and-forget)
            mailSender.sendHtmlMailAsync(sujet, msg, to, pathICS, null, player.getPlayerLanguage());
            LOG.debug(methodName + " - async mail queued for " + to);
            return true;
    } // end method
    public Boolean sendMailActivationOK(Player player) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

            String href = utils.LCUtil.firstPartUrl() + "/login.xhtml";
            String sujet = "Succesfull activation to golflc !!!";
            String msg ="ok with your activation !!"
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br> for a connection to the application,"
                + " <br>click on the next url : "
                + " <a href=" + href + ">"
                + "Click for connection</a>"
                + " <br/> The GolfLC team"
                ;
                     String to = System.getenv("SMTP_USERNAME");
                     byte[] pathICS = null;
                     // ✅ async — ne bloque plus le thread HTTP (fire-and-forget)
                     mailSender.sendHtmlMailAsync(sujet, msg, to, pathICS, null, player.getPlayerLanguage());
                     LOG.debug(methodName + " - async mail queued for " + to);
            return true;
    } // end method
    
    /*
    void main() throws IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(456783);
        player.setPlayerLastName("Muntingh");
        player.setPlayerEmail("theo.muntingh@skynet.be");
        new ActivationMail().sendMailActivationOK(player);
    } // end main
    */

} // end class