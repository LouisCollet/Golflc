package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.inject.Named;
import utils.LCUtil;

@Named
public class Activation implements Serializable, interfaces.Log, interfaces.GolfInterface{
    private static final long serialVersionUID = 1L;
    private String activationKey;
    private Integer activationPlayerId;
    private String activationLanguage;
    private LocalDateTime activationCreationDate;

    public Activation(){

    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public Integer getActivationPlayerId() {
        return activationPlayerId;
    }

    public void setActivationPlayerId(Integer activationPlayerId) {
        this.activationPlayerId = activationPlayerId;
    }

    public String getActivationLanguage() {
        return activationLanguage;
    }

    public void setActivationLanguage(String activationLanguage) {
        this.activationLanguage = activationLanguage;
    }

    public LocalDateTime getActivationCreationDate() {
        return activationCreationDate;
    }

    public void setActivationCreationDate(LocalDateTime activationCreationDate) {
        this.activationCreationDate = activationCreationDate;
    }

     @Override
public String toString(){ 
LOG.info("starting toString for Activation!");
 try{
   if(this.getActivationKey() != null){
       return 
        (NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEWLINE 
              
               + " ,Activation Key : "   + this.getActivationKey()
               + " ,activationPlayerId : "   + this.getActivationPlayerId()
               + " ,Activation Language : " + this.getActivationLanguage()
               + " ,Creation format LocalDateTime: "   + this.getActivationCreationDate().format(ZDF_TIME)
        );
   }else{
       return
      (NEW_LINE + "from entite : " + this.getClass().getSimpleName() + NEWLINE 
         + " idActivation = null !!");
    }
        }catch(Exception e){
        String msg = " EXCEPTION in Activation.toString = " + e.getMessage();
        LOG.error(msg);
     //   LCUtil.showMessageFatal(msg);
        return msg;
  }
}
public static Activation mapActivation(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Activation a = new Activation();
            a.setActivationKey(rs.getString("ActivationKey") );
            a.setActivationPlayerId(rs.getInt("ActivationPlayerId") );
            a.setActivationLanguage(rs.getString("ActivationPlayerLanguage") );
            a.setActivationCreationDate(rs.getTimestamp("activationCreationDate").toLocalDateTime());

   return a;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map
} //end class