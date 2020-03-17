package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import utils.LCUtil;

@Named
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Password implements Serializable, interfaces.Log, interfaces.GolfInterface{
    private static final long serialVersionUID = 1L;
    
@JsonIgnore // ne sera pas chargé en database
private List<String>previouspasswords = new ArrayList<>();
@JsonIgnore
private String currentPassword;
private String [] arraypasswords;

@JsonIgnore
@NotNull(message="{player.password.notnull}")
@Size(max=15,message="{player.password.size}") 
@Pattern(regexp = "\\A(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])\\S{8,}\\z",message="{player.password.regex}")
private String wrkpassword;

@JsonIgnore
@NotNull(message="{player.confirmpassword.notnull}")
@Size(max=15,message="{player.confirmpassword.size}") 
@Pattern(regexp = "\\A(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])\\S{8,}\\z",message="{player.confirmpassword.regex}")
private String wrkconfirmpassword;

@JsonIgnore
private String playerPassword; // new 07-08-2018

public Password(){    // constructor
// empty
}

    public List<String> getPreviouspasswords() {
        return previouspasswords;
    }

    public void setPreviouspasswords(List<String> previouspasswords) {
        this.previouspasswords = previouspasswords;
    }

    public String[] getArraypasswords() {
        return arraypasswords;
    }

    public void setArraypasswords(String[] arraypasswords) {
        this.arraypasswords = arraypasswords;
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

 @Override
public String toString(){
  try{
        LOG.info("starting toString Password!");
    return 
        (NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName()
               + " ,list previous passwords : "   + this.previouspasswords
               + " ,array previous passwords : "   + Arrays.toString(this.arraypasswords)
               + " ,current Password : "   + this.getCurrentPassword()
               + " ,wrkpassword = " + this.getWrkpassword()
               + " ,wrkconfirmpassword = " + this.getWrkconfirmpassword()
               + " ,playerPassword (SHA 256) : " + this.getPlayerPassword()
        );
  }catch(Exception e){
        String msg = "£££ Exception in Password.toString = " + e.getMessage();
        LOG.error(msg);
  //      LCUtil.showMessageFatal(msg);
        return msg;
  }
}

public static Password mapPassword(ResultSet rs) throws SQLException{
    String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
  //  LOG.info("starting mapPassword for player = "); // + player);
        Password pa = new Password();
        ObjectMapper om = new ObjectMapper();
    //   on récupère l'array avec les anciens mots de passe
        String s = rs.getString("PlayerPreviousPasswords");
          if(s != null){
            pa = om.readValue(s,Password.class);
            List<String> list = new ArrayList<>(Arrays.asList(pa.getArraypasswords()));// éviter java.lang.UnsupportedOperationException !!
            pa.setPreviouspasswords(list);
  //       LOG.info("at the end of mapper Password = " + pa);
        }else{
  //          LOG.info("s is null");
        }
          // on récupére le mot de passe actuel (se trouve dans Player)
            pa.setPlayerPassword(rs.getString("PlayerPassword"));
   //           LOG.info("at the very end Password = " + pa);
   return pa;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class