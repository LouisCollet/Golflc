
package calc;
import entite.Player;
import entite.Round;
import entite.ScoreMatchplay;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class CalculateScoreMatchplay implements interfaces.GolfInterface {
  private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
  
public CalculateScoreMatchplay(){
    //LOG.debug(" from constructor GolfWriteLog = ");
 }

public static ScoreMatchplay calc(final Player player, ScoreMatchplay score,
        final Round round, final Connection conn){
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("... entering " + methodName);
    LOG.debug(" with Round = " + round);
    LOG.debug(" with Player = " + player);
    LOG.debug(" with ScoreMatchplay = " + score);
    
try{
   var v = result(score.getstrokesEur(),score.getstrokesUsa());
   score.setResult(v);
   return score;
}catch (Exception e){
     LOG.debug("Exception in " + methodName + e);
     return null;
}
}

public static String[] result(int[] teamA, int[]teamB){
 try{
   LOG.debug("entering calc A "  + Arrays.toString(teamA));  // scores team A
   LOG.debug("entering calc B "  + Arrays.toString(teamB));
   // LOG.debug(" validations " + )
   // à faire : validations : exemple field 2e équipe non complétée
   int totA = 0;
   int totB = 0;
   int j = 0;
   int max = teamA.length;
   String[] result = {"",""};
   int i = 0;
   for(i=0; i < max; i++){
       j=i;
       if(teamA[i] == 0 ||teamB[i] == 0){
  //         LOG.debug("break for current index = zero");
           break; 
       }
       if(teamA[i] == teamB[i]){
           LOG.debug((i+1) +" A/S");
       }
       if(teamA[i] < teamB[i]){
           totA++;
           LOG.debug((i+1) + " A < B donc A+1, score A = " + totA);
       }
       if(teamB[i] < teamA[i]){
           totB++;
           LOG.debug((i+1) + " B < A  donc B+1, score B = " + totB);
       }
   }
     LOG.debug("j = "+ j);
     int holes = max - j; // tester sur le hole ??
     LOG.debug("holes still to play = "+ holes);
      LOG.debug("holes still to play max-j+1= " + (max-j+1));
     LOG.debug("total A = "+ totA);
     LOG.debug("total B = "+ totB);
   if(totA == totB){
 //          LOG.debug("SQUARE");
 //          LOG.debug("holes still to play = " + holes);
           result[0] = "HALVED";
           result[1] = "HALVED";
           return result;
   }
   if(totB > totA){ // usa gagne
           LOG.debug("case = B > A");
           int d = totB-totA;
           LOG.debug("B-A ou d = " + d);
            // new 26-09-2021
            LOG.debug("i = " + i);
         if(i == 18 && d == 2){  //trou 18
              LOG.debug("hole 17 special case 1 for difference = " + d);
             result[1] = "B-2Up";
             return result;
   }
         // bien utile 
         if(i == 17 && d == 1){  //trou 17
              LOG.debug("hole 18 special case 2 for difference = " + d);
             result[1] = "B-1Up";
             return result;
         }
         
           if(d > holes){  // différence supérieure au nombre de trous à jouer
                result[1] = "B-"+ d + "&" + holes;
                return result;
           }else{
                result[1] = "B-" + d + "Up";
                return result;
           }
   }
   if(totA > totB){ // eur gagne
           LOG.debug("case = A > B");
           int d = totA-totB;
           LOG.debug("A-B ou d = " + d);
    // new 26-09-2021
         if(i == 18 && d == 2){  //trou 18
             LOG.debug("hole 17 special case 3 for difference = " + d);
             result[0] = "A-2Up";
              return result;
          }
         if(i == 17 && d == 1){  //trou 17
               LOG.debug("hole 17 2e special case 4 for difference = " + d);
             result[0] = "A-1Up";
             return result;
         }
           if(d > holes){  // différence supérieure au nombre de trous à jouer
               result[0] = "A-" + d + "&" + holes;
               return result;
           }else{
               result[0] = "A-" + d + "Up";
               return result;
           }
   }
return result;
   }catch (Exception e){
     LOG.debug("Exception in " + e);
     return null;
}
} // end method


void main() throws SQLException, Exception{
 //   pour le testing, voir application TEst avec scanner et introduction score par score
 
 /*     Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
 //   LOG.debug("line 010");
    Round round = new Round();
    round.setIdround(437);
 //    LOG.debug("line 011");
   // LocalDateTime ldt = LocalDateTime.of(2017,Month.AUGUST,26,0,0);
   // round.setRoundDate(ldt);
   //        LOG.debug("line 012");
 //   int[][] a = new LoadstrokesEurrray().load(conn, points, player, round);
 //   LOG.debug(" array points filled = " + Arrays.deepToString(a));

DBConnection.closeQuietly(conn, null, null, null);
*/
}// end main
} // end class