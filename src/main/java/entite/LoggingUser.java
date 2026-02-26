
package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import utils.LCUtil;


public class LoggingUser implements Serializable, interfaces.Log{
    private static final long serialVersionUID = 1L;
    

    private Integer LoggingIdPlayer;
    private Integer LoggingIdRound;
    private String LoggingType;
    private String LoggingCalculations;
//    @BsonIgnore // mongoDB
    private LocalDateTime LoggingModificationDate;

    
    
    public Integer getLoggingIdPlayer() {
        return LoggingIdPlayer;
    }

    public void setLoggingIdPlayer(Integer LoggingIdPlayer) {
        this.LoggingIdPlayer = LoggingIdPlayer;
    }

    public Integer getLoggingIdRound() {
        return LoggingIdRound;
    }

    public void setLoggingIdRound(Integer LoggingIdRound) {
        this.LoggingIdRound = LoggingIdRound;
    }

    public String getLoggingType() {
        return LoggingType;
    }

    public void setLoggingType(String LoggingType) {
        this.LoggingType = LoggingType;
    }

    public String getLoggingCalculations() {
        return LoggingCalculations;
    }

    public void setLoggingCalculations(String LoggingCalculations) {
        this.LoggingCalculations = LoggingCalculations;
    }

    public LocalDateTime getLoggingModificationDate() {
        return LoggingModificationDate;
    }

    public void setLoggingModificationDate(LocalDateTime LoggingModificationDate) {
        this.LoggingModificationDate = LoggingModificationDate;
    }


 @Override
public String toString(){
 try{   
       if(this.getClass() == null){
         return ("Logging is null, no print !");
    }
    return 
        (NEW_LINE + "FROM ENTITE = "+ this.getClass().getSimpleName().toUpperCase()+ NEW_LINE
               + " ,idplayer : "   + this.LoggingIdPlayer
               + " ,idround: "   + this.LoggingIdRound
               + " ,Hole Type : " + this.LoggingType
               + " ,Calculations : " + this.LoggingCalculations
                       );
    }catch(Exception e){
        String msg = "£££ Exception in Logging.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}

  public static LoggingUser map(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(); 
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