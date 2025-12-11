package entite.composite;

import entite.MatchplayPlayerResult;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import java.io.Serializable;
import jakarta.inject.Inject;
import utils.LCUtil;

//@Named
//@SessionScoped 
//@RequestScoped
public class EMatchplayResult implements Serializable{
    //@Inject creates instance + initialize : pas nécessaire dans constructeur !
@Inject private MatchplayPlayerResult player1;
@Inject private MatchplayPlayerResult player2;

    public MatchplayPlayerResult getPlayer1() {
        return player1;
    }

    public void setPlayer1(MatchplayPlayerResult player1) {
        this.player1 = player1;
    }

    public MatchplayPlayerResult getPlayer2() {
        return player2;
    }

    public void setPlayer2(MatchplayPlayerResult player2) {
        this.player2 = player2;
    }


@Override
public String toString(){ 
 try{
//    LOG.debug("starting toString ECompetition !");
    return ( 
          NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase()
 //       + NEW_LINE + TAB + " ,vers Club : " + club
        + NEW_LINE + TAB + " ,vers Player1 : " + player1
        + NEW_LINE + TAB + " ,vers Player2 : " + player2
        );
  }catch(Exception e){
        String msg = "£££ Exception in EMatchplayResult.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method
} // end class