package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import utils.LCUtil;
import jakarta.servlet.http.HttpSessionBindingListener;
import jakarta.servlet.http.HttpSessionListener;


@WebListener // new 26-08-2025 https://www.logicbig.com/tutorials/java-ee-tutorial/java-servlet/http-session-binding-listener.html

@Named("cotisation")
@RequestScoped


//public class Cotisation implements Serializable, interfaces.GolfInterface{
public class Cotisation implements Serializable, HttpSessionBindingListener, HttpSessionListener {    
    private static final long serialVersionUID = 1L;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private Integer idplayer;
    
  @NotNull(message="{cotisation.startdate.notnull}")
//          ,groups = validator.ContactGroup.class)  // new 10-04-2021
    private LocalDateTime cotisationStartDate;
  
  // new 06-04-2022 for filter with date "between"
//  private LocalDate cotisationStartDate;
  
  @NotNull(message="{cotisation.enddate.notnull}")
//          ,groups = validator.ContactGroup.class)  // new 10-04-2021
      private LocalDateTime cotisationEndDate;
  
// new 07-04-2022 for filter with date "between"
 // private LocalDate cotisationEndDate;
  
    private Integer idclub;
    
    private LocalDateTime paymentDate;
 //   @NotNull(message="{subscription.notnull}")
 //   private String subCode;
 //   @Max(value=5,message="{subscription.trial.max}")


    private String paymentReference;
    double price;
    private String communication;
    private String items;
    private String status;
    boolean cotisationError;
    private int days;
    private String type; // "round"=lors inscription round, "spontaneous" pour payment spontané
    
public Cotisation(){    // constructor
   cotisationError = false;
 //  LOG.debug("constructor Cotisation executed !");
}

// new 26-08-2025
@Override
  public void valueBound(HttpSessionBindingEvent event) {
      LOG.debug("-- HttpSessionBindingListener#valueBound() --");
      System.out.printf("added attribute name: %s, value:%s %n",
              event.getName(), event.getValue());
  }

  @Override
  public void valueUnbound(HttpSessionBindingEvent event) {
      LOG.debug("-- HttpSessionBindingEvent#valueUnbound() --");
      System.out.printf("removed attribute name: %s, value:%s %n",
              event.getName(), event.getValue());
  }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public boolean isCotisationError() {
        return cotisationError;
    }

    public void setCotisationError(boolean cotisationError) {
        this.cotisationError = cotisationError;
    }



@Override
    protected Object clone() throws CloneNotSupportedException {
        Cotisation other = (Cotisation) super.clone();
        other.setCotisationStartDate(this.getCotisationStartDate());
        other.setCotisationEndDate(this.getCotisationEndDate());
        return other;
    }

    public Integer getIdplayer() {
        return idplayer;
    }

    public void setIdplayer(Integer idplayer) {
        this.idplayer = idplayer;
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

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public LocalDateTime getCotisationStartDate() {
        return cotisationStartDate;
    }

    public void setCotisationStartDate(LocalDateTime cotisationStartDate) {
        this.cotisationStartDate = cotisationStartDate;
    }

    public LocalDateTime getCotisationEndDate() {
        return cotisationEndDate;
    }

    public void setCotisationEndDate(LocalDateTime cotisationEndDate) {
        this.cotisationEndDate = cotisationEndDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



 @Override
public String toString(){
    try{
     //   LOG.debug("starting toString Cotisation!");
    return 
        ( NEW_LINE + "<br/>FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE 
               + " idplayer : "   + this.getIdplayer()
               + " ,startDate : "  + this.getCotisationStartDate()
               + " ,endDate : "    + this.getCotisationEndDate()
            + NEW_LINE + TAB
               + " ,reference payment : "  + this.getPaymentReference()
               + " ,price : "  + this.getPrice()
               + " ,communication : "  + this.getCommunication()
             + NEW_LINE + TAB
               + " ,date paiement : "  + this.getPaymentDate()
               + " ,items : "  + this.getItems()
               + " ,club : "  + this.getIdclub()
               + " ,status : "  + this.getStatus()
               + " ,error : "  + cotisationError
               + " ,type : round or spontaneous : "  + type
            );
        }catch(Exception e){
        String msg = "£££ Exception in Cotisation.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}

public static Cotisation map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        Cotisation c = new Cotisation(); // différence avec @Inject Cotisation.cotisation ??
        c.setIdclub(rs.getInt("CotisationIdClub"));
        c.setIdplayer(rs.getInt("CotisationIdPlayer"));
        c.setCotisationStartDate(rs.getTimestamp("CotisationStartDate").toLocalDateTime());
        c.setCotisationEndDate(rs.getTimestamp("CotisationEndDate").toLocalDateTime());
        c.setPaymentReference(rs.getString("CotisationPaymentReference"));
        c.setCommunication(rs.getString("CotisationCommunication"));
        c.setPrice(rs.getDouble("CotisationAmount"));
        c.setItems(rs.getString("CotisationItems"));
        c.setStatus(rs.getString("CotisationStatus"));
        c.setPaymentDate(rs.getTimestamp("CotisationModificationDate").toLocalDateTime());
   return c;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
/* enlevé 23-01-2023
public static Cotisation mapGreenfee(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        Cotisation c = new Cotisation(); // différence avec @Inject Cotisation.cotisation ??
        c.setIdclub(rs.getInt("GreenfeeIdClub"));
        c.setIdplayer(rs.getInt("GreenfeeIdPlayer"));
        c.setCotisationStartDate(rs.getTimestamp("GreenfeeRoundDate").toLocalDateTime());
     //  c.setCotisationEndDate(rs.getTimestamp("GreenfeeRoundDate").toLocalDateTime()); // endDate not used
        c.setPaymentReference(rs.getString("GreenfeePaymentReference"));
        c.setCommunication(rs.getString("GreenfeeCommunication"));
        c.setPrice(rs.getDouble("GreenfeeAmount"));
        c.setItems(rs.getString("GreenfeeItems"));
        c.setStatus(rs.getString("GreenfeeStatus"));
        c.setPaymentDate(rs.getTimestamp("GreenfeeModificationDate").toLocalDateTime());
   return c;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
*/

} // end class