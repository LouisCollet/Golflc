
package mail;

import entite.Player;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.Log.LOG;
import javax.mail.MessagingException;

public class ResetPasswordMail {

    public Boolean send(Player player, String href) throws MessagingException, Exception {

      LOG.info("entering ResetPasswordMail.send");
    String sujet = "You Forgot Your Password for the Application GolfLC";

    String msg =
                  " <br/>Welcome to GolfLC! at " + SDF_TIME.format(new java.util.Date() )
                + " <br/> You have ask the reset of your password !"
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getPlayerCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Please click the following link to reset your password: "
                + "<b><font color='red';size='12'> WITHIN THE 10 MINUTES</b></font>"
                + " <br/> <a href=" + href + "> "
                + "Click here to Reset your password</a>"
                + " <br/> See you soon back !"
                + " <br/> The GolfLC team"
                    ; 

                LOG.info("mail to be sended = " + msg);
            String to = "louis.collet@skynet.be";
            boolean b = new utils.SendEmail().sendHtmlMail(sujet,msg,to,"PASSWORD");
                LOG.info("HTML Mail status = " + b);
return b;
} //end method
     public Boolean sendMailResetOK(Player player) throws MessagingException, Exception {
 
               String sujet = "Successfull password reset to Golflc !!!";
               String href = utils.LCUtil.firstPartUrl() + "/login.xhtml";
               String msg =
                  " <br/>Welcome to GolfLC! at " + SDF_TIME.format(new java.util.Date() )
                + " <br/> Your password is reseted !"
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getPlayerCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Please click the following link to connect again to the Application Golflc: "
                + " <br/> <a href=" + href + "> "
                + "Click here to Connect</a>"
                + " <br/> We hope to see you back soon!"
                + " <br/> The GolfLC team"
                       ;
                     String to = "louis.collet@skynet.be";
                     boolean b = new utils.SendEmail().sendHtmlMail(sujet,msg,to,"PASSWORD");
                        LOG.info("sendMailResetOK status = " + b);
         
    return true;
     }
} // end class