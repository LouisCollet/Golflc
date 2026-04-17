package entite;

import enumeration.WorkingDay;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class Professional implements Serializable{

@NotNull(message="{unavailable.startdate.notnull}")
    private LocalDateTime proStartDate;
@NotNull(message="{unavailable.enddate.notnull}")
    private LocalDateTime proEndDate;

@NotNull(message="{player.id.notnull}")
    private Integer proPlayerId;

@NotNull(message="{professional.amount.notnull}")
    private Double proAmount;
private Integer proId;
private Integer proClubId;
/** Bitmask: Mon=1 Tue=2 Wed=4 Thu=8 Fri=16 Sat=32 Sun=64 — default 127 = all days */
// private int proWorkDays = 127; // defautl = tous les jours travaillés
@Min(value = 1, message = "{professional.workdays.min}")
private int proWorkDays = 0; // defaut = aucun jour travaillé
public Professional(){ // constructor
 //  LOG.debug("constructor Professional executed !");
    } // end constructor

 //   @PostConstruct
//    public void init(){
 //       LOG.debug("init pro");
 //   }

    public LocalDateTime getProStartDate() {
        return proStartDate;
    }

    public void setProStartDate(LocalDateTime proStartDate) {
        this.proStartDate = proStartDate;
    }

    public LocalDateTime getProEndDate() {
        return proEndDate;
    }

    public void setProEndDate(LocalDateTime proEndDate) {
        this.proEndDate = proEndDate;
    }

    public Integer getProId() {
        return proId;
    }

    public void setProId(Integer proId) {
        this.proId = proId;
    }

    public Integer getProPlayerId() {
        return proPlayerId;
    }

    public void setProPlayerId(Integer proPlayerId) {
        this.proPlayerId = proPlayerId;
    }

    public Integer getProClubId() {
        return proClubId;
    }

    public void setProClubId(Integer proClubId) {
        this.proClubId = proClubId;
    }

    public Double getProAmount() {
        return proAmount;
    }

    public void setProAmount(Double proAmount) {
        this.proAmount = proAmount;
    }

    public int getProWorkDays() {
        return proWorkDays;
    }

    public void setProWorkDays(int proWorkDays) {
        this.proWorkDays = proWorkDays;
    }

@Size(min = 1, message = "{professional.workdays.min}")
public List<String> getSelectedDays() {
    EnumSet<WorkingDay> days = WorkingDay.fromMask(proWorkDays);
    return days.stream()
               .map(WorkingDay::name)
               .collect(Collectors.toList());
}

public void setSelectedDays(List<String> days) {
    if (days == null || days.isEmpty()) {
        this.proWorkDays = 0;
        return;
    }
    EnumSet<WorkingDay> enumDays = days.stream()
            .map(WorkingDay::valueOf)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(WorkingDay.class)));

    this.proWorkDays = WorkingDay.toMask(enumDays);
}
    public WorkingDay[] getDays() {
        return WorkingDay.values();
}


    /** Returns true if the pro works on the given DayOfWeek
     * @param dow
     * @return  */
    public boolean isWorkingOn(java.time.DayOfWeek dow) {
        return (proWorkDays & WorkingDay.from(dow).mask()) != 0;
    }

 @Override
public String toString(){
 try {
    LOG.debug("starting toString Professional !");
    return
            (NEW_LINE  + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + NEW_LINE + "<br>"  + "Pro Start Date : "   + this.proStartDate //.format(ZDF_TIME)
            + NEW_LINE + "<br>"  + "Pro End Date : "   + this.proEndDate //.format(ZDF_TIME)
            +"<br>"  + "Pro Id: "   + this.proId
            +"<br>"  + "Pro Id player : " + this.proPlayerId
            +"<br>"  + "Pro Id club : " + this.proClubId
            + NEW_LINE + "<br>"  + "Pro prix lesson : " + this.proAmount
            + NEW_LINE + "<br>"  + "Pro work days (bitmask) : " + this.proWorkDays
            + NEW_LINE + "<br>" + WorkingDay.printWorkingDays(this.proWorkDays) // added 08-04-2026 by LC
            + NEW_LINE + "<br>" + WorkingDay.printWorkingDaysLine(this.proWorkDays) // added 08-04-2026 by LC
          //  + getWorkingDaysLine
            );
        } catch (Exception ex) {
           LOG.error("Exception in Professional to String" + ex);
           return null;
        }
} //end method
} // end class