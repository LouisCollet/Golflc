package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.util.Date;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;


//@Named enlevé 27-11-2020
@Named
@RequestScoped
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CardBelgium implements Serializable{
    final private static long serialVersionUID = 1L;

private String cardNumber;
private String chipNumber; 
private Date validFrom;
private Date validTo; 
private String municipality;
private String city;
private String country;
private String nationalNumber; 
private String lastName;
private String firstName; 
//private String firstname3;
private String nationality; 
private String birthPlace;
private Date birthDate;
private String photoLocation;
private String sex; 
private String nobleCondition;
private long documentType; 
private boolean isWhiteCane;

/*		
 @Override
public String toString()
{ return 
        ("from entite.Club = "
               + " ,idclub : "   + this.getIdclub()
               + " ,club Name : " + this.getClubName()
               + " ,club City : " + this.getClubCity()
        );
}   
/*
public void allNull()
{
    LOG.debug("all Club fields nulled !");
    setIdclub(null);
    setClubName(null);
    setClubAddress(null);
    setClubCity(null);
    setClubCountry(null);
    setClubLatitude(null);
    setClubLongitude(null);

}
*/

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getChipNumber() {
        return chipNumber;
    }

    public void setChipNumber(String chipNumber) {
        this.chipNumber = chipNumber;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getNationalNumber() {
        return nationalNumber;
    }

    public void setNationalNumber(String nationalNumber) {
        this.nationalNumber = nationalNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }





    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNobleCondition() {
        return nobleCondition;
    }

    public void setNobleCondition(String nobleCondition) {
        this.nobleCondition = nobleCondition;
    }

    public long getDocumentType() {
        return documentType;
    }

    public void setDocumentType(long documentType) {
        this.documentType = documentType;
    }

    public boolean isIsWhiteCane() {
        return isWhiteCane;
    }

    public void setIsWhiteCane(boolean isWhiteCane) {
        this.isWhiteCane = isWhiteCane;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhotoLocation() {
        return photoLocation;
    }

    public void setPhotoLocation(String photoLocation) {
        this.photoLocation = photoLocation;
    }
    
@Override
public String toString(){
    return 
        ( NEW_LINE + "from entite : " + getClass().getSimpleName() + NEW_LINE 
        + " birthPlace : "   + this.birthPlace
        + " ,cardNumber : " + this.cardNumber
        + " ,chip Number : " + this.chipNumber
        + " , City : " + this.city
        + " , Country : " + this.country
        + " ,document Type : " + this.documentType
     + NEW_LINE
         + " ,lastname : " + this.lastName
        + " ,firstname : " + this.firstName
        + " ,national Number : " + this.nationalNumber
        + " ,nationality : " + this.nationality  // faut tout sauter !!
        + " ,gender : " + this.sex   // fait tout sauter !!!
        + " ,valid from : " + this.validFrom
        + " ,valid to : " + this.validTo
        
        
        );
}   
/*
public static CardBelgium map(ResultSet rs) throws SQLException{
 //   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        CardBelgium c = new CardBelgium();
/*        c.setIdclub(rs.getInt("idclub") );
        c.setClubName(rs.getString("clubName") );
        c.setClubCity(rs.getString("clubCity") );
        c.setClubCountry(rs.getString("clubCountry") );
        c.setClubAddress(rs.getString("clubAddress") );
      // new 22-10-2018    
                        TimeZone gtz = new TimeZone();
                   //     tz.setTimeZoneName(NEW_LINE);
                        gtz.setTimeZoneId(rs.getString("ClubZoneId"));
                        if(gtz.getTimeZoneId() == null){
                              gtz.setTimeZoneId("Europe/Brussels"); // le même pour tous ! par defaut
                         }
         c.setClubTimeZone(gtz);
         c.setClubLatitude(rs.getBigDecimal("ClubLatitude") );
         c.setClubLongitude(rs.getBigDecimal("ClubLongitude") );
         c.setClubWebsite(rs.getString("ClubWebsite"));
         c.setClubZoneId(rs.getString("ClubZoneId"));
         c.setClubLocalAdmin(rs.getInt("ClubLocalAdmin") );

   return c;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + "mapresult" + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
*/



} // end class