package entite;

import java.io.Serializable;
import java.util.Arrays;
import javax.inject.Named;

@Named
public class HolesGlobal implements Serializable
{
    private int[][] dataHoles;

public HolesGlobal(){ // constructor
        dataHoles = new int[18][4]; // 18 trous, 4 données : number, par, strokeindex, distance
        for(int[] subarray : dataHoles)
        {
            Arrays.fill(subarray, 0);
        }
        // initialiser 
        for(int i = 0; i < dataHoles.length; i++) {
           dataHoles[i][0] = i+1; // numérote de 1 à 18
        } 
  //      LOG.info("array dataHoles initialized at : " + Arrays.deepToString(dataHoles));
} // end constructor

    public int[][] getDataHoles() {
        return dataHoles;
    }

    public void setDataHoles(int[][] dataHoles) {
        this.dataHoles = dataHoles;
    }
    
    @Override
    public String toString()
{ return 
        ("from entite : " + this.getClass().getSimpleName()
               + " ,array dataHoles : " + Arrays.deepToString(dataHoles)
        );
}
} // end class