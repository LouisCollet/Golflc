
package entite;

import javax.inject.Named;

/**not used !!
 *
 * @author Collet
 */
@Named
public enum Game {
     STABLEFORD("Stableford"),SCRAMBLE("Scramble"),CHAPMAN("Chapman"),
     STROKEPLAY("Strokeplay"),ZWANZEURS("Zwanzeurs"),MP_FOURBALL("MP_Fourball"),
     MP_FOURSOME("MP_Foursome"),MP_SINGLE("MP_Single");
     
     
     
    private String label;

    private Game(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
     
}

