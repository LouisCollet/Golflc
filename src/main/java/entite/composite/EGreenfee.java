package entite.composite;

import entite.Club;
import entite.Greenfee;
import entite.Player;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import utils.LCUtil;

@Named
@RequestScoped 

public class EGreenfee implements Serializable{
    //@Inject creates instance + initialize : pas nécessaire dans constructeur !
@Inject  private Greenfee greenfee;
@Inject  private Player player;
@Inject  private Club club;

public EGreenfee(){  //constructor
   
}

    public Greenfee getGreenfee() {
        return greenfee;
    }

    public void setGreenfee(Greenfee greenfee) {
        this.greenfee = greenfee;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

@Override
public String toString(){ 
 try{
//    LOG.debug("starting toString ECompetition !");
    return ( 
          NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase()
        + TAB + greenfee
        + TAB + player
        + TAB + club    
        );
  }catch(Exception e){
        String msg = "£££ Exception in ECotisation.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method
} // end class