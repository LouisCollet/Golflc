package calc;

import entite.PlayingHandicap;
import static interfaces.Log.LOG;
import Controllers.UtilsController;
import utils.LCUtil;

public class CalcScramblePlayingHandicap {
    public  int getScramblePlayingHcp(final PlayingHandicap playingHcp, final int players) throws Exception{
try {
   // int players = UtilsController.getElem(playingHcp);
    LOG.debug("Starting getScramblePlaying Hcp"); 
          LOG.debug("Scramble Hcp with PlayingHcp = " + playingHcp.toString());
          LOG.debug("Scramble Hcp with number of players = " + players );
          UtilsController uc = new UtilsController();
          double sum = uc.getSum(playingHcp);
          LOG.debug("The sum is :" + sum);
          PlayingHandicap ph = new PlayingHandicap(); // attention modifié 19/08/2018
          if(players == 0){
              ph.setPlayingHandicap(0);
          }
          if(players == 2){
              ph.setPlayingHandicap((int) (sum * .25));
          }
          if(players == 3){
               ph.setPlayingHandicap((int) (sum * .20));
          }
          if(players == 4){  
               ph.setPlayingHandicap((int) (sum * .10));
          }
       //     LOG.debug("ending Playing Hcp calculated !! = " + playingHcp.getPlayingHandicap() );
            LOG.debug("ending Playing Hcp calculated !! = " + ph.getPlayingHandicap() );
            
    return playingHcp.getPlayingHandicap();
} catch(final Exception e){ 
    //   LOG.error(" -- Exception by LC = " + e.getMessage());
        String msg = "-- Exception by LC = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return 99;
} finally{

 // LOG.debug("finally : end of getStoredList ");
//return 0;
}
} //end getPlayingHandicap
} // end class