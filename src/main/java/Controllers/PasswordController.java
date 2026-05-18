
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

    private static final long serialVersionUID = 1L;

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
    private cache.CacheInvalidator cacheInvalidator;
// new 02-03-2024
public Password isExists(EPlayerPassword epp){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    try{
LOG.debug("1. verifying if there is an existing password");
     Password password = epp.password();
     if(password.getPlayerPassword() == null){
          String err = utils.LCUtil.prepareMessageBean("password.empty");
          LOG.debug("{}", err);
          showMessageFatal(err);
          password = null;
      }else{
          LOG.info("There is a password for this player");
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
    // find
    Blocking blocking = loadBlocking.load(player);
    if(blocking == null){
        LOG.debug("pas de blocage pour ce player");
        return false;
    }
    if(blocking != null){
        if(blocking.getBlockingAttempts() < 3){
            LOG.info("attempts = {} thus < than maximum 3", blocking.getBlockingAttempts());
            return false; // pas de blocage
        }
        if(LocalDateTime.now().isBefore(blocking.getBlockingRetryTime())){
            String msg = "blocage for now = " + LocalDateTime.now().format(ZDF_TIME) + " Retrytime = "
               + blocking.getBlockingRetryTime().format(ZDF_TIME);
            LOG.debug("{}", msg);
            showMessageFatal(msg);
            return true;
        }else{
             LOG.debug("temps de blocage dépassé - delete record now = {} Retrytime = {}", LocalDateTime.now().format(ZDF_TIME), blocking.getBlockingRetryTime().format(ZDF_TIME));
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
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    try{
        LOG.debug("entering resetPassword for activation = {}", activation);
        Player player = new Player();
        player.setIdplayer(activation.getActivationPlayerId());
        Password password = null;
        EPlayerPassword epp = new EPlayerPassword(player, password);

    // Lecture du Player + password en une seule ligne
    epp = playerManager.readPlayerWithPassword(epp.player().getIdplayer());
        player= epp.player();
        password = epp.password();

    if(player.getIdplayer() != null){ // trouvé dans table Activation et < 10 minutes
        LOG.debug("OK : idplayer ready for delete activation  = {}", player.getIdplayer());
        if(! deleteActivation.delete(activation.getActivationKey())){
            String msg = "Failure delete record in Table activation !!!";
            LOG.error("{}", msg);
            showMessageFatal(msg);
            return null;
        }else{  // delete OK else 1
            LOG.debug("Success record deleted in Table activation");
            password.setWrkpassword("RESET PASSWORD");
               LOG.debug("Wrkpassword =   = {}", password.getWrkpassword());
            epp.withPassword(password);

    // 2e action
          if(! updatePassword.update(epp)){
                String msg = "Failure ModifyPassword/RESET in Table player !!!";
                LOG.error("{}", msg);
                showMessageFatal(msg);
                return null;
          }else{
                    LOG.debug("Sucessfull ModifyPassword/RESET in Table player");
                    cacheInvalidator.invalidatePlayersList();
                resetPasswordMail.sendMailResetOK(epp.player());
                return epp;
            } //end else 1
        } //end else 2
    } //else{ // player non trouvé dans table activation
    LOG.debug("player not found in activation or too late !");
     return null;
  }catch (TimeLimitException e){
        LOG.error("%%TimeLimitException in PasswordController.checkpassword", e);
        showMessageFatal("%%TimeLimitException in PasswordController.checkpassword: " + e.getMessage());
        throw new Exception("time limit for the second time : getCause = " + e.getCause());
   } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
   }
} // end resetPassword

} // end class