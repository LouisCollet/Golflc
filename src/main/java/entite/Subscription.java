package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.inject.Named;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDate;

@Named
public class Subscription implements Serializable, interfaces.Log, interfaces.GolfInterface
{
     // Constants ----------------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    private Integer idplayer;
    private LocalDate startDate;
    private LocalDate endDate; // mod 30/01/2017
    @NotNull(message="{subscription.notnull}")
    private String subCode;
    @Max(value=5,message="{subscription.trial.max}")
    private Integer trialCount;
    public enum etypeSubscrition{TRIAL, MONTHLY, YEARLY};
    private String paymentReference;
    
public Subscription()    // constructor
{ 
// empty
}

// getter and setters

    public Integer getIdplayer() {
        return idplayer;
    }

    public void setIdplayer(Integer idplayer) {
        this.idplayer = idplayer;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
 //       LOG.info("getEnd Date subscription = " + endDate);
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
  //      LOG.info("setEnd Date subscription = " + endDate);
        this.endDate = endDate;
    }
    
public String getSubCode() {
   //      LOG.info("getSubCode subscription = " + subCode);
        return subCode;
    }

    public void setSubCode(String subCode) {
   //      LOG.info("setsubcode subscription = " + subCode);
        this.subCode = subCode;
    }
    public Integer getTrialCount() {
        return trialCount;
    }

    public void setTrialCount(Integer trialCount) {
        this.trialCount = trialCount;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

 @Override
public String toString()
{ return 
        (NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName()
               + " ,idplayer : "   + this.getIdplayer()
               + " ,startDate : "  + this.getStartDate()
               + " ,endDate : "    + this.getEndDate()
               + " ,subcode : "    + this.getSubCode()
               + " ,trial count : "  + this.getTrialCount()
               + " ,reference payment : "  + this.getPaymentReference()
        );
}

public static Subscription mapSubscription(ResultSet rs) throws SQLException{
    String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Subscription s = new Subscription();
        s.setIdplayer(rs.getInt("subscription_player_id") );
            java.util.Date d = rs.getTimestamp("SubscriptionStartDate");
        s.setStartDate(DatetoLocalDate(d));
        d = rs.getTimestamp("SubscriptionEndDate");
        s.setEndDate(DatetoLocalDate(d));
        s.setTrialCount(rs.getInt("SubscriptionTrialCount"));
        s.setPaymentReference(rs.getString("SubscriptionPaymentReference"));
   return s;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method


} // end class