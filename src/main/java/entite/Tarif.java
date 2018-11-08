package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.util.Arrays;
import javax.inject.Named;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
// @JsonInclude(Include.NON_NULL)  // ne fonctionne pas dans table multidimentional intéressant ?
@Named
public class Tarif implements Serializable{
    
    @JsonIgnore
    @NotNull(message="{tarif.number.notnull}")
    @Min(value=0,message="{tarif.number.min}")
    @Max(value=20,message="{tarif.number.max}")
    private Integer tarifIndexSeasons; // pour periods
@JsonIgnore
    private Integer tarifIndexHours; // pour periods
@JsonIgnore
    private Integer tarifIndexEquipments; // pour periods
@JsonIgnore
    private Integer tarifIndexPayment; // pour periods
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
      
@JsonFormat(
      shape = JsonFormat.Shape.STRING,pattern = "dd-MM")
   //   pattern = "dd-MM-yyyy hh:mm:ss")

  
  private String[][] datesSeason; // low, medium High puis des paires de dates début et fin;
  private String [][] teeTimes; // = new String[10][5];
  private String [] priceEquipments; // = new String[10]
  private String [][] days; // = new String[5][3]

public Tarif() // constructor 1
    {
        datesSeason = new String[20][3]; // 20 dates (5 paired début et fin), 3 périodes Low, Medium, High
//            LOG.info("from construtor : dateseason = " + Arrays.deepToString(datesSeason));
        teeTimes = new String[10][5];    //
        priceEquipments = new String[10];
        days = new String[5][3];
        priceItem = new String[10];
        tarifIndexSeasons = 0;
        tarifIndexHours = 0;
        tarifIndexEquipments = 0;
        tarifIndexPayment = 0;
        quantity = new String[10];
    } // end constructor

public Tarif(String[][] datesSeason, String [][]teeTimes) // constructor test
    {
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

    public String[] getPriceEquipments() {
        return priceEquipments;
    }

    public void setPriceEquipments(String[] priceEquipments) {
        this.priceEquipments = priceEquipments;
    }

    public String[][] getDays() {
        return days;
    }

    public void setDays(String[][] days) {
        this.days = days;
    }

    public Double getPriceGreenfee() {
        return priceGreenfee;
    }

    public void setPriceGreenfee(Double priceGreenfee) {
        this.priceGreenfee = priceGreenfee;
        this.unitPrice = priceGreenfee;
    }

    public void RemoveNull(){
    datesSeason = utils.LCUtil.removeNull2D(datesSeason);
    teeTimes = utils.LCUtil.removeNull2D(teeTimes);
    priceEquipments = utils.LCUtil.removeNull1D(priceEquipments); // à vérifier
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
public String toString()
{       try {
    return
            (NEW_LINE 
            + "from entite :" + this.getClass().getSimpleName()
            + NEW_LINE + "<br>"
            + " ,seasons : "   + Arrays.deepToString(getDatesSeason() )
            + NEW_LINE + "<br>"
            + " ,tee Times : "   + Arrays.deepToString(getTeeTimes() )
            + NEW_LINE + "<br>"
            + " ,equipments : "   + Arrays.deepToString(getPriceEquipments() )
            + NEW_LINE + "<br>"
            + " ,days : "   + Arrays.deepToString(getDays() )
            + NEW_LINE + "<br>"
            + " ,price Greenfee: "   + this.getPriceGreenfee()
            );
        } catch (Exception ex) {
           LOG.error("Exception in Tarif to String" + ex);
           return null;
        }
} //end method
} // end class