
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

public PasswordController() // constructor
{
    //
}
public Boolean checkPassword(String uuid, Connection conn) throws SQLException, Throwable { 
 try{
    LOG.info("starting checkPassword with uuid = " + uuid);  // récupérer de fViewParam dans password_check.xhtml
// les lignes suivantes ne fonctionnent pas : il s'agit d'un autre contexte ,??
//    FacesContext context = FacesContext.getCurrentInstance();
//        Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
//        String keyLC = paramMap.get("uuid");
//    LOG.info("keyLC  = " + keyLC);
//    String keyLC2 = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("uuid");
//    LOG.info("keyLC2  = " + keyLC2);

    FindActivationPlayer fap = new FindActivationPlayer();  // new instance
//        LOG.info("after fap instantiation");
    Player player = new Player();
    player = fap.findActivationPlayer(conn, uuid);
  // vérifie si uuid est en attente dans table activation
        LOG.info(" checkPassword : player returned from findActivationPlayer = " + player.getIdplayer());
    if(player.getIdplayer() == null){ // pas trouvé dans table Activation
         String msg = "Player not found in findActivationPlayer";
         LOG.error(msg);
    //     LCUtil.showMessageFatal(msg);
         return false;
      //   throw new Exception(msg);
     } 
    if(player.getIdplayer() != null) // trouvé dans table Activation et < 10 minutes
    {
        LOG.info("OK : idplayer ready for activation  = " + player.getIdplayer() );
    //     conn = DBConnection.getConnection();
        DeleteActivation da = new DeleteActivation();
        boolean b = da.delete(conn, uuid);
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
          player.setWrkpassword("RESET PASSWORD");
          ModifyPassword mp = new ModifyPassword();
          b = mp.modifypassword(player, conn); //
          if(b == false){
                String msg = "Failure ModifyPassword/RESET in Table player !!!";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
               // throw new Exception(msg);
                return false;
          }else{
                 String msg = "Sucessfull ModifyPassword/RESET  in Table player = ";
                    LOG.info(msg);
            //        LCUtil.showMessageInfo(msg);
                // send mail to user
                
                mail.ResetPasswordMail rpm = new mail.ResetPasswordMail();
                rpm.sendMailResetOK(player);

                        
                        
                  return true;
            } //end else 1
        } //end else 2
    } //else{ // player non trouvé dans table activation
        
  //  }
  //   LOG.info(" result valid in checkPassword = " + valid);
     return false;
  //   if(valid){ 
     //    String language = player.getPlayerLanguage();
 //        String playerid = Integer.toString(player.getIdplayer());
   //      return "password_create.xhtml?faces-redirect=true&language=" + language + "&id=" + playerid;
    // }else{
    //     return "activation_failure.xhtml?faces-redirect=true";
  //   }
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
 //   public void setValid(boolean valid) {
  //      this.valid = valid;
  //  }

 //   public void setPlayer(Player player) {
 //       this.player = player;
 //   }

 public void main(String[] args) throws Exception, TimeLimitException, Throwable {
     
     Connection conn = new DBConnection().getConnection();
  try{
     //   PasswordController pc = new PasswordController();
     //   boolean b = pc.checkPassword("fcb35e1e-970d-46fc-88f0-929a8555d0d8",conn);
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