package calc;

import entite.PlayingHcp;
import static interfaces.Log.LOG;
import lc.golfnew.UtilsController;
import utils.LCUtil;

public class CalcScramblePlayingHandicap {
    public  int getScramblePlayingHcp(final PlayingHcp playingHcp, final int players) throws Exception{
try {
   // int players = UtilsController.getElem(playingHcp);
    LOG.info("Starting getScramblePlaying Hcp"); 
          LOG.info("Scramble Hcp with PlayingHcp = " + playingHcp.toString());
          LOG.info("Scramble Hcp with number of players = " + players );
          UtilsController uc = new UtilsController();
          double sum = uc.getSum(playingHcp);
          LOG.info("The sum is :" + sum);
          PlayingHcp ph = new PlayingHcp(); // attention modifié 19/08/2018
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
       //     LOG.info("ending Playing Hcp calculated !! = " + playingHcp.getPlayingHandicap() );
            LOG.info("ending Playing Hcp calculated !! = " + ph.getPlayingHandicap() );
            
    return playingHcp.getPlayingHandicap();
} catch(final Exception e){ 
    //   LOG.error(" -- Exception by LC = " + e.getMessage());
        String msg = "-- Exception by LC = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return 99;
} finally{

 // LOG.info("finally : end of getStoredList ");
//return 0;
}
} //end getPlayingHandicap
} // end class