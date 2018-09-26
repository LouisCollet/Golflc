
package entite;

/**
 *
 * @author collet
 */

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.inject.Named;
import javax.validation.constraints.*;
@Named
public class PlayerCSV implements Serializable, interfaces.Log, interfaces.GolfInterface
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
    private String playerFirstName;

@NotNull(message="{player.lastname.notnull}")
@Size(max=45,message="{player.lastname.size}") 
@Pattern(regexp = "[a-zA-Z0-9éèàê ç]*",message="Bean validation : REGEXP error for Last Name (special characters not allowed)")
    private String playerLastName;

@NotNull(message="{player.city.notnull}")
@Size(max=45,message="{player.city.size}") 
@Pattern(regexp = "[a-zA-Z0-9éèàê ç-]*",message="Bean validation : REGEXP error for Club City (special characters not allowed)")
    private String playerCity;

@NotNull(message="{player.country.notnull}")
@Size(max=2,message="{player.country.size}") 
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
@Pattern(regexp = "([^.@]+)(\\.[^.@]+)*@([^.@]+\\.)+([^.@]+)", message="{player.email.format}")
    private String playerEmail;

@Past(message="{handicap.start.past}")
    private Date handicapStart;

@NotNull(message="{handicap.player.notnull}")
@Min(value=5,message="{handicap.player.min}")
@Max(value=36,message="{handicap.player.max}")

    private BigDecimal handicapPlayer;
// new 02/09/2012

public PlayerCSV()    // constructor
{
    //idplayer = 324713;
    playerGender="M"; //set default value to Man in radiobutton
    playerHomeClub=0;
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
    }

    public String getPlayerCountry() {
        return playerCountry;
    }

    public void setPlayerCountry(String playerCountry) {
        this.playerCountry = playerCountry;
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



@Override
public String toString()
{ return 
        ("from entite.Player = " + this.getClass().getSimpleName()
               + " ,idplayer : "   + this.getIdplayer()
               + " ,playerFirstName : " + this.getPlayerFirstName()
               + " ,playerLastName : " + this.getPlayerLastName()
        );
}

    public Date getHandicapStart() {
        return handicapStart;
    }

    public void setHandicapStart(Date handicapStart) {
        this.handicapStart = handicapStart;
    }

    public BigDecimal getHandicapPlayer() {
        return handicapPlayer;
    }

    public void setHandicapPlayer(BigDecimal handicapPlayer) {
        this.handicapPlayer = handicapPlayer;
    }

} // end class