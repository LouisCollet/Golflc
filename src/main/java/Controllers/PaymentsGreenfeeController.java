package Controllers;

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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class PaymentsGreenfeeController implements Serializable, interfaces.Log{
    
public PaymentsGreenfeeController(){ // constructor
    //
}
// new 21-01-2023 vient de coursecontroller
public boolean RegisterPaymentandInscription(final Creditcard creditcard, final Greenfee greenfee, final Player player, final Round round, final Club club, 
        final Course course,
        Inscription inscription,
        final Connection conn) throws SQLException, Exception {
try{
           LOG.debug("entering RegisterPaymentandInscriptionGreenfee");
           LOG.debug("with greenfee = " + greenfee);
 ///          LOG.debug("with inscription = " + inscription);

  // 1. Register payment         
       //   if(! new PaymentsGreenfeeController().payment(player, greenfee, conn)){ 
               if(! payment(player, greenfee, conn)){ 
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
}catch (Exception e) {
            String msg = "££ Exception in RegisterPaymentandInscriptionGreenfee  = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
           return false; // indicates that the same view should be redisplayed
     //   } finally {}
//return null;
   } 
} //end method

public String manageGreenfee(TarifGreenfee tarifGreenfee, Club club, Round round, Player player, Connection conn) throws Exception{ // called from price_round_greenfee.xhtml
 try{
         LOG.debug("entering manageGreenfee");
         LOG.debug("entering manageGreenfee for tarifGreenfee " + tarifGreenfee);
         LOG.debug("entering manageGreenfee for round = " + round);
         LOG.debug("entering manageGreenfee for club = " + club);
         LOG.debug("entering manageGreenfee for player = " + player);

 ///        LOG.debug("sessionMap creditcardType created = " + sessionMap.get("creditcardType"));
  //   LOG.debug("manageGreenfee Greenfee = " + greenfee.toString());
 //      LOG.debug("à ce moment tarif greenfee est uniquement complété par idplayer= "); // + tarifGreenfee.toString());
              // concerne le payment avec carte de crédit
 
       Greenfee greenfee = new TarifGreenfeeController().completeGreenfee(tarifGreenfee, club, round, player); 
            LOG.debug("Greenfee completed = " + greenfee);
  // non ?    greenfee.setIdplayer(player.getIdplayer());      
////      greenfee.setPaymentReference(creditcard.getReference());  //  mod 03-01-2022
            LOG.debug("Greenfee with paymentReference = " + greenfee.toString());
 //      boolean OK = false;
       if(greenfee.getPrice() == 0){
          String msg = "amount ZERO,  no payment needed !!";
          LOG.info(msg);
          showMessageInfo(msg);
          return null;
       }   
   //     if(CompleteCreditcardWithGreenfee(greenfee, player, conn)){   // le paiement par carte de crédit est exécuté correctement
        Creditcard creditcard = new CreditcardController().completeWithGreenfee(greenfee, player, conn);  // le paiement par carte de crédit est exécuté correctement
             LOG.debug("creditcard is now completed by greenfee info = " + creditcard);
        if(creditcard != null){    
            String msg = "Paiement Greenfee par creditcard OK"  + greenfee;
            LOG.info(msg);
            showMessageInfo(msg);
            return "inscription.xhtml?faces-redirect=true";
        }else{
            String msg = "paiement Greenfee par creditcard KO : quelle conclusion ?";
            LOG.error(msg);
            showMessageFatal(msg);
            throw new Exception(msg);
        }
 //    LOG.debug("after call to creditcardpayment");
  }catch (Exception ex){
            String msg = "Exception in manageGreenfee " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
//         return null;
 } //end method manageGreenfee





private static boolean payment(Player player, Greenfee greenfee, Connection conn){
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
                boolean b = new create.CreatePaymentGreenfee().create(p, greenfee, conn);
                   LOG.debug("create other players OK");
            } //end for
        
        } //end if

      if(new create.CreatePaymentGreenfee().create(player,greenfee, conn)){ // true
  //        LOG.debug("after createGreenfee : we are OK");
          String msg = LCUtil.prepareMessageBean("greenfee.success") 
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
