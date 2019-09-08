/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;

import entite.Player;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.Log.LOG;
import java.util.UUID;
import javax.mail.MessagingException;

/**
 *
 * @author Collet
 */
public class ActivationMail{

    public Boolean sendMailAccountCreated(Player player) throws MessagingException, Exception {

        LOG.info("entering sendActivationMail");
        UUID uuid = UUID.randomUUID();
               LOG.info("Universally Unique Identifier = " + uuid.toString());
  //              see createActivation.java for more explanations

         String url = utils.LCUtil.firstPartUrl();
         LOG.info("url = " + url);
     //    String href = "http://" + host + ":" + port + uri + "/activation_check.xhtml?key=" + uuid.toString();       
         String href = url + "/password_create.xhtml?faces-redirect=true&uuid=" + uuid.toString(); 
         //    String href = "http://localhost:8080/GolfNew-1.0-SNAPSHOT/activation_check.xhtml?key=" + uuid.toString();  
      LOG.info("** href for activation = " + href);   
   //      String ms = mailText(player, href);
 //       boolean b =  mail.ActivationMail.sendActivationMail(player, href);   //envoi du mail
    //   LOG.info("Universally Unique Identifier = " + uuid) ; //.toString());
       //LOG.info("UUID version/variant = " + uuid.version() + " ,UUID version = " + uuid.variant() );
  //  String href = "http://localhost:8080/HelloGolf-1.0-SNAPSHOT/activation_check.xhtml?key=" + uuid;
    String msg =
                  " <br/>Welcome to GolfLC! at "
                + SDF_TIME.format(new java.util.Date() )
                + " <br/> Thanks for signing up!"
                + " <br/> Your account has been created, but before can login with the following credentials, you have to activate your account."
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getPlayerCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Please click this link to activate your account: "
                + "<b><font color='red';size='12'> WITHIN THE 10 MINUTES</b></font>"
                + " <a href=" + href + "> "
                + "Click to authenticate</a>"
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 
     //           LOG.info(msg);
            String sujet = "Activate Your Account for GolfLC";
            String to = "louis.collet@skynet.be";
       //     utils.SendEmail sm = new utils.SendEmail();
            boolean b = new utils.SendEmail().sendHtmlMail(sujet,msg,to,"ACTIVATION");
                LOG.info("HTML Mail status = " + b);
return b;
}
    public Boolean sendMailActivationOK(Player player) throws MessagingException, Exception {
                     
            String href =  utils.LCUtil.firstPartUrl() + "/login.xhtml";
            String sujet = "Succesfull activation to golflc !!!";
            String msg ="ok with your activation !!"
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getPlayerCity()
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
                //     utils.SendEmail sm = new utils.SendEmail();
                     boolean b = new utils.SendEmail().sendHtmlMail(sujet,msg,to,"ACTIVATION");
                        LOG.info("HTML Mail status = " + b);
     return b;
    }

} // end class
