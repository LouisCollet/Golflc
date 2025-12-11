package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.util.Arrays;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class HolesGlobal implements Serializable{
    private int[][] dataHoles;
    private String type;
public HolesGlobal(){ // constructor
        dataHoles = new int[18][4]; // 18 trous, 4 données : number, par, strokeindex, distance
        for(int[] subarray : dataHoles){
            Arrays.fill(subarray, 0);
        }
        // initialiser 
        for(int i = 0; i < dataHoles.length; i++) {
           dataHoles[i][0] = i+1; // numérote de 1 à 18
        } 
  //      LOG.debug("array dataHoles initialized at : " + Arrays.deepToString(dataHoles));
} // end constructor

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
    public String toString()
{ return 
        (
        NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEW_LINE 
               + " ,array dataHoles : number, par, strokeindex, distance: " + Arrays.deepToString(dataHoles)
               + " ,type (golbal or distance : " + type
        );
}
} // end class