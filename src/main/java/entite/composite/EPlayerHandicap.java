package entite.composite;

import entite.HandicapIndex;
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
public class EPlayerHandicap implements Serializable{
    //@Inject creates instance + initialize : pas nécessaire dans constructeur !
@Inject private HandicapIndex handicapIndex;
@Inject private Player player;
public EPlayerHandicap(){  //constructor
   
  }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public HandicapIndex getHandicapIndex() {
        return handicapIndex;
    }

    public void setHandicapIndex(HandicapIndex handicapIndex) {
        this.handicapIndex = handicapIndex;
    }

@Override
public String toString(){ 
 try{
//    LOG.debug("starting toString ECompetition !");
    return ( 
          NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase()
 //       + NEW_LINE + TAB + " ,vers Club : " + club
        + NEW_LINE + TAB + " ,vers HandicapIndex : " + handicapIndex
        + NEW_LINE + TAB + " ,vers Player : " + player
        );
  }catch(Exception e){
        String msg = "£££ Exception in EPlayerHandicap.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method
} // end class