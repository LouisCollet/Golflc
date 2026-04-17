
package Controllers;

import delete.DeleteActivation;
import entite.Activation;
import entite.Blocking;
import entite.composite.EPlayerPassword;
import entite.Password;
import entite.Player;
import exceptions.TimeLimitException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.*;
import java.time.LocalDateTime;
import jakarta.inject.Inject;
import manager.PlayerManager;
import static exceptions.LCException.handleGenericException;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showMessageFatal;

@Named("passwordC")
@SessionScoped

public class PasswordController implements Serializable, interfaces.Log{

public PasswordController() {
    //
}
// Injection ou récupération du PlayerManager (CDI recommandé)
    @Inject
    private PlayerManager playerManager;
    @Inject
    private read.LoadBlocking loadBlocking;                 // migrated 2026-02-24
    @Inject
    private delete.DeleteActivation deleteActivation;       // migrated 2026-02-24
    @Inject
    private delete.DeleteBlocking deleteBlocking;           // migrated 2026-02-24
    @Inject
    private update.UpdatePassword updatePassword;           // migrated 2026-02-24
    @Inject
    private mail.ResetPasswordMail resetPasswordMail;       // migrated 2026-02-26
    @Inject
    private lists.PlayersList playersListService;            // fix password cache bug 2026-03-07
// new 02-03-2024
public Password isExists(EPlayerPassword epp){
//public EPlayerPassword resetPassword(EPlayerPassword epp, Activation activation, final Connection conn) throws SQLException, Throwable {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    try{
LOG.debug("1. verifying if there is an existing password");
     Password password = epp.password();
 //       LOG.debug("entite password = {}", password);
 //       LOG.debug("player password = {}", password.getPlayerPassword());
     if(password.getPlayerPassword() == null){
          String err = utils.LCUtil.prepareMessageBean("password.empty"); // + " = " + password.getPlayerPassword(); 
          LOG.debug(err);
          showMessageFatal(err);
          password = null;
   //     return password;
      }else{
          String msg = "There is a password for this player";
          LOG.info(msg);
    //      showMessageInfo(msg);
      }
     return password;
   } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
   }
}
public boolean isBlocking(Player player){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    try{
        if(passwordBlocking(player)){
             String err = prepareMessageBean("password.blocked"); // + blocking.getBlockingRetryTime().format(ZDF_TIME); // ,player.getPlayerPassword()); 
             LOG.error(err);
             showMessageFatal(err);
             return true; //"selectPlayer.xhtml?faces-redirect=true";
          }else{
            return false;
          }
  } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
   }
}

 public boolean passwordBlocking (Player player){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
try{ // coming from selectPlayer 5926
//    LOG.debug(" starting passwordBlocking ? ");
 //   LOG.debug("for player = {}", player);
    // find
    Blocking blocking = loadBlocking.load(player);
 //       LOG.debug("blocking after LoadBlocking = {}", blocking);
    if(blocking == null){
        LOG.debug("pas de blocage pour ce player");
 //       LOG.debug("blocking = {}", blocking);
        // pas de blocage
        return false;
    }
    if(blocking != null){
        // comparer 
  //      LOG.debug("blocking is not nul = {}", blocking);
        if(blocking.getBlockingAttempts() < 3){
            String msg = "attempts = " + blocking.getBlockingAttempts() + " thus < than maximum 3";
            LOG.info(msg);
        //    showMessageFatal(msg);
            return false; // pas de blocage
        }
        if(LocalDateTime.now().isBefore(blocking.getBlockingRetryTime())){
            String msg = "blocage for now = " + LocalDateTime.now().format(ZDF_TIME) + " Retrytime = " 
               + blocking.getBlockingRetryTime().format(ZDF_TIME);
            LOG.debug(msg);
            showMessageFatal(msg);
            return true;
        }else{
             String msg = "temps de blocage dépassé - delete record"+ " now = " + LocalDateTime.now().format(ZDF_TIME) + " Retrytime = " 
               + blocking.getBlockingRetryTime().format(ZDF_TIME);
             LOG.debug(msg);
             boolean b = deleteBlocking.delete(player);
       // new 29-06-2020  // a faire : tester sur le résultat
             Short s = 0;
             blocking.setBlockingAttempts(s);
             LOG.debug("result delete = {}", b);
             return false;
             }
        }
    return true;
  } catch (Exception e) {
            handleGenericException(e, methodName);
            return true; // indicates that the same view should be redisplayed
        }
} // end method passwordBlocking




public EPlayerPassword resetPassword(Activation activation) throws Exception{
//public EPlayerPassword resetPassword(EPlayerPassword epp, Activation activation, final Connection conn) throws SQLException, Throwable {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    try{
    //    LOG.debug("entering resetPassword for player = {}", player);
        LOG.debug("entering resetPassword for activation = {}", activation);
        Player player = new Player();
        player.setIdplayer(activation.getActivationPlayerId());
        Password password = null; // ?? record 2026
        EPlayerPassword epp = new EPlayerPassword(player, password);


    // Lecture du Player + password en une seule ligne
    epp = playerManager.readPlayerWithPassword(epp.player().getIdplayer());
       // epp = new read.ReadPlayer().read(epp, conn); // 2e version, la première reste valable output = player only
        player= epp.player(); // partial
//        password = epp.getPassword();
//        Player player = epp.getPlayer();
        password = epp.password();
        
        
 //   LOG.debug("starting checkPassword with epp = {}", epp);
 //   LOG.debug("activation = {}", activation);
    
    if(player.getIdplayer() != null){ // trouvé dans table Activation et < 10 minutes
        LOG.debug("OK : idplayer ready for delete activation  = {}", player.getIdplayer());
    //    boolean b = new DeleteActivation().delete(conn, activation.getActivationKey());// delete record dans table Activation
        if(! deleteActivation.delete(activation.getActivationKey())){
            String msg = "Failure delete record in Table activation !!!";
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
            //throw new Exception(msg);
        }else{  // delete OK else 1
            LOG.debug("Success record deleted in Table activation  = ");
//            b = false;
            password.setWrkpassword("RESET PASSWORD");
               LOG.debug("Wrkpassword =   = {}", password.getWrkpassword());
      //       LOG.debug("player = {}", player);
         //   epp.setPassword(password);
            epp.withPassword(password); // migration record 2026
            
    // 2e action          
      //    b = new UpdatePassword().update(epp, conn);
          if(! updatePassword.update(epp)){
                String msg = "Failure ModifyPassword/RESET in Table player !!!";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
          }else{
                 String msg = "Sucessfull ModifyPassword/RESET  in Table player = ";
                    LOG.debug(msg);
                    playersListService.invalidateCache(); // fix password cache bug 2026-03-07
            //        LCUtil.showMessageInfo(msg);
                // send mail to user
                // new mail.ResetPasswordMail().sendMailResetOK(...)
                resetPasswordMail.sendMailResetOK(epp.player()); // migrated 2026-02-26
                return epp;
            } //end else 1
        } //end else 2
    } //else{ // player non trouvé dans table activation
    LOG.debug("player not found in activation or too late !");
     return null;
  }catch (TimeLimitException e){
    String msg = " %%TimeLimitException in PasswordController.checkpassword = " + e.toString(); // + ", SQLState = " + e.getSQLState()
        //    + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        showMessageFatal(msg);
        throw new Exception("time limit for the second time : getCause = " + e.getCause());
   } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
   }
} // end resetPassword

/*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Activation activation = new Activation();
        activation.setActivationPlayerId(324720);
        activation.setActivationKey("fcb35e1e-970d-46fc-88f0-929a8555d0d8");
        EPlayerPassword epp = new PasswordController().resetPassword(activation);
        LOG.debug("from main, after !! = {}", epp);
    } // end main
*/
} // end class