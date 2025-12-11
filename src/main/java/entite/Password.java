package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.LCUtil;

@Named
@RequestScoped
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Password implements Serializable, interfaces.Log, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static final long serialVersionUID = 1L;
    
@JsonIgnore // ne sera pas chargé en database
private List<String>previousPasswords = new ArrayList<>();

@JsonIgnore
@NotNull(message="{player.password.notnull}") // new 28-06-2020
private String currentPassword;

private String[] arrayPasswords;

@JsonIgnore
@NotNull(message="{player.password.notnull}")
@Size(max=15,message="{player.password.size}") 
@Pattern(regexp = "\\A(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])\\S{8,}\\z",message="{player.password.regex}")
private String wrkpassword;

@JsonIgnore private String wrkpasswordVisual;

@JsonIgnore
@NotNull(message="{player.confirmpassword.notnull}")
@Size(max=15,message="{player.confirmpassword.size}") 
@Pattern(regexp = "\\A(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])\\S{8,}\\z",message="{player.confirmpassword.regex}")
private String wrkconfirmpassword;

@JsonIgnore private String playerPassword; // new 07-08-2018

//@JsonIgnore private String passwordShowHide; // new 07-08-2018
@JsonIgnore private boolean hidden; 
//private boolean showCurrentPassword; 


 public Password(){   // ici constructor
   //   showCurrentPassword = false;
      hidden = true;
    }
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public List<String> getPreviousPasswords() {
        // here a modification !
        return previousPasswords;
    }

    public void setPreviousPasswords(List<String> previousPasswords) {
        this.previousPasswords = previousPasswords;
    }

    public String[] getArrayPasswords() {
        return arrayPasswords;
    }

    public void setArrayPasswords(String[] arrayPasswords) {
        this.arrayPasswords = arrayPasswords;
    }



    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getWrkpassword() {
        return wrkpassword;
    }

    public void setWrkpassword(String wrkpassword) {
        this.wrkpassword = wrkpassword;
    }

    public String getWrkconfirmpassword() {
        return wrkconfirmpassword;
    }

    public void setWrkconfirmpassword(String wrkconfirmpassword) {
        this.wrkconfirmpassword = wrkconfirmpassword;
    }

    public String getPlayerPassword() {
        return playerPassword;
    }

    public void setPlayerPassword(String playerPassword) {
        this.playerPassword = playerPassword;
    }

    public String getWrkpasswordVisual() {
        return wrkpasswordVisual;
    }

    public void setWrkpasswordVisual(String wrkpasswordVisual) {
        this.wrkpasswordVisual = wrkpasswordVisual;
    }
/* mod 27-02-2024
public String hideOrShow(){
      LOG.debug("hidden was  = " + hidden);
   if(hidden){
       setWrkpasswordVisual("");
   }else{
       setWrkpasswordVisual(getCurrentPassword());
   }
  LOG.debug("visual = " + getWrkpasswordVisual());
   PrimeFaces.current().ajax().update("form_password_modify:panelGroup2");
   // ou essayer PrimeFaces.current().executeScript(“dialog.hide()”);
   hidden = !hidden;
      LOG.debug("hidden is now = " + hidden);
 return null;
}
*/
 @Override
public String toString(){
  try{
    //    LOG.debug("starting toString Password!");     (NEW_LINE  + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
    return 
        NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName().toUpperCase() 
    + NEW_LINE + " list previous passwords : "   + this.previousPasswords.toString()
               + " ,array previous passwords : "   + Arrays.toString(this.arrayPasswords)
               + " ,current Password : "   + this.getCurrentPassword()
               + " ,wrkpassword = " + this.getWrkpassword()
               + " ,wrkconfirmpassword = " + this.getWrkconfirmpassword()
               + " ,playerPassword (SHA 256) : " + this.getPlayerPassword()
        ;
  }catch(Exception e){
        String msg = "£££ Exception in Password.toString = " + e.getMessage();
        LOG.error(msg);
  //      LCUtil.showMessageFatal(msg);
        return msg;
  }
}

public static Password map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
  //  LOG.debug("starting mapPassword for player = "); // + player);
        Password password = new Password();
        ObjectMapper om = new ObjectMapper();
    //   on récupère l'array avec les anciens mots de passe
        String s = rs.getString("PlayerPreviousPasswords");
        if(s != null){
            password = om.readValue(s,Password.class);
            List<String> list = new ArrayList<>(Arrays.asList(password.getArrayPasswords()));
            password.setPreviousPasswords(list);
  //       LOG.debug("at the end of mapper Password = " + pa);
        }else{
  //          LOG.debug("s is null");
        }
          // on récupère le mot de passe actuel (se trouve dans Player)
            password.setPlayerPassword(rs.getString("PlayerPassword"));
   //           LOG.debug("at the very end Password = " + pa);
   return password;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class