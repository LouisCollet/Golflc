package calc;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;
import Controllers.LoggingUserController;

public class CalcStablefordPlayingHandicap{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 public int calc(final ScoreStableford score, final Player player, final Round round) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
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

 void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
  Player player = new Player();
  player.setIdplayer(324713);
   //   player = new load.LoadPlayer().load(player, conn);
   
      Round round = new Round();
      round.setIdround(589); // 473 = 9 holes
 //     LoggingUserController.setFILE_NAME(
 //              String.valueOf(player.getIdplayer()) 
 //              + "-" + String.valueOf(round.getIdround())
 //              + ".txt");
      LoggingUserController.write("1. text from main of " + CLASSNAME);
      LoggingUserController.write("2. text from main of " + CLASSNAME);
      
      round = new read.ReadRound().read(round, conn);
      ScoreStableford score = new ScoreStableford();
      int ph = new CalcStablefordPlayingHandicap().calc(score, player, round);
         LOG.debug("main - course Handicap calculated = " + ph);
     DBConnection.closeQuietly(conn, null, null, null);
    }// end main
} // end class