package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.time.LocalDateTime;
import static utils.LCUtil.showMessageFatal;

public class EquipmentsAndBasicAndRange{
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String item;
  private String range; 
  private Double price;
  private Integer quantity;

  //Global class constructor
  public EquipmentsAndBasicAndRange(LocalDateTime s, LocalDateTime e, String i, String n, Double p, Integer q){
     startDate = s;
     endDate = e;
     item = i;
     range = n;
     price = p;
     quantity = q;
  }
  public EquipmentsAndBasicAndRange(){  // empty constructor
 
  }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
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
  
  @Override
  public String toString(){
 try {
//      LOG.debug("starting toString TarifGreenfee !");
    return
            ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + ", Start Date = " + startDate 
            + ", End Date = " + endDate 
            + ", item = " + item
            + ", range age = " + range
            + ", price = " + price
            + ", quantity = " + quantity
            );
 }catch(Exception e){
    String msg = "£££ Exception in EquipmentsAndBasic.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
        }
} //end method
} // end class HoursSeasons