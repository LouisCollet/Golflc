package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.LCUtil;

public class MatchplayPlayerResult{
    
    private static final long serialVersionUID = 1L;

    private Integer playerId;
    private Integer hole;
    private Integer strokes;
    private Integer result;
    private String  playerLastFirst;

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Integer getHole() {
        return hole;
    }

    public void setHole(Integer hole) {
        this.hole = hole;
    }

    public Integer getStrokes() {
        return strokes;
    }

    public void setStrokes(Integer strokes) {
        this.strokes = strokes;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getPlayerLastFirst() {
        return playerLastFirst;
    }

    public void setPlayerLastFirst(String playerLastFirst) {
        this.playerLastFirst = playerLastFirst;
    }

public static MatchplayPlayerResult map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
              MatchplayPlayerResult result = new MatchplayPlayerResult();
              result.setPlayerId(rs.getInt("idplayer"));
              result.setHole(rs.getInt("ScoreHole")); 
              result.setStrokes(rs.getInt("ScoreStroke"));
              result.setResult(null); // complété ultérieurement
    //            String first = rs.getString("PlayerFirstName");
    //            String last  = rs.getString("PlayerLastName");
              result.setPlayerLastFirst(rs.getString("PlayerLastName") + ", " + rs.getString("PlayerFirstName"));
   return result;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
 @Override
public String toString()
{ return 
        (NEW_LINE +"from entite : " + this.getClass().getSimpleName().toUpperCase()+ NEW_LINE 
               + " ,playerid : "   + this.playerId
               + " ,hole : " + this.hole
               + " ,strokes : " + this.strokes
               + " ,result : " + this.result
               + " ,name : " + this.playerLastFirst
        );
}
} // end class