package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import utils.LCUtil;

//@Named enlevé 14-02-2026
//@RequestScoped
public class Subscription implements Serializable, interfaces.GolfInterface{
     // Constants ----------------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private Integer idplayer;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate; // mod 30/01/2017
    
    @NotNull(message="{subscription.notnull}")
    private String subCode;
    @Max(value=5,message="{subscription.trial.max}")
    private Short trialCount;
    public enum etypeSubscription{TRIAL,MONTHLY,YEARLY,INITIAL};
    private String paymentReference;
 //   double price;
    private double subscriptionAmount; // new 22-02-2024 ajouté field dans table
    private String communication;
    private LocalDateTime paymentDate;
    private boolean errorStatus;
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    
public String getSubCode() {
   //      LOG.debug("getSubCode subscription = " + subCode);
        return subCode;
    }

    public void setSubCode(String subCode) {
   //      LOG.debug("setsubcode subscription = " + subCode);
        this.subCode = subCode;
    }
    public Short getTrialCount() {
        return trialCount;
    }

    public void setTrialCount(Short trialCount) {
        this.trialCount = trialCount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public double getSubscriptionAmount() {
        return subscriptionAmount;
    }

    public void setSubscriptionAmount(double subscriptionAmount) {
        this.subscriptionAmount = subscriptionAmount;
    }

    public boolean isErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(boolean errorStatus) {
        this.errorStatus = errorStatus;
    }



    public String getCommunication() {
        return communication;
    }

    public void setCommunication(String communication) {
        this.communication = communication;
    }

 @Override
public String toString(){
     final String methodName = utils.LCUtil.getCurrentMethodName();
  try{  
 //     LOG.debug("starting toString Subscription!");
     if(this.getClass() == null){
       return (CLASSNAME + " is null, no print !");
    } 
    return 
        (NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName().toUpperCase()+ NEW_LINE 
               + " ,idplayer : "   + this.getIdplayer()
               + " ,startDate : "  + this.getStartDate()
               + " ,endDate : "    + this.getEndDate()
               + " ,payment Date : "    + this.getPaymentDate()
         + NEW_LINE
               + " ,subcode : "    + this.getSubCode()
               + " ,trial count : "  + this.getTrialCount()
               + " ,reference payment : "  + this.getPaymentReference()
               + " ,amount : "  + this.getSubscriptionAmount()
               + " ,communication : "  + this.getCommunication()
        );
    }catch(Exception e){
        String msg = "£££ Exception in Subscription.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}
/*
public static Subscription map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
        Subscription s = new Subscription();
        s.setIdplayer(rs.getInt("SubscriptionIdPlayer") );
        s.setStartDate(rs.getTimestamp("SubscriptionStartDate").toLocalDateTime());
        s.setEndDate(rs.getTimestamp("SubscriptionEndDate").toLocalDateTime());
        s.setTrialCount(rs.getShort("SubscriptionTrialCount"));
        s.setPaymentReference(rs.getString("SubscriptionPaymentReference"));
        s.setCommunication(rs.getString("SubscriptionCommunication")); // new 03-03-2024
        s.setSubscriptionAmount(rs.getDouble("SubscriptionAmount")); // new 22-02-2024
        s.setPaymentDate(rs.getTimestamp("SubscriptionModificationDate").toLocalDateTime());
   return s;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map
*/
} // end class