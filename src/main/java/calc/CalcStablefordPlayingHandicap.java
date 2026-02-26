package calc;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import static interfaces.Log.LOG;
import java.sql.SQLException;
import utils.LCUtil;
import Controllers.LoggingUserController;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;

@ApplicationScoped
public class CalcStablefordPlayingHandicap implements Serializable {

    private static final long serialVersionUID = 1L;

    public CalcStablefordPlayingHandicap() { }
    
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 public int calc(final ScoreStableford score, final Player player, final Round round) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
try{
    // voir appendix C page 67/79
   
    LOG.debug("... entering " + methodName);
//    LOG.debug("with scoreStableford = " + score);
     LoggingUserController.write(CLASSNAME + "." + methodName,"i"); 
     LoggingUserController.write("playing handicap", "t");
    int handicapAllowance = 0;
    if("STABLEFORD".equals(round.getRoundGame())){
        // à adapter pour les autres fomules de jeu
        handicapAllowance = 1; 
    }
        LOG.debug("handicap Allowance = " +  handicapAllowance);
        LoggingUserController.write("handicap Allowance = " + handicapAllowance);
  //      LOG.debug("Round input is the same ? = " + round);
    int playingHandicap = score.getCourseHandicap() * handicapAllowance;
        LOG.debug("playingHandicap = " + playingHandicap);
        LoggingUserController.write("playingHandicap =" + playingHandicap);
  return playingHandicap;
}catch (Exception ex) {
                LOG.error("Exception in " + methodName + ex);
                LCUtil.showMessageFatal("Exception = " + ex.toString());
                return 0;
} finally{}
 } // end method

 /*
 void main() throws SQLException, Exception{
      Player player = new Player();
      player.setIdplayer(324713);
      Round round = new Round();
      round.setIdround(589);
      LoggingUserController.write("1. text from main of " + CLASSNAME);
      LoggingUserController.write("2. text from main of " + CLASSNAME);
      // round = readRound.read(round);  // @Inject ReadRound readRound
      ScoreStableford score = new ScoreStableford();
      int ph = new CalcStablefordPlayingHandicap().calc(score, player, round);
      LOG.debug("main - course Handicap calculated = " + ph);
 } // end main
 */
} // end class