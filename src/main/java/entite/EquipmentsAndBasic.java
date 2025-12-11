package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static utils.LCUtil.showMessageFatal;

// utilisé dans TarifGreenfee : à intégrer en inner class ?
public class EquipmentsAndBasic{
  private String item;
  private String season; 
  private Double price;
  private Integer quantity;

  //Global class constructor
  public EquipmentsAndBasic(String i, String n, Double p, Integer q){
     item = i;
     season = n;
     price = p;
     quantity = q;
  }
  public EquipmentsAndBasic(){  // empty constructor
 
  }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

 
  
  public String toString(){
 try {
//      LOG.debug("starting toString TarifGreenfee !");
    return
            ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + ", item = " + item
            + ", season = " + season
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