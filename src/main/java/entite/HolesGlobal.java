package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.util.Arrays;

//@Named enlevé 21-02-2026
//@RequestScoped
public class HolesGlobal implements Serializable{

    private static final int NB_HOLES   = 18;
    private static final int NB_COLUMNS = 4;

    public static final int COL_NUMBER      = 0;    // ✅ décommenté
    public static final int COL_PAR         = 1;
    public static final int COL_STROKEINDEX = 2;
    public static final int COL_DISTANCE    = 3;

    private int[][] dataHoles;                       // ✅ type déclaré
    private String type;

    public HolesGlobal() {
        dataHoles = new int[NB_HOLES][NB_COLUMNS];
        for (int i = 0; i < NB_HOLES; i++) {
            dataHoles[i][COL_NUMBER] = i + 1;        // ✅ COL_NUMBER défini
        }
    //    LOG.debug("HolesGlobal initialized: " + Arrays.deepToString(dataHoles)); // ✅ LOG accessible
    }

    public int[][] getDataHoles() {
        return dataHoles;
    }

    public void setDataHoles(int[][] dataHoles) {
        this.dataHoles = dataHoles;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString(){
        return (
        NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase()
        + NEW_LINE 
        + " ,array dataHoles : number, par, strokeindex, distance: " + Arrays.deepToString(dataHoles)
        + " ,type (golbal or distance : " + type
        );
}
} // end class