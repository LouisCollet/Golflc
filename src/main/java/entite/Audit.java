
package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
// import jakarta.enterprise.context.RequestScoped;  // migrated 2026-02-24
// import jakarta.inject.Named;  // migrated 2026-02-24
import utils.LCUtil;

// @Named  // migrated 2026-02-24
// @RequestScoped  // migrated 2026-02-24
public class Audit implements Serializable, interfaces.Log, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static final long serialVersionUID = 1L;
    private Integer idaudit;
    private Integer auditPlayerId;
    private LocalDateTime auditStartDate; 
    private LocalDateTime auditEndDate;
  //  private Short auditAttempts;
  //  private LocalDateTime auditRetryTime;

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
/*
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
*/
     @Override
public String toString(){ 
LOG.debug("starting toString for Audit!");
 try{
//    LOG.debug("idaudit : "   + this.getIdaudit());
//    LOG.debug("AuditPlayer : " + this.getAuditPlayerId());
//    LOG.debug("AuditStartDate no format: "   + this.getAuditStartDate());
//    LOG.debug("AuditStartDate format LocalDateTime: "   + this.getAuditStartDate().format(ZDF_TIME));
  if(this.getClass() == null){
        return (CLASSNAME + " is null, no print !! ");
     } 
       return 
        (NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE 
               + " idaudit : "   + this.getIdaudit()
               + " , Audit Player : "   + this.getAuditPlayerId()
               + " , StartDate : "   + this.getAuditStartDate() //.format(ZDF_TIME)
               + " , EndDate : "   + this.getAuditEndDate() //.format(ZDF_TIME)
           + NEW_LINE
    //           + " ,Attempts : " + this.getAuditAttempts()
    //           + " ,RetryTime format LocalDateTime: "   + this.getAuditEndDate().format(ZDF_TIME)
        );
//   }else{
//       return
 //     (NEW_LINE + "from entite : " + this.getClass().getSimpleName() + NEW_LINE 
 //        + " idAudit = null !!");
 //   }
        }catch(Exception e){
        String msg = " EXCEPTION in Audit.toString = " + e.getMessage();
        LOG.error(msg);
     //   LCUtil.showMessageFatal(msg);
        return msg;
  }
}
public static Audit mapAudit(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(); 
 try{
        LOG.debug("entering mapAudit");
    Audit a = new Audit();
    a.setIdaudit(rs.getInt("AuditId") );
    a.setAuditPlayerId(rs.getInt("AuditPlayerId") );
    a.setAuditStartDate(rs.getTimestamp("auditStartDate").toLocalDateTime());
    a.setAuditEndDate(rs.getTimestamp("auditEndDate").toLocalDateTime());
        LOG.debug ("audit returned = " + a);
   return a;
 }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map

} //end class