
package mail;

import entite.Player;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.Log.LOG;
import javax.mail.MessagingException;

public class ResetPasswordMail {

    public Boolean sendMail(Player player, String uuid) throws MessagingException, Exception {

      LOG.info("entering sendResetPasswordMail");
 //     LOG.info("** href for activation = " + href); 
    String sujet = "You Forgot Your Password for the Application GolfLC";
    
         //   String url = ;
     //    String href = "http://" + host + ":" + port + uri + "/activation_check.xhtml?key=" + uuid.toString();       
            String href = utils.LCUtil.firstPartUrl() + "/password_check.xhtml?uuid=" + uuid.toString() //; // mod 02-12-2018
                          + "&firstname=" + player.getPlayerFirstName()
                          + "&lastname=" + player.getPlayerLastName()
                          + "&language=" + player.getPlayerLanguage();
            href = href.replaceAll(" ","%20");
                LOG.info("** href for activation password after replace= " + href);  
             //        UnicodeEscaper ue = UnicodeEscaper.above(0);
    //        String result = ue.translate(player.getPlayerFirstName());
    //            LOG.info("**unicode de firstname = " + result);          
    //   href = StringEscapeUtils.escapeHtml4(href);
    //    LOG.info("**result for activation password  after escapeHtml = " + href);
   //    result = URLEncoder.encode(href, "UTF-8"); // new 03-12-2018
   //          LOG.info("**result for activation password  after encode = " + result);   
   //  result = StringEscapeUtils.escapeHtml4(href);
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
//Confirm Your Email Adress
 //       A confirmation email has been sent to ???
            //Click on the confirmation link in the email to activate your account
                LOG.info("mail to be sended = " + msg);

            String to = "louis.collet@skynet.be";
            utils.SendEmail sm = new utils.SendEmail();
            boolean b = sm.sendHtmlMail(sujet,msg,to,"PASSWORD");
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
           //          String url = utils.LCUtil.firstPartUrl();
                    // à modifier utilier <href ....>
                    //  msg = msg + "http://localhost:8080/GolfNew-1.0-SNAPSHOT/login.xhtml";
                //     String href = msg + url + "/login.xhtml";
                     String to = "louis.collet@skynet.be";
                     utils.SendEmail sm = new utils.SendEmail();
                     boolean b = sm.sendHtmlMail(sujet,msg,to,"PASSWORD");
                        LOG.info("HTML Mail status = " + b);
         
    return true;
     }
} // end class