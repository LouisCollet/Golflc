package entite;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.inject.Named;
import utils.LCUtil;

@Named
public class Cotisation implements Serializable, interfaces.Log, interfaces.GolfInterface
{
     // Constants ----------------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    private Integer idplayer;
    private LocalDateTime startDate;
    private LocalDateTime endDate; // mod 30/01/2017
    private Integer idclub;
 //   @NotNull(message="{subscription.notnull}")
 //   private String subCode;
 //   @Max(value=5,message="{subscription.trial.max}")
 //   private Integer trialCount;
 //   public enum etypeSubscription{TRIAL,MONTHLY,YEARLY};
    private String paymentReference;
    double price;
    private String communication;
    private String items;
    private String status;
    
public Cotisation()    // constructor
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
 //       LOG.info("getEnd Date subscription = " + endDate);
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
  //      LOG.info("setEnd Date subscription = " + endDate);
        this.endDate = endDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



 @Override
public String toString()
{ return 
        (NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName()
               + " ,idplayer : "   + this.getIdplayer()
               + " ,startDate : "  + this.getStartDate()
               + " ,endDate : "    + this.getEndDate()
         //      + " ,subcode : "    + this.getSubCode()
         //      + " ,trial count : "  + this.getTrialCount()
               + " ,reference payment : "  + this.getPaymentReference()
               + " ,price : "  + this.getPrice()
               + " ,communication : "  + this.getCommunication()
               + " ,items : "  + this.getItems()
               + " ,club : "  + this.getIdclub()
               + " ,status : "  + this.getStatus()
        );
}

public static Cotisation mapCotisation(ResultSet rs) throws SQLException{
    String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Cotisation c = new Cotisation();
        c.setIdclub(rs.getInt("CotisationIdClub"));
        c.setIdplayer(rs.getInt("CotisationIdPlayer"));
//           LOG.info("line 01");
        c.setStartDate(rs.getTimestamp("CotisationStartDate").toLocalDateTime());
 //             LOG.info("line 02");
   //        LOG.info("first solution = + " + rs.getTimestamp("CotisationStartDate").toLocalDateTime());
 ////       java.util.Date d = rs.getTimestamp("CotisationStartDate");   
 ////       c.setStartDate(utils.LCUtil.DatetoLocalDateTime(d));
 ////       LOG.info("second solution = + " + utils.LCUtil.DatetoLocalDateTime(d));
   //     Timestamp ts = Timestamp.valueOf(cotisation.getStartDate());
 ////       d = rs.getTimestamp("CotisationEndDate");
     //    c.setEndDate(utils.LCUtil.DatetoLocalDateTime(d));
        c.setEndDate(rs.getTimestamp("CotisationEndDate").toLocalDateTime());
        c.setPaymentReference(rs.getString("CotisationPaymentReference"));
        c.setCommunication(rs.getString("CotisationCommunication"));
        c.setItems(rs.getString("CotisationItems"));
        c.setStatus(rs.getString("CotisationStatus"));
   return c;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method


} // end class