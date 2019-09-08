
package lc.golfnew;

import delete.DeleteActivation;
import entite.Player;
import exceptions.TimeLimitException;
import find.FindActivationPlayer;
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

public class PasswordController implements Serializable, interfaces.Log
{
// key = le nom de la field dans http://localhost:8080/HelloGolf-1.0-SNAPSHOT/activation_check.xhtml?key="keyLC"
//private String uuid;
//private boolean valid;
//private Player player = null;

public PasswordController() {
    //
}
public Boolean checkPassword(String uuid, Connection conn) throws SQLException, Throwable { 
 try{
    LOG.info("starting checkPassword with uuid = " + uuid);  // récupérer de fViewParam dans password_check.xhtml
  //  FindActivationPlayer fap = new FindActivationPlayer();  // new instance
    Player p = new Player();
    p = new FindActivationPlayer().find(conn, uuid);
  // vérifie si uuid est en attente dans table activation
        LOG.info(" checkPassword : player returned from findActivationPlayer = " + p.getIdplayer());
    if(p.getIdplayer() == null){ // pas trouvé dans table Activation
         String msg = "Player not found in findActivationPlayer";
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
         return false;
      //   throw new Exception(msg);
     } 
    if(p.getIdplayer() != null){ // trouvé dans table Activation et < 10 minutes
        LOG.info("OK : idplayer ready for activation  = " + p.getIdplayer() );
    //    DeleteActivation da = new DeleteActivation();
        boolean b = new DeleteActivation().delete(conn, uuid);
        // delete record dans table Activation
        if(b == false){
            String msg = "Failure delete record Table activation !!!";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
            //throw new Exception(msg);
        }else{  // delete OK else 1
            LOG.info("Success record deleted in Table activation  = ");
            b = false;
          p.setWrkpassword("RESET PASSWORD");
       //   ModifyPassword mp = new ModifyPassword();
          b = new ModifyPassword().modifypassword(p, conn); //
          if(b == false){
                String msg = "Failure ModifyPassword/RESET in Table player !!!";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
          }else{
                 String msg = "Sucessfull ModifyPassword/RESET  in Table player = ";
                    LOG.info(msg);
            //        LCUtil.showMessageInfo(msg);
                // send mail to user
                new mail.ResetPasswordMail().sendMailResetOK(p);
                  return true;
            } //end else 1
        } //end else 2
    } //else{ // player non trouvé dans table activation
     return false;
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
         boolean b = new PasswordController().checkPassword("fcb35e1e-970d-46fc-88f0-929a8555d0d8",conn);
            LOG.info("from main, after !! = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main//
} // end class