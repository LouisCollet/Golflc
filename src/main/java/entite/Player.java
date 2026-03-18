package entite;

import entite.composite.EPlayerPassword;
// import Controllers.LanguageController; // removed — fix multi-user 2026-03-07 (POJO cannot use CDI)
import com.fasterxml.jackson.annotation.JsonIgnore;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// enlevé 11-02-2026 @Named("player")
// enlevé 11-02-2026 @ViewScoped  // needed for player_wizard.xhtml ??


public class Player implements Serializable{
    private static final long serialVersionUID = 1L;
    
    
 //   @Inject private Address address; // New 04-08-2022 Inject ne sert à rien ??
    private Address address; // New 04-08-2022
 //   private LatLng latLng;

@NotNull(message="{player.id.notnull}")
@Min(value=100000,message="{player.id.min}")
@Max(value=999999,message="{player.id.max}")
    private Integer idplayer;

@NotNull(message="{player.firstname.notnull}")
@Size(max=45,message="{player.firstname.size}") 
@Pattern(regexp = "[a-zA-Z0-9éèàê ç]*",message="{player.firstname.regex}")


private String playerFirstName;

@NotNull(message="{player.lastname.notnull}")
@Size(max=45,message="{player.lastname.size}") 
@Pattern(regexp = "[a-zA-Z0-9éèàê ç]*",message="{player.lastname.regex}")
   private String playerLastName;
   private String formattedAddress;   // 11/04/2022 from findCoordinates pour vérifier si bon latlng
    
@NotNull(message="{player.dob.notnull}")
@Past(message="{player.dob.past}")
 //   private Date playerBirthDate;
    private LocalDateTime playerBirthDate;

@NotNull(message="{player.gender.notnull}")
    private String playerGender;

@NotNull(message="{player.homeclub.notnull}")
@Min(value=0,message="{player.homeclub.min}")
@Max(value=2000,message="{player.homeclub.max}")
    private Integer playerHomeClub;

    private String playerPhotoLocation;
    
@NotNull(message="{player.language.notnull}")
@Size(max=2,message="{player.language.size}") 
    private String playerLanguage;

@NotNull(message="{player.email.notnull}")
@Email(message="{player.email.format}")
    private String playerEmail;

@NotNull(message="{player.email.notnull}")
@Email(message="{player.email.format}")
    private String playerEmailConfirmation;

private short playerActivation;

@NotNull(message="{player.zoneid.notnull}")
//private String playerZoneId;

//private LatLng playerLatLng;

//@JsonIgnore private String playerStringLatLng;  // utilisé pour afficher dans player.xhtml
// utilisé pour afficher dans player.xhtml

@JsonIgnore private Boolean eID;
@JsonIgnore private boolean showMenu = false;  // pas Boolean !
//@JsonIgnore private boolean NextPanelPassword = false;  // 16/11//2013
    // new 11/07/2017
@JsonIgnore private List<Player> droppedPlayers;
    // new 28/02/2022
@JsonIgnore private List<EPlayerPassword> draggedPlayers; 


// https://stackoverflow.com/questions/8229638/how-to-use-enum-values-in-fselectitems
public enum LanguageType {
    ENGLISH("en"),
    GERMAN("de"),
    FRANÇAIS("fr"), // // Alt 128
    NEDERLANDS("nl"),
    ESPAÑOL("es"); // // Alt 165
     final private String label;
     private LanguageType(String label) {  // constructor
        this.label = label;
     }
     public String getLabel() {
        return label;
     }
  } // end enum
    public LanguageType[] LanguageType() {  // new 25-08-2023
        return LanguageType.values();
    }
/*
(?=.*[0-9]) a digit must occur at least once
(?=.*[a-z]) a lower case letter must occur at least once
(?=.*[A-Z]) an upper case letter must occur at least once
(?=.*[@#$%^&+=]) a special character must occur at least once
(?=\\S+$) no whitespace allowed in the entire string
.{8,} at least 8 characters
*/

  private Date playerModificationDate;
// coming from Subscription entite and not from data entry
@JsonIgnore private LocalDate endDate; // mod 30/01/2017

//@JsonIgnore private TimeZone playerTimeZone;

private String playerRole;

public Player(){ // constructor
    address = new Address(); // 05-09-2022
  //  latLng = new LatLng();
    playerGender="M"; //set default value to Man in radiobutton
    playerHomeClub=0;
    eID = false;
    showMenu = false; // new 14-04-2024
//    playerLatLng = null;
    droppedPlayers = new ArrayList<>();
    draggedPlayers = new ArrayList<>(); // new 24/02/2022
}

//@PostConstruct n'eet appelé qe dans un bean cdi
//    public void init(){
//        // sert à quoi?
//}
    
    public void clearDroppedPlayers(){
        droppedPlayers.clear();// Remove all elements from the List
    }
    public void clearDraggedPlayers(){
        draggedPlayers.clear();// Remove all elements from the List
    }
    
    public Integer getIdplayer(){
       // LOG.debug("getIdplayer = " + idplayer);
        return idplayer;
    }
    public void setIdplayer(Integer idplayer) {
       // LOG.debug("setIdplayer = " + idplayer);
        this.idplayer = idplayer;
    }

public String getPlayerFirstName()
    {  // LOG.debug("getPlayerFirstName = " + playerFirstName);
        return playerFirstName;
    }
    public void setPlayerFirstName(String playerFirstName) { 
        // LOG.debug("setPlayerFirstName = " + playerFirstName);
        this.playerFirstName = playerFirstName;
    }

public String getPlayerLastName(){ 
    // LOG.debug("getPlayerLastName = " + playerLastName);
        return playerLastName;
    }
    public void setPlayerLastName(String playerLastName) {
        this.playerLastName = playerLastName;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public String getPlayerEmailConfirmation() {
        return playerEmailConfirmation;
    }

    public void setPlayerEmailConfirmation(String playerEmailConfirmation) {
        this.playerEmailConfirmation = playerEmailConfirmation;
    }

    public LocalDateTime getPlayerBirthDate() {
        return playerBirthDate;
    }

    public void setPlayerBirthDate(LocalDateTime playerBirthDate) {
        this.playerBirthDate = playerBirthDate;
    }
    
public String getPlayerGender() {
        return playerGender;
    }
    public void setPlayerGender(String playerGender) {
        this.playerGender = playerGender;
    }

public Integer getPlayerHomeClub() {
        return playerHomeClub;
    }
    public void setPlayerHomeClub(Integer playerHomeClub) {
        this.playerHomeClub = playerHomeClub;
    }

public String getPlayerPhotoLocation() {
        return playerPhotoLocation;
    }
    public void setPlayerPhotoLocation(String playerPhotoLocation) {
        this.playerPhotoLocation = playerPhotoLocation;
    }

public String getPlayerLanguage() {
        return playerLanguage;
    }
    public void setPlayerLanguage(String playerLanguage) {
        this.playerLanguage = playerLanguage;
    }

public String getPlayerEmail() {
        return playerEmail;
    }
    public void setPlayerEmail(String playerEmail) {
        this.playerEmail = playerEmail;
    }

    public short getPlayerActivation() {
        return playerActivation;
    }

    public void setPlayerActivation(short playerActivation) {
        this.playerActivation = playerActivation;
    }
// remplacé par 
    // Getter - DOIT s'appeler isShowMenu() ou getShowMenu() pas les deux !!!!! 11-02-2026
public boolean isShowMenu() {
  //  LOG.debug("🔍 isShowMenu() = " + showMenu);
    return showMenu;
}
 
    // Setter
   public void setShowMenu(boolean showMenu) {
    LOG.debug("🔧 setShowMenu(" + showMenu + ")");
    this.showMenu = showMenu;
}
    
/*DstOffset:    Offset for daylight-savings time in seconds. This will be zero if the time zone is not in Daylight Savings Time during the specified timestamp.
	RawOffset:    Offset from UTC (in seconds) for the given location. This does not take into effect daylight savings.
	TimezoneID:   Contains the ID of the time zone, such as "America/Los_Angeles" or "Australia/Sydney".
	TimezoneName: Contains the long form name of the time zone. This field will be localized if the language parameter is set. eg. "Pacific Daylight Time" or "Australian Eastern Daylight Time"
	Status:       Indicates the status of the response.
*/

    public Boolean geteID() {
        return eID;
    }
    public boolean iseID()
    {  // LOG.debug("isValid = " + valid);
        return eID;
    }
    public void seteID(Boolean eID) {
        this.eID = eID;
    }

public Date getPlayerModificationDate(){
        return playerModificationDate;
    }
    public void setPlayerModificationDate(Date playerModificationDate) {
        this.playerModificationDate = playerModificationDate;
   }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
/*   public LatLng getPlayerLatLng() {
        return playerLatLng;
    }

    public void setPlayerLatLng(LatLng playerLatLng) {
        this.playerLatLng = playerLatLng;
    }
*/


   public List<Player> getDroppedPlayers() {
   //     LOG.debug("dp = " + droppedPlayers.toString());
//   if(droppedPlayers != null){
//         droppedPlayers.forEach(item -> LOG.debug("getDroppedPlayers = " + item.getIdplayer())); 
//   }
       return droppedPlayers;
    }

   public void setDroppedPlayers(List<Player> droppedPlayers) {
    //   LOG.debug("dp = " + droppedPlayers.toString());
       this.droppedPlayers = droppedPlayers;
//       this.droppedPlayers.forEach(item -> LOG.debug("setDroppedPlayers = " + item.getIdplayer())); 
  }

    public List<EPlayerPassword> getDraggedPlayers() {
        return draggedPlayers;
    }

    public void setDraggedPlayers(List<EPlayerPassword> draggedPlayers) {
        this.draggedPlayers = draggedPlayers;
    }

   
    public String getPlayerRole() {
        return playerRole;
    }

    public void setPlayerRole(String playerRole) {
        this.playerRole = playerRole;
    }

public void playerLanguageListener(ValueChangeEvent e) {
  //      LOG.debug("playerLanguage OldValue = " + e.getOldValue());
        LOG.debug("playerLanguage NewValue = " + e.getNewValue());
        setPlayerLanguage(e.getNewValue().toString() );
    // LanguageController.setLanguage removed — fix multi-user 2026-03-07
    // Language change must be handled by the controller (PlayerController), not the POJO
}
public void playerCityListener(ValueChangeEvent e) {
//        LOG.debug("playerCity OldValue = " + e.getOldValue());
//        LOG.debug("playerCity NewValue = " + e.getNewValue());
    address.setCity(e.getNewValue().toString() );
}    

public void playerZipCodeListener(ValueChangeEvent e) {
//        LOG.debug("playerCity OldValue = " + e.getOldValue());
//        LOG.debug("playerCity NewValue = " + e.getNewValue());
    address.setZipCode(e.getNewValue().toString() );
}    

public void playerCountryListener(ValueChangeEvent e) {
        LOG.debug("playerCountry OldValue = " + e.getOldValue());
       
    address.getCountry().setCode(e.getNewValue().toString() );
     LOG.debug("player address is now = " + address);
     
}

public void playerStreetListener(ValueChangeEvent e) {  // new 11/04/2022
 //       LOG.debug("playerStreet OldValue = " + e.getOldValue());
 //       LOG.debug("playerStreetCountry NewValue = " + e.getNewValue());
    address.setStreet(e.getNewValue().toString() );
}

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
/*
    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
*/
@Override
public String toString(){ 
 try{
    //  LOG.debug("starting toString Player ! for this = " + this.);
   if(this.getIdplayer() == null){
       return "nothing printed because idplayer = null";
   }
 //    LOG.debug("playerTimeZoneId : " + this.getPlayerTimeZone().getTimeZoneId());
 //    LOG.debug("playerLatLng : " + this.getPlayerLatLng());
 StringBuilder sb = new StringBuilder();
 //if(this.getIdplayer() != null){
    sb.append(NEW_LINE).append("FROM ENTITE : ").append(this.getClass().getSimpleName().toUpperCase());
    sb.append(NEW_LINE).append(TAB);
    sb.append(" ,idplayer : ").append(this.getIdplayer());
    sb.append(" ,playerFirstName : ").append(this.getPlayerFirstName());
    sb.append(" ,playerLastName : ").append(this.getPlayerLastName());
  //  sb.append(" ,playerTimeZoneId :").append(this.getPlayerZoneId());
    sb.append(" ,playerGender : ").append(this.getPlayerGender());
    sb.append(" ,playerEmail: ").append(this.getPlayerEmail()).append(NEW_LINE).append(TAB);
    sb.append(" ,playerEmailConfirmation: ").append(this.getPlayerEmailConfirmation());
    sb.append(" ,playerLanguage: ").append(this.getPlayerLanguage());
    sb.append(" ,playerBithDate: ").append(this.getPlayerBirthDate());
    sb.append(" ,playerHomeClub: ").append(this.getPlayerHomeClub());
    sb.append(" ,playerRole: ").append(this.getPlayerRole());
    sb.append(" ,playerPhoto: ").append(this.getPlayerPhotoLocation()).append(NEW_LINE).append(TAB);
   // sb.append( " ,vers LatLng : ").append(this.getPlayerLatLng()).append(TAB);
 ///   sb.append(this.getPlayerLatLng()).append(TAB);
  //  sb.append("second").append(latLng).append(TAB);
    sb.append(address).append(TAB);  // new 05-09-2022
   return sb.toString();
 } catch (Exception e) {
        String msg = "£££ Exception in toString entite Player = " + e.getMessage();
        LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
        return null;
}
} // end method toString

  public static boolean examineRs(ResultSet rs) throws SQLException{
    //    LOG.debug("entering examineRs ! ");
      ResultSetMetaData metaData = rs.getMetaData();
      Integer columnCount = metaData.getColumnCount();
      
      for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++) {
            String catalogName = metaData.getCatalogName(columnNumber);
            String className = metaData.getColumnClassName(columnNumber);
            String columnLabel = metaData.getColumnLabel(columnNumber);
       //        LOG.debug("columnLabel = " + columnLabel);
            String columnName = metaData.getColumnName(columnNumber);
       //        LOG.debug("columnName = " + columnName);
            String typeName = metaData.getColumnTypeName(columnNumber);
            int type = metaData.getColumnType(columnNumber);
       //        LOG.debug("type = " + type);
            String tableName = metaData.getTableName(columnNumber);
       //         LOG.debug("tableName = " + tableName);
            String schemaName = metaData.getSchemaName(columnNumber);
            boolean isAutoIncrement = metaData.isAutoIncrement(columnNumber);
       //         LOG.debug("isAutoIncrement = " + isAutoIncrement);
            boolean isCaseSensitive = metaData.isCaseSensitive(columnNumber);
            boolean isCurrency = metaData.isCurrency(columnNumber);
            boolean isDefiniteWritable = metaData.isDefinitelyWritable(columnNumber);
            boolean isReadOnly = metaData.isReadOnly(columnNumber);
            boolean isSearchable = metaData.isSearchable(columnNumber);
            boolean isReadable = metaData.isReadOnly(columnNumber);
            boolean isSigned = metaData.isSigned(columnNumber);
            boolean isWritable = metaData.isWritable(columnNumber);
            int nullable = metaData.isNullable(columnNumber);
    }  //end for
            
      return true;
  }
} // end class