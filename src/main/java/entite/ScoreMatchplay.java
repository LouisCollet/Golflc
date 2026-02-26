package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.util.Arrays;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

//@Named("scoreMatchplay")enlevé 14-02-2026
//@RequestScoped
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // private or public !
public class ScoreMatchplay implements Serializable{
    private static final long serialVersionUID = 1L;

private int[] strokesEur;
private int[] strokesUsa;
@JsonIgnore private int[] parArray; 
private String playersA;
private String playersB;
private String[] result;

public ScoreMatchplay(){ // constructor
   //     players = new Integer[4];
   //     scoreMP4 = new String[6][18]; // , 4 resultats joueurs, 2 Match Progress sur 18 trous
   //   for(String[] subarray : scoreMP4){
   //         Arrays.fill(subarray, " ");
    //    }
 //     LOG.debug("strokesEurB initialized !");
        strokesEur = new int[18];
        strokesUsa = new int[18];
        result = new String[2];
        parArray = new int[18];
} //end constructor

    public int[] getstrokesEur() {
        return strokesEur;
    }

    public void setstrokesEur(int[] strokesEur) {
        this.strokesEur = strokesEur;
    }

    public int[] getstrokesUsa() {
        return strokesUsa;
    }

    public void setstrokesUsa(int[] strokesUsa) {
        this.strokesUsa = strokesUsa;
    }

    public String[] getResult() {
        return result;
    }

    public void setResult(String[] result) {
        this.result = result;
    }

    public int[] getParArray() {
        return parArray;
    }

    public void setParArray(int[] parArray) {
        this.parArray = parArray;
    }

    public String getPlayersA() {
        return playersA;
    }

    public void setPlayersA(String playersA) {
        this.playersA = playersA;
    }

    public String getPlayersB() {
        return playersB;
    }

    public void setPlayersB(String playersB) {
        this.playersB = playersB;
    }

@Override
public String toString(){
try {
    return
            ("from entite." + this.getClass().getSimpleName()
      //      + NEW_LINE + " ,players : "   + Arrays.deepToString(getPlayers() )
      //      + NEW_LINE + " ,Score Array  : " + Arrays.deepToString(getScoreMP4() )
      //      + NEW_LINE + " ,Score String : " + getScoreString()
            + NEW_LINE + " ,Score Strokes Team A : " + Arrays.toString(strokesEur)
            + NEW_LINE + " ,Score Strokes Team b : " + Arrays.toString(strokesUsa)
            + NEW_LINE + " ,Players Team A : " + playersA
            + NEW_LINE + " ,Players Team B : " + playersB
            + NEW_LINE + " ,Scoreresult : " + Arrays.toString(result)
     //       + NEW_LINE + " ,parArray : " + Arrays.toString(parArray)
            );
   } catch (Exception ex) {
            LOG.debug("Exception in toString de ScoreMatchplay " + ex);
            return null;
        }
}
} // end class