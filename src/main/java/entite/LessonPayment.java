package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import jakarta.annotation.PostConstruct;
// import jakarta.enterprise.context.RequestScoped;  // migrated 2026-02-24

// import jakarta.inject.Named;  // migrated 2026-02-24

import utils.LCUtil;

// @Named("lessonPayment")  // migrated 2026-02-24
// @RequestScoped  // migrated 2026-02-24

public class LessonPayment implements Serializable{
    
//    private Integer proId;
    private LocalDateTime paymentStartDate;
    private LocalDateTime paymentEndDate;
    private Double paymentAmount;
    private Integer paymentIdStudent;
    private String paymentCommunication;
    private LocalDateTime paymentDate;
    private Integer paymentIdClub;
    
  //  private Integer eventProId;
    
  //  private boolean eventAllDay;
  //  @NotNull(message="{schedule.title.notnull}")
  //  private String eventTitle;
   // private String eventDescription;
   
// public LessonPayment(){ }// constructor

  //  @PostConstruct
  //  public void init(){
  //          LOG.debug("Postconstruct executed !" );
  //  }

    public LocalDateTime getPaymentStartDate() {
        return paymentStartDate;
    }

    public void setPaymentStartDate(LocalDateTime paymentStartDate) {
        this.paymentStartDate = paymentStartDate;
    }

    public LocalDateTime getPaymentEndDate() {
        return paymentEndDate;
    }

    public void setPaymentEndDate(LocalDateTime paymentEndDate) {
        this.paymentEndDate = paymentEndDate;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Integer getPaymentIdStudent() {
        return paymentIdStudent;
    }

    public void setPaymentIdStudent(Integer paymentIdStudent) {
        this.paymentIdStudent = paymentIdStudent;
    }

    public String getPaymentCommunication() {
        return paymentCommunication;
    }

    public void setPaymentCommunication(String paymentCommunication) {
        this.paymentCommunication = paymentCommunication;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Integer getPaymentIdClub() {
        return paymentIdClub;
    }

    public void setPaymentIdClub(Integer paymentIdClub) {
        this.paymentIdClub = paymentIdClub;
    }
/*
    public static LessonPayment map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try{
            LessonPayment lp = new LessonPayment();
                LOG.debug("starting LessonPayment");
            lp.setPaymentStartDate(rs.getTimestamp("LessonStartDate").toLocalDateTime());
            lp.setPaymentEndDate(rs.getTimestamp("LessonEndDate").toLocalDateTime());
            lp.setPaymentDate(rs.getTimestamp("LessonModificationDate").toLocalDateTime());
            lp.setPaymentCommunication(rs.getString("LessonCommunication"));
            lp.setPaymentAmount(rs.getDouble("LessonAmount"));
            lp.setPaymentIdStudent(rs.getInt("LessonIdStudent")); // mod 02-02-2023
            lp.setPaymentIdClub(rs.getInt("LessonIdClub"));
                LOG.debug("PaymentIdStudent = " +  lp.getPaymentIdStudent());
            
       //     lp.setEventEndDate(rs.getTimestamp("EventEndDate").toLocalDateTime());
            
       //     lp.setEventProId(rs.getInt("EventProId"));
       //     lp.setEventPlayerId(rs.getInt("EventPlayerId"));
      //      event.setEventAllDay(rs.getBoolean("EventAllDay"));
       //     event.setEventTitle(rs.getString("EventTitle"));
       //     event.setEventDescription(rs.getString("EventDescription"));
 //              LOG.debug("ScheduleEvent event returned from map = " + event);
            return lp;
        }catch(Exception e){
            String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } } //end method
    */
 @Override
public String toString(){
 try {
 //   LOG.debug("starting toString ScheduleEvent !");
    return
            (NEW_LINE  + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
     //       + NEW_LINE + "<br>"
     //       + "Pro Id : " + this.proId
            + NEW_LINE + "<br>"  + "Start Date : "   + this.paymentStartDate //.format(ZDF_TIME)
            + NEW_LINE + "<br>"  + "End Date : "   + this.paymentEndDate //.format(ZDF_TIME)
//            + NEW_LINE + "<br>"  + "Id Pro : "   + this.eventProId
            + NEW_LINE + "<br>"  + "Id student : " + this.paymentIdStudent
        //    + NEW_LINE + "<br>"  + "All Day : " + this.eventAllDay
         //   + NEW_LINE + "<br>"  + "Title : " + this.eventTitle
   //         + NEW_LINE + "<br>"  + "Description : " + this.eventDescription
            + NEW_LINE + "<br>"  + "LessonAmount : " + this.paymentAmount
            );
        } catch (Exception ex) {
           LOG.error("Exception in ScheduleEvent to String" + ex);
           return null;
        }
} //end method
} // end class