package entite;

import com.google.maps.model.LatLng;
import googlemaps.GoogleTimeZone;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.validation.constraints.*;
import org.primefaces.event.DragDropEvent;
import utils.LCUtil;

@Named  // new 05-12-2017
@SessionScoped // new 05-12-2017
public class Player implements Serializable, interfaces.Log, interfaces.GolfInterface
{
     // Constants ----------------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;

@NotNull(message="{player.id.notnull}")
@Min(value=100000,message="{player.id.min}")
@Max(value=800000,message="{player.id.max}")
    private Integer idplayer;

@NotNull(message="{player.firstname.notnull}")
@Size(max=45,message="{player.firstname.size}") 
@Pattern(regexp = "[a-zA-Z0-9éèàê ç]*",message="Bean validation : REGEXP error for First Name (special characters not allowed)")

@Produces
//@PLAYERFIRSTNAME
@Named("playerFirstName")
private String playerFirstName;

@NotNull(message="{player.lastname.notnull}")
@Size(max=45,message="{player.lastname.size}") 
@Pattern(regexp = "[a-zA-Z0-9éèàê ç]*",message="{player.lastname.regex}")
    private String playerLastName;

@NotNull(message="{player.city.notnull}")
@Size(max=45,message="{player.city.size}") 
@Pattern(regexp = "[a-zA-Z0-9éèàê' ç,-]*",message="Bean validation : REGEXP error for Club City (special characters not allowed)")
    private String playerCity;

@NotNull(message="{player.country.notnull}")
@Size(max=45,message="{player.country.size}") // mod 26/03/2017 was 2
    private String playerCountry;

@NotNull(message="{player.dob.notnull}")
@Past(message="{player.dob.past}")
    private Date   playerBirthDate;

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
//@Pattern(regexp = "([^.@]+)(\\.[^.@]+)*@([^.@]+\\.)+([^.@]+)", message="{player.email.format}")
@Email(message="{player.email.format}")
    private String playerEmail;

private short playerActivation;

@NotNull(message="{player.zoneid.notnull}")
private String playerZoneId;

private String playerZoneName;

@NotNull(message="{player.latlng.notnull}")
private LatLng playerLatLng;  //google

private String playerStringLatLng;  // utilisé pour afficher dans player.xhtml

private Boolean eID;

@NotNull(message="{player.password.notnull}")
private String playerPassword; // new 07-08-2018

@NotNull(message="{player.password.notnull}")
@Size(max=15,message="{player.password.size}") 
//String pattern = "\\A(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}\\z";
@Pattern(regexp = "\\A(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])\\S{8,}\\z",message="{player.password.regex}")
private String wrkpassword;
/*
(?=.*[0-9]) a digit must occur at least once
(?=.*[a-z]) a lower case letter must occur at least once
(?=.*[A-Z]) an upper case letter must occur at least once
(?=.*[@#$%^&+=]) a special character must occur at least once
(?=\\S+$) no whitespace allowed in the entire string
.{8,} at least 8 characters
*/

    private Date   playerModificationDate;
// coming from Subscription entite and not from data entry
private LocalDate endDate; // mod 30/01/2017
private GoogleTimeZone playerTimeZone;

private List<Player> selectedOtherPlayers = null; // new 11/07/2017
private List<Player> droppedPlayers = null; // new 11/07/2017

public Player()    // constructor
{
    //idplayer = 324713;
    playerGender="M"; //set default value to Man in radiobutton
    playerHomeClub=0;
    eID = false;
    droppedPlayers = new ArrayList<>();
    wrkpassword = "";
}
@PostConstruct
    public void init(){
    //    user = new User("Elder Moraes", "elder@eldermoraes.com");
}
// getter and setters

    public Integer getIdplayer()
    {
       // LOG.info("getIdplayer = " + idplayer);
        return idplayer;
    }
    public void setIdplayer(Integer idplayer)
    {
       // LOG.info("setIdplayer = " + idplayer);
        this.idplayer = idplayer;
    }

public String getPlayerFirstName()
    {  // LOG.info("getPlayerFirstName = " + playerFirstName);
        return playerFirstName;
    }
    public void setPlayerFirstName(String playerFirstName)
    {   // LOG.info("setPlayerFirstName = " + playerFirstName);
        this.playerFirstName = playerFirstName;
    }

public String getPlayerLastName()
    {   // LOG.info("getPlayerLastName = " + playerLastName);
    /*
     UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
   // UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
     LOG.info("UIcomponent = " + component);
     LOG.info("UIcomponent, getClientId = " + component.getClientId());
   //  LOG.info("UIcomponent = " + component.getClientId(null));
     LOG.info("UIcomponent, getFamily = " + component.getFamily());
     LOG.info("UIcomponent, getId = " + component.getId());
  //   LOG.info("UIcomponent = " + component.);
     */
    
    
///if(FacesContext.getCurrentInstance().isPostback()){ 
///             LOG.info(" Postpack invocation :: Player Last Name is loaded from the cache"); 
///       } 
///        else { 
//            LOG.info(" Initial Invocation :: Player Last Name is loaded and returned "); 
/// // See more at: http://www.javabeat.net/primefaces-5-features/#sthash.8VVwo4bJ.dpuf
///            }
    
    
        return playerLastName;
    }
    public void setPlayerLastName(String playerLastName) {
        this.playerLastName = playerLastName;
    }

public String getPlayerCity() {
        return playerCity;
    }
    public void setPlayerCity(String playerCity) {
        this.playerCity = playerCity;
  //      LOG.info("setPlayerCity = " + playerCity);
 //       utils.LCUtil.printCurrentPhaseID();
    }

    public String getPlayerCountry() {
        return playerCountry;
    }

    public void setPlayerCountry(String playerCountry) {
        this.playerCountry = playerCountry;
   //     LOG.info("setPlayerCountry = " + playerCountry);
  //      utils.LCUtil.printCurrentPhaseID();
    }

    public String getPlayerZoneName() {
        return playerZoneName;
    }

    public void setPlayerZoneName(String playerZoneName) {
        this.playerZoneName = playerZoneName;
    }

    public GoogleTimeZone getPlayerTimeZone() {
        return playerTimeZone;
    }

public Date getPlayerBirthDate()
    {   //LOG.info("getPlayerBirthDate = " + SDF.format(playerBirthDate) );
        return playerBirthDate;
    }
    public void setPlayerBirthDate(Date playerBirthDate) {
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

    public String getPlayerZoneId() {
        return playerZoneId;
    }

    public void setPlayerZoneId(String playerZoneId) {
        this.playerZoneId = playerZoneId;
    }

    public void setPlayerTimeZone(GoogleTimeZone playerTimeZone) {
        this.playerTimeZone = playerTimeZone;
    }

    public String getPlayerStringLatLng() {
        return playerStringLatLng;
    }

    public void setPlayerStringLatLng(String playerStringLatLng) {
        this.playerStringLatLng = playerStringLatLng;
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
    {  // LOG.info("isValid = " + valid);
        return eID;
    }
    public void seteID(Boolean eID) {
        this.eID = eID;
    }

public Date getPlayerModificationDate()
{
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

    public LatLng getPlayerLatLng() {
        return playerLatLng;
    }

    public void setPlayerLatLng(LatLng playerLatLng) {
        this.playerLatLng = playerLatLng;
    }

    public List<Player> getSelectedOtherPlayers() {
        return selectedOtherPlayers;
    }

    public void setSelectedOtherPlayers(List<Player> selectedOtherPlayers) {
        this.selectedOtherPlayers = selectedOtherPlayers;
    }

    public String getPlayerPassword() {
        return playerPassword;
    }

    public void setPlayerPassword(String playerPassword) {
        this.playerPassword = playerPassword;
    }

    public String getWrkpassword() {
        return wrkpassword;
    }

    public void setWrkpassword(String wrkpassword) {
        this.wrkpassword = wrkpassword;
    }

    public List<Player> getDroppedPlayers() {
        return droppedPlayers;
    }

    public void setDroppedPlayers(List<Player> droppedPlayers) {
        this.droppedPlayers = droppedPlayers;
    }

    public void onPlayerDrop(DragDropEvent ddEvent) {  // used in inscriptions_other_players.xhtml
            LOG.info("entering onPlayerDrop");
        Player player = ((Player) ddEvent.getData());
            LOG.info("Player dropped = " + player.toString());
        droppedPlayers.add(player);
            LOG.info("droppedPlayers = " + droppedPlayers.toString());
            LOG.info("number of dropped players = " + droppedPlayers.size());
          if(droppedPlayers.size() == 3){
            String msg = "Third and last player";
            LCUtil.showMessageInfo(msg);
   };   
        /// A FAIRE limiter à 3 inscriptions !
        selectedOtherPlayers.remove(player);
            LOG.info("selectedOtherPlayers = " + selectedOtherPlayers.toString());

    }

@Override
public String toString()
{      
 try{
     LOG.info("idplayer : "   + this.getIdplayer());
     LOG.info("playerFirstName : " + this.getPlayerFirstName());
     LOG.info("playerLastName  : " + this.getPlayerLastName());
     LOG.info("playerCity : " + this.getPlayerCity());
     LOG.info("playerCountry : " + this.getPlayerCountry());
     LOG.info("playerGender : " + this.getPlayerGender());
     LOG.info("playerLanguage : " + this.getPlayerLanguage());
     LOG.info("playerBirthDate : " + this.getPlayerBirthDate());
     LOG.info("playerEmail : " + this.getPlayerEmail());
     
 //    LOG.info("playerTimeZoneId : " + this.getPlayerTimeZone().getTimeZoneId());
 //    LOG.info("playerLatLng : " + this.getPlayerLatLng());

   if(this.getIdplayer() != null){
     String str = 
        "from entite : " + this.getClass().getSimpleName() + " // "
               + " ,idplayer : "   + this.getIdplayer()
               + " ,playerFirstName : " + this.getPlayerFirstName()
               + " ,playerLastName : " + this.getPlayerLastName()
               + " ,playerCity : " + this.getPlayerCity()
               + " ,playerCountry : " + this.getPlayerCountry()
               + " ,playerTimeZoneId : " + this.getPlayerTimeZone().getTimeZoneId()
               + " ,playerLatLng : " + this.getPlayerLatLng()
               + " ,playerGender : " + this.getPlayerGender()
               + " ,playerEmail : " + this.getPlayerEmail()
               + " ,playerLanguage : " + this.getPlayerLanguage()
               + " ,playerBirthDate : " + this.getPlayerBirthDate()
               + " ,Birth Date = " + SDF.format(this.getPlayerBirthDate())
               + " ,playerPassword (encrypted) : " + this.getPlayerPassword()
       ;
    return str;
 }
  } catch (Exception e) {
            String msg = "£££ Exception in toString entite Player = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}
 return null;
} // end method toString

} // end class