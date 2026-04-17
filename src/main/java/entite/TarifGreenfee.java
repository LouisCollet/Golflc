package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import static utils.LCUtil.showMessageFatal;

// ATTENTION ! si on change la structure, la situation DB  fields dans DB ne sont plus reconnues par la version actuelle du l'entite  !!
// donc rupture de compatibilité ascendante !! c'est très grave !!

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // y compris private fields
@JsonInclude(JsonInclude.Include.NON_NULL) // new 27/05/2022
@JsonPropertyOrder({"datesSeasonsList","greenfeeType","daysList","teeTimesList","equipmentsList","basicList","twilightList"}) // new 22/01/2019 not working ?

public class TarifGreenfee implements Serializable{
    
// fields reprises en json dans table mysql
  private ArrayList<DatesSeasons> datesSeasonsList = new ArrayList<>(); // pas de dimension de départ
  private ArrayList<TeeTimes> teeTimesList = new ArrayList<>(); 
  private ArrayList<EquipmentsAndBasic> equipmentsList = new ArrayList<>(); 
  private ArrayList<EquipmentsAndBasic> basicList = new ArrayList<>();
  private ArrayList<DaysGreenfee> daysList = new ArrayList<>(); 
  private ArrayList<Twilight> twilightList = new ArrayList<>();
  private String greenfeeType = ""; //  BA = basic, DA = days, HO = Hours, EQ = equipments
  
// les fields qui suivent ne sont pas reprises dans le json
  
@JsonIgnore private ArrayList<TeeTimes> teeTimeChoosen = new ArrayList<>(); // new 04/05/2022
@JsonIgnore private ArrayList<EquipmentsAndBasic> equipmentChoosen = new ArrayList<>(); 
@JsonIgnore private ArrayList<DaysWeek> dayChoosen = new ArrayList<>(); 
@JsonIgnore private ArrayList<TeeTimes> timeChoosen = new ArrayList<>(); 
//@JsonIgnore public Double [] workDaysPrice; 
@JsonIgnore private Double  [] workDaysPrice;
@JsonIgnore private boolean[] workDaysAvailable; // true=available, false=N/A — parallel to workDaysPrice
@JsonIgnore private String season;
@JsonIgnore private List<LocalDate> multiTwilight;
@JsonIgnore final private DayType type = DayType.MONDAY; // Default priority

@NotNull(message="{tarif.period.startdate.notnull}")
@JsonIgnore private LocalDateTime startDate;

@NotNull(message="{tarif.period.enddate.notnull}")
@JsonIgnore private LocalDateTime endDate;

//@NotNull(message="{tarifMember.startdate.notnull}")
@JsonIgnore  private LocalTime startHour;

//@NotNull(message="{tarifMember.enddate.notnull}")
@JsonIgnore  private LocalTime endHour;
    
    @NotNull(message="{tarif.number.notnull}")
    @Min(value=0,message="{tarif.number.min}")
    @Max(value=20,message="{tarif.number.max}")

@JsonIgnore  private Integer tarifIndexPayment; // pour period
@JsonIgnore  private Double priceGreenfee; // pour periods
// saisie from screens
@NotNull(message="{tarifMember.workitem.notnull}")
@JsonIgnore  private String workItem;

 @NotNull(message="{tarifMember.workprice.notnull}")
 @JsonIgnore private Double workPrice;

@NotNull(message="{tarifMember.workseason.notnull}")
@JsonIgnore private String workSeason;
@JsonIgnore private String workSeasonTwilight;
@JsonIgnore private String workLinkedSlotKey; // null = not linked to any HO slot

@NotNull(message="{tarifMember.twilight.notnull}")
@JsonIgnore private String workTwilight;

@JsonIgnore  private boolean updateReady;
@JsonIgnore  private boolean equipmentsReady;
@JsonIgnore  private boolean twilightReady;
@JsonIgnore  private boolean twilightDone;
@JsonIgnore  private String dayOfWeek;
@JsonIgnore  private Integer tarifId;           // DB primary key — set by RowMapper
@JsonIgnore  private Integer tarifYear;         // DB column TarifYear — set by RowMapper
@JsonIgnore  private List<Integer> tarifCourseId = new ArrayList<>();
@JsonIgnore  private List<Integer> tarifHoles = new ArrayList<>(List.of(18));  // 9 et/ou 18 — not stored in TarifJson
@JsonIgnore  private String currency;


public enum DayType {MONDAY,FRIDAY,WEEK,WEEKEND,HOLIDAY};
//  testing 
//private int num = 175;

/*
    class Inner_demo{
        private String lc = "Louis";
        public void print(){
            LOG.debug("print from Inner_demo");
        }
        public int getNum(){
            LOG.debug("this is the getNum() method of the inner class");
            return num;
        }

        public String getLc() {
            return lc;
        }

        public void setLc(String lc) {
            this.lc = lc;
        }
        
    } //end inner classDemo
   */ 
    
    
public static class TeeTimes{
  private LocalTime startTime;
  private LocalTime endTime;
  private String season;
  private String item;
  private Double price;
  private Integer quantity;
  public String twilight;
  private boolean available = true; // true=available, false=N/A
  private String slotKey;           // UUID substring — identifies this slot for linked equipments

    public String getSlotKey() { return slotKey; }
    public void setSlotKey(String slotKey) { this.slotKey = slotKey; }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getTwilight() {
        return twilight;
    }

    public void setTwilight(String twilight) {
        this.twilight = twilight;
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

  public String toString(){
 try {
//      LOG.debug("starting toString TarifGreenfee !");
    return
            ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + " start='" + startTime
            + ", end=" + endTime
            + ", season=" + season
            + ", item=" + item
            + ", price=" + price
            + ", quantity=" + quantity
            + ", Twilight=" + twilight
            + ", available=" + available
            );
 }catch(Exception e){
    String msg = "£££ Exception in TeeTimes.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
        }
} //end method
} // end inner class TeeTimes
public static class DaysGreenfee{
  private String season;
  private String category;
  public Double[] price;     // monday, week, friday, weekend, holiday
  public boolean[] available; // parallel — true=available, false=N/A
  public String twilight;
  public DaysGreenfee(){  // empty constructor
      price     = new Double [5];
      available = new boolean[]{true, true, true, true, true};
  }

    public boolean[] getAvailable() { return available; }
    public void setAvailable(boolean[] available) { this.available = available; }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
      public Double[] getPrice() {
        return price;
    }

    public void setPrice(Double[] price) {
        this.price = price;
    }  

    public String getTwilight() {
        return twilight;
    }

    public void setTwilight(String twilight) {
        this.twilight = twilight;
    }

  @Override
  public String toString(){
 try {
    return
            ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + ", season = " + season 
            + ", category = " + category 
            + ", price[] = " + Arrays.toString(price)
            + ", twilight = " + twilight
            );
 }catch(Exception e){
    String msg = "£££ Exception in DaysGreenfee.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
 }
} //end method
} // end class DaysGreenfee
public static class Twilight{
  private LocalTime startTime;
  private String season; 
  private List<Integer> months;

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public List<Integer> getMonths() {
        return months;
    }

    public void setMonths(List<Integer> months) {
        this.months = months;
    }

 
  @Override
  public String toString(){
 try {
//      LOG.debug("starting toString TarifGreenfee !");
    return
            ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + " start='" + startTime
       //     + ", end=" + endTime
            + ", season=" + season
            + ", months List=" + months
       //     + ", price=" + price
        //    + ", quantity=" + quantity
            );
 }catch(Exception e){
    String msg = "£££ Exception in Twilight.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
        }
} //end method
} // end class Twilight
public class DaysWeek{
  private String category;
  private String season; 
  private Double price;
  private Integer quantity;
  private TarifGreenfee.DayType dayType; // {MONDAY,FRIDAY,WEEK,WEEKEND,HOLIDAY};
  private LocalTime twilightStartTime;
  //Global class constructor
  public DaysWeek(String i, String n, Double p, TarifGreenfee.DayType d, Integer q, LocalTime t){
     category= i;
     season = n;
     price = p;
     dayType = d;
     quantity = q;
     twilightStartTime = t;
  }
  public DaysWeek(){  // constructor
    quantity = 0;
  }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public TarifGreenfee.DayType getDayType() {
        return dayType;
    }

    public void setDayType(TarifGreenfee.DayType dayType) {
        this.dayType = dayType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalTime getTwilightStartTime() {
        return twilightStartTime;
    }

    public void setTwilightStartTime(LocalTime twilightStartTime) {
        this.twilightStartTime = twilightStartTime;
    }
  
  @Override
  public String toString(){
 try {
//      LOG.debug("starting toString TarifGreenfee !");
    return
            ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + ", category = " + category
            + ", season = " + season
            + ", price = " + price
            + ", DayType = " + dayType
            + ", quantity = " + quantity
            + ", twilight start time = " + twilightStartTime
            );
 }catch(Exception e){
    String msg = "£££ Exception in DaysWeek.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
        }
} //end method
} // end class DaysWeek
public static class DatesSeasons{
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String season;  

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

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }
  
  
  //Employee class constructor
  public DatesSeasons(LocalDateTime st, LocalDateTime en, String n){
     startDate =st;
     endDate =en;
     season = n;
  }
  public DatesSeasons(){
  //   startDate =st;
  //   endDate =en;
  //   season = n;
  }
  
  public String toString(){
 try {
//      LOG.debug("starting toString TarifGreenfee !");
    return
            ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + " start='" + startDate
            + ", end=" + endDate
            + ", season=" + season
            );
 }catch(Exception e){
    String msg = "£££ Exception in DatesSeasons.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
        }
} //end method
  
} // end class DatesSeasons


public TarifGreenfee(){ // constructor 1
        workDaysPrice    = new Double[5];   // price for monday, week ... holidays
        workDaysAvailable = new boolean[]{true, true, true, true, true};
//        for(String[] subarray : workDays){
 //           Arrays.fill(subarray, "0");
 //       }
        priceGreenfee = 0.0;
        tarifIndexPayment = 0;
        updateReady = false;
        equipmentsReady = false;
        twilightReady = false;
        twilightDone = false;
        workTwilight = "N";
        multiTwilight = null;
        currency = "???";
    } // end constructor

    public Integer getTarifId() { return tarifId; }
    public void setTarifId(Integer tarifId) { this.tarifId = tarifId; }

    public Integer getTarifYear() { return tarifYear; }
    public void setTarifYear(Integer tarifYear) { this.tarifYear = tarifYear; }

    public List<Integer> getTarifCourseId() {
        return tarifCourseId;
    }

    public void setTarifCourseId(List<Integer> tarifCourseId) {
        this.tarifCourseId = tarifCourseId != null ? tarifCourseId : new ArrayList<>();
    }

    /** Retourne le premier holes — backward compat pour RowMapper, display, delete, find. */
    public Integer getTarifHoles() {
        return (tarifHoles == null || tarifHoles.isEmpty()) ? 18 : tarifHoles.get(0);
    } // end method

    /** Appelé par RowMapper (single value depuis DB). */
    public void setTarifHoles(Integer h) {
        this.tarifHoles = new ArrayList<>(List.of(h != null ? h : 18));
    } // end method

    /** Liste complète — utilisée par le wizard et CreateTarifGreenfee. */
    public List<Integer> getTarifHolesList() {
        return tarifHoles != null ? tarifHoles : new ArrayList<>(List.of(18));
    } // end method

    public void setTarifHolesList(List<Integer> tarifHoles) {
        this.tarifHoles = tarifHoles;
    } // end method

    /** Affichage formaté ex: "18T" ou "18T + 9T" — pour wizard header et confirm. */
    public String getTarifHolesDisplay() {
        if (tarifHoles == null || tarifHoles.isEmpty()) return "18T";
        return tarifHoles.stream().map(h -> h + "T").collect(java.util.stream.Collectors.joining(" + "));
    } // end method

    public LocalTime getStartHour() {
        return startHour;
    }

    public void setStartHour(LocalTime startHour) {
        this.startHour = startHour;
    }

    public LocalTime getEndHour() {
        return endHour;
    }

    public void setEndHour(LocalTime endHour) {
        this.endHour = endHour;
    }

    public DayType getType() {
        return type;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public boolean isEquipmentsReady() {
        return equipmentsReady;
    }

    public void setEquipmentsReady(boolean equipmentsReady) {
        this.equipmentsReady = equipmentsReady;
    }

    public boolean isTwilightReady() {
        return twilightReady;
    }

    public boolean isTwilightDone() {
        return twilightDone;
    }

    public void setTwilightDone(boolean twilightDone) {
        this.twilightDone = twilightDone;
    }

    public String getWorkTwilight() {
        return workTwilight;
    }

    public void setWorkTwilight(String workTwilight) {
        this.workTwilight = workTwilight;
    }

    public void setTwilightReady(boolean twilightReady) {
        this.twilightReady = twilightReady;
    }

    public ArrayList<EquipmentsAndBasic> getEquipmentsList() {
        return equipmentsList;
    }

    public void setEquipmentsList(ArrayList<EquipmentsAndBasic> equipmentsList) {
        this.equipmentsList = equipmentsList;
    }

    public boolean isUpdateReady() {
        return updateReady;
    }

    public void setUpdateReady(boolean updateReady) {
        this.updateReady = updateReady;
    }

    public ArrayList<EquipmentsAndBasic> getEquipmentChoosen() {
        return equipmentChoosen;
    }

    public void setEquipmentChoosen(ArrayList<EquipmentsAndBasic> equipmentChoosen) {
        this.equipmentChoosen = equipmentChoosen;
    }

    public Double getPriceGreenfee() {
        //      LOG.debug("getPriceGreenfee = " + priceGreenfee);
        return priceGreenfee;
    }

    public void setPriceGreenfee(Double priceGreenfee) {
        this.priceGreenfee = priceGreenfee;
 //       LOG.debug("setPriceGreenfee = " + priceGreenfee);
//        this.unitPrice = priceGreenfee;
    }

//public void RemoveNull(){
//    datesSeason = utils.LCUtil.removeNull2D(datesSeason);
// 27/04/2022    teeTimes = utils.LCUtil.removeNull2D(teeTimes);
 //   priceEquipments = utils.LCUtil.removeNull2D(priceEquipments);
  //  priceGreenfees = utils.LCUtil.removeNull2D(priceGreenfees);
//    days = utils.LCUtil.removeNull2D(days);
//    workDays = utils.LCUtil.removeNull2D(workDays); // new 21/04/2022
 //  daysChoosen = utils.LCUtil.removeNull2D(daysChoosen); // new 21/04/2022
  //      LOG.debug("null removed from all TarifGreenfee Arrays");
//}

    
    public Integer getTarifIndexPayment() {
        return tarifIndexPayment;
    }

    public ArrayList<Twilight> getTwilightList() {
        return twilightList;
    }

    public void setTwilightList(ArrayList<Twilight> twilightList) {
        this.twilightList = twilightList;
    }

    public void setTarifIndexPayment(Integer tarifIndexPayment) {
        this.tarifIndexPayment = tarifIndexPayment;
    }

    public String getWorkItem() {
        return workItem;
    }

    public void setWorkItem(String workItem) {
        this.workItem = workItem;
    }

    public Double getWorkPrice() {
        return workPrice;
    }

    public void setWorkPrice(Double workPrice) {
        this.workPrice = workPrice;
    }

    public String getWorkSeason() {
        return workSeason;
    }

    public void setWorkSeason(String workSeason) {
        this.workSeason = workSeason;
    }

    public String getWorkLinkedSlotKey() { return workLinkedSlotKey; }
    public void setWorkLinkedSlotKey(String workLinkedSlotKey) { this.workLinkedSlotKey = workLinkedSlotKey; }

    public ArrayList<EquipmentsAndBasic> getBasicList() {
        return basicList;
    }

    public void setBasicList(ArrayList<EquipmentsAndBasic> basicList) {
        this.basicList = basicList;
    }


    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
      // ici modifier    value="#{courseC.tarifGreenfee.datesSeason[courseC.tarifGreenfee.tarifIndexSeasons][0]}"
 //  enlevé 03-05-2021   datesSeason[tarifIndexSeasons][0] = ZDF_DAY.format(startDate);
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        // ici modifier value="#{courseC.tarifGreenfee.datesSeason[courseC.tarifGreenfee.tarifIndexSeasons][1]}"
        // date to String : quel format
        //static java.text.DateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");
//String s = SDF.format(endDate);
  //      datesSeason[tarifIndexSeasons][1] = ZDF_DAY.format(endDate);
        this.endDate = endDate;
    }

    public List<LocalDate> getMultiTwilight() {
        return multiTwilight;
    }

    public void setMultiTwilight(List<LocalDate> multiTwilight) {
        this.multiTwilight = multiTwilight;
    }

    public String getSeason() {
        return season;
    }

    public String getWorkSeasonTwilight() {
        return workSeasonTwilight;
    }

    public void setWorkSeasonTwilight(String workSeasonTwilight) {
        this.workSeasonTwilight = workSeasonTwilight;
    }

    public void setSeason(String season) {
        this.season = season;
 //       LOG.debug("setted setSeason = " + season);
    }

    public String getGreenfeeType() {
        return greenfeeType;
    }

    public void setGreenfeeType(String greenfeeType) {
        this.greenfeeType = greenfeeType;
    }

    public ArrayList<DatesSeasons> getDatesSeasonsList() {
        return datesSeasonsList;
    }

    public void setDatesSeasonsList(ArrayList<DatesSeasons> datesSeasonsList) {
        this.datesSeasonsList = datesSeasonsList;
    }

    public ArrayList<TeeTimes> getTeeTimesList() {
        return teeTimesList;
    }

    public void setTeeTimesList(ArrayList<TeeTimes> teeTimesList) {
        this.teeTimesList = teeTimesList;
    }

    public ArrayList<TeeTimes> getTeeTimeChoosen() {
        return teeTimeChoosen;
    }

    public void setTeeTimeChoosen(ArrayList<TeeTimes> teeTimeChoosen) {
        this.teeTimeChoosen = teeTimeChoosen;
    }

    public ArrayList<DaysWeek> getDayChoosen() {
        return dayChoosen;
    }

    public void setDayChoosen(ArrayList<DaysWeek> dayChoosen) {
        this.dayChoosen = dayChoosen;
    }

    public ArrayList<TeeTimes> getTimeChoosen() {
        return timeChoosen;
    }

    public void setTimeChoosen(ArrayList<TeeTimes> timeChoosen) {
        this.timeChoosen = timeChoosen;
    }

    public ArrayList<DaysGreenfee> getDaysList() {
        return daysList;
    }

    public void setDaysList(ArrayList<DaysGreenfee> daysList) {
        this.daysList = daysList;
    }


    public Double[] getWorkDaysPrice() {
        return workDaysPrice;
    }

    public void setWorkDaysPrice(Double[] workDaysPrice) {
        this.workDaysPrice = workDaysPrice;
    }

    public boolean[] getWorkDaysAvailable() { return workDaysAvailable; }
    public void setWorkDaysAvailable(boolean[] workDaysAvailable) { this.workDaysAvailable = workDaysAvailable; }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

public String showTarifGreenfee() {
    LOG.debug("entering showTarifGreenfee");
    try {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:monospace; font-size:0.82rem; line-height:1.7;")
          .append(" max-height:300px; overflow-y:auto; padding-right:0.5rem;'>");

        // header
        sb.append("<div style='background:var(--surface-100,#f4f4f4); padding:0.3rem 0.6rem;")
          .append(" border-radius:4px; margin-bottom:0.5rem;'>")
          .append("<b>Type:</b> ").append(greenfeeType)
          .append(" &nbsp;|&nbsp; <b>Holes:</b> ").append(getTarifHolesDisplay())
          .append(" &nbsp;|&nbsp; <b>Course:</b> ").append(tarifCourseId)
          .append(" &nbsp;|&nbsp; <b>Currency:</b> ").append(currency)
          .append("</div>");

        // datesSeasonsList
        sb.append(sectionTitle("&#128197; Periods", datesSeasonsList.size()));
        if (datesSeasonsList.isEmpty()) {
            sb.append(emptyLine());
        } else {
            for (var ds : datesSeasonsList) {
                sb.append(row("<b>" + ds.getSeason() + "</b>"
                    + " : " + (ds.getStartDate() != null ? ds.getStartDate().toLocalDate() : "?")
                    + " &#8594; " + (ds.getEndDate()   != null ? ds.getEndDate().toLocalDate()   : "?")));
            }
        }

        // teeTimesList
        if (!teeTimesList.isEmpty()) {
            sb.append(sectionTitle("&#9200; Tee Times", teeTimesList.size()));
            for (var tt : teeTimesList) {
                sb.append(row("<b>" + tt.getItem() + "</b>"
                    + " [" + tt.getSeason() + "]"
                    + " &nbsp; " + tt.getStartTime() + " &#8594; " + tt.getEndTime()
                    + " &nbsp; " + tt.getPrice() + " &#8364;"
                    + " &nbsp; twilight=" + tt.getTwilight()
                    + " &nbsp; key=<code>" + tt.getSlotKey() + "</code>"));
            }
        }

        // basicList
        if (!basicList.isEmpty()) {
            sb.append(sectionTitle("&#128181; Basic", basicList.size()));
            for (var b : basicList) {
                sb.append(row("<b>" + b.getItem() + "</b>"
                    + " [" + b.getSeason() + "]"
                    + " &nbsp; " + b.getPrice() + " &#8364;"));
            }
        }

        // daysList
        if (!daysList.isEmpty()) {
            sb.append(sectionTitle("&#128198; Days", daysList.size()));
            for (var d : daysList) {
                sb.append(row("<b>" + d.getCategory() + "</b>"
                    + " [" + d.getSeason() + "]"
                    + " &nbsp; Mon=" + d.getPrice()[0]
                    + " Wk=" + d.getPrice()[1]
                    + " Fri=" + d.getPrice()[2]
                    + " W-E=" + d.getPrice()[3]
                    + " Hol=" + d.getPrice()[4]));
            }
        }

        // equipmentsList
        if (!equipmentsList.isEmpty()) {
            sb.append(sectionTitle("&#127922; Equipments", equipmentsList.size()));
            for (var eq : equipmentsList) {
                String slotDisplay = "all";
                if (eq.getLinkedSlotKey() != null) {
                    slotDisplay = teeTimesList.stream()
                            .filter(t -> eq.getLinkedSlotKey().equals(t.getSlotKey()))
                            .findFirst()
                            .map(t -> t.getStartTime() + "–" + t.getEndTime())
                            .orElse("<code>" + eq.getLinkedSlotKey() + "</code>");
                }
                sb.append(row("<b>" + eq.getItem() + "</b>"
                    + " [" + eq.getSeason() + "]"
                    + " &nbsp; " + eq.getPrice() + " &#8364;"
                    + " &nbsp; slot=" + slotDisplay));
            }
        }

        // twilightList
        if (!twilightList.isEmpty()) {
            sb.append(sectionTitle("&#127749; Twilight", twilightList.size()));
            for (var tw : twilightList) {
                sb.append(row("[" + tw.getSeason() + "]"
                    + " &nbsp; start=" + tw.getStartTime()
                    + " &nbsp; months=" + tw.getMonths()));
            }
        }

        sb.append("</div>");
        return sb.toString();
    } catch (Exception e) {
        String msg = "Exception in showTarifGreenfee = " + e.getMessage();
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
    }
} // end method

private String sectionTitle(String label, int count) {
    return "<div style='font-weight:bold; color:var(--primary-color,#4CAF50);"
         + " margin-top:0.5rem; border-bottom:1px solid var(--surface-300,#ddd);'>"
         + label + " <span style='font-weight:normal;'>(" + count + ")</span></div>";
} // end method

private String row(String content) {
    return "<div style='padding-left:1rem;'>&#8226; " + content + "</div>";
} // end method

private String emptyLine() {
    return "<div style='padding-left:1rem; color:#999;'>— empty —</div>";
} // end method

 @Override
public String toString(){
 try {
 //     LOG.debug("starting toString TarifGreenfee !");
    return
            ( NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + NEW_LINE + "<br>"
            + " ,startDate : "   + this.getStartDate()
       //     + NEW_LINE + "<br>"
            + " ,end Date : "   + this.getEndDate()
       //     + NEW_LINE + "<br>"
            + " ,start Time : "   + this.getStartHour()
       //     + NEW_LINE + "<br>"
            + " ,end Time : "   + this.getEndHour()
            + NEW_LINE + "<br>"
            + " ,season : "   + this.getSeason()
            + " ,workSeason : "   + this.getWorkSeason()
            + " ,GreenfeeType : " + this.getGreenfeeType()
            + NEW_LINE + "<br>"
            + " ,CourseId : "   + this.getTarifCourseId()
            + NEW_LINE + "<br>"
//            + " ,seasons : "   + Arrays.deepToString(utils.LCUtil.removeNull2D(getDatesSeason()))
//            + NEW_LINE + "<br>"
            + ", datesSeasonsList : " + datesSeasonsList
  //          + NEW_LINE + "<br>"
   //         + " ,tee Times : "   + Arrays.deepToString(utils.LCUtil.removeNull2D(getTeeTimes()))
            + NEW_LINE + "<br>"
            + ", teeTimesList : " + teeTimesList
            + NEW_LINE + "<br>"
      //      + " ,PriceEquipments : "   + Arrays.deepToString(utils.LCUtil.removeNull2D(getPriceEquipments()) )
            + " ,EquipmentsList : "   + equipmentsList
            + NEW_LINE + "<br>"
  //          + " ,EquipmentsChoice : "   + Arrays.deepToString(getEquipmentsChoice())
  //          + NEW_LINE + "<br>"
            + " ,getPriceGreenfee : "   + this.getPriceGreenfee()
            + NEW_LINE + "<br>"
            + " ,BasicList: " + basicList
  //          + NEW_LINE + "<br>"
  //          + " ,GreenfeesChoice: "   + Arrays.deepToString(getGreenfeesChoice())
    //        + NEW_LINE + "<br>"
    //        + " ,greenfees : "   + Arrays.deepToString(getPriceGreenfees() )
            + NEW_LINE + "<br>"
            + " ,daysList : "   + daysList
            + NEW_LINE + "<br>"
            + " ,twilightList : "   + twilightList
             + NEW_LINE + "<br>"
  //          + " ,daysChoosen : "   + Arrays.deepToString(utils.LCUtil.removeNull2D(getDaysChoosen()))
            + " ,equipmentChoosen : " + equipmentChoosen
            + NEW_LINE + "<br>"
            + " ,teeTimeChoosen : " + teeTimeChoosen
            + NEW_LINE + "<br>"
            + " ,DayChoosen : " + dayChoosen
            + " ,multiTwilight : " + multiTwilight
            + " ,currency : " + currency
            );
 }catch(Exception e){
    String msg = "£££ Exception in TarifGreenfee.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
        }
} //end method

} // end class TarifGreenfee
