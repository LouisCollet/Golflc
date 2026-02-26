package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
// import jakarta.annotation.PostConstruct;  // migrated 2026-02-26 — POJO, not CDI-managed
// import jakarta.enterprise.context.SessionScoped;  // migrated 2026-02-24
// import jakarta.inject.Named;  // migrated 2026-02-24
import jakarta.validation.constraints.NotNull;
import utils.LCUtil;

// @Named("scheduleEvent")  // migrated 2026-02-24
// @SessionScoped  // migrated 2026-02-24

public class Lesson implements Serializable{
    
//    private Integer proId;
    private LocalDateTime eventStartDate;
    private LocalDateTime eventEndDate;
    private Integer eventProId;
    private Integer eventPlayerId;
    private boolean eventAllDay;
    @NotNull(message="{schedule.title.notnull}")
    private String eventTitle;
    private String eventDescription;
    private Double lessonAmount;
public Lesson(){ // constructor

    } // end constructor

    // @PostConstruct  // migrated 2026-02-26 — POJO, not CDI-managed
    public void init(){
            LOG.debug("Postconstruct executed !" );
    }

    public LocalDateTime getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(LocalDateTime eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public LocalDateTime getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(LocalDateTime eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public Integer getEventProId() {
        return eventProId;
    }

    public void setEventProId(Integer eventProId) {
        this.eventProId = eventProId;
    }

    public Integer getEventPlayerId() {
        return eventPlayerId;
    }

    public void setEventPlayerId(Integer eventPlayerId) {
        this.eventPlayerId = eventPlayerId;
    }


    public boolean isEventAllDay() {
        return eventAllDay;
    }

    public void setEventAllDay(boolean eventAllDay) {
        this.eventAllDay = eventAllDay;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public Double getLessonAmount() {
        return lessonAmount;
    }

    public void setLessonAmount(Double lessonAmount) {
        this.lessonAmount = lessonAmount;
    }

    public static Lesson map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try{
            Lesson event = new Lesson();
            event.setEventStartDate(rs.getTimestamp("EventStartDate").toLocalDateTime());
            event.setEventEndDate(rs.getTimestamp("EventEndDate").toLocalDateTime());
            event.setEventProId(rs.getInt("EventProId"));
            event.setEventPlayerId(rs.getInt("EventPlayerId"));
            event.setEventAllDay(rs.getBoolean("EventAllDay"));
            event.setEventTitle(rs.getString("EventTitle"));
            event.setEventDescription(rs.getString("EventDescription"));
 //              LOG.debug("ScheduleEvent event returned from map = " + event);
            return event;
        }catch(Exception e){
            String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } } //end method
 @Override
public String toString(){
 try {
 //   LOG.debug("starting toString ScheduleEvent !");
    return
            (NEW_LINE  + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
     //       + NEW_LINE + "<br>"
     //       + "Pro Id : " + this.proId
            + NEW_LINE + "<br>"  + "Start Date : "   + this.eventStartDate //.format(ZDF_TIME)
            + NEW_LINE + "<br>"  + "End Date : "   + this.eventEndDate //.format(ZDF_TIME)
            + NEW_LINE + "<br>"  + "Id Pro : "   + this.eventProId
            + NEW_LINE + "<br>"  + "Id player : " + this.eventPlayerId
            + NEW_LINE + "<br>"  + "All Day : " + this.eventAllDay
            + NEW_LINE + "<br>"  + "Title : " + this.eventTitle
            + NEW_LINE + "<br>"  + "Description : " + this.eventDescription
            + NEW_LINE + "<br>"  + "LessonAmount : " + this.lessonAmount
            );
        } catch (Exception ex) {
           LOG.error("Exception in ScheduleEvent to String" + ex);
           return null;
        }
} //end method
} // end class