package entite;

import static interfaces.GolfInterface.NEWLINE;
import java.io.Serializable;
import java.util.Date;
import javax.inject.Named;
/**
 *
 * @author collet
 */
@Named
public class CardBelgium implements Serializable, interfaces.Log
{
    final private static long serialVersionUID = 1L;

private String cardNumber;
private String chipNumber; 
private Date validFrom;
private Date validTo; 
private String municipality;
private String city;
private String country;
private String nationalNumber; 
private String name;
private String firstname1; 
private String firstname3;
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
    LOG.info("all Club fields nulled !");
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstname1() {
        return firstname1;
    }

    public void setFirstname1(String firstname1) {
        this.firstname1 = firstname1;
    }

    public String getFirstname3() {
        return firstname3;
    }

    public void setFirstname3(String firstname3) {
        this.firstname3 = firstname3;
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
public String toString()
{ return 
        ( NEWLINE + "from entite : " + getClass().getSimpleName() + NEWLINE 
        + " birthPlace : "   + this.birthPlace
        + " ,cardNumber : " + this.cardNumber
        + " ,chip Number : " + this.chipNumber
        + " , City : " + this.city
        + " , Country : " + this.country
        + " ,document Typee : " + this.documentType
        + " ,firstname 1 : " + this.firstname1
        + " ,national Number : " + this.nationalNumber
        + " ,nationality : " + this.nationality  // faut tout sauter !!
        + " ,gender : " + this.sex   // fait tout sauter !!!
        
        );
}   
} // end class