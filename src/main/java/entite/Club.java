package entite;

import com.google.maps.model.LatLng;
import googlemaps.GoogleTimeZone;
import static interfaces.GolfInterface.NEWLINE;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.GroupSequence;
import javax.validation.constraints.*;
import utils.LCUtil;
import validator.ClubValidation;
import validator.FirstUpper;
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

@Pattern(regexp="[a-zA-Z0-9éèàê'!â& ç-]*",message="{club.name.characters}")
@NotEmpty(message="{club.name.notnull}")
@Size(min=3, max=45,message="{club.name.size}")
@FirstUpper(max=7) // new 10/05/2013 custom validation !!! mod 1/11/2016  param max non utilisé
    private String clubName;
//---------------------
@Pattern(regexp="[a-zA-Z0-9'éèàê'&â!., ç-]*",message="{club.address.characters}")
@NotEmpty(message="{club.address.notnull}")
@Size(min=3, max=45,message="{club.address.size}")
    private String clubAddress;

@Pattern(regexp="[a-zA-Z0-9'éèàê ç-]*",message="{club.city.characters}")
@NotEmpty(message="{club.city.notnull}")
@Size(min=3, max=45,message="{club.city.size}")
    private String clubCity;

@NotEmpty(message="{club.country.notnull}")
@Size(min=1, max=2,message="{club.country.size}")
    private String clubCountry;
//-- latitude : -90 = south pole, 90 = north pole, 0 = equator -->

@NotNull(message="{club.latitude.notnull}")
@DecimalMin(value="-90.0",message="{club.latitude.min}")
@DecimalMax(value="90.0",message="{club.latitude.max}")
@Digits(integer=2, fraction=6,message = "{club.latitude.digits}")
    private BigDecimal clubLatitude;

//-- longitude : -180 = west principal meridien (London), 180 = east, 0 = London -->
@NotNull(message="{club.longitude.notnull}")
@DecimalMin(value="-180.0",message="{club.longitude.min}") // possibly negative values !!
@DecimalMax(value="180.0",message="{club.longitude.max}")
    private BigDecimal clubLongitude;

@NotEmpty(message="{club.latlng.notnull}")
private LatLng clubLatLng;

private String clubStringLatLng;
private String coordinates;

@Pattern(regexp = "(http[s]?://|ftp://)?(www\\.)?[a-zA-Z0-9-\\.]+\\.([a-zA-Z]{2,5})$",
        message = "{club.website.regexp}")
@NotEmpty(message="{club.website.notnull}")
@Size(min=3, max=45,message="{club.website.size}")
    private String clubWebsite;

    private Date clubModificationDate;
//private String clubZoneId;
@Inject
private GoogleTimeZone clubTimeZone;  // intéressant voir googlemaps.GoogleTimeZone
// contient 
 ///   private double rawOffset;
 ///   private String timeZoneId;
 ///   private String timeZoneName;
private boolean CreateModify = true; // 12/08/2017
private String clubFormattedAddress;
    public Club()
    {
  //      clubTimeZone = new GoogleTimeZone(); // new 03/02/2019 éviter npe
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
  //      LOG.info("getClubName = " + clubName);
        return clubName;
    }

    public void setClubName(String clubName) {
   //      LOG.info("setClubName = " + clubName);
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

    public String getClubFormattedAddress() {
        return clubFormattedAddress;
    }

    public void setClubFormattedAddress(String clubFormattedAddress) {
        this.clubFormattedAddress = clubFormattedAddress;
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
        ( NEWLINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEWLINE 
        + " idclub : "   + this.getIdclub()
               + " ,club Name : " + this.getClubName()
               + " ,club Address : " + this.getClubAddress()
               + " ,club City : " + this.getClubCity()
               + " ,club Country : " + this.getClubCountry()
               + " ,club Latitude : " + this.getClubLatitude()
               + " ,club Longitude  = " + String.format("%.6f", this.getClubLongitude())  + " ,club Longitude : " + this.getClubLongitude()
               + " ,club Website : " + this.getClubWebsite()
             
       //        + "club Zone ID   = " + this.getClubTimeZone().getTimeZoneId()
      //         + " ,club ZoneId : " + this.clubTimeZone.getTimeZoneId()  // fait tout sauter !!
       //        + " ,club ZoneId : " + this.clubTimeZone.getTimeZoneId()   // fait tout sauter !!!
              );
}   
public static Club mapClub(ResultSet rs) throws SQLException{
    String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
        Club c = new Club();
        c.setIdclub(rs.getInt("idclub") );
           //                 LOG.debug("idcclub setted = " + c.getIdclub() );
        c.setClubName(rs.getString("clubName") );
          //                  LOG.debug("clubname setted = " + c.getClubName() );       
        c.setClubCity(rs.getString("clubCity") );
          //                  LOG.debug("clubcity setted = " + c.getClubCity() );    
        c.setClubCountry(rs.getString("clubCountry") );
          //                  LOG.debug("clubcountry setted = " + c.getClubCountry() );       
        c.setClubAddress(rs.getString("clubAddress") );
          //                  LOG.debug("clubaddress setted from c = " + c.getClubAddress() ); 
      // new 22-10-2018    
                        GoogleTimeZone gtz = new GoogleTimeZone();
                   //     tz.setTimeZoneName(NEW_LINE);
                        gtz.setTimeZoneId(rs.getString("ClubZoneId"));
                        if(gtz.getTimeZoneId() == null){
                              gtz.setTimeZoneId("Europe/Brussels"); // le même pour tous ! par defaut
                         }
         c.setClubTimeZone(gtz);
         c.setClubLatitude(rs.getBigDecimal("ClubLatitude") );
         c.setClubLongitude(rs.getBigDecimal("ClubLongitude") );
         c.setClubWebsite(rs.getString("ClubWebsite"));
   return c;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class
