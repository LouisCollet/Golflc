
package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.inject.Named;
import utils.LCUtil;

@Named
public class Audit implements Serializable, interfaces.Log, interfaces.GolfInterface{
    private static final long serialVersionUID = 1L;
    private Integer idaudit;
    private Integer auditPlayerId;
    private LocalDateTime auditStartDate; 
    private LocalDateTime auditEndDate;
    private Short auditAttempts;
    private LocalDateTime auditRetryTime;

    public Audit(){

    }

    public Integer getIdaudit() {
        return idaudit;
    }

    public void setIdaudit(Integer idaudit) {
        this.idaudit = idaudit;
    }

    public Integer getAuditPlayerId() {
        return auditPlayerId;
    }

    public void setAuditPlayerId(Integer auditPlayerId) {
        this.auditPlayerId = auditPlayerId;
    }

    public LocalDateTime getAuditStartDate() {
        return auditStartDate;
    }

    public void setAuditStartDate(LocalDateTime auditStartDate) {
        this.auditStartDate = auditStartDate;
    }

    public LocalDateTime getAuditEndDate() {
        return auditEndDate;
    }

    public void setAuditEndDate(LocalDateTime auditEndDate) {
        this.auditEndDate = auditEndDate;
    }

    public Short getAuditAttempts() {
        return auditAttempts;
    }

    public void setAuditAttempts(Short auditAttempts) {
        this.auditAttempts = auditAttempts;
    }

    public LocalDateTime getAuditRetryTime() {
        return auditRetryTime;
    }

    public void setAuditRetryTime(LocalDateTime auditRetryTime) {
        this.auditRetryTime = auditRetryTime;
    }

     @Override
public String toString(){ 
LOG.info("starting toString for Audit!");
 try{
//    LOG.info("idaudit : "   + this.getIdaudit());
//    LOG.info("AuditStartDate no format: "   + this.getAuditStartDate());
//    LOG.info("AuditStartDate format LocalDateTime: "   + this.getAuditStartDate().format(ZDF_TIME));
   if(this.getIdaudit() != null){
       return 
        (NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEWLINE 
               + " ,idaudit : "   + this.getIdaudit()
               + " ,Audit Player : "   + this.getAuditPlayerId()
               + " ,StartDate format LocalDateTime: "   + this.getAuditStartDate().format(ZDF_TIME)
               + " ,EndDate format LocalDateTime: "   + this.getAuditEndDate().format(ZDF_TIME)
               + " ,Attempts : " + this.getAuditAttempts()
               + " ,RetryTime format LocalDateTime: "   + this.getAuditEndDate().format(ZDF_TIME)
        );
   }else{
       return
      (NEW_LINE + "from entite : " + this.getClass().getSimpleName() + NEWLINE 
         + " idAudit = null !!");
    }
        }catch(Exception e){
        String msg = " EXCEPTION in Audit.toString = " + e.getMessage();
        LOG.error(msg);
     //   LCUtil.showMessageFatal(msg);
        return msg;
  }
}
public static Audit mapAudit(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Audit a = new Audit();
            a.setIdaudit(rs.getInt("AuditId") );
            a.setAuditPlayerId(rs.getInt("AuditPlayerId") );
            a.setAuditStartDate(rs.getTimestamp("auditStartDate").toLocalDateTime());
            a.setAuditEndDate(rs.getTimestamp("auditEndDate").toLocalDateTime());
            a.setAuditAttempts(rs.getShort("AuditAttempts") );
            a.setAuditRetryTime(rs.getTimestamp("auditRetryTime").toLocalDateTime());
           
   return a;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map

} //end class