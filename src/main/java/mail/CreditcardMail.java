
package mail;

import entite.Club;
import entite.Cotisation;
import entite.Creditcard;
import entite.Greenfee;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.Subscription;
import entite.TarifGreenfee;
import entite.TarifMember;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_DAY;
import static interfaces.Log.LOG;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import jakarta.mail.MessagingException;
import utils.DBConnection;
import static utils.LCUtil.showMessageFatal;

public class CreditcardMail{

    public Boolean sendMailGreenfee(Player player, Creditcard creditcard, TarifGreenfee tarif, Round round,
            Inscription inscription) throws MessagingException, Exception {
try{
     String sujet = "Your creditcard payment for your Round Inscription via GolfLC";
                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditcardNumber()
                        // affiche les 4 derniers chiffres en clair
                + " <br/> Creditcard Number  secret = " +  creditcard.getCreditCardNumberSecret()
                            
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditcardIssuer()
                + " <br/> Tarif details  = " + tarif.toString()
                + " <br/> credit card details  = " + creditcard.toString()
                        
                         + " <br/> round details  = " + round.toString()
                         + " <br/> inscription details  = " + inscription.toString()
        //        + " <br/> Round Date   = " + round.getRoundDate().format(ZDF_TIME_HHmm)
        //        + " <br/> Course Name  = " + course.getCourseName()
        //        + " <br/> Club Name    = " + club.getClubName()
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
         //       Player player = new Player();
         //       player.setIdplayer(creditcard.getIdplayer());
         //       player = new load.LoadPlayer().load(player, conn);
                Path path = null;
                return new mail.SendEmail().sendHtmlMail(sujet,mail,to,path,
                        path, player.getPlayerLanguage());
  //      return b;
    }catch (Exception e){
    String msg = "Exception in sendcreditcardmail() = " + ", SQLState = " + e;
      //      + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        showMessageFatal(msg);
        return false;
    }
} //end method
    
 public Boolean sendMailSubscription(Player player, Creditcard creditcard, Subscription subscription) throws MessagingException, Exception {
try{
    
       LOG.debug("entering sendMailSubscription");
    String sujet = "Your Subscription at GolfLC is paid !";

                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditcardNumber()
                + " <br/> Creditcard Number  secret = " + creditcard.getCreditCardNumberSecret()
                                    
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditcardIssuer()
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
                Path path = null;
                return new mail.SendEmail().sendHtmlMail(sujet,mail,to,path,
                        path, player.getPlayerLanguage());
    //                LOG.debug("HTML Mail status = " + b);
//          return b;
    }catch (Exception ex){
            String msg = "Exception in creditCardSubscription mail " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}
} //end method 
    
 public Boolean sendMailCotisation(Player player, Creditcard creditcard, Cotisation cotisation, Club club, TarifMember tarifMember) throws MessagingException, Exception {
try{
       LOG.debug("entering sendMailCotisation");
    String sujet = "Your Cotisation via GolfLC is paid !";

                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditcardNumber()
                + " <br/> Creditcard Number  secret = " +  creditcard.getCreditCardNumberSecret()
                                    
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditcardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                        
                + " <br/> credit card details  = " + creditcard.toString()
                + " <br/> cotisation details  = " + cotisation.toString()
                + " <br/> cotisation EndDate  = " + cotisation.getCotisationEndDate().format(ZDF_TIME_DAY)
        //        + " <br/> Course Name  = " + course.getCourseName()
               + " <br/> Club Name    = " + club.getClubName()
            //   + " <br/> Club City    = " + club.getClubCity()
               + " <br/> Club City    = " + club.getAddress().getCity()         
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
                 Path path = null;
               return new mail.SendEmail().sendHtmlMail(sujet,mail,to,path,
                       path, player.getPlayerLanguage());
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
  void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
     
  try{
      String to = "louis.collet@skynet.be";
 //   boolean b = new mail.SendEmail().sendHtmlMail("sujet","mail",to,"CREDITCARD",player.getPlayerLanguage());
  //      LOG.debug("from main, after lp = " + b);
 } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
  } // end class