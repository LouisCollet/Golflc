
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
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import update.UpdatePassword;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

@Named("passwordC")
@SessionScoped

public class PasswordController implements Serializable, interfaces.Log{

public PasswordController() {
    //
}
// new 02-03-2024
public Password isExists(EPlayerPassword epp, Connection conn) throws SQLException{ 
//public EPlayerPassword resetPassword(EPlayerPassword epp, Activation activation, final Connection conn) throws SQLException, Throwable { 
    try{
LOG.debug("1. verifying if there is an existing password");
     Password password = epp.getPassword();
 //       LOG.debug("entite password = " + password);
 //       LOG.debug("player password = " + password.getPlayerPassword());
     if(password.getPlayerPassword() == null){
          String err = LCUtil.prepareMessageBean("password.empty"); // + " = " + password.getPlayerPassword(); 
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
            String msg = "£££ Exception in isValid  = " + e.getMessage(); // + " ,SQLState = "
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
   }
}
public boolean isBlocking(Player player, Connection conn) throws SQLException{ 
    try{
        if(passwordBlocking(player, conn)){   //dans CourseController
             String err = LCUtil.prepareMessageBean("password.blocked"); // + blocking.getBlockingRetryTime().format(ZDF_TIME); // ,player.getPlayerPassword()); 
             LOG.error(err);
             showMessageFatal(err);
             return true; //"selectPlayer.xhtml?faces-redirect=true";
          }else{
            return false;
          }
  } catch (Exception e) {
            String msg = "£££ Exception in isBlocking  = " + e.getMessage(); // + " ,SQLState = "
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }
}

 public boolean passwordBlocking (Player player, Connection conn){
try{ // coming from selectPlayer 5926
//    LOG.debug(" starting passwordBlocking ? ");
 //   LOG.debug("for player = " + player);
    // find
    Blocking blocking = new read.LoadBlocking().load(player,conn);
 //       LOG.debug("blocking after LoadBlocking = " + blocking);
    if(blocking == null){
        LOG.debug("pas de blocage pour ce player");
 //       LOG.debug("blocking = " + blocking);
        // pas de blocage
        return false;
    }
    if(blocking != null){
        // comparer 
  //      LOG.debug("blocking is not nul = " + blocking);
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
             boolean b = new delete.DeleteBlocking().delete(player,conn);
       // new 29-06-2020  // a faire : tester sur le résultat
             Short s = 0;
             blocking.setBlockingAttempts(s);
             LOG.debug("result delete = " + b);
             return false;
             }
        }
    return true;
  } catch (Exception e) {
            String msg = "££ Exception in passwordVerification = " + e.getMessage() + " for player = " + player.getPlayerLastName();
            LOG.error(msg);
            showMessageFatal(msg);
            return true; // indicates that the same view should be redisplayed
        } finally {        }  
} // end method passwordBlocking




public EPlayerPassword resetPassword(Activation activation, Connection conn) throws SQLException, Throwable { 
//public EPlayerPassword resetPassword(EPlayerPassword epp, Activation activation, final Connection conn) throws SQLException, Throwable { 
    try{
    //    LOG.debug("entering resetPassword for player = " + player);
        LOG.debug("entering resetPassword for activation = " + activation);
        Player player = new Player();
        player.setIdplayer(activation.getActivationPlayerId());
        EPlayerPassword epp = new EPlayerPassword();
        epp.setPlayer(player);
        epp = new read.ReadPlayer().read(epp, conn); // 2e version, la première reste valable output = player only
        player= epp.getPlayer(); // partial
//        password = epp.getPassword();
//        Player player = epp.getPlayer();
        Password password = epp.getPassword();
        
 //   LOG.debug("starting checkPassword with epp = " + epp);
 //   LOG.debug("activation = " + activation);
    
    if(player.getIdplayer() != null){ // trouvé dans table Activation et < 10 minutes
        LOG.debug("OK : idplayer ready for delete activation  = " + player.getIdplayer() );
    //    boolean b = new DeleteActivation().delete(conn, activation.getActivationKey());// delete record dans table Activation
        if(! new DeleteActivation().delete(conn, activation.getActivationKey())){
            String msg = "Failure delete record in Table activation !!!";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
            //throw new Exception(msg);
        }else{  // delete OK else 1
            LOG.debug("Success record deleted in Table activation  = ");
//            b = false;
            password.setWrkpassword("RESET PASSWORD");
               LOG.debug("Wrkpassword =   = " + password.getWrkpassword());
      //       LOG.debug("player = " + player);
            epp.setPassword(password);
    // 2e action          
      //    b = new UpdatePassword().update(epp, conn);
          if(! new UpdatePassword().update(epp, conn)){
                String msg = "Failure ModifyPassword/RESET in Table player !!!";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return null;
          }else{
                 String msg = "Sucessfull ModifyPassword/RESET  in Table player = ";
                    LOG.debug(msg);
            //        LCUtil.showMessageInfo(msg);
                // send mail to user
                new mail.ResetPasswordMail().sendMailResetOK(epp.getPlayer());
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
        LCUtil.showMessageFatal(msg);
        throw new Exception("time limit for the second time : getCause = " + e.getCause());
   } catch (Exception e) {
            String msg = "£££ Exception in activation controller  = " + e.getMessage(); // + " ,SQLState = "
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;        
}finally{
    //     DBConnection.closeQuietly(null, null, null,null); 
          }
} // end checkPassword

 public void main(String[] args) throws Exception, TimeLimitException, Throwable {
      Connection conn = new DBConnection().getConnection();
  try{
   //   Player player= new Player();
   //   player.setIdplayer(324720);
      Activation activation = new Activation();
    //  EPlayerPassword epp = new EPlayerPassword();
    //  epp.setPlayer(player);
      activation.setActivationPlayerId(324720);
      activation.setActivationKey("fcb35e1e-970d-46fc-88f0-929a8555d0d8");
      EPlayerPassword epp = new PasswordController().resetPassword(activation,conn);
            LOG.debug("from main, after !! = " + epp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main//
} // end class