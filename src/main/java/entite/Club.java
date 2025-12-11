package entite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;
import validator.FirstUpperConstraint;

// à faire : implémenter zipCode
@Named("club")
@ViewScoped // mod 29-12-2022
@GroupSequence({Club.class, FirstUpperConstraint.class}) // test pour le fun

public class Club implements Serializable{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static final long serialVersionUID = 1L;
    private Address address ;
        
@NotNull(message="Bean validation : the Club ID must be completed")
  private Integer idclub;

@Pattern(regexp="[a-zA-Z0-9éèàê'!â& ç-]*",message="{club.name.characters}")
@NotEmpty(message="{club.name.notnull}")
@Size(min=3, max=55,message="{club.name.size}")
@FirstUpperConstraint(max=7) // new 10/05/2013 custom validation !!! mod 1/11/2016  param max non utilisé
    private String clubName;

/* enlevé 06-12-2023
@NotNull(message="{club.latitude.notnull}")
@DecimalMin(value="-90.0",message="{club.latitude.min}")
@DecimalMax(value="90.0",message="{club.latitude.max}")
@Digits(integer=2, fraction=6,message = "{club.latitude.digits}")
  //  private BigDecimal clubLatitude;
    private Double clubLatitude;

//-- longitude : -180 = west principal meridien (London), 180 = east, 0 = London -->
@NotNull(message="{club.longitude.notnull}")
@DecimalMin(value="-180.0",message="{club.longitude.min}") // possibly negative values !!
@DecimalMax(value="180.0",message="{club.longitude.max}")
 //   private BigDecimal clubLongitude; // mod 02-04-2021
    private Double clubLongitude;
*/

@Pattern(regexp = "(http[s]?://|ftp://)?(www\\.)?[a-zA-Z0-9-\\//.]+\\.([a-zA-Z]{2,5})$",
        message = "{club.website.regexp}")
@NotEmpty(message="{club.website.notnull}")
@Size(min=3, max=45,message="{club.website.size}")
    private String clubWebsite;
private boolean CreateModify = true; // 12/08/2017
private boolean showCoordinatesManual = false;
private Integer clubLocalAdmin; // new 14-02-2018
private UnavailableStructure unavailableStructure;
private String clubUnavailableStructure;
private String clubZoneId; 
    public Club(){   ///  // No-args constructor
      unavailableStructure = new UnavailableStructure(); // new 03/02/2019 éviter npe
      address = new Address();
    }

    public Integer getIdclub() {
        return idclub;
    }

    public void setIdclub(Integer idclub){
     //   LOG.debug("idclub setted to = " + idclub);
        this.idclub = idclub;
    }

    public String getClubName() {
  //      LOG.debug("getClubName = " + clubName);
        return clubName;
    }

    public void setClubName(String clubName) {
   //      LOG.debug("setClubName = " + clubName);
        this.clubName = clubName;
    }

    public String getClubWebsite() {
  //      LOG.debug("getting clubwebsite = " + clubWebsite);
        return clubWebsite;
    }

    public void setClubWebsite(String clubwebsite) {
 //       LOG.debug("setting clubwebsite = " + clubwebsite);
        this.clubWebsite = clubwebsite;
    }

    // new 2/7/2014
    public void valueChangeClubAddress(ValueChangeEvent e){
             LOG.debug(" starting valueChangeClubAdress");
             LOG.debug(" valueChangeClubAdress - new value = " + e.getNewValue().toString() );
         //   LOG.debug(" valueChangeClubAdress - old value = " + e.getOldValue().toString() );

             LOG.debug(" valueChangeClubAdress - comp. id  = " + e.getComponent().getId() );
             LOG.debug(" valueChangeClubAdress - component = " + e.getComponent().toString() );
        }

    public boolean isShowCoordinatesManual() {
        return showCoordinatesManual;
    }

    public void setShowCoordinatesManual(boolean showCoordinatesManual) {
        this.showCoordinatesManual = showCoordinatesManual;
    }

    public boolean isCreateModify() {
        return CreateModify;
    }

    public void setCreateModify(boolean CreateModify) {
        this.CreateModify = CreateModify;
    }

    public Integer getClubLocalAdmin() {
        return clubLocalAdmin;
    }

    public void setClubLocalAdmin(Integer clubLocalAdmin) {
        this.clubLocalAdmin = clubLocalAdmin;
    }

    public UnavailableStructure getUnavailableStructure() {
        return unavailableStructure;
    }

    public void setUnavailableStructure(UnavailableStructure unavailableStructure) {
        this.unavailableStructure = unavailableStructure;
    }

    public String getClubUnavailableStructure() {
        return clubUnavailableStructure;
    }

    public void setClubUnavailableStructure(String clubUnavailableStructure) {
        this.clubUnavailableStructure = clubUnavailableStructure;
    }

    
    public void cityListener(ValueChangeEvent e) {
  //      LOG.debug("clubCity OldValue = " + e.getOldValue());
 //       LOG.debug("clubCity NewValue = " + e.getNewValue());
 
    address.setCity(e.getNewValue().toString() );
}
    
    public void websiteListener(ValueChangeEvent e) {
    //String selectedMarketplaceId = (String) event.getNewValue();
  //      LOG.debug("clubWebsite OldValue = " + e.getOldValue());
   //     LOG.debug("clubWebsite NewValue Website = " + e.getNewValue());
        setClubWebsite(e.getNewValue().toString() );
}
    

public void countryListener(ValueChangeEvent e) {
        LOG.debug("clubCountry OldValue = " + e.getOldValue());
      address.getCountry().setCode(e.getNewValue().toString() );
    //    LOG.debug("clubCountry NewValue = " + address.getCountry().getCode());
        LOG.debug("Club address is now = " + address);
     // club.set  
}

public void nameListener(ValueChangeEvent e) {
   //     LOG.debug("clubName OldValue = " + e.getOldValue());
   //     LOG.debug("clubName NewValue = " + e.getNewValue());
    setClubName(e.getNewValue().toString() );
}

public void addressListener(ValueChangeEvent e) {
   //     LOG.debug("clubAddress OldValue = " + e.getOldValue());
   //     LOG.debug("clubAddress NewValue = " + e.getNewValue());
 //   setClubAddress(e.getNewValue().toString() );
    address.setStreet(e.getNewValue().toString() ); // devient street
}

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

 @Override
public String toString(){
 try{ 
 //   LOG.debug("starting toString Club!");
 //    LOG.debug("idclub : "   + this.getIdclub());

 return 
        ( NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEW_LINE 
        + " idclub : "   + this.getIdclub()
               + " ,club Name : " + this.getClubName()
           + NEW_LINE + TAB
               + " ,club Website : " + this.getClubWebsite()
               + " ,club Local Admin : " + this.getClubLocalAdmin()
           + address
           + NEW_LINE + TAB
               + TAB + this.getUnavailableStructure()
              );
    }catch(Exception e){
        String msg = "£££ Exception in Club.toString = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}   
// new 23-03-2020 experimental !!
public static PreparedStatement psClubUpdate(PreparedStatement ps, Club club){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
      // voir aussi http://www.javased.com/index.php?source_dir=archaius/archaius-core/src/main/java/com/netflix/config/sources/JDBCConfigurationSource.java
      int index = 0;
            ps.setString(++index, club.getClubName());   // 1
            ps.setString(2, club.getAddress().getStreet());
            ps.setString(3, club.getAddress().getCity());
            ps.setString(4, club.getAddress().getCountry().getCode().toUpperCase());
            ps.setDouble(5, club.getAddress().getLatLng().getLat()); 
            ps.setDouble(6, club.getAddress().getLatLng().getLng());
            ps.setString(7, club.getClubWebsite());
            ps.setString(8, club.getAddress().getZoneId());
            ps.setInt(9, club.getClubLocalAdmin());
    //        ps.setString(10, json); fait localement car donnée locale
   //// ps. 12 modification date non nécessaire (faite par DB System)
return ps;
  }catch(Exception e){
   String msg = "£££ Exception in psClubUpdate = " + methodName + " / "+ e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method

// new 05/09/2022 restructuration address
public static PreparedStatement psClubCreate(PreparedStatement ps, Club club){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
      int index = 0;
            ps.setNull(++index, java.sql.Types.INTEGER);
            ps.setString(2, club.getClubName());
            ps.setString(3, club.getAddress().getStreet());
            ps.setString(4, club.getAddress().getCity());
              LOG.debug("Club country too long ? = " + club.getAddress().getCountry().getCode().toUpperCase());
            ps.setString(5, club.getAddress().getCountry().getCode().toUpperCase()); // chipotage transitoire !!
            ps.setDouble(6, club.getAddress().getLatLng().getLat()); 
            ps.setDouble(7, club.getAddress().getLatLng().getLng());
            ps.setString(8, club.getClubWebsite());
            if(club.getAddress().getZoneId() != null){
                 ps.setString(9, club.getAddress().getZoneId());
            }else{
                ps.setString(9, "Europe/Brussels");
            }
            ps.setInt(10,324713);  // mod 29-03-2019 default LocalAdmin
            ps.setString(11,null);// json unavailableStructure
            ps.setTimestamp(12, Timestamp.from(Instant.now()));
return ps;
  }catch(Exception e){
   String msg = "£££ Exception in psClubCreate = " + methodName + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method


public static Club dtoMapper(ResultSet rs){  // was map Data Transfer Object signifie 
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
      // à vérifier : si mod club, on perd ClubUnavailableStruc
        Club club = new Club();
        club.setIdclub(rs.getInt("idclub") );
        club.setClubName(rs.getString("clubName") );
        club.setAddress(Address.mapClub(rs)); // new 15-10-2024
    //      LOG.debug("verification map - address = " + club.getAddress());
        club.setClubWebsite(rs.getString("ClubWebsite"));
        club.setClubLocalAdmin(rs.getInt("ClubLocalAdmin"));
            if(rs.getString("ClubUnavailableStructure") != null){
                ObjectMapper om = new ObjectMapper();
                om.registerModule(new JavaTimeModule());
                UnavailableStructure structure = om.readValue(rs.getString("ClubUnavailableStructure"),UnavailableStructure.class);
 //                  LOG.debug("UnavailableStructure extracted from database = "  + us);
                club.setUnavailableStructure(structure);
            }else{
                club.setUnavailableStructure(null);
            }
  
 return club;
  }catch(Exception e){
    String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class