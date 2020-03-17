package entite;

import static interfaces.GolfInterface.NEWLINE;
import static interfaces.Log.LOG;
import javax.inject.Named;
import utils.LCUtil;

@Named
public class EPlayerPassword{
    public Player Eplayer; 
    public Password Epassword;

 public EPlayerPassword(){
        Eplayer = new Player();
        Epassword = new Password();
    }
 
    public Player getPlayer() {
        return Eplayer;
    }

    public void setPlayer(Player player) {
        this.Eplayer = player;
    }

    public Password getPassword() {
        return Epassword;
    }

    public void setPassword(Password password) {
        this.Epassword = password;
    }

@Override
public String toString(){ 
 try{
    LOG.info("starting toString EPlayerPassword !");
    return 
        (NEWLINE 
            + "from entite " + getClass().getSimpleName() + " : "
       +  getPlayer().toString()
       +  getPassword().toString()
        );
    }catch(Exception e){
        String msg = "£££ Exception in EPlayerPassword.toString = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method
} // end class