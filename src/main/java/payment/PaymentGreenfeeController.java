package payment;

// import Controllers.CreditcardController; // removed 2026-02-25
// import Controllers.TarifGreenfeeController; // removed 2026-02-25
import entite.Club;
import entite.Course;
import entite.Creditcard;
import entite.Greenfee;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.TarifGreenfee;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import java.io.*;
// import java.sql.Connection; // removed 2026-02-25
import java.sql.SQLException;
import java.util.Arrays;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentGreenfeeController implements Serializable, interfaces.Log{

    private static final long serialVersionUID = 1L;

    @Inject private create.CreateInscription createInscriptionService; // migrated 2026-02-25
    @Inject private create.CreatePaymentGreenfee createPaymentGreenfeeService; // migrated 2026-02-25

public PaymentGreenfeeController(){ // constructor
    //
}
// new 21-01-2023 vient de coursecontroller
public boolean RegisterPaymentandInscription(final Creditcard creditcard, final Greenfee greenfee, final Player player, final Round round, final Club club,
        final Course course,
        Inscription inscription) throws SQLException, Exception { // conn removed 2026-02-25
try{
           LOG.debug("entering RegisterPaymentandInscriptionGreenfee");
           LOG.debug("with greenfee = " + greenfee);
 ///          LOG.debug("with inscription = " + inscription);

  // 1. Register payment
       //   if(! new PaymentsGreenfeeController().payment(player, greenfee, conn)){
               if(! payment(player, greenfee)){ 
                  String msg = "Create Payment Greenfee FAILED - no round inscription accepted !!";
                  LOG.error(msg);
                  showMessageFatal(msg);
                  throw new Exception(msg);
          }
// true
             String msg = "PaymentGreenfee registered"; 
           LOG.debug(msg);
  // 2. Register Inscription 
          //    Inscription inscription = new Inscription();
              
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
}catch (Exception e) {
            String msg = "££ Exception in RegisterPaymentandInscriptionGreenfee  = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
           return false; // indicates that the same view should be redisplayed
     //   } finally {}
//return null;
   } 
} //end method

/* legacy — not called by registrars — uses new TarifGreenfeeController() and new CreditcardController()
public String manageGreenfee(TarifGreenfee tarifGreenfee, Club club, Round round, Player player) throws Exception{
    // commented out 2026-02-25
} //end method manageGreenfee
*/





private boolean payment(Player player, Greenfee greenfee) { // static removed + conn removed 2026-02-25
try{
        LOG.debug("entering payment");
        LOG.debug("with greenfee = " + greenfee);
        LOG.debug("droppedPlayers is before : " + Arrays.toString(player.getDroppedPlayers().toArray()));
        int size = player.getDroppedPlayers().size();
        LOG.debug("size/number of iterations players = " + size );
        if(size != 0){
   //         Player p = new Player();
            for(int i=0; i < size; i++){
    //        LOG.debug(" -- treated idplayer = " + player.getDroppedPlayers().get(i).getIdplayer() );
                Player p = player.getDroppedPlayers().get(i);
                   LOG.debug("we have to CreateGreenfee for :" + p.toString());
                // boolean b = new create.CreatePaymentGreenfee().create(p, greenfee);
                boolean b = createPaymentGreenfeeService.create(p, greenfee); // migrated 2026-02-25
                   LOG.debug("create other players OK");
            } //end for

        } //end if

      // if(new create.CreatePaymentGreenfee().create(player, greenfee)){ // true
      if(createPaymentGreenfeeService.create(player, greenfee)){ // migrated 2026-02-25
  //        LOG.debug("after createGreenfee : we are OK");
          String msg = prepareMessageBean("greenfee.success") 
                + greenfee.getRoundDate().format(ZDF_DAY) + " - " 
                + " Round = " + greenfee.getIdround();
          LOG.info(msg);
          showMessageInfo(msg);
      return true;
     }else{
        String msg = "Error :Greenfee NOT paid !!";
        LOG.error(msg);
 //       showMessageFatal(msg);
        return false; 
    }
 }catch (SQLException e){
            String msg = "SQL Exception in PaymentsGreenfeeController = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }catch (Exception ex){
            String msg = "Exception in PaymentsGreenfeeController " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }
 } //end method createPaymentGreenfee



    /*
    void main() throws Exception, Throwable {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
/*
public boolean createPaymentCotisation(Player player, Cotisation cotisation, Connection conn){ 
 try{
      LOG.debug("entering createPaymentCotisation");
      LOG.debug("with cotisation = " + cotisation);
 
            
      if(new create.CreatePaymentCotisation().create(cotisation, conn)){ //true
        String msg = LCUtil.prepareMessageBean("cotisation.success")
                  + cotisation //.getCotisationStartDate().format(ZDF_DAY) + " - " 
          //        + cotisation.getCotisationEndDate().format(ZDF_DAY)
                  + " for club = " + cotisation.getIdclub();
        LOG.info(msg);
        showMessageInfo(msg);
        return true;
     }else{
        String msg = "Error : cotisation NOT modified !!";
        LOG.error(msg);
        showMessageFatal(msg);
        return false; // retourne d'ou il vient : où ??
    }
  }catch (Exception ex){
            String msg = "Exception in createCotisation " + ex.getLocalizedMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }
 } //end method createPaymentCotisation
*/
