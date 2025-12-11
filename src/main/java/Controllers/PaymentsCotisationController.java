package Controllers;

import entite.Club;
import entite.Cotisation;
import entite.Course;
import entite.Creditcard;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class PaymentsCotisationController implements Serializable, interfaces.Log{
    
public PaymentsCotisationController(){ } // constructor

// new 21-01-2023 vient de courseController
//@Inject @SessionMap
//  private Map<String, Object> sessionMap;

public boolean RegisterPaymentandInscription(final Creditcard creditcard, final Cotisation cotisation, final Player player, final Round round, final Club club, 
        final Course course,
        Inscription inscription,
        final Connection conn) throws SQLException, Exception {
try{
           LOG.debug("entering RegisterPaymentandInscription");
           LOG.debug("with cotisation = " + cotisation);
 ///          LOG.debug("with inscription = " + inscription);

  // 1. Register payment  faut le faire avant car l'inscription va vérifier !!! 
          if(! payment(cotisation, conn)){ 
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
              inscription = new create.CreateInscription().create(round, player, player,
                      inscription,
                      club, course, "A", conn);
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


private static boolean payment(Cotisation cotisation, Connection conn){ 
 try{
      LOG.debug("entering createPaymentCotisation");
      LOG.debug("with cotisation = " + cotisation);
 
      if(new create.CreatePaymentCotisation().create(cotisation, conn)){ //true
        String msg = LCUtil.prepareMessageBean("subscription.success")
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

 void main() throws Exception, Throwable {
    Connection conn = new utils.DBConnection().getConnection();
  try{

 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
     utils.DBConnection.closeQuietly(conn, null, null , null); 
   }
} // end main//
} // end class
