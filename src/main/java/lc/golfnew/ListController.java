
package lc.golfnew;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import lists.*;

/**
 *
 * @author collet DCI needs beans.xml under Other Sources/META-INF or /src/main/java/resources/META-INF
 */
@Named("listC")
@SessionScoped

public class ListController implements Serializable, interfaces.GolfInterface, interfaces.Log
{
private static final List<SelectItem> COUNTRIES = new ArrayList<>();

private static BigDecimal HandicapPlayer;
private static int zwanzeur;
private static int greenshirt;

public ListController()    // constructor
{
    //
}

    public BigDecimal getHandicapPlayer() {
       return HandicapPlayer;
   }

public int getGreenshirt()
{
       greenshirt = ScoreCard3List.getListe().get(0).getPlayerhasroundZwanzeursGreenshirt();
  //  LOG.debug("greenshirt listsc3 = " + greenshirt);
        return greenshirt;
}

    public static void setGreenshirt(int greenshirt) {
        ListController.greenshirt = greenshirt;
    }

public int getZwanzeur()
{
    zwanzeur = ScoreCard3List.getListe().get(0).getPlayerhasroundZwanzeursResult();
    LOG.debug("zwanzeur listsc3 = " + zwanzeur);
        return zwanzeur;
  //  }
}

    public static void setZwanzeur(int zwanzeur)
    {
        ListController.zwanzeur = zwanzeur;
    }

    
 
public void main(String args[]) // throws InstantiationException, SQLException, ClassNotFoundException, IllegalAccessException
{

LOG.info(" -- main terminated" );

    } // end main
} //end class
