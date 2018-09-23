package entite;

import com.google.maps.model.LatLng;
import custom_validations.ClubValidation;
import custom_validations.FirstUpper;
import googlemaps.GoogleTimeZone;
import static interfaces.GolfInterface.NEWLINE;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.validation.GroupSequence;
import javax.validation.constraints.*;
/**
 *
 * @author collet
 */
//  enlevé 04/05/2014  @javax.enterprise.context.SessionScoped   // added 05/10/2013 change quelque chose ???

@Named
@ClubValidation // new 25/10/2015
@GroupSequence({Club.class, FirstUpper.class}) // 1/11/2016 test pour le fun
public class Club implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;

@NotNull(message="Bean validation : the Club ID must be completed")
  private Integer idclub;

@Pattern(regexp="[a-zA-Z0-9éèàê' ç-]*",message="{club.name.characters}")
@NotNull(message="{club.name.notnull}")
@Size(max=45,message="{club.name.size}")
@FirstUpper(max=7) // new 10/05/2013 custom validation !!! mod 1/11/2016  param max non utilisé
    private String clubName;
//---------------------
@Pattern(regexp="[a-zA-Z0-9'éèàê' ç-]*",message="{club.address.characters}")
@NotNull(message="{club.address.notnull}")
@Size(max=45,message="{club.address.size}")
    private String clubAddress;

@Pattern(regexp="[a-zA-Z0-9'éèàê ç-]*",message="{club.city.characters}")
@NotNull(message="{club.city.notnull}")
@Size(max=45,message="{club.city.size}")
    private String clubCity;

@NotNull(message="{club.country.notnull}")
@Size(max=2,message="{club.country.size}")
    private String clubCountry;
//-- latitude : -90 = south pole, 90 = north pole, 0 = equator -->

@NotNull(message="{club.latitude.notnull}")
@DecimalMin(value="-90.0",message="{club.latitude.min}")
@DecimalMax(value="90.0",message="{club.latitude.max}")
    private BigDecimal clubLatitude;

//-- longitude : -180 = west principal meridien (London), 180 = east, 0 = London -->
@NotNull(message="{club.longitude.notnull}")
@DecimalMin(value="-180.0",message="{club.longitude.min}") // possibly negative values !!
@DecimalMax(value="180.0",message="{club.longitude.max}")
    private BigDecimal clubLongitude;

@NotNull(message="{club.latlng.notnull}")
private LatLng clubLatLng;
private String clubStringLatLng;
private String coordinates;

@Pattern(regexp = "(http[s]?://|ftp://)?(www\\.)?[a-zA-Z0-9-\\.]+\\.([a-zA-Z]{2,5})$",
        message = "Bean validation : REGEXP error - the Club Website is not valid")
@NotNull(message="{club.website.notnull}")
@Size(max=45,message="{club.website.size}")
    private String clubWebsite;

    private Date clubModificationDate;
//private String clubZoneId;

private GoogleTimeZone clubTimeZone;  // intéressant voir googlemaps.GoogleTimeZone
// contient 
 ///   private double rawOffset;
 ///   private String timeZoneId;
 ///   private String timeZoneName;
private boolean CreateModify = true; // 12/08/2017

    public Club()
    {
   
    }

    public Integer getIdclub() {
        return idclub;
    }

    public void setIdclub(Integer idclub)
    {    
     //   LOG.info("idclub setted to = " + idclub);
        this.idclub = idclub;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getClubCity() {
        return clubCity;
    }

    public void setClubCity(String clubCity) {
        this.clubCity = clubCity;
    }

    public String getClubCountry() {
        return clubCountry;
    }

    public void setClubCountry(String clubCountry) {
        this.clubCountry = clubCountry;
    }

    public String getClubAddress() {
        return clubAddress;
    }

    public void setClubAddress(String clubAddress) {
        this.clubAddress = clubAddress;
    }

    
    
    public BigDecimal getClubLatitude() { // used in createclub
        return clubLatitude;
    }
    public void setClubLatitude(BigDecimal clubLatitude) {
        this.clubLatitude = clubLatitude;
        
    }
    
    public BigDecimal getClubLongitude() { // // used in createclub
        return clubLongitude;
    }
    public void setClubLongitude(BigDecimal clubLongitude) {
        this.clubLongitude = clubLongitude;
    }
    public String getClubStringLatLng() {
          return clubStringLatLng = getClubLatLng().toString();
    }
    
//   public void setClubStringLatLng(String clubStringLatLng) {
//        this.clubStringLatLng = clubStringLatLng;
//    }
    public LatLng getClubLatLng() {
        return clubLatLng;
    }
    public void setClubLatLng(LatLng clubLatLng) {
   //     LOG.debug("line 01");
   //     double latitude  = clubLatLng.lat;
   //      LOG.debug("line 02");
        this.clubLatitude = BigDecimal.valueOf(clubLatLng.lat);
    //     LOG.debug("line 03");
 ///       double longitude = clubLatLng.lng;
    //     LOG.debug("line 04");
        this.clubLongitude = BigDecimal.valueOf(clubLatLng.lng);
    //     LOG.debug("line 05");
        this.clubLatLng = clubLatLng;
    }
    public String getCoordinates()
    {
        coordinates = getClubLatitude() + "," + getClubLongitude();
        return coordinates;
    }
    public void setCoordinates(String coordinates)
    {
        this.coordinates = coordinates;
    }

    public String getClubWebsite() {
  //      LOG.info("getting clubwebsite = " + clubWebsite);
        return clubWebsite;
    }

    public void setClubWebsite(String clubwebsite) {
 //       LOG.info("setting clubwebsite = " + clubwebsite);
        this.clubWebsite = clubwebsite;
    }

    public Date getClubModificationDate() {
        return clubModificationDate;
    }

    public void setClubModificationDate(Date clubModificationDate) {
        this.clubModificationDate = clubModificationDate;
    }

 //   public String getClubZoneId() {
 //       return clubZoneId;
 //   }

  //  public void setClubZoneId(String clubZoneId) {
  //      this.clubZoneId = clubZoneId;
  //  }

  public GoogleTimeZone getClubTimeZone() {
       return clubTimeZone;
   }

    public void setClubTimeZone(GoogleTimeZone clubTimeZone) {
        this.clubTimeZone = clubTimeZone;
    }

 //   public String getClubZoneName() {
 //       return clubZoneName;
 //   }

 //   public void setClubZoneName(String clubZoneName) {
 //       this.clubZoneName = clubZoneName;
 //   }

    // new 2/7/2014
    public void valueChangeClubAddress(ValueChangeEvent e)
        {
             LOG.info(" starting valueChangeClubAdress");
             LOG.info(" valueChangeClubAdress - new value = " + e.getNewValue().toString() );
         //   LOG.info(" valueChangeClubAdress - old value = " + e.getOldValue().toString() );

             LOG.info(" valueChangeClubAdress - comp. id  = " + e.getComponent().getId() );
             LOG.info(" valueChangeClubAdress - component = " + e.getComponent().toString() );
        }

    public boolean isCreateModify() {
        return CreateModify;
    }

    public void setCreateModify(boolean CreateModify) {
        this.CreateModify = CreateModify;
    }
    
 @Override
public String toString()
{ return 
        ( NEWLINE + "from entite : " + getClass().getSimpleName() + NEWLINE 
        + " idclub : "   + this.getIdclub()
               + " ,club Name : " + this.getClubName()
               + " ,club Address : " + this.getClubAddress()
               + " ,club City : " + this.getClubCity()
               + " ,club Country : " + this.getClubCountry()
               + " ,club Latitude : " + this.getClubLatitude()
               + " ,club Longitude : " + this.getClubLongitude()
               + " ,club Website : " + this.getClubWebsite()
      //         + " ,club ZoneId : " + this.clubTimeZone.getTimeZoneId()  // faut tout sauter !!
       //        + " ,club ZoneId : " + this.clubTimeZone.getTimeZoneId()   // fait tout sauter !!!
        
        );
}   

} // end class
