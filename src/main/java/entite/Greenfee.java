package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import jakarta.enterprise.inject.Model;
import utils.LCUtil;

//@Named
@Model
public class Greenfee implements Serializable, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static final long serialVersionUID = 1L;
    private Integer idplayer;
    private LocalDateTime roundDate;
 //   private LocalDateTime endDate; // mod 30/01/2017
    private Integer idround;
    private Integer idclub;
    private String paymentReference;
    private LocalDateTime paymentDate;
    private double price;
    private String communication;
    private String items;
    private String status;
    private String currency;

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

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }



 @Override
public String toString(){
  try{
      LOG.debug("starting toString Greenfee !");
     return 
        (NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName().toUpperCase()
               + " ,idplayer : "   + this.getIdplayer()
               + " ,startDate : "  + this.getRoundDate()
         //      + " ,endDate : "    + this.getEndDate()
         //      + " ,subcode : "    + this.getSubCode()
         //      + " ,trial count : "  + this.getTrialCount()
            + NEW_LINE
               + " ,reference payment : "  + this.getPaymentReference()
               + " ,price : "  + this.getPrice()
               + " ,communication : "  + this.getCommunication()
               + " ,items : "  + this.getItems()
            + NEW_LINE
               + " ,club : "  + this.getIdclub()
               + " ,round : "  + this.getIdround()
               + " ,status : "  + this.getStatus()
               + " ,currency : "  + this.getCurrency()
        );
        }catch(Exception e){
        String msg = "£££ Exception in Greenfee.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}

public static Greenfee map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        Greenfee greenfee = new Greenfee();
        greenfee.setIdclub(rs.getInt("GreenfeeIdClub"));
        greenfee.setIdplayer(rs.getInt("GreenfeeIdPlayer"));
        greenfee.setIdround(rs.getInt("GreenfeeIdRound"));
        greenfee.setRoundDate(rs.getTimestamp("GreenfeeRoundDate").toLocalDateTime());
        greenfee.setPaymentDate(rs.getTimestamp("GreenfeeModificationDate").toLocalDateTime());
        greenfee.setPaymentReference(rs.getString("GreenfeePaymentReference"));
        greenfee.setCommunication(rs.getString("GreenfeeCommunication"));
        greenfee.setItems(rs.getString("GreenfeeItems"));
        greenfee.setPrice(rs.getDouble("GreenfeeAmount"));
        greenfee.setStatus(rs.getString("GreenfeeStatus"));
        greenfee.setCurrency(rs.getString("GreenfeeCurrency")); // new 28-04-2025
   return greenfee;
 }catch(Exception e){
    String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class