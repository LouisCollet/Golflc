
package Controllers;

import delete.DeleteActivation;
import entite.Activation;
import entite.Player;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import update.UpdatePlayerActivation;
import utils.LCUtil;

@Named("activationC")
@RequestScoped //@SessionScoped mod 26-08-2023
public class ActivationController implements Serializable, interfaces.Log{
    
// key = le nom de la field dans http://localhost:8080/HelloGolf-1.0-SNAPSHOT/activation_check.xhtml?key="keyLC"
//private String uuid;
private boolean valid;

public ActivationController(){ // constructor
}
// public String check(Player player, Activation activation, Connection conn) throws SQLException, Exception, Throwable{ 
    public String check(Activation activation, Connection conn) throws SQLException, Exception, Throwable{ 
    // c'est ici qu'il faut vérifier !!!
    try{  
    //    LOG.debug("entering check with player = " + player);
          LOG.debug("entering check with activation = " + activation);
     Player player = new Player();
     player.setIdplayer(activation.getActivationPlayerId());
       LOG.debug("searching playerid = " + player.getIdplayer());
     player = new read.ReadPlayer().read(player, conn);
        LOG.debug("player found from activation new Player = " + player); // c'est OK
    if(player.getIdplayer() != null){ // trouvé dans table Activation
            LOG.debug("idplayer ready for activation new player = " + player.getIdplayer() );
     //   boolean b = new DeleteActivation().delete(conn, activation.getActivationKey());
        // delete record dans table Activation
        if(! new DeleteActivation().delete(conn, activation.getActivationKey())){
            LOG.debug("echec delete record Table activation !!!");
            setValid(false);
        }else{
            LOG.debug("OK record deleted in Table activation  = ");
            // update player !!
       //     String d = new ModifyPlayerActivation().modify(player, conn); // setPlayerActivation = 1 
            if(! new UpdatePlayerActivation().update(player, conn)){
                LOG.debug("echec update activation in Table player !!!");
                setValid(false);
            }else{
                 LOG.debug("Sucessfull updated activation in Table player ");
                setValid(true);
                new mail.ActivationMail().sendMailActivationOK(player);
            }
        }
    }else{
        setValid(false);
        LOG.debug("Player = null ");
    }
     LOG.debug("final result valid in check = " + valid);
     if(valid){ 
         String language = player.getPlayerLanguage();
         String playerid = Integer.toString(player.getIdplayer());
         return "activation_success.xhtml?faces-redirect=true&language=" + language + "&id=" + playerid;
     }else{
         return "activation_failure.xhtml?faces-redirect=true";
     }
  } catch (SQLException sqle) {
            String msg = "£££ SQLException in ActivationController = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
      } catch (Exception e) {
            String msg = "£££ SQLException in activation controller  = " + e.getMessage(); // + " ,SQLState = "
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;        
}finally
{
   //     DBConnection.closeQuietly(null, null, null, null);
}
}  //end method
    public boolean isValid()
    {   LOG.debug("isValid = " + valid);
        return valid;
    }

    public void setValid(boolean valid)
    {  LOG.debug("setValid = " + valid);
        this.valid = valid;
    }

 void main() throws Exception, Throwable {
    Connection conn = new utils.DBConnection().getConnection();
  try{
        Player p = new Player();
        p.setIdplayer(566666); // 456895
        // a modifier
        Activation a = new Activation();
        // à compléter
        String s = new  ActivationController().check(a, conn); //"fcb35e1e-970d-46fc-88f0-929a8555d0d8");
  //          LOG.debug("01");
            LOG.debug("from main, after !! = " + s);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
     utils.DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main
} // end class