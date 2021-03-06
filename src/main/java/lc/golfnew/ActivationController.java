
package lc.golfnew;

import delete.DeleteActivation;
import entite.Activation;
import entite.Player;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import modify.ModifyPlayerActivation;
import utils.LCUtil;

@Named("activationC")
@SessionScoped

public class ActivationController implements Serializable, interfaces.Log{
    
// key = le nom de la field dans http://localhost:8080/HelloGolf-1.0-SNAPSHOT/activation_check.xhtml?key="keyLC"
private String uuid;
private boolean valid;

public ActivationController(){ // constructor
    //
}
public String check(Player player, Activation activation, Connection conn) throws SQLException, Exception, Throwable{ 
    // c'est ici qu'il faut vérifier !!!
    try{
         LOG.info("key in check entite activation= " + activation.getActivationKey());  // récupérer de fViewParam dans activation_check.xhtml
         LOG.info("key in check uuid = " + uuid);
 /*   FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
        String keyLC = paramMap.get("uuid");
    LOG.info("keyLC  = " + keyLC);
    // ou aussi
    String keyLC2 = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("uuid");
    LOG.info("keyLC2  = " + keyLC2);
    
   player = new FindActivationPlayer().find(conn, uuid);// vérifie si uuid est en attente dans table activation
      LOG.info(" return from = " + player.getIdplayer());
*/
    if(player.getIdplayer() != null){ // trouvé dans table Activation
            LOG.info("idplayer ready for activation new player = " + player.getIdplayer() );
        boolean b = new DeleteActivation().delete(conn, activation.getActivationKey());
        // delete record dans table Activation
        if(b == false){
            LOG.info("echec delete record Table activation !!!");
            setValid(false);
        }else{
            LOG.info("OK record deleted in Table activation  = ");
            // update player !!
    //        ModifyPlayerActivation mpa = new ModifyPlayerActivation();
            String d = new ModifyPlayerActivation().modify(player, conn); // setPlayerActivation = 1 
            if(d==null){
                LOG.info("echec update activation in Table player !!!");
                setValid(false);
            }else{
                 LOG.info("Sucessfull updated activation in Table player = " + d);
                setValid(true);
                new mail.ActivationMail().sendMailActivationOK(player);
            }
        }
    }else{
        setValid(false);
        LOG.info("Player = null ");
    }
     LOG.info("final result valid in check = " + valid);
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
    {   LOG.info("isValid = " + valid);
        return valid;
    }

    public void setValid(boolean valid)
    {  LOG.info("setValid = " + valid);
        this.valid = valid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

 public static void main(String[] args) throws Exception, Throwable {
    Connection conn = new utils.DBConnection().getConnection();
  try{
        Player p = new Player();
        p.setIdplayer(566666); // 456895
        Activation a = new Activation();
        // à compléter
        String s = new  ActivationController().check(p, a, conn); //"fcb35e1e-970d-46fc-88f0-929a8555d0d8");
  //          LOG.info("01");
            LOG.info("from main, after !! = " + s);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
     utils.DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} // end class