package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import static interfaces.GolfInterface.NEWLINE;
import static interfaces.GolfInterface.SDF;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import javax.inject.Named;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import static utils.LCUtil.showMessageFatal;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
// @JsonInclude(Include.NON_NULL)  // ne fonctionne pas dans table multidimentional intéressant ?
@Named
@JsonPropertyOrder({"datesSeason","days","teeTimes","priceEquipments"}) // new 22/01/2019 not working ?
public class TarifGreenfee implements Serializable{
@JsonIgnore // ne sera pas chargé en database
   @NotNull(message="{tarifMember.startdate.notnull}")
    private Date startDate;

@JsonIgnore
    @NotNull(message="{tarifMember.enddate.notnull}")
    private Date endDate;
    

@JsonIgnore
    @NotNull(message="{tarif.number.notnull}")
    @Min(value=0,message="{tarif.number.min}")
    @Max(value=20,message="{tarif.number.max}")
    private Integer tarifIndexSeasons; // pour periods
@JsonIgnore
    private Integer tarifIndexHours; // pour periods
@JsonIgnore
    private Integer tarifIndexDays; // new 24-02-2019
@JsonIgnore
    private Integer tarifIndexEquipments;
@JsonIgnore
    private Integer tarifIndexGreenfee; // new 17/02/2019
@JsonIgnore
    private Integer tarifIndexPayment; // pour period
@JsonIgnore
    private Double priceGreenfee; // pour periods
@JsonIgnore
    private Double unitPrice; 
@JsonIgnore
    private Double totalPrice;
      //    @JsonIgnore
  //  private Double totalPriceHidden;  
//   @JsonIgnore
//    private Integer quantity; 
@JsonIgnore
    private String [] priceItem;
@JsonIgnore
    private String [] quantity;
// @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-MM") // deleted 22-01-2019
   //   pattern = "dd-MM-yyyy hh:mm:ss")
@JsonIgnore
@NotNull(message="{tarifMember.workitem.notnull}")
  private String workItem;
@JsonIgnore
 @NotNull(message="{tarifMember.workprice.notnull}")
  private String workPrice;
@JsonIgnore
 @NotNull(message="{tarifMember.workseason.notnull}")
  private String workSeason;
  private String[][]  datesSeason; // low, medium High puis des paires de dates début et fin;
  private String [][] teeTimes; // = new String[10][5];
  private String [][] priceEquipments; // mod 24-01-2018
  private String [][] priceGreenfees; // new 17/02/2019
  private String [][] days;//first array lundi ... second array normal,invited,junior, HML
  @JsonIgnore
  private String [][] daysWrk;//first array lundi ... second array normal,invited,junior, HML
  private String inputtype; // new 23-02-2019 GR = greenfee, DA = days, TI = Times
@JsonIgnore
    private Integer [] equipmentsChoice;
@JsonIgnore
    private Integer [] greenfeesChoice;
    private String season;
public TarifGreenfee(){ // constructor 1
        datesSeason = new String[20][3]; // 20 dates (5 paired début et fin), 3 périodes Low, Medium, High
//            LOG.info("from construtor : dateseason = " + Arrays.deepToString(datesSeason));
        teeTimes        = new String[20][5];
        priceEquipments = new String[15][3]; // item, price, choix pourquoi 3 ??
     //   days            = new String[15][4]; // 15 = 3 hml * 5 possibilités dns la semaine
        days            = new String[15][4]; // 15 = 3 hml * 5 possibilités dns la semaine
        daysWrk = new String[5][4];
        priceGreenfees = new String[10][3]; // item, H/M/L, prix
        equipmentsChoice = new Integer[priceEquipments.length];  // choice of quantity egals 0, 1
        greenfeesChoice = new Integer[priceGreenfees.length];
        Arrays.fill(equipmentsChoice, 0);
        Arrays.fill(greenfeesChoice, 0); // new 18/02/2019
        
   //     daysWrk = new String[5][4];
        priceItem = new String[10];
        priceGreenfee = 0.0;
        tarifIndexSeasons = 0;
        tarifIndexHours = 0;
        tarifIndexEquipments = 0;
        tarifIndexPayment = 0;
        tarifIndexGreenfee = 0; // new 18/02/2019
        tarifIndexDays = 0; // new 24/02/2019
        quantity = new String[10];
    } // end constructor

public TarifGreenfee(String[][] datesSeason, String [][]teeTimes){ // constructor test
this.datesSeason = datesSeason;
this.teeTimes = teeTimes;
}

//public TimeZone getTimeZone() {  
//  TimeZone timeZone = TimeZone.getDefault();  
//  return timeZone;  
//  } 

    public void setTarifIndexSeasons(Integer tarifIndexSeasons) {
        this.tarifIndexSeasons = tarifIndexSeasons;
    }


    public Integer getTarifIndexSeasons() {
        return tarifIndexSeasons;
    }

 //   public void setTarifIndex(Integer tarifIndexSeasons) {
 //       this.tarifIndexSeasons = tarifIndexSeasons;
 //   }

    public Integer getTarifIndexHours() {
        return tarifIndexHours;
    }

    public void setTarifIndexHours(Integer tarifIndexHours) {
        this.tarifIndexHours = tarifIndexHours;
    }

    public Integer getTarifIndexEquipments() {
        return tarifIndexEquipments;
    }

    public void setTarifIndexEquipments(Integer tarifIndexEquipments) {
        this.tarifIndexEquipments = tarifIndexEquipments;
    }

    public String[][] getDatesSeason() {
        return datesSeason;
    }

    public void setDatesSeason(String[][] datesSeason) {
        this.datesSeason = datesSeason;
    }

    public String[][] getTeeTimes() {
        return teeTimes;
    }

    public void setTeeTimes(String[][] teeTimes) {
        this.teeTimes = teeTimes;
    }

    public String[][] getPriceEquipments() {
        return priceEquipments;
    }

    public void setPriceEquipments(String[][] priceEquipments) {
        this.priceEquipments = priceEquipments;
    }

    public String[][] getDays() {
        return days;
    }

    public void setDays(String[][] days) {
        this.days = days;
    }

    public Double getPriceGreenfee() {
        LOG.info("getPriceGreenfee = " + priceGreenfee);
        return priceGreenfee;
    }

    public void setPriceGreenfee(Double priceGreenfee) {
        this.priceGreenfee = priceGreenfee;
        LOG.info("setPriceGreenfee = " + priceGreenfee);
        this.unitPrice = priceGreenfee;
    }

    public void RemoveNull(){
    datesSeason = utils.LCUtil.removeNull2D(datesSeason);
    teeTimes = utils.LCUtil.removeNull2D(teeTimes);
    priceEquipments = utils.LCUtil.removeNull2D(priceEquipments);
    priceGreenfees = utils.LCUtil.removeNull2D(priceGreenfees);  // new 18/02/2019
    days = utils.LCUtil.removeNull2D(days);
        LOG.info("null removed from all Tarif Arrays");
}

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    //    this.updateTotalPrice();
    }

    public Double getTotalPrice() {
        LOG.info("getTotalPrice = " + totalPrice);
        return totalPrice;
    }
 //   public String getTotalPriceString() {
  //      LOG.info("getTotalPriceString = " + totalPrice);
 ////       return String.valueOf(this.totalPrice);
  //  }
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
        LOG.info("setTotalPrice = " + totalPrice);
    }
    
    public Integer getTarifIndexPayment() {
        return tarifIndexPayment;
    }

    public void setTarifIndexPayment(Integer tarifIndexPayment) {
        this.tarifIndexPayment = tarifIndexPayment;
    }

    public Integer getTarifIndexDays() {
        return tarifIndexDays;
    }

    public void setTarifIndexDays(Integer tarifIndexDays) {
        this.tarifIndexDays = tarifIndexDays;
    }

    public String[] getQuantity() {
        return quantity;
    }

    public void setQuantity(String[] quantity) {
        this.quantity = quantity;
    }


    public String[] getPriceItem() {
        return priceItem;
    }

    public void setPriceItem(String[] priceItem) {
        this.priceItem = priceItem;
    }

    public String getWorkItem() {
        return workItem;
    }

    public void setWorkItem(String workItem) {
        this.workItem = workItem;
    }

    public String getWorkPrice() {
        return workPrice;
    }

    public void setWorkPrice(String workPrice) {
        this.workPrice = workPrice;
    }

    public String getWorkSeason() {
        return workSeason;
    }

    public void setWorkSeason(String workSeason) {
        this.workSeason = workSeason;
    }

    public Integer[] getEquipmentsChoice() {
        return equipmentsChoice;
    }

    public void setEquipmentsChoice(Integer[] equipmentsChoice) {
        this.equipmentsChoice = equipmentsChoice;
    }

    public Integer[] getGreenfeesChoice() {
        return greenfeesChoice;
    }

    public void setGreenfeesChoice(Integer[] greenfeesChoice) {
        this.greenfeesChoice = greenfeesChoice;
    }

    public Integer getTarifIndexGreenfee() {
        return tarifIndexGreenfee;
    }

    public void setTarifIndexGreenfee(Integer tarifIndexGreenfee) {
        this.tarifIndexGreenfee = tarifIndexGreenfee;
    }

    public String[][] getPriceGreenfees() {
        return priceGreenfees;
    }

    public void setPriceGreenfees(String[][] priceGreenfees) {
        this.priceGreenfees = priceGreenfees;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
      // ici modifier    value="#{courseC.tarifGreenfee.datesSeason[courseC.tarifGreenfee.tarifIndexSeasons][0]}"
        datesSeason[tarifIndexSeasons][0] = SDF.format(startDate);
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        // ici modifier value="#{courseC.tarifGreenfee.datesSeason[courseC.tarifGreenfee.tarifIndexSeasons][1]}"
        // date to String : quel format
        //static java.text.DateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");
//String s = SDF.format(endDate);
        datesSeason[tarifIndexSeasons][1] = SDF.format(endDate);
        this.endDate = endDate;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
        LOG.info("setted setSeason = " + season);
    }

    public String getInputtype() {
        return inputtype;
    }

    public void setInputtype(String inputtype) {
        this.inputtype = inputtype;
    }

    public String[][] getDaysWrk() {
        return daysWrk;
    }

    public void setDaysWrk(String[][] daysWrk) {
        this.daysWrk = daysWrk;
    }

   
    /*
private void updateTotalPrice() {
    LOG.info("entering updateTotalPrice");
    double q = Double.valueOf(quantity[tarifIndexPayment]);
    LOG.info("quantity = " + q);
    double p = Double.valueOf(priceEquipments[tarifIndexPayment]);
    LOG.info("unitprice = " + priceEquipments[tarifIndexPayment]);
    
    if (unitPrice != null && quantity != null){
 //       this.totalPrice = this.unitPrice * this.quantity;
        setTotalPrice(p * q);
            priceItem[tarifIndexPayment] = String.valueOf(totalPrice);
     LOG.info("priceItem array = " + Arrays.deepToString(priceItem));
  //      LOG.info("total price = " + getTotalPrice()); // * getQuantity());
    }
}
*/
 @Override
public String toString(){
 try {
      LOG.info("starting toString TarifGreenfee !");
    return
            ( NEWLINE + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + NEW_LINE + "<br>"
            + " ,seasons : "   + Arrays.deepToString(getDatesSeason() )
            + NEW_LINE + "<br>"
            + " ,tee Times : "   + Arrays.deepToString(getTeeTimes() )
            + NEW_LINE + "<br>"
            + " ,PriceEquipments : "   + Arrays.deepToString(getPriceEquipments() )
            + NEW_LINE + "<br>"
            + " , EquipmentsChoice : "   + Arrays.deepToString(getEquipmentsChoice())
            + NEW_LINE + "<br>"
            + " ,getPriceGreenfee : "   + this.getPriceGreenfee()
            + NEW_LINE + "<br>"
            + " ,PriceGreenfees: "   + Arrays.deepToString(getPriceGreenfees())
            + NEW_LINE + "<br>"
            + " ,GreenfeesChoice: "   + Arrays.deepToString(getGreenfeesChoice())
    //        + NEW_LINE + "<br>"
    //        + " ,greenfees : "   + Arrays.deepToString(getPriceGreenfees() )
            + NEW_LINE + "<br>"
            + " ,days : "   + Arrays.deepToString(getDays() )
            + NEW_LINE + "<br>"
            + " ,daysWrk : "   + Arrays.deepToString(getDaysWrk() )
            + NEW_LINE + "<br>"
            + " ,input Type : "   + this.getInputtype() // GR ou TI ou DA
            + NEW_LINE + "<br>"
            + " ,season : "   + this.getSeason()
            );
 }catch(Exception e){
    String msg = "£££ Exception in TarifGreenfee.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
        }
} //end method
} // end class