package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.inject.Named;
import utils.LCUtil;

@Named
public class Greenfee implements Serializable, interfaces.Log, interfaces.GolfInterface
{
    private static final long serialVersionUID = 1L;
    private Integer idplayer;
    private LocalDateTime roundDate;
 //   private LocalDateTime endDate; // mod 30/01/2017
    private Integer idround;
    private Integer idclub;
    private String paymentReference;
    double price;
    private String communication;
    private String items;
    private String status;

public Greenfee()    // constructor
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

    public LocalDateTime getRoundDate() {
        return roundDate;
    }

    public void setRoundDate(LocalDateTime roundDate) {
        this.roundDate = roundDate;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCommunication() {
        return communication;
    }

    public void setCommunication(String communication) {
        this.communication = communication;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public Integer getIdclub() {
        return idclub;
    }

    public void setIdclub(Integer idclub) {
        this.idclub = idclub;
    }

    public Integer getIdround() {
        return idround;
    }

    public void setIdround(Integer idround) {
        this.idround = idround;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



 @Override
public String toString(){
  try{
      LOG.info("starting toString Greenfee !");
     return 
        (NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName().toUpperCase()
               + " ,idplayer : "   + this.getIdplayer()
               + " ,startDate : "  + this.getRoundDate()
         //      + " ,endDate : "    + this.getEndDate()
         //      + " ,subcode : "    + this.getSubCode()
         //      + " ,trial count : "  + this.getTrialCount()
               + " ,reference payment : "  + this.getPaymentReference()
               + " ,price : "  + this.getPrice()
               + " ,communication : "  + this.getCommunication()
               + " ,items : "  + this.getItems()
               + " ,club : "  + this.getIdclub()
               + " ,round : "  + this.getIdround()
               + " ,status : "  + this.getStatus()
        );
        }catch(Exception e){
        String msg = "£££ Exception in Greenfee.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}

public static Greenfee mapGreenfee(ResultSet rs) throws SQLException{
    String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
    
  try{
        Greenfee g = new Greenfee();
        g.setIdclub(rs.getInt("GreenfeeIdClub"));
        g.setIdplayer(rs.getInt("GreenfeeIdPlayer"));
        g.setIdround(rs.getInt("GreenfeeIdRound"));
        g.setRoundDate(rs.getTimestamp("GreenfeeRoundDate").toLocalDateTime());
        g.setPaymentReference(rs.getString("GreenfeePaymentReference"));
        g.setCommunication(rs.getString("GreenfeeCommunication"));
        g.setItems(rs.getString("GreenfeeItems"));
        g.setPrice(rs.getDouble("GreenfeeAmount"));
        g.setStatus(rs.getString("GreenfeeStatus"));
   return g;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class