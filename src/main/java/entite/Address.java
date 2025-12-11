package entite;

//import com.google.maps.model.LatLng;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.LCUtil;

//@Named("address") // mod 06-12-223
//@ViewScoped // was session
public class Address implements Serializable{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static final long serialVersionUID = 1L;
    private Country country;
    private LatLng latLng;
 public Address() {// default constructor
    country = new Country();
    latLng = new LatLng();
 }
    
@Pattern(regexp="[a-zA-Z0-9'éèàê'&â!., ç-]*",message="{address.street.characters}")
@NotEmpty(message="{address.street.notnull}")
@Size(min=3, max=45,message="{address.street.size}")
    private String street;

@Pattern(regexp="[a-zA-Z0-9'éèàê ç-]*",message="{address.city.characters}")
@NotEmpty(message="{address.city.notnull}")
@Size(min=3, max=45,message="{address.city.size}")
    private String city;

@NotNull(message="{player.email.notnull}")
@Email(message="{player.email.format}")    
    private String playerEmail;

@NotNull(message="{address.zipcode.notnull}")
    private String zipCode;

 private String zoneId;
 
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getPlayerEmail() {
        return playerEmail;
    }

    public void setPlayerEmail(String playerEmail) {
        this.playerEmail = playerEmail;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
 //
 @Override
public String toString(){
  try{
    // LOG.debug("starting toString Address !");
     return 
        (NEW_LINE + TAB +
             "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
         + NEW_LINE + TAB
               + " Street : " + this.getStreet()
               + " ,City : " + this.getCity()  // mod 05/09/2022
               + " ,Zip Code : " + this.zipCode  // new 31/12/2022  // encore à implémenter dans Club et Player
               + " ,ZoneId : " + this.getZoneId()
            //   + " ,Country : " + this.getCountry()
         //      + NEW_LINE + TAB
               + latLng.toString()
               + country.toString2() // attention !! was toString2
         );
        }catch(Exception e){
           String msg = "£££ Exception in Address.toString = " + e.getMessage();
           LOG.error(msg);
           LCUtil.showMessageFatal(msg);
           return msg;
  }
}

public static Address mapPlayer(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        Address address = new Address();
        address.setStreet(rs.getString("PlayerStreet"));
        address.setCity(rs.getString("PlayerCity"));
        address.getCountry().setCode(rs.getString("PlayerCountry"));
        LatLng latLng = LatLng.mapPlayer(rs); // ajout 06-12-2023
        address.setLatLng(latLng);
        address.setZoneId(rs.getString("PlayerZoneId"));
        if(address.getZoneId() == null){
           address.setZoneId("Europe/Brussels");
        }
   //       LOG.debug("address mapped Player " + address);
      return address;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method

public static Address mapClub(ResultSet rs){ // throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        Address address = new Address();
        address.setStreet(rs.getString("clubAddress") );
        address.setCity(rs.getString("clubCity"));
        address.getCountry().setCode(rs.getString("clubCountry") );
        LatLng latLng = LatLng.mapClub(rs);  // ajout 06-12-2023
        address.setLatLng(latLng);
        address.setZoneId(rs.getString("ClubZoneId"));
 //         LOG.debug("address mapped Club " + address);
   return address;
  }catch(Exception e){
    String msg = "£££ Exception in mapClub = " + methodName + " / "+ e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class