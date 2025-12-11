
package calc;
import entite.composite.EMatchplayResult;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Named;
import utils.DBConnection;

@Named // nécessaire pour show_participants_matchplay.xhtml
public class CalcMatchplayResult {
  private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
  private static List<EMatchplayResult> finalResult = null; 
  
public List<EMatchplayResult> calc(final Player player1, final Player player2,final Round round, final Connection conn){
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("... entering " + methodName);
    LOG.debug(" with Round   = " + round.getIdround());
    LOG.debug(" with Player1 = " + player1.getIdplayer());
    LOG.debug(" with Player2 = " + player2.getIdplayer());
try{
    if(finalResult != null){
        LOG.debug("finalResult returned !! ");
        return finalResult;
    }
    
    
    var p1 = new find.FindMatchplayResult().find(player1, round, conn);
  //     LOG.debug("p1 = " + p1);
    var p2 = new find.FindMatchplayResult().find(player2, round, conn);
  //     LOG.debug("p2 = " + p1);
  //  List<EMatchplayResult> finalResult = new ArrayList<>();  // concatenation player1 and player2
     finalResult = new ArrayList<>();  // concatenation player1 and player2
      for(int i = 0 ; i < p1.size() ; i++) {
           EMatchplayResult result = new EMatchplayResult();
           result.setPlayer1(p1.get(i));  // transfer et regroupement
           result.setPlayer2(p2.get(i));
           if(result.getPlayer1().getStrokes() > result.getPlayer2().getStrokes()){
               result.getPlayer1().setResult(0);
               result.getPlayer2().setResult(1); // win hole
           }
           if(result.getPlayer1().getStrokes() < result.getPlayer2().getStrokes()){
               result.getPlayer1().setResult(1);  // win hole
               result.getPlayer2().setResult(0); 
           }
        //   if(result.getPlayer1().getStrokes() == result.getPlayer2().getStrokes()){
           if(result.getPlayer1().getStrokes().equals(result.getPlayer2().getStrokes())){    
               result.getPlayer1().setResult(0);  // square
               result.getPlayer2().setResult(0); 
           }
           finalResult.add(result);
 //              LOG.debug(" result for this hole = " + result);
      } // end for
  //        LOG.debug(" finalResult = " + finalResult);
 return finalResult;
 
}catch (Exception e){
     LOG.debug("Exception in " + methodName + e);
     return null;
}
}

    public static List<EMatchplayResult> getFinalResult() {
        return finalResult;
    }

    public static void setFinalResult(List<EMatchplayResult> finalResult) {
        CalcMatchplayResult.finalResult = finalResult;
    }

void main() throws SQLException, Exception{
    Connection conn = new DBConnection().getConnection();
    Player player1 = new Player();
    player1.setIdplayer(324713);
    Player player2 = new Player();
    player2.setIdplayer(456781);
    Round round = new Round();
    round.setIdround(694);
    var v = new CalcMatchplayResult().calc(player1, player2, round,conn);
       LOG.debug("result in main = " + v.toString());
    for(int i = 0 ; i < v.size() ; i++) {
         LOG.debug("hole = " + v.get(i).getPlayer1().getHole());
         LOG.debug("player1 strokes = " + v.get(i).getPlayer1().getStrokes());
         LOG.debug("player2 strokes = " + v.get(i).getPlayer2().getStrokes());
         LOG.debug("player1 result = " + v.get(i).getPlayer1().getResult());
         LOG.debug("player2 result = " + v.get(i).getPlayer2().getResult());
        
     }
      Integer sum = v.stream()
                    .mapToInt(x -> x.getPlayer1().getResult())
                    .sum();
          LOG.debug("total player 1 = " + sum + " id = " + v.getFirst().getPlayer1().getPlayerId());
      sum = v.stream()
                    .mapToInt(x -> x.getPlayer2().getResult())
                    .sum();
          LOG.debug("total player 2 = " + sum + " id = " + v.get(0).getPlayer2().getPlayerId());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class