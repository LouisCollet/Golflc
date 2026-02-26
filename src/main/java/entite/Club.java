package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import jakarta.faces.event.ValueChangeEvent;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import utils.LCUtil;
import validator.FirstUpperConstraint;

// à faire : implémenter zipCode

@GroupSequence({Club.class, FirstUpperConstraint.class}) // test pour le fun

public class Club implements Serializable{
    
    private static final long serialVersionUID = 1L;
    private Address address ;

    @NotNull(message="{club.id.notnull}")
    @Min(value=1, message="{club.id.min}")
    private Integer idclub;
    
    
//@NotNull(message="Bean validation : the Club ID must be completed")
//  private Integer idclub;

@Pattern(regexp="[a-zA-Z0-9éèàê'!â& ç-]*",message="{club.name.characters}")
@NotEmpty(message="{club.name.notnull}")
@Size(min=3, max=55,message="{club.name.size}")
@FirstUpperConstraint     // ← vérifie uniquement la majuscule
    private String clubName;

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
private String Region;
    public Club(){   ///  // No-args constructor
      unavailableStructure = new UnavailableStructure();
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
    public void zipListener(ValueChangeEvent e) {
  //      LOG.debug("clubCity OldValue = " + e.getOldValue());
 //       LOG.debug("clubCity NewValue = " + e.getNewValue());
     address.setZipCode(e.getNewValue().toString() );
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

    public String getRegion() {
        return Region;
    }

    public void setRegion(String Region) {
        this.Region = Region;
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
               + " ,club Region : " + this.getRegion()
           + address
           + NEW_LINE + TAB
               + TAB + this.getUnavailableStructure()
              );
    }catch(Exception e){
        String msg = "Exception in Club.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}   
} // end class