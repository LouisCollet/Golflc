package payment;

import entite.Club;
import entite.Cotisation;
import entite.Course;
import entite.Creditcard;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.io.*;
import java.sql.SQLException;
import static utils.LCUtil.prepareMessageBean;

import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentCotisationController implements Serializable, interfaces.Log{

    private static final long serialVersionUID = 1L;

    @Inject private create.CreateInscription createInscriptionService; // migrated 2026-02-25
    @Inject private create.CreatePaymentCotisation createPaymentCotisationService; // migrated 2026-02-26

public PaymentCotisationController(){ } // constructor

// new 21-01-2023 vient de courseController
//@Inject @SessionMap
//  private Map<String, Object> sessionMap;

public boolean RegisterPaymentandInscription(final Creditcard creditcard, final Cotisation cotisation, final Player player, final Round round, final Club club,
        final Course course,
        Inscription inscription) throws SQLException, Exception { // Connection conn removed 2026-02-28 — unused
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
try{
           LOG.debug("with cotisation = " + cotisation);

  // 1. Register payment  faut le faire avant car l'inscription va vérifier !!!
          if(! payment(cotisation)){ 
               String msg = "Create Payment Cotisation FAILED !";
               LOG.error(msg);
               showMessageFatal(msg);
               throw new Exception(msg);
          }
// true
             String msg = "Payment aand Inscription registered"; 
           LOG.debug(msg);
  // 2. Register Inscription 
          //    Inscription inscription = new Inscription();
          
          // à modifier et en faire un paramètre !!
             //  if(sessionMap.get("inputSelectCourse").equals("PaymentCotisationSpontaneous")){
            if(cotisation.getType().equalsIgnoreCase("spontaneous")){  
                 msg = "Spontaneous payment accepted - NO inscription";
                 LOG.debug(msg);
                 showMessageInfo(msg);
                 return true;
            }
               LOG.debug("inscription error = " + inscription.isInscriptionError());
               LOG.debug("inscription OK = " + inscription.isInscriptionOK());
           // inscription = new create.CreateInscription().create(round, player, player, inscription, club, course, "A", conn);
              inscription = createInscriptionService.create(round, player, player,
                      inscription,
                      club, course, "A"); // migrated 2026-02-25
              if( ! inscription.isInscriptionError()){  // no errors
                  msg = "no error :  Inscription done";
                  LOG.info(msg);
                  showMessageInfo(msg);
                  return true;
              }else{ // error inscription
                  msg = "FATAL error : we cannot create the Inscription BUT the payment is registered - refund needed !!";
                  LOG.error(msg);
                  showMessageFatal(msg);
                  throw new Exception(msg);
              }
        //      return "welcome.xhtml?faces-redirect=true";
}catch (Exception e) {
            String msg = "££ Exception in RegisterPaymentandInscription = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
           return false; // indicates that the same view should be redisplayed
     //   } finally {}
//return null;
   } 
} //end method


private boolean payment(Cotisation cotisation) { // Connection conn removed 2026-02-28 — unused
 try{
      LOG.debug("entering createPaymentCotisation");
      LOG.debug("with cotisation = " + cotisation);
 
      if(createPaymentCotisationService.create(cotisation)){ // migrated 2026-02-26
        String msg = prepareMessageBean("subscription.success")
                  + cotisation //.getCotisationStartDate().format(ZDF_DAY) + " - " 
          //        + cotisation.getCotisationEndDate().format(ZDF_DAY)
                  + " for club = " + cotisation.getIdclub();
        LOG.info(msg);
        showMessageInfo(msg);
        return true;
     }else{
        String msg = "Error : payment cotisation NOT done !";
        LOG.error(msg);
        showMessageFatal(msg);
        return false;
    }
  }catch (Exception ex){
            String msg = "Exception in payment " + ex.getLocalizedMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }
 } //end method createPaymentCotisation

    /*
    void main() throws Exception, Throwable {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
