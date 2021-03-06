
package mail;

import entite.Club;
import entite.Cotisation;
import entite.Creditcard;
import entite.Greenfee;
import entite.Inscription;
import entite.Round;
import entite.Subscription;
import entite.TarifGreenfee;
import entite.TarifMember;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_DAY;
import static interfaces.Log.LOG;
import javax.mail.MessagingException;
import static utils.LCUtil.showMessageFatal;
/**
 *
 * @author Collet
 */
public class CreditcardMail {

    public Boolean sendMailInscription(Creditcard creditcard, TarifGreenfee tarif, Round round,
            Inscription inscription) throws MessagingException, Exception {
try{
     String sujet = "Your creditcard payment for your Round Inscription via GolfLC";
                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + SDF_TIME.format(new java.util.Date() )
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditCardNumber()
                        // affiche les 4 derniers chiffres en clair
                + " <br/> Creditcard Number  secret = " +  utils.LCUtil.creditcardSecret(creditcard.getCreditCardNumber())
                            
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditCardIssuer()
                + " <br/> Tarif details  = " + tarif.toString()
                + " <br/> credit card details  = " + creditcard.toString()
                        
                         + " <br/> round details  = " + round.toString()
                         + " <br/> inscription details  = " + inscription.toString()
        //        + " <br/> Round Date   = " + round.getRoundDate().format(ZDF_TIME_HHmm)
        //        + " <br/> Course Name  = " + course.getCourseName()
        //        + " <br/> Club Name    = " + club.getClubName()
        //        + " <br/> Club City    = " + club.getClubCity()
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
          //      utils.SendEmail sm = new utils.SendEmail();
                boolean b = new utils.SendEmail().sendHtmlMail(sujet,mail,to,"CREDITCARD");
                
    
    
        return b;
    }catch (Exception e){
    String msg = "Exception in sendcreditcardmail() = " + ", SQLState = " + e;
      //      + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        showMessageFatal(msg);
        return false;
    }
    
} //end method
    
 public Boolean sendMailSubscription(Creditcard creditcard, Subscription subscription) throws MessagingException, Exception {
try{
    
       LOG.info("entering creditCardSubscriptionMail");
    String sujet = "Your Subscription via GolfLC is paid !";

                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + SDF_TIME.format(new java.util.Date() )
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditCardNumber()
                + " <br/> Creditcard Number  secret = " +  utils.LCUtil.creditcardSecret(creditcard.getCreditCardNumber())
                                    
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditCardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                        
                + " <br/> credit card details  = " + creditcard.toString()
                + " <br/> subscription details  = " + subscription.toString()        
                + " <br/> Subscription EndDate  = " + subscription.getEndDate().format(ZDF_TIME_DAY)
        //        + " <br/> Course Name  = " + course.getCourseName()
        //        + " <br/> Club Name    = " + club.getClubName()
        //        + " <br/> Club City    = " + club.getClubCity()
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
            //    utils.SendEmail sm = new utils.SendEmail();
                boolean b = new utils.SendEmail().sendHtmlMail(sujet,mail,to,"CREDITCARD");
                    LOG.info("HTML Mail status = " + b);
          return b;
    }catch (Exception ex){
            String msg = "Exception in creditCardSubscription mail " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}
} //end method 
    
 public Boolean sendMailCotisation(Creditcard creditcard, Cotisation cotisation, Club club, TarifMember tarifMember) throws MessagingException, Exception {
try{
       LOG.info("entering creditCardMailCotisation");
    String sujet = "Your Cotisation via GolfLC is paid !";

                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + SDF_TIME.format(new java.util.Date() )
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditCardNumber()
                + " <br/> Creditcard Number  secret = " +  utils.LCUtil.creditcardSecret(creditcard.getCreditCardNumber())
                                    
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditCardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                        
                + " <br/> credit card details  = " + creditcard.toString()
                + " <br/> cotisation details  = " + cotisation.toString()
                + " <br/> cotisation EndDate  = " + cotisation.getEndDate().format(ZDF_TIME_DAY)
        //        + " <br/> Course Name  = " + course.getCourseName()
               + " <br/> Club Name    = " + club.getClubName()
               + " <br/> Club City    = " + club.getClubCity()
               + " <br/><b>Tarif Member         = </b>" + tarifMember.toString()
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
          //      utils.SendEmail sm = new utils.SendEmail();
                boolean b = new utils.SendEmail().sendHtmlMail(sujet,mail,to,"CREDITCARD");
                    LOG.info("HTML Mail status = " + b);
          return b;
    }catch (Exception ex){
            String msg = "Exception in creditCardSubscription mail " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}
} //end method 
 
 public Boolean sendMailGreenfee(Creditcard creditcard, Greenfee greenfee, Club club) throws MessagingException, Exception {
try{
       LOG.info("entering creditCardMailGreenfee");
    String sujet = "Your Greenfee via GolfLC is paid !";

                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + SDF_TIME.format(new java.util.Date() )
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditCardNumber()
                + " <br/> Creditcard Number  secret = " +  utils.LCUtil.creditcardSecret(creditcard.getCreditCardNumber())
                                    
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditCardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                        
                + " <br/> credit card details  = " + creditcard.toString()
                + " <br/> Greenfee details  = " + greenfee.toString()
                + " <br/> Greenfee RoundDate  = " + greenfee.getRoundDate().format(ZDF_TIME_DAY)
        //        + " <br/> Course Name  = " + course.getCourseName()
               + " <br/> Club Name    = " + club.getClubName()
               + " <br/> Club City    = " + club.getClubCity()
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
          //      utils.SendEmail sm = new utils.SendEmail();
                boolean b = new utils.SendEmail().sendHtmlMail(sujet,mail,to,"CREDITCARD");
                    LOG.info("HTML Mail status = " + b);
          return b;
    }catch (Exception ex){
            String msg = "Exception in creditCardSubscription mail " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}
} //end method 
  
  } // end class
