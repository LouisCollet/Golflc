package Controllers;

import entite.Player;
import entite.Subscription;
import entite.Subscription.etypeSubscription;
import static interfaces.Log.LOG;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import utils.LCUtil;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class PaymentsSubscriptionController implements Serializable, interfaces.Log{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public PaymentsSubscriptionController(){ // constructor
}
private static Subscription completeTrial(Subscription subscription, Subscription previous) throws Exception{
try{
    LOG.debug("entering completeTrial with subscription = " + subscription);
      
          Short t = previous.getTrialCount();
             LOG.debug("previous trialCount = " + previous.getTrialCount());
          subscription.setTrialCount(++t);  // attention ++ doit se trouver devant !!!
             LOG.debug("This is a TRIAL, new trialCount = " + subscription.getTrialCount());
             LocalTime startTime = LocalTime.of(0, 0, 0); // important isBefore and isAfter tiennent compte de l'heure de now()
             LocalTime endTime = LocalTime.of(23, 59, 59);
             LocalDate now = LocalDateTime.now().toLocalDate();
        //     LocalDateTime ldt = LocalDateTime.of(date, endTime);
          subscription.setStartDate(LocalDateTime.of(now, startTime));
          subscription.setEndDate(LocalDateTime.of(now, endTime));
       //   subscription.setEndDate(LocalDateTime.now()); //.plusDays(1)); // donc valable deux jours 
          if(subscription.getTrialCount() > 5 && LocalDateTime.now().isAfter(subscription.getEndDate())){  // new 22-02-2019 
             String msg = LCUtil.prepareMessageBean("subscription.create.toomuchtrials")
        //    String msg = "subscription Trial > 5 - Use Subscription Month of Year instead !!! "
                  + " player = " + subscription.getIdplayer()
                  + " , trial  = <h1>" + subscription.getTrialCount() + "</h1>";
             LOG.error(msg);
             showMessageFatal(msg);
         }
   return subscription;
}catch (Exception ex){
    LOG.error("Exception in complete ! " + ex);
  //  LCUtil.showMessageFatal("Exception in LoadClub = " + ex.toString() );
     return null;
}finally{}
} //end method

//private static Subscription completePriceAndCommunication(Subscription subscription) throws Exception{
public Subscription completePriceAndCommunication(Subscription subscription) throws Exception{   
try{
    LOG.debug("entering completePriceAndCommunication with subscription = " + subscription);
  //1. complete price
     // double price = new PaymentsSubscriptionController().findTarif(subscription); // déjà cherché avant !!
      double price = findTarif(subscription); // déjà cherché avant !! mod 15-08-2025
         LOG.debug("price subscription = " + price);
      subscription.setSubscriptionAmount(price); // mod 22-02-2024
      
 //2. complete communication
       if(subscription.getSubCode().equals(etypeSubscription.MONTHLY.toString())){
             subscription.setCommunication(utils.LCUtil.prepareMessageBean("subscription.month")
                     + " (" + price + ")"
                             + " period : " + subscription.getStartDate().format(DateTimeFormatter.ISO_DATE)
                                   + " - " + subscription.getEndDate().format(DateTimeFormatter.ISO_DATE));
       }
        if(subscription.getSubCode().equals(etypeSubscription.YEARLY.toString())){
             subscription.setCommunication(utils.LCUtil.prepareMessageBean("subscription.year")
              + " (" + price + ")");
        }
        if(subscription.getSubCode().equals(etypeSubscription.TRIAL.toString())){ // pas de reference payment
            subscription.setPaymentReference("Trial - No reference");
            subscription.setCommunication(utils.LCUtil.prepareMessageBean("subscription.trial")
              + " (" + price + ")");
        }
        if(subscription.getSubCode().equals(Subscription.etypeSubscription.INITIAL.toString())){
            subscription.setPaymentReference("Initial - No reference");
            subscription.setTrialCount((short)1);
            subscription.setCommunication(utils.LCUtil.prepareMessageBean("subscription.initial"));
        }
      return subscription;
}catch (Exception ex){
    String msg = "Exception in completePriceAndCommunication ! " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return null;
}finally{}
} //end method

public Subscription complete(Subscription subscription, Connection conn) throws Exception{
try{
        LOG.debug("entering complete with subscription = " + subscription); // id and subcode
    //    LOG.debug("entering LoadSubscription.load with lastSubscription = " + lastSubscription);
 //1. completes startDate endDate
 ///     
   //   List<Subscription> listSubscription = null;
 //     Subscription v = null;  enlevé 14-04-2024
      if(subscription.getSubCode().equals(etypeSubscription.INITIAL.toString())){
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(subscription.getStartDate().plusMonths(1));
         //   subscription = new completePriceAndCommunication(subscription);
            subscription = new PaymentsSubscriptionController().completePriceAndCommunication(subscription);
            return subscription;
      }
      Player player = new Player();
      player.setIdplayer(subscription.getIdplayer());
      List<Subscription> listSubscription = new find.FindCurrentSubscription().payments(player,"latest",conn);

      // si pas de subscription,, revient avec une liste nulle
      LOG.debug("listSubscription = " + listSubscription);
      if(listSubscription == null){
          LOG.debug("il n'y a pas de subscriptions dans la liste");
          
          if(subscription.getSubCode().equals(etypeSubscription.MONTHLY.toString())){
             subscription.setStartDate(LocalDateTime.now());  
             subscription.setEndDate(subscription.getStartDate().plusMonths(1));
  //          LOG.debug("endDate = " + subscription.getEndDate());
          }
          if(subscription.getSubCode().equals(etypeSubscription.YEARLY.toString())){
             subscription.setStartDate(LocalDateTime.now());   
             subscription.setEndDate(subscription.getStartDate().plusYears(1));
          }
          if(subscription.getSubCode().equals(etypeSubscription.TRIAL.toString())){
             subscription.setStartDate(LocalDateTime.of(LocalDate.now(), LocalTime.MIN));  // 0.0  new 24-08-2025
             subscription.setEndDate(LocalDateTime.of(LocalDate.now(), LocalTime.MAX).plusDays(1)); // lendemain 23.59  new 24-08-2025
             subscription.setPaymentReference("Trial - No reference");
             subscription.setCommunication(utils.LCUtil.prepareMessageBean("subscription.trial"));
             subscription.setTrialCount(Short.valueOf("0"));
          }
          return subscription;
      }
      
      
      var v = listSubscription.get(0);
            LOG.debug("v.startDate = " + v.getStartDate());
            LOG.debug("v.endDate = " + v.getEndDate());
  
      if(subscription.getSubCode().equals(etypeSubscription.TRIAL.toString())){
         //  subscription.setEndDate(subscription.getStartDate().plusMonths(1));
        //   subscription.setStartDate(subscription.getStartDate().plusMonths(1)); enlevé 14-08-2025
           subscription = completeTrial(subscription, v);
         //  subscription = completePriceAndCommunication(subscription);
              LOG.debug("subscription trial after completeTrial = " + subscription);
           subscription = new PaymentsSubscriptionController().completePriceAndCommunication(subscription);
              LOG.debug("subscription trial completed with proce and communication = " + subscription);
           return subscription;
      }
// autre que TRIAL
      if(LocalDateTime.now().isBefore(v.getEndDate())){   // renouvellement subscription avant fin subscription en cours
            LOG.debug("case isBefore");
          subscription.setStartDate(v.getEndDate().plusDays(1));
           LOG.debug("startDate = end + 1 day " + subscription.getStartDate());
      }else{
          LOG.debug("case NOT isBefore");
          subscription.setStartDate(LocalDateTime.now());
      }   

      if(subscription.getSubCode().equals(etypeSubscription.MONTHLY.toString())){
         subscription.setEndDate(subscription.getStartDate().plusMonths(1));
  //          LOG.debug("endDate = " + subscription.getEndDate());
      }
      
      if(subscription.getSubCode().equals(etypeSubscription.YEARLY.toString())){
         subscription.setEndDate(subscription.getStartDate().plusYears(1));
      }
  subscription = new PaymentsSubscriptionController().completePriceAndCommunication(subscription);
   //   subscription = new completePriceAndCommunication(subscription);
    //concerne les payements mensuels et annuels : si payement, réinitialisation du trialCount !      
  //     Short s = 0;
       subscription.setTrialCount((short)0);
  return subscription;
  
}catch (Exception ex){
    LOG.error("Exception in complete ! " + ex);
    showMessageFatal("Exception in complete = " + ex.toString() );
     return null;
}finally{}
} //end method

public boolean createPayment(Subscription subscription, Connection conn){ 
 try{
         LOG.debug("entering createPayment Subscription");
         LOG.debug("with subscription = " + subscription);
      subscription = complete(subscription, conn);
         LOG.debug("subscription completed = " + subscription);

      if(new create.CreatePaymentSubscription().create(subscription, conn)){ //true
        String msg = prepareMessageBean("subscription.success") + subscription;
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
        String msg = "Exception in createPayment " + ex.getLocalizedMessage();
        LOG.error(msg);
        showMessageFatal(msg);
        return false;
  }
 } //end method createPaymentCotisation

public static double findTarif (Subscription subscription){
  final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
     LOG.debug(" -- Start of " + methodName);
     LOG.debug(" --  with Subscription = " + subscription);
     String price = "";
 try {
        if(subscription.getSubCode().equals(etypeSubscription.MONTHLY.toString())){
             price = utils.LCUtil.findProperties("subscription", "month");
                 LOG.debug("Monthly subscription price = " + price);
             return Double.parseDouble(price);
        }
        if(subscription.getSubCode().equals(etypeSubscription.YEARLY.toString())){
             price =  utils.LCUtil.findProperties("subscription", "year");
                 LOG.debug("Yearly subscription price = " + price);
             return Double.parseDouble(price);
         }
        if(subscription.getSubCode().equals(etypeSubscription.TRIAL.toString())){
             return Double.parseDouble("0.0");
         }
        if(subscription.getSubCode().equals(etypeSubscription.INITIAL.toString())){
             return Double.parseDouble("0.0");
         }
 } catch (Exception e) {
      String msg = " -- Error in findTarifSubscription " + e.getMessage();
      LOG.error(msg);
      showMessageFatal(msg);
      return 99;
 }
 finally { }
      return 99;
} // end method 

public Subscription isExists(Player player, Connection conn) throws SQLException{ 
    try{
     LOG.debug("entering isExists with player = " + player);
        Subscription subscription = new Subscription(); 
        subscription.setIdplayer(player.getIdplayer());
        if(new find.FindSubscriptionStatus().find(subscription, player, conn)){  //true
           String msg = "Subscription OK, found = " + subscription;
           LOG.debug(msg);
           List<Subscription> listSubscription = new find.FindCurrentSubscription().payments(player,"now",conn);
           subscription = listSubscription.getFirst();
           subscription.setErrorStatus(false);
        }else{
             // LOG.debug(" subscription verification NOT ok, going to : subscription.xhtml");  // no valid subscription
           String msg = LCUtil.prepareMessageBean("subscription.invalid"); 
           LOG.error(msg);
         //  showMessageFatal(msg);
  ///   16-04-      List<Subscription> listSubscription = new find.FindCurrentSubscription().payments(player,conn);
  ///         subscription = listSubscription.get(0);
           subscription.setErrorStatus(true);
        //   subscription = null;
         //  return "subscription.xhtml?faces-redirect=true";
        }
        return subscription;
    } catch (Exception e) {
            String msg = "£££ Exception in isValid  = " + e.getMessage(); // + " ,SQLState = "
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
   }       
}

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