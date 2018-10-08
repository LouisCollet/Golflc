package entite;

import validator.CreditCardV;
import static interfaces.GolfInterface.SDF;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import lc.golfnew.CardType;
import lc.golfnew.MajorIndustryIdentifier;

@Named
public class Creditcard implements Serializable{
    private final static List<SelectItem> CARDS = new ArrayList<>();
    private Double totalPrice;
    @NotNull(message="creditCardHolder ne peut être null")
    private String creditCardHolder;
    
  @NotNull(message="{tarif.number.notnull}")
  @CreditCardV(max=16) // new 10/05/2013 custom validation !!! mod 1/11/2016  param max non utilisé
    private String creditCardNumber;
  //@Future(message="expiration date must be in the future")
    private java.util.Date creditCardExpirationDate;
 //  private LocalDate creditCardExpirationDate;
    private String creditCardType; // input from end user
    private String creditCardIssuer;  // calculated for validtion equality with creditC    ardType
   @NotNull(message="creditCardVerificationCode ne peut être null")
    private String creditCardVerificationCode; 
   private String creditCardMajorIndustryIdentifier;
   private boolean paymentOK = false; // 23/06/2013
   private String selected; // 15/04/2018
   private String communication;
public Creditcard() // constructor 1
    {
     //   paymentOK = false;
}
// see http://javaevangelist.blogspot.com.es/2017/
public List<SelectItem> getCards() {
    if(CARDS.isEmpty())
    {
 //       List<SelectItem> items = new ArrayList<>();
            LOG.info("cards is empty");
        CARDS.add(new SelectItem("", ""));
        CARDS.add(new SelectItem("VISA", "Visa"));  // field 1=selected, field 2 = affoché écran
        CARDS.add(new SelectItem("MAESTRO", "Maestro"));
        CARDS.add(new SelectItem("MASTERCARD", "Mastercard"));
        CARDS.add(new SelectItem("JCB", "Jcb"));
        CARDS.add(new SelectItem("UPI", "Upi"));
        CARDS.add(new SelectItem("DINERS_CLUB", "Diners Club"));
        CARDS.add(new SelectItem("AMERICAN_EXPRESS", "American Express"));
        CARDS.add(new SelectItem("DISCOVER", "Discover"));
        CARDS.add(new SelectItem("CHINA_UNION_PAY", "China Union Pay"));
    }    
    return CARDS;
    }

    public String getCreditCardHolder() {
        return creditCardHolder;
    }

    public void setCreditCardHolder(String creditCardHolder) {
        this.creditCardHolder = creditCardHolder;
    }

    public String getCreditCardNumber() {
        LOG.info("getCreditCardNumber = " + creditCardNumber);
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        LOG.info("setCreditCardNumber = " + creditCardNumber);
        creditCardNumber = creditCardNumber.replaceAll(" ", "");  // enlève les blancs pour la présentation
        LOG.info("setCreditCardNumber spaces removed = " + creditCardNumber);
        this.creditCardNumber = creditCardNumber;
        setCreditCardMajorIndustryIdentifier(MajorIndustryIdentifier.MIIfrom(creditCardNumber).toString()); //from(creditCardNumber);
        setCreditCardIssuer(CardType.detect(creditCardNumber).toString());
    }

    public String getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(String creditCardType) {
        LOG.info("setCreditCardType to = " + creditCardType);
        this.creditCardType = creditCardType;
    }

    public String getCreditCardIssuer() {
        return creditCardIssuer;
    }

    public void setCreditCardIssuer(String creditCardIssuer) {
        this.creditCardIssuer = creditCardIssuer;
        LOG.info("set creditCardIssuer =  " + this.creditCardIssuer );
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        LOG.info("setTotalPrice = " + totalPrice);
        this.totalPrice = totalPrice;
    }

    public String getCreditCardVerificationCode() {
        return creditCardVerificationCode;
    }

    public void setCreditCardVerificationCode(String creditCardVerificationCode) {
        this.creditCardVerificationCode = creditCardVerificationCode;
    }


    public java.util.Date getCreditCardExpirationDate() {
            LOG.info("get expiration date =  " + creditCardExpirationDate );
        return creditCardExpirationDate;
    }

    public void setCreditCardExpirationDate(java.util.Date creditCardExpirationDate) {
        LOG.info("starting set expiration date");
            LOG.info("set expiration date =  " + creditCardExpirationDate );
        this.creditCardExpirationDate = creditCardExpirationDate;
    }

    public String getCreditCardMajorIndustryIdentifier() {
        return creditCardMajorIndustryIdentifier;
    }

    public void setCreditCardMajorIndustryIdentifier(String creditCardMajorIndustryIdentifier) {
        this.creditCardMajorIndustryIdentifier = creditCardMajorIndustryIdentifier;
          LOG.info("set creditCardMajorIndustryIdentifier =  " + this.creditCardMajorIndustryIdentifier );
    }

    public boolean isPaymentOK() {
        return paymentOK;
    }

    public void setPaymentOK(boolean paymentOK) {
        LOG.info("setPaymentOK = " + paymentOK);
        this.paymentOK = paymentOK;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public String getCommunication() {
        return communication;
    }

    public void setCommunication(String communication) {
        this.communication = communication;
    }

public void setMyStrings(){
    //info coming from payment.xhtml et totalprice.js
   Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
   Double totPrice = Double.valueOf(params.get("TotalPrice"));
    LOG.info("TotalPrice in Bean = " + totPrice);
   setTotalPrice(totPrice);
   
 //  Double greenfee = Double.valueOf(params.get("Greenfee"));
  //  LOG.info("Greenfee in Bean = " + greenfee);
 //  Double buggy = Double.valueOf(params.get("Buggy"));
  //  LOG.info("Buggy in Bean = " + buggy);

 }

 @Override
public String toString()
{       try {
    return
            (NEW_LINE 
            + "from entite :" + this.getClass().getSimpleName()
            + NEW_LINE + "<br>"
            + " , TotalPrice : "   + this.getTotalPrice()
            + NEW_LINE + "<br>"
            + " ,Number: "   + this.creditCardNumber
            + NEW_LINE + "<br>"
            + " ,Type : "   + this.getCreditCardType()
            + NEW_LINE + "<br>"
            + " ,Expiration date : "   + SDF.format(this.creditCardExpirationDate)
           + NEW_LINE + "<br>"
           + " ,communication : "   + this.getCommunication()
            );
        } catch (Exception ex) {
           LOG.error("Exception in Creditcard to String" + ex);
           return null;
        }
} //end method
} // end class Creditcard