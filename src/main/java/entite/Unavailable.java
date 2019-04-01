package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import javax.inject.Named;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import utils.LCUtil;

@Named
public class Unavailable implements Serializable, interfaces.Log, interfaces.GolfInterface{
     // Constants ----------------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    private Integer idcourse;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
 @NotNull(message="{unavailable.cause.notnull}")
    private String cause;
 
@NotNull(message="{tarifMember.startdate.notnull}")
@Future(message="{unavailable.start.future}")
    private Date workStartDate;

@NotNull(message="{tarifMember.enddate.notnull}")
@Future(message="{unavailable.end.future}")
    private Date workEndDate;

public Unavailable(){    // constructor
// empty
}

    public Integer getIdcourse() {
        return idcourse;
    }

    public void setIdcourse(Integer idcourse) {
        this.idcourse = idcourse;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        LOG.info("getEnd Date  = " + endDate);
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        LOG.info("setEnd Date ldt = " + endDate);
        this.endDate = endDate;
    }

    public Date getWorkStartDate() {
       LOG.info("getWorkStartDate = " + workStartDate);
        return workStartDate;
    }

    public void setWorkStartDate(Date workStartDate) {
        LOG.info("setWorkStartDate = " + workStartDate);
        setStartDate(utils.LCUtil.DatetoLocalDateTime(workStartDate));
        this.workStartDate = workStartDate;
    }
 public void setStartDate(LocalDateTime startDate) {
        LOG.info("setStartDate ldt = " + startDate);
        this.startDate = startDate;
    }
    public Date getWorkEndDate() {
        return workEndDate;
    }

    public void setWorkEndDate(Date workEndDate) {
        setEndDate(utils.LCUtil.DatetoLocalDateTime(workEndDate));
        this.workEndDate = workEndDate;
    }

 @Override
public String toString()
{ return 
        (NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName()
               + " ,idcourse : "   + this.getIdcourse()
               + " ,startDate : "  + this.getStartDate()
               + " ,endDate : "    + this.getEndDate()
               + " ,cause : "    + this.getCause()
        );
}

public static Unavailable mapUnavailable(ResultSet rs) throws SQLException{
    String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Unavailable u = new Unavailable();
        u.setIdcourse(rs.getInt("UnavailableIdCourse"));
        u.setStartDate(rs.getTimestamp("UnavailableStartDate").toLocalDateTime());
        u.setEndDate(rs.getTimestamp("UnavailableEndDate").toLocalDateTime());
        u.setCause(rs.getString("UnavailableCause"));
   return u;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class