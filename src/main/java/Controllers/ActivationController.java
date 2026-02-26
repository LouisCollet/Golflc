
package Controllers;

import Controller.refact.PlayerController;
import context.ApplicationContext;
import delete.DeleteActivation;
import entite.Activation;
import entite.Player;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import static exceptions.LCException.handleGenericException;
import java.io.*;
import utils.LCUtil;

@Named("activationC")
@RequestScoped //@SessionScoped mod 26-08-2023
public class ActivationController implements Serializable, interfaces.Log{
    // ✅ Injection du contexte de session
    @Inject
    private ApplicationContext appContext;
    @Inject
    private update.UpdatePlayerActivation updatePlayerActivation;                // migrated 2026-02-24
    @Inject
    private delete.DeleteActivation deleteActivation;                            // migrated 2026-02-24
    @Inject
    private Controller.refact.PlayerController playerC;                          // migrated 2026-02-24
    @Inject
    private mail.ActivationMail activationMail;                                  // migrated 2026-02-26
// key = le nom de la field dans http://localhost:8080/HelloGolf-1.0-SNAPSHOT/activation_check.xhtml?key="keyLC"
//private String uuid;
private boolean valid;

public ActivationController(){ // constructor
}
// public String check(Player player, Activation activation, Connection conn) throws SQLException, Exception, Throwable{ 
    public String check(Activation activation) throws Exception{
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with activation = " + activation);
    try{
   //  Player player = new Player();
  //   player.setIdplayer(activation.getActivationPlayerId());
   //    LOG.debug("searching playerid = " + player.getIdplayer());
   //  player = new read.ReadPlayer().read(player, conn);
     
     // Utilisation de PlayerController pour charger le Player mmod 12-02-2026
    // Charger le player par son id via PlayerController (injecté)
    playerC.loadPlayer(activation.getActivationPlayerId());
    Player player = appContext.getPlayer();

    LOG.debug("searching playerid = " + player.getIdplayer());

     
        LOG.debug("player found from activation new Player = " + player); // c'est OK
    if(player.getIdplayer() != null){ // trouvé dans table Activation
            LOG.debug("idplayer ready for activation new player = " + player.getIdplayer() );
     //   boolean b = new DeleteActivation().delete(conn, activation.getActivationKey());
        // delete record dans table Activation
        if(! deleteActivation.delete(activation.getActivationKey())){
            LOG.debug("echec delete record Table activation !!!");
            setValid(false);
        }else{
            LOG.debug("OK record deleted in Table activation  = ");
            // update player !!
       //     String d = new ModifyPlayerActivation().modify(player, conn); // setPlayerActivation = 1 
            if(! updatePlayerActivation.update(player)){
                LOG.debug("echec update activation in Table player !!!");
                setValid(false);
            }else{
                 LOG.debug("Sucessfull updated activation in Table player ");
                setValid(true);
                // new mail.ActivationMail().sendMailActivationOK(player)
                activationMail.sendMailActivationOK(player); // migrated 2026-02-26
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
  } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
  }
  } // end method
    public boolean isValid()
    {   LOG.debug("isValid = " + valid);
        return valid;
    }

    public void setValid(boolean valid)
    {  LOG.debug("setValid = " + valid);
        this.valid = valid;
    }

/*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Activation a = new Activation();
        String s = new ActivationController().check(a);
        LOG.debug("from main, after !! = " + s);
    } // end main
*/
} // end class