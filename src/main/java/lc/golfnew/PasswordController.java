
package lc.golfnew;

import delete.DeleteActivation;
import entite.Activation;
import entite.EPlayerPassword;
import entite.Password;
import entite.Player;
import exceptions.TimeLimitException;
import static interfaces.Log.LOG;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import modify.ModifyPassword;
import utils.DBConnection;
import utils.LCUtil;

@Named("passwordC")
@SessionScoped

public class PasswordController implements Serializable, interfaces.Log{

public PasswordController() {
    //
}
//public Player checkPassword(Player player, Activation activation, Connection conn) throws SQLException, Throwable { 
public EPlayerPassword checkPassword(EPlayerPassword epp, Activation activation, Connection conn) throws SQLException, Throwable { 

    try{
        Player player = epp.getPlayer();
        Password password = epp.getPassword();
        
    LOG.info("starting checkPassword with epp = " + epp);  // récupérer de fViewParam dans password_check.xhtml
    LOG.info("activation = " + activation);
    
    if(player.getIdplayer() != null){ // trouvé dans table Activation et < 10 minutes
        LOG.info("OK : idplayer ready for delete activation  = " + player.getIdplayer() );
        boolean b = new DeleteActivation().delete(conn, activation.getActivationKey());// delete record dans table Activation
        if(b == false){
            String msg = "Failure delete record Table activation !!!";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
            //throw new Exception(msg);
        }else{  // delete OK else 1
            LOG.info("Success record deleted in Table activation  = ");
            b = false;
            
            password.setWrkpassword("RESET PASSWORD");
            
            LOG.info("Wrkpassword =   = " + password.getWrkpassword());
      //       LOG.info("player = " + player);
             epp.setPassword(password);
    // 2e action          
          b = new ModifyPassword().modify(epp, conn);
          if(b == false){
                String msg = "Failure ModifyPassword/RESET in Table player !!!";
                LOG.error(msg);
 //               LCUtil.showMessageFatal(msg);
                return null;
          }else{
                 String msg = "Sucessfull ModifyPassword/RESET  in Table player = ";
                    LOG.info(msg);
            //        LCUtil.showMessageInfo(msg);
                // send mail to user
                new mail.ResetPasswordMail().sendMailResetOK(epp.Eplayer);
                return epp;
            } //end else 1
        } //end else 2
    } //else{ // player non trouvé dans table activation
    LOG.info("player not found in activation or too late !");
     return null;
  }catch (TimeLimitException e){
    String msg = " %%TimeLimitException in PasswordController.checkpassword = " + e.toString(); // + ", SQLState = " + e.getSQLState()
        //    + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        throw new Exception("time limit for the second time : getCause = " + e.getCause());
   } catch (Exception e) {
            String msg = "£££ SQLException in activation controller  = " + e.getMessage(); // + " ,SQLState = "
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
      Player player= new Player();
      player.setIdplayer(324720);
      Activation activation = new Activation();
      EPlayerPassword epp = new EPlayerPassword();
      epp.setPlayer(player);
      activation.setActivationKey("fcb35e1e-970d-46fc-88f0-929a8555d0d8");
         EPlayerPassword p = new PasswordController().checkPassword(epp,activation ,conn);
            LOG.info("from main, after !! = " + p);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main//
} // end class