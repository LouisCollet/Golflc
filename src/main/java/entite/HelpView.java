
package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import utils.LCUtil;

@Named
@SessionScoped // absolument nécessaire !!
public class HelpView implements Serializable{
    private static final long serialVersionUID = 1L;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private String _id;  // key name mongodb
    private String HelpViewText;
    private String HelpViewLanguage;
    private LocalDateTime HelpViewModificationDate;

    
public HelpView(){  // constructor
    
}

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getHelpViewText() {
        return HelpViewText;
    }

    public void setHelpViewText(String HelpViewText) {
        this.HelpViewText = HelpViewText;
    }

    public String getHelpViewLanguage() {
        return HelpViewLanguage;
    }

    public void setHelpViewLanguage(String HelpViewLanguage) {
        this.HelpViewLanguage = HelpViewLanguage;
    }

    public LocalDateTime getHelpViewModificationDate() {
        return HelpViewModificationDate;
    }

    public void setHelpViewModificationDate(LocalDateTime HelpViewModificationDate) {
        this.HelpViewModificationDate = HelpViewModificationDate;
    }

 @Override
public String toString(){
 try{   
       if(this.getClass() == null){
         return ("HelpView is null, no print !");
    }
    return 
        (NEW_LINE + "FROM ENTITE = "+ this.getClass().getSimpleName().toUpperCase()+ NEW_LINE
               + " ,_Id : "   + this._id
               + " ,Language : " + this.HelpViewLanguage
               + " ,ModificationDate : " + this.HelpViewModificationDate
          //     + "dateText : " + this.getDateText()
               + " ,Text : "   + this.HelpViewText
        );
    }catch(Exception e){
        String msg = "£££ Exception in HelpView.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}
// à modifier ultérieurement !!
  public static LoggingUser map(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        LoggingUser logging = new LoggingUser();
        logging.setLoggingIdPlayer(rs.getInt("LoggingIdPlayer"));
        logging.setLoggingIdRound(rs.getInt("LoggingIdRound") );
        logging.setLoggingType(rs.getString("LoggingType") );
        logging.setLoggingCalculations(rs.getString("LoggingCalculations"));
        logging.setLoggingModificationDate(LocalDateTime.now()); // 16/08/2022 for reprise vers mongoDB
      return logging;
  }catch(Exception e){
      String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
  }
} //end method map
} // end class