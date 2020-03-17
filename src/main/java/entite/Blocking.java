
package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import utils.LCUtil;
@Named
public class Blocking implements Serializable, interfaces.Log, interfaces.GolfInterface{
    
    // à modifier
    
    private static final long serialVersionUID = 1L;
    private Integer blockingPlayerId;
    private LocalDateTime blockingLastAttempt; 
    private Short blockingAttempts;
    private LocalDateTime blockingRetryTime;

    public Blocking(){

    }

    public Integer getBlockingPlayerId() {
        return blockingPlayerId;
    }

    public void setBlockingPlayerId(Integer blockingPlayerId) {
        this.blockingPlayerId = blockingPlayerId;
    }

    public LocalDateTime getBlockingLastAttempt() {
        return blockingLastAttempt;
    }

    public void setBlockingLastAttempt(LocalDateTime blockingLastAttempt) {
        this.blockingLastAttempt = blockingLastAttempt;
    }

    public Short getBlockingAttempts() {
        return blockingAttempts;
    }

    public void setBlockingAttempts(Short blockingAttempts) {
        this.blockingAttempts = blockingAttempts;
    }

    public LocalDateTime getBlockingRetryTime() {
        return blockingRetryTime;
    }

    public void setBlockingRetryTime(LocalDateTime blockingRetryTime) {
        this.blockingRetryTime = blockingRetryTime;
    }
    
    public void Retry(){
        LOG.info("from blocking Retry");
   PrimeFaces.current().ajax().update("selectPlayer:countdown");
      //  return getBlockingRetryTime();
    }
            
            
@Override
public String toString(){ 
LOG.info("starting toString for Blocking!");
 try{
   if(this.getBlockingPlayerId() != null){
       return 
        (NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEWLINE 
    //           + " ,idaudit : "   + this.getIdaudit()
               + " ,Audit Player : "   + this.getBlockingPlayerId()
               + " ,LastAttempt format LocalDateTime: "   + this.getBlockingLastAttempt().format(ZDF_TIME)
    //           + " ,LastAttempt format LocalDateTime: "   + this.getBlockingLastAttempt().format(ZDF_TIME)
               + " ,Attempts : " + this.getBlockingAttempts()
               + " ,RetryTime format LocalDateTime: "   + this.getBlockingRetryTime().format(ZDF_TIME)
        );
   }else{
       return
      (NEW_LINE + "from entite : " + this.getClass().getSimpleName() + NEWLINE 
         + " BlockingPlayerId = null !!");
    }
        }catch(Exception e){
        String msg = " EXCEPTION in Blocking.toString = " + e.getMessage();
        LOG.error(msg);
     //   LCUtil.showMessageFatal(msg);
        return msg;
  }
}
public static Blocking mapBlocking(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Blocking b = new Blocking();
            b.setBlockingPlayerId(rs.getInt("BlockingPlayerId") );
            b.setBlockingLastAttempt(rs.getTimestamp("BlockingLastAttempt").toLocalDateTime());
            b.setBlockingAttempts(rs.getShort("BlockingAttempts") );
            b.setBlockingRetryTime(rs.getTimestamp("BlockingRetryTime").toLocalDateTime());
           
   return b;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map
} //end class