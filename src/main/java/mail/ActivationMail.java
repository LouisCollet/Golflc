package mail;

import entite.Club;
import entite.Player;
import entite.Round;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import jakarta.mail.MessagingException;
import static utils.LCUtil.showMessageFatal;

@ApplicationScoped
public class ActivationMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private mail.MailSender mailSender; // migrated 2026-02-26

    public ActivationMail() { }

    public Boolean sendMailAccountCreated(Player player, String href) throws MessagingException, Exception {
        LOG.debug("entering sendMailAccountCreated for player = " + player);
         LOG.debug("** href/url for activation = " + href);
     String msg =
                  " <br/>Welcome to GolfLC! at "
            //    + SDF_TIME.format(new java.util.Date() )
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
     //           LOG.debug(msg);
            String sujet = "Activate Your Account for GolfLC";
            String to = "louis.collet@skynet.be";
         //   Path path = null;
         //   byte[] path = null;
            byte[] pathICS = null;
            boolean b = mailSender.sendHtmlMail(sujet,msg,to, pathICS, player.getPlayerLanguage());
                LOG.debug("sendMailAccountCreated status = " + b);
return b;
}
    public Boolean sendMailActivationOK(Player player) throws MessagingException, Exception {

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
                    // à modifier utilier <href ....>
                    //  msg = msg + "http://localhost:8080/GolfNew-1.0-SNAPSHOT/login.xhtml";
                     
 // à mofifier             //       <a href=" + href + ">"
                     String to = "louis.collet@skynet.be";
                  //   Path path = null;
                     byte[] pathICS = null;
                     boolean b = mailSender.sendHtmlMail(sujet,msg,to,pathICS, player.getPlayerLanguage());
                        LOG.debug("HTML Mail status = " + b);
     return b;
    }
    
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