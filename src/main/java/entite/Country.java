package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

//@Named("Country") // enlevé 19-02-2024 redondant mod 29-12-2022 ajouté "Country" si pas majuscule, pas de flags pas compris pourquoi !!!!
//@ViewScoped

//@RequestScoped // was session 
public class Country implements Serializable, Comparable<Country> {
    private int id;
    private String name;
    
   @NotNull(message="{address.country.code.notnull}") 
    private String code;
    private Locale locale;
    private boolean rtl;
    // new 27-04-2025
//    private String currency;
//    private String code_backup;
    public Country() { 
     //  LOG.debug("empty constructor");
    }

    public Country(int id, Locale locale) {
        this(id, locale.getDisplayCountry(), locale.getCountry().toLowerCase(), locale);
        //  LOG.debug("country constructor 1");
    }

    public Country(int id, Locale locale, boolean rtl) {
        this(id, locale.getDisplayCountry(), locale.getCountry().toLowerCase(), locale);
        this.rtl = rtl;
        //   LOG.debug("country constructor 2");
    }

    public Country(int id, String name, String code) {
     //   LOG.debug("country constructor 3");
        this(id, name, code, null);
    }

    public Country(int id, String name, String code, Locale locale) {
      //  LOG.debug("country constructor 4");
        this.id = id;
        this.name = name;
        this.code = code;
        this.locale = locale;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
       //  LOG.debug("entering Country.getName for getCode() = " + getCode());
         if (getCode() == null){ // mod 24-06-2025
             LOG.debug("Country.getCode() is null");
             return null;
          //   LOG.debug("code_backup = ", code_backup);
         }else{
           //  LOG.debug("code is not null");
           //  LOG.debug("Locale.of = " + Locale.of("", code).getDisplayName());
          //  LOG.debug("after");
            return Locale.of("", code).getDisplayName();
         }
    }

    public String getExtendedName() {
     //   LOG.debug("entering Country.getExtendedName");
     //   LOG.debug("country code = " + code);
    //  Locale loc = Locale.of("", code);  // was "BE"
     //   LOG.debug("display country = " + loc.getDisplayCountry());
     //   LOG.debug("display name = " + loc.getDisplayName());
     //   Locale.of("", code).getDisplayCountry();
        return Locale.of("", code).getDisplayCountry();
    }
        public String getCurrency() {
      //      LOG.debug("entering currency");
      //   LOG.debug("currency code = " + Currency.getInstance(Locale.of("", code)).getCurrencyCode()); // USD  EUR
        return Currency.getInstance(Locale.of("", code)).getCurrencyCode();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

//    public void setCode(String code) {
 //       this.code = code;
   // }
/* new 31-01-2026 conseil chatgpt
    public void setCode(String code) {
        this.code = (code == null)
                ? null
                : code.trim().toUpperCase(Locale.ROOT);
        LOG.debug("code new formule : " + this.code);
    }
  */   
    public void setCode(String code) {
    if (code == null) {
        this.code = null;
        return;
    }
   // LOG.debug("setCode en entrée : " + code);
  //  LOG.debug("setCode length : " + code.length());
    String normalized = code.trim().toUpperCase(Locale.ROOT);
      // LOG.debug("normalized : " + normalized);
      // LOG.debug("normalized length: " + normalized.length());
    if (normalized.length() > 2) {
        // on reçoit BELGIUM et on veut BE
        code = utils.CountryUtils.getCountryCode(code);
      //  LOG.debug("country - code modified : " + code);
        LOG.debug("code new formule after transformation: " + this.code);
       // throw new IllegalArgumentException("Code must contain at most 2 characters");
    }

    this.code = code.toUpperCase(Locale.ROOT);
    
}

    public Locale getLocale() {
        return Locale.of("", code);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getLanguage() {
        return locale == null ? "en" : locale.getLanguage();
    }

    public String getDisplayLanguage() {
        return locale == null ? "English" : locale.getDisplayLanguage();
    }

    public boolean isRtl() {
        return rtl;
    }

    public void setRtl(boolean rtl) {
        this.rtl = rtl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Country country = (Country) o;
        return id == country.id
                && Objects.equals(name, country.name)
                && Objects.equals(code, country.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, code);
    }

  //  @Override
  //  public String toString() {
  //      return name;
 //   }

  public String toString2() {  // attention chipotage !!
 try{
     if(code == null){
       return (NEW_LINE + TAB + TAB + this.getClass().getSimpleName().toUpperCase() + " is null, no print !! ");
    }
    return 
        (NEW_LINE + TAB + TAB + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
             + NEW_LINE + TAB + TAB
             + " Id : "   + this.getId()
             + " , Locale : " + this.getLocale()
             + " , Code : " + code
             + " , Name : " + this.getName()
             + " , RTL: " + this.rtl
             + " , Currency: " + this.getCurrency()
             + " , Extended Name: " + this.getExtendedName()
        );
  }catch(Exception e){
        String msg = " EXCEPTION in Country toString2 = " + e.getMessage() + " for = " + this.toString();
        LOG.error(msg);
     //   LCUtil.showMessageFatal(msg);
        return msg;
  }
    }

  //  @Override
  public String toString() { // utilisé dans club.xhml !!!
      return name;
  }
 
  @Override
    public int compareTo(Country o) {
        return name.compareTo(o.name);
    }
}