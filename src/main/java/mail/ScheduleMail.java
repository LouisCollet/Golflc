
package mail;

import Controllers.LanguageController;
import entite.Club;
import entite.Creditcard;
import entite.Greenfee;
import entite.Player;
import entite.Professional;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_DAY;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.mail.MessagingException;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import static utils.LCUtil.showMessageFatal;

public class ScheduleMail {

 public void createLesson(Creditcard creditcard,
                          Professional professional,
                          entite.Lesson lesson,
                          Connection conn) throws MessagingException, Exception {
try{
    LOG.debug("entering scheduleMail.createLesson" );
    LOG.debug("with creditcard = " + creditcard);
    LOG.debug(" with professional = " + professional);
    LOG.debug(" with lesson = " + lesson);
    
      Player giver = new Player();  // chercher nom du pro
      giver.setIdplayer(professional.getProPlayerId());
      giver = new read.ReadPlayer().read(giver, conn);
      
      Player taker = new Player();  // chercher nom du pro
      taker.setIdplayer(lesson.getEventPlayerId());
      taker = new read.ReadPlayer().read(giver, conn);
    
      Club c = new Club(); // chercher nom du club
      c.setIdclub(professional.getProClubId());
      c = new read.ReadClub().read(c, conn);
      
//////////    mail 1    to lesson taker
     String sujet = "Lesson taker : Confirmation of your reservation lesson via GolfLC";
                String mail = 
     " <br/><b>Nous avons reçu le paiement suivant :</b>"
                +  LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Titulaire  = " + creditcard.getCreditcardHolder()
                + " <br/> Communication  = " + creditcard.getCommunication()
                + " <br/> Référence  = " + creditcard.getCreditcardPaymentReference()
   
      //          + " <br/><b>Email      = </b>" + player.getPlayerEmail()
    + "<br/><br/><b>Concernant la leçon de golf suivante :</b>" 
                + " <br/> Club = " + professional.getProClubId()
                + " <br/> Nom du Club = " + c.getClubName()       
                + " <br/> Heure de début = " + lesson.getEventStartDate().format(ZDF_TIME_HHmm)
      //                  + " = " + lesson.getEventStartDate().getDayOfWeek()   // par defaut = MONDAY ??
       //    + " = " + lesson.getEventStartDate().getDayOfWeek()
        //           .getDisplayName(TextStyle.FULL , 
                           //new LanguageController().getLocale())  // Locale
       //                  LanguageController.Locale())  // Locale
                + " <br/> Heure de fin = " + lesson.getEventEndDate().format(ZDF_TIME_HHmm)
      //          + " <br/> + lsson = " + lesson
        
                + " <br/> Professional = " + professional.getProPlayerId()
                + " <br/>Nom  = " + giver.getPlayerLastName()
                + " <br/><br/> Thank you !"
                + " <br/> The GolfLC team"
                  
      + "<br/><b>En cas d'empêchement ou pour toute autre raison, vous pouvez contacter le pro </b>:" 
                  + " <br/>Email de votre pro" + "pro email"
                  + "<br/><br/> envoyé le : " + LocalDateTime.now().format(ZDF_TIME)
                ; 
       String to = "louis.collet@skynet.be";
        Path path = null;
       boolean b = new mail.SendEmail().sendHtmlMail(sujet,mail,to,path,
               path, taker.getPlayerLanguage()); // pourquoi CREDITCARD ?
         LOG.debug("mail sent for lesson taker " + b);
  
 //////////   mall 2 to lesson giver
        sujet = "Lesson giver : Confirmation of a lesson via GolfLC";
        mail = 
     "<br/><br/><b>Concernant la leçon de golf suivante :</b>" 
                + " <br/> Club = " + professional.getProClubId()
                + " <br/> Nom du Club = " + c.getClubName()       
                + " <br/> Heure de début = " + lesson.getEventStartDate().format(ZDF_TIME_HHmm)
      //                  + " = " + lesson.getEventStartDate().getDayOfWeek()   // par defaut = MONDAY ??
           + " = " + lesson.getEventStartDate().getDayOfWeek()
                   .getDisplayName(TextStyle.FULL , new LanguageController().getLocale())  // Locale
                + " <br/> Heure de fin = " + lesson.getEventEndDate().format(ZDF_TIME_HHmm)
      //          + " <br/> + lsson = " + lesson
        
                + " <br/> Professional = " + professional.getProPlayerId()
                + " <br/>Nom  = " + giver.getPlayerLastName()
                + " <br/><br/> Thank you !"
                + " <br/> The GolfLC team"
                  
      + "<br/><b>En cas d'empêchement ou pour toute autre raison, vous pouvez contacter le demandeur  </b>:" 
                  + taker.getPlayerEmail()
                  + "<br/><br/> envoyé le : " + LocalDateTime.now().format(ZDF_TIME)
                ; 

 //       to = "louis.collet@skynet.be";
        Path path2 = null;
        b =  new mail.SendEmail().sendHtmlMail(sujet, mail, to,path,
                path2, taker.getPlayerLanguage()); // pourquoi CREDITCARD ?

        LOG.debug("mail sent for lesson giver " + b);
  
  
  
    }catch (Exception e){
    String msg = "Exception in sendcreditcardmail() = " + ", SQLState = " + e;
      //      + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        showMessageFatal(msg);
//        return false;
    }
} //end method
    
 public Boolean deleteLesson(Professional professional, entite.Lesson lesson, Connection conn) throws MessagingException, Exception {
try{
       LOG.debug("entering deleteLesson");
    String sujet = "Your Subscription at GolfLC is paid !";
    Player taker = new Player();
    taker.setIdplayer(professional.getProPlayerId());
    taker = new read.ReadPlayer().read(taker, conn);
                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
/*             + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditCardNumber()
                + " <br/> Creditcard Number  secret = " + creditcard.getCreditCardNumberSecret()
                                    
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditCardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                        
                + " <br/> credit card details  = " + creditcard.toString()
      */
                + " <br/> Professional details  = " + professional        
                + " <br/> lesson details  = " + lesson                
                    
        //        + " <br/> subscription details  = " + subscription.toString()        
       //         + " <br/> Subscription EndDate  = " + subscription.getEndDate().format(ZDF_TIME_DAY)
        //        + " <br/> Course Name  = " + course.getCourseName()
       //         + " <br/><b>ID         = </b>" + player.getIdplayer()
        //        + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
        //        + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
       //         + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
      //          + " <br/><b>City       = </b>" + player.getPlayerCity()
      //          + " <br/><b>Email      = </b>" + player.getPlayerEmail()
     //           + " <br/><b>Invited by     = </b>" + invitedBy.getPlayerLastName() + ", " + invitedBy.getPlayerFirstName()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 
                
                String to = "louis.collet@skynet.be";
                Path path = null;
                return new mail.SendEmail().sendHtmlMail(sujet,mail,to,path,
                        path, taker.getPlayerLanguage());
    //                LOG.debug("HTML Mail status = " + b);
//          return b;
    }catch (Exception ex){
            String msg = "Exception in creditCardSubscription mail " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}
} //end method 
    
 public Boolean modifyLesson(Professional professional, entite.Lesson lesson, Connection conn) throws MessagingException, Exception {
try{
       LOG.debug("entering sendMailCotisation");
    String sujet = "Your Cotisation via GolfLC is paid !";
    Player taker = new Player();
    taker.setIdplayer(professional.getProPlayerId());
    taker = new read.ReadPlayer().read(taker, conn);
                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
         /*       + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditCardNumber()
                + " <br/> Creditcard Number  secret = " +  creditcard.getCreditCardNumberSecret()
                                    
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditCardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                        
                + " <br/> credit card details  = " + creditcard.toString()
          */      + " <br/> professional details  = " + professional
                 + " <br/> lesson details  = " + lesson        
    //            + " <br/> cotisation details  = " + cotisation.toString()
    //            + " <br/> cotisation EndDate  = " + cotisation.getCotisationEndDate().format(ZDF_TIME_DAY)
        //        + " <br/> Course Name  = " + course.getCourseName()
    //           + " <br/> Club Name    = " + club.getClubName()
     //          + " <br/> Club City    = " + club.getClubCity()
     //          + " <br/><b>Tarif Member         = </b>" + tarifMember.toString()
        //        + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
        //        + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
       //         + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
      //          + " <br/><b>City       = </b>" + player.getPlayerCity()
      //          + " <br/><b>Email      = </b>" + player.getPlayerEmail()
     //           + " <br/><b>Invited by     = </b>" + invitedBy.getPlayerLastName() + ", " + invitedBy.getPlayerFirstName()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 
                String to = "louis.collet@skynet.be";
                 Path path = null;
               return new mail.SendEmail().sendHtmlMail(sujet,mail,to,path,
                       path, taker.getPlayerLanguage());
 //                   LOG.debug("HTML Mail status = " + b);
 //         return b;
    }catch (Exception ex){
            String msg = "Exception in creditCardSubscription mail " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}
} //end method 
 
 public Boolean sendMailGreenfee(Player player, Creditcard creditcard, Greenfee greenfee, Club club) throws MessagingException, Exception {
try{
       LOG.debug("entering sendMailGreenfee");
    String sujet = "Your Greenfee via GolfLC is paid !";

                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditcardNumber()
                + " <br/> Creditcard Number  secret = " +  creditcard.getCreditCardNumberSecret()
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditcardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                        
                + " <br/> credit card details  = " + creditcard.toString()
                + " <br/> Greenfee details  = " + greenfee.toString()
                + " <br/> Greenfee RoundDate  = " + greenfee.getRoundDate().format(ZDF_TIME_DAY)
        //        + " <br/> Course Name  = " + course.getCourseName()
               + " <br/> Club Name    = " + club.getClubName()
               + " <br/> Club City    = " + club.getAddress().getCity()
         //      + " <br/><b>Tarif Member         = </b>" + tarifMember.toString()
        //        + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
        //        + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
       //         + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
      //          + " <br/><b>City       = </b>" + player.getPlayerCity()
      //          + " <br/><b>Email      = </b>" + player.getPlayerEmail()
     //           + " <br/><b>Invited by     = </b>" + invitedBy.getPlayerLastName() + ", " + invitedBy.getPlayerFirstName()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 
                String to = "louis.collet@skynet.be";
                Path path = null;
                boolean b = new mail.SendEmail().sendHtmlMail(sujet,mail,to,path,
                        path, player.getPlayerLanguage());
                    LOG.debug("HTML Mail status = " + b);
          return b;
    }catch (Exception ex){
            String msg = "Exception in creditCardSubscription mail " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}
} //end method 
  
  } // end class