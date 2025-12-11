package entite.composite;

import entite.UnavailablePeriod;
import entite.UnavailableStructure;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import utils.LCUtil;

@Named // enlevé 14/06/2022
@ViewScoped // new 13-02-2021
public class EUnavailable implements Serializable{
    private UnavailableStructure structure;
    private UnavailablePeriod period;
 
 public EUnavailable(){  // init dans constructor
        structure = new UnavailableStructure();
        period = new UnavailablePeriod();
    }

    public UnavailableStructure getStructure() {
        return structure;
    }

    public void setStructure(UnavailableStructure structure) {
        this.structure = structure;
    }

    public UnavailablePeriod getPeriod() {
        return period;
    }

    public void setPeriod(UnavailablePeriod period) {
        this.period = period;
    }


@Override
public String toString(){ 
 try{
    LOG.debug("starting toString EUnavailable !");
    return 
        (NEW_LINE 
            + "from entite " + getClass().getSimpleName().toUpperCase() + " : "
       +  "<br/>" + getStructure()
       +  "<br/>" + getPeriod()
        );
    }catch(Exception e){
        String msg = "£££ Exception in EUnavailable.toString = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method
} // end class