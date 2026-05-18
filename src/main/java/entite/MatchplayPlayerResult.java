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