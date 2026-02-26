
package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
// import jakarta.inject.Named;  // migrated 2026-02-24
import java.io.Serializable;
import utils.LCUtil;

//https://stackoverflow.com/questions/14926148/java-enum-usage-getting-and-setting
// @Named  // migrated 2026-02-24
public class ValidationsLC implements Serializable{
private ValidationStatus stat = ValidationStatus.APPROVED; // Default priority

private String[] status = null;   // ne peutêtre static !!
// [0] = error code APPROVED or 
// [1] = string error
// [2] = messageBean
private String status0;
private String status1;
private String status2;

    public ValidationsLC(){ // connector
        status = new String [3];
        status0 = status[0];
        status1 = status[1];
        status2 = status[2];
        // status2 not used on 26-03-2020
    }
    
////// this is a class in an other class
    // getters et setters écrits par LC et non générés par NB !
public enum ValidationStatus {
        APPROVED, REJECTED;
    }


////////// concerne la classe de départ
    public String[] getStatus() {
        return status;
    }

    public void setStatus(String[] status) {
        this.status = status;
    }

    public String getStatus0() {
        return status0;
    }

    public void setStatus0(String status0) {
        // vérifier que c'est approve or rejected
        this.status0 = status0;
    }

    public String getStatus1() {
        return status1;
    }

    public void setStatus1(String status1) {
        this.status1 = status1;
    }

    public String getStatus2() {
        return status2;
    }

    public void setStatus2(String status2) {
        this.status2 = status2;
    }

@Override
public String toString(){
    try{
 //       LOG.debug("starting toString ValidationsLC!");
    return 
        ( NEW_LINE  + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEW_LINE
             + " status 0: "   + this.status0
             + " status 1: "   + this.status1
             + " status 2: "   + this.status2

        );
        }catch(Exception e){
        String msg = "£££ Exception in ValidationsLC.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}   
} // end class