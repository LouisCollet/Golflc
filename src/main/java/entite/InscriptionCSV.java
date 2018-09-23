
package entite;

/**
 *
 * @author collet
 */

import java.io.Serializable;
import javax.inject.Named;
//import javax.faces.component.UIComponent;
//import javax.faces.context.FacesContext;
//import javax.faces.bean.ManagedProperty;
@Named
public class InscriptionCSV implements Serializable, interfaces.Log, interfaces.GolfInterface
{
     // Constants ----------------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    private Integer idplayer;

    private Integer idround;

    private String inscriptionTeam;


// new 02/09/2012

public InscriptionCSV()    // constructor
{
 
}

// getter and setters

    public Integer getIdplayer() {
        return idplayer;
    }

    public void setIdplayer(Integer idplayer) {
        this.idplayer = idplayer;
    }

    public Integer getIdround() {
        return idround;
    }

    public void setIdround(Integer idround) {
        this.idround = idround;
    }

    public String getInscriptionTeam() {
        return inscriptionTeam;
    }

    public void setInscriptionTeam(String inscriptionTeam) {
        this.inscriptionTeam = inscriptionTeam;
    }

 

 @Override
public String toString()
{ return 
        ("from entite.InscriptionCSV = "
               + " ,idplayer : "   + this.getIdplayer()
               + " ,idround : "   + this.getIdround()
               + " ,Team : "   + this.getInscriptionTeam()

        );
}

} // end class