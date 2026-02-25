package entite.composite;

import entite.Club;
import entite.Player;
import entite.Professional;
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

public class EClubPro implements Serializable{
    //@Inject creates instance + initialize : pas nécessaire dans constructeur !

@Inject private Club club;
@Inject private Professional professional;
@Inject private Player player;
public EClubPro(){  //constructor
   
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

@Override
public String toString(){ 
 try{
//    LOG.debug("starting toString ECompetition !");
    return ( 
          NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase()
        + NEW_LINE + TAB + " ,vers Club : " + club
        + NEW_LINE + TAB + " ,vers Professional : " + professional
        + NEW_LINE + TAB + " ,vers Player Pro: " + player
        );
  }catch(Exception e){
        String msg = "£££ Exception in EClubPro.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method
} // end class