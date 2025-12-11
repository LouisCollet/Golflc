package entite;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import utils.LCUtil;

@Named
@RequestScoped
public class Activation implements Serializable, interfaces.Log, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
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
     // LOG.debug("starting toString for Activation!");
 try{
 //  if(this.getActivationKey() != null){
       return 
        NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE 
               + " ,ActivationKey : "   + this.getActivationKey()
               + " ,activationPlayerId : "   + this.getActivationPlayerId()
               + " ,ActivationLanguage : " + this.getActivationLanguage()
               + " ,CreationDate : "   + this.getActivationCreationDate() //.format(ZDF_TIME)
        ;
//   }else{
//       return
//      (NEW_LINE + "from entite : " + this.getClass().getSimpleName() + NEW_LINE 
 //        + " idActivation = null !!");
 //   }
        }catch(Exception e){
        String msg = " EXCEPTION in Activation.toString = " + e.getMessage();
        LOG.error(msg);
     //   LCUtil.showMessageFatal(msg);
        return msg;
  }
}
public static Activation map(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        Activation a = new Activation();
            a.setActivationKey(rs.getString("ActivationKey") );
            a.setActivationPlayerId(rs.getInt("ActivationPlayerId") );
            a.setActivationLanguage(rs.getString("ActivationPlayerLanguage") );
            a.setActivationCreationDate(rs.getTimestamp("activationCreationDate").toLocalDateTime());

   return a;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map
} //end class