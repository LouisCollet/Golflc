package entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
// import jakarta.enterprise.context.SessionScoped;  // migrated 2026-02-24
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
// import jakarta.inject.Named;  // migrated 2026-02-24
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static utils.LCUtil.showMessageFatal;

// 05-04-2021 à essayer !! https://github.com/sualeh/creditcardnumber

// @Named  // migrated 2026-02-24
// @SessionScoped  // migrated 2026-02-24

public class Creditcard implements Serializable{
@JsonIgnore private final  static  List<SelectItem> CARDS = new ArrayList<>();

    private Double totalPrice;
    private Integer creditCardIdPlayer;
  @NotNull(message="{creditcard.holder.notnull}")
    private String creditcardHolder;
  @NotNull(message="{creditcard.number.notnull}")
    private String creditcardNumber;
  
  @Future(message="{creditcard.expiration.future}")
  @NotNull
  
  // ,attention plus tard enlever ce @JsonIgnore
  //@JsonIgnore // enlevé 21-08-2025
  private String communication;
   @NotNull(message="{creditcard.verification.notnull}")
  private String creditCardExpirationDate;  // nom imposé par Amazone Inc!!
  private Short creditcardVerificationCode; 
  @NotNull(message="{creditcard.type.notnull}")
  private String creditcardType; // input from end user
  private String creditcardIssuer;  // calculated for validation equality with creditCardType
  private String creditcardCurrency; // new 29-04-2025
@JsonIgnore private String creditCardExpirationDateString; // from Card solution javascript    
@JsonIgnore private LocalDateTime creditCardExpirationDateLdt; 
//@JsonIgnore private LocalDate creditCardExpirationDateLdt; 
@JsonIgnore private String creditCardMajorIndustryIdentifier;
@JsonIgnore private boolean paymentOK;
@JsonIgnore private String selected;
@JsonIgnore private String creditcardPaymentReference;
@JsonIgnore private String typePayment;
@JsonIgnore private YearMonth expirationDateYearMonth;  // new 22-08-2025

   public enum etypePayment{
       SUBSCRIPTION, GREENFEE, COTISATION, LESSON;
       
       public static String SUBSCRIPTION() {  // Enumeration is a type of a class. We can define our own methods.
        return etypePayment.SUBSCRIPTION.toString();
       }
       
   }

   // SUBSCRIPTION à GolfLC
   // GREENFEE pour inscription à une partie (green fee et accessoires)
   // COTISATION devenir membre d'un club
   // LESSON pour payer le cours d'un Pro
   
@JsonIgnore
    private final Creditcard.etypePayment stat = Creditcard.etypePayment.SUBSCRIPTION; // Default priority

//@JsonIgnore private String errorMessage; // new 28-04-2025

public Creditcard(){ // constructor 1
    creditcardCurrency = "eur"; // new 30-07-2025
}
// see http://javaevangelist.blogspot.com.es/2017/
@JsonIgnore
public List<SelectItem> getCards() {
    if(CARDS.isEmpty()){
 //       List<SelectItem> items = new ArrayList<>();
    //        LOG.debug("cards is empty");
        CARDS.add(new SelectItem("", ""));
        CARDS.add(new SelectItem("VISA", "Visa"));  // field 1=selected, field 2 = affiché écran
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
// new 22-08-2025 https://github.com/sualeh/creditcardnumber/blob/main/src/main/java/us/fatehi/creditcardnumber/ExpirationDate.java
public YearMonth expirationDate(final int year, final int month) {
  //  super(null);
    YearMonth expirationDate;
    try {
      expirationDate = YearMonth.of(year, month);
    } catch (final Exception e) {
      expirationDate = null;
    }
   // this.expirationDate = expirationDate; mod LC
    return expirationDate;
  }

    public YearMonth getExpirationDateYearMonth() {
        return expirationDateYearMonth;
    }

    public void setExpirationDateYearMonth(YearMonth expirationDateYearMonth) {
        this.expirationDateYearMonth = expirationDateYearMonth;
    }



    public String getTypePayment() {
        return typePayment;
    }

    public void setTypePayment(String typePayment) {
        this.typePayment = typePayment;
    }

    public String getCreditcardHolder() {
        return creditcardHolder;
    }

    public void setCreditcardHolder(String creditcardHolder) {
        this.creditcardHolder = creditcardHolder;
    }

    public Short getCreditcardVerificationCode() {
        return creditcardVerificationCode;
    }

    public void setCreditcardVerificationCode(Short creditcardVerificationCode) {
        this.creditcardVerificationCode = creditcardVerificationCode;
    }

    public void setCreditCardHolder(String creditCardHolder) {
        if (creditCardHolder == null){
            this.creditcardHolder = creditCardHolder;
        }else{
            this.creditcardHolder = creditCardHolder.toUpperCase();
        }
    }

    public String getCreditcardNumber() {
//        LOG.debug("getCreditCardNumber = " + creditCardNumber);
        return creditcardNumber;
    }

    public void setCreditcardNumber(String creditCardNumber) {
      //    LOG.debug("setCreditCardNumber = " + creditCardNumber);
        creditCardNumber = creditCardNumber.replaceAll(" ", "");
      //    LOG.debug("setCreditCardNumber spaces removed = " + creditCardNumber);
        this.creditcardNumber = creditCardNumber;
     //      LOG.debug("Major card identifier = " + lc.golfnew.MajorIndustryIdentifier.MIIfrom(creditCardNumber).toString());
        setCreditCardMajorIndustryIdentifier(lc.golfnew.MajorIndustryIdentifier.MIIfrom(creditCardNumber).toString()); //from(creditCardNumber);
     //     LOG.debug("issuer detected = " + enums.CardType.detect(creditCardNumber));
// mod 22-08-2025        setCreditcardIssuer(enums.CardType.detect(creditcardNumber).toString());
        setCreditcardIssuer(enumeration.CardBrand.from(creditCardNumber).toString()); // mod 22-08-2025
           LOG.debug("issuer CardBrand detected = " + getCreditcardIssuer());
    }
@JsonIgnore
    public String getCreditCardNumberSecret() {
        return utils.LCUtil.mask(getCreditcardNumber(), 4); // mod 02-12-2025 // ************3456
     //   return StringUtils.repeat("*", 12) + getCreditcardNumber().substring(getCreditcardNumber().length()-4);
    }

    public String getCreditCardExpirationDate() {
        return creditCardExpirationDate;
    }

    public void setCreditCardExpirationDate(String creditCardExpirationDate) {
        this.creditCardExpirationDate = creditCardExpirationDate;
    }


    public String getCreditcardType() {
        return creditcardType;
    }

    public void setCreditcardType(String creditcardType) {
        this.creditcardType = creditcardType;
    }

    public String getCreditcardIssuer() {
        return creditcardIssuer;
    }

    public void setCreditcardIssuer(String creditcardIssuer) {
        this.creditcardIssuer = creditcardIssuer;
    }

    public static List<SelectItem> getCARDS() {
        return CARDS;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
  //      LOG.debug("setTotalPrice = " + totalPrice);
        this.totalPrice = totalPrice;
    }

    public String getCreditCardMajorIndustryIdentifier() {
        return creditCardMajorIndustryIdentifier;
    }

    public void setCreditCardMajorIndustryIdentifier(String creditCardMajorIndustryIdentifier) {
        this.creditCardMajorIndustryIdentifier = creditCardMajorIndustryIdentifier;
  //        LOG.debug("set creditCardMajorIndustryIdentifier =  " + this.creditCardMajorIndustryIdentifier );
    }

    public boolean isPaymentOK() {
        return paymentOK;
    }

    public void setPaymentOK(boolean paymentOK) {
//       LOG.debug("setPaymentOK = " + paymentOK);
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

    public String getCreditcardPaymentReference() {
        return creditcardPaymentReference;
    }

    public void setCreditcardPaymentReference(String creditcardPaymentReference) {
        this.creditcardPaymentReference = creditcardPaymentReference;
    }

    public Integer getCreditCardIdPlayer() {
        return creditCardIdPlayer;
    }

    public void setCreditCardIdPlayer(Integer creditCardIdPlayer) {
        this.creditCardIdPlayer = creditCardIdPlayer;
    }

    public String getCreditcardCurrency() {
        return creditcardCurrency;
    }

    public void setCreditcardCurrency(String creditcardCurrency) {
        this.creditcardCurrency = creditcardCurrency;
    }

//    public String getErrorMessage() {
 //       return errorMessage;
//    }

 //   public void setErrorMessage(String errorMessage) {
 //       this.errorMessage = errorMessage;
 //   }

// calcul du prix total par totalprice.js dans creditcard_accepted.xhtml
public void setMyStrings(){
    LOG.debug("entering setMyStrings");
   Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
   Double totPrice = Double.valueOf(params.get("TotalPrice"));
 //   LOG.debug("TotalPrice in Bean = " + totPrice);
   setTotalPrice(totPrice);
 }


    public String getCreditCardExpirationDateString() {
         LOG.debug("getCreditCardExpirationDateString = " + creditCardExpirationDateString);
        return creditCardExpirationDateString;
 //       return "07/25";
    }

    public void setCreditCardExpirationDateString(String creditCardExpirationDateString) {
         LOG.debug("setCreditCardExpirationDateString = " + creditCardExpirationDateString);
        this.creditCardExpirationDateString = creditCardExpirationDateString;
    }

    public LocalDateTime getCreditCardExpirationDateLdt() {
        return creditCardExpirationDateLdt;
    }

    public void setCreditCardExpirationDateLdt(LocalDateTime creditCardExpirationDateLdt) {
        this.creditCardExpirationDateLdt = creditCardExpirationDateLdt;
    }

    /*   mod 31-07-2025
    public void setCreditCardExpirationDateString(String creditCardExpirationDateString) {
    //     LOG.debug("setCreditCardExpirationDateString = " + creditCardExpirationDateString);
    // générer une LocalDateTime
    String[] s = creditCardExpirationDateString.split("/");
    //        LOG.debug("s = " + Arrays.toString(s));
    //   LOG.debug("s[0] = " + s[0] + ":");
    //   LOG.debug("s[1] = " + s[1] + ":");
    s[0] = s[0].replaceAll(" ", ""); // MM
    //            LOG.debug("s[0] Replaced = " + ":" + s[0] + ":");
    s[1] = s[1].replaceAll(" ", "");
    //            LOG.debug("s[1] replaced = "  +":" + s[1] + ":");
    int month = Integer.parseInt(s[0]);
    int year  = Integer.parseInt(s[1]); // + 2000; // yyyy
    year = year + 2000;
    int day = 01;
    //        String str = "2016-03-04 11:30";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");
    String str = year + "-" + month + "-" + day + " 00:00";
    //            LOG.debug ("dateExpiration formatted = " + str);
    LocalDateTime ldt = LocalDateTime.parse(str, formatter);
    //            LOG.debug("LocalDateTime = " + ldt);
    // attention astuce !!
    setCreditCardExpirationDate(ldt);
    LOG.debug("CreditCardExpirationDate setted from setCreditCardExpirationDateString = " + getCreditCardExpirationDate());
    this.creditCardExpirationDateString = creditCardExpirationDateString;
    }
     */
 
/*
    public static Creditcard map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try{
            Creditcard c = new Creditcard();
            c.setCreditCardIdPlayer(rs.getInt("CreditcardIdPlayer"));
            c.setCreditcardHolder(rs.getString("CreditcardHolder"));
            c.setCreditcardNumber(rs.getString("CreditcardNumber"));
            c.setCreditCardExpirationDateLdt(rs.getTimestamp("CreditcardExpirationDate").toLocalDateTime());
            c.setCreditcardVerificationCode(rs.getShort("CreditcardVerificationCode"));
            c.setCreditcardType(rs.getString("CreditcardType"));
            // new 31-07-2025 compléter date version String
            c.setCreditCardExpirationDateString(
                    c.getCreditCardExpirationDateLdt().getMonthValue() + "/" +   // if you just wanted the two-digit number,
                            c.getCreditCardExpirationDateLdt().getYear()% 100
            );
             LOG.debug("expiration dateString = " +  c.getCreditCardExpirationDateString());
       //     LOG.debug("expirationDateYearMonth fixed = " +  YearMonth.of(2025,02));  // expirationDateYearMonth = 2025-02
             // Create a YearMonth object
        YearMonth thisYearMonth =
                YearMonth.of(
                c.getCreditCardExpirationDateLdt().getYear(),
                c.getCreditCardExpirationDateLdt().getMonthValue());
        // Create a DateTimeFormatter string
    //    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM");  // MM/yy
        // Format this year-month
        LOG.debug("expirationDateYearMonth formatted from Ldt = " + thisYearMonth.format(DateTimeFormatter.ofPattern("MM/yy")));
            c.setExpirationDateYearMonth(thisYearMonth);
            
            return c;
        }catch(Exception e){
            String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
    } //end method
*/
 @Override
public String toString(){
    final String methodName = utils.LCUtil.getCurrentMethodName();
 try { 
    //  if(this.getClass() == null){
    //     return (CLASSNAME + "is null, no print : ");
    //  } 
//     LOG.debug("entering creditcard toString()");
     LOG.debug(NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName().toUpperCase());
    return
            (NEW_LINE + "from entite :" + this.getClass().getSimpleName().toUpperCase()
            + NEW_LINE +  " ,idPlayer : "   + this.creditCardIdPlayer
             +  " ,TotalPrice : "   + this.getTotalPrice()
             +  " ,Holder: "   + this.creditcardHolder
             +  " ,Issuer: "   + this.creditcardIssuer
             +  " ,Number: "   + this.creditcardNumber
            + NEW_LINE + "<br>" + " ,Type : "   + this.getCreditcardType()
 //           + NEW_LINE + "<br>" + " ,ExpirationDateYYYYMM = " + this.getCreditCardExpirationDateYYMM()
            + " ,ExpirationDate : "   + this.getCreditCardExpirationDateLdt()
            + " ,ExpiratioDateString : "   + this.getCreditCardExpirationDate()  // format 07/27
            + " ,ExpirationDateYearMonth : "   + this.getExpirationDateYearMonth()  // format 07/27
      //      + " ,Expiration date Instant: "   + this.getCreditCardExpirationDate().toInstant()
            + NEW_LINE + "<br>" + " ,communication : "   + this.getCommunication()
            + " ,reference : "   + this.getCreditcardPaymentReference()
            + " ,typePayment : "   + this.getTypePayment()
            + " ,verificationCode : "   + this.getCreditcardVerificationCode()
       //     + " ,ExpirationDateTest : "   + this.getCreditCardExpirationDateTest()
            + " ,currency : "   + this.getCreditcardCurrency()
    //        + " ,error Message : "   + this.getErrorMessage()

            );
    }catch(Exception e){
        String msg = "£££ Exception in Creditcard.toString = " + e.getMessage();
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
  }
} //end method
} // end class Creditcard