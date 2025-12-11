package entite.composite;

import entite.Password;
import entite.Player;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import utils.LCUtil;

@Named // nécessaire ?
@RequestScoped
public class EPlayerPassword implements Serializable{
    private Player player;  // was public
    private Password password; // was public

 public EPlayerPassword(){  // init dans constructor
        player = new Player();
        password = new Password();
    }
 
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

@Override
public String toString(){ 
 try{
  //  LOG.debug("starting toString EPlayerPassword !");
    return 
        (NEW_LINE + "from entite " + getClass().getSimpleName().toUpperCase() + " : "
       + getPlayer()
       + getPassword()
        );
    }catch(Exception e){
        String msg = "£££ Exception in EPlayerPassword.toString = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method
} // end class