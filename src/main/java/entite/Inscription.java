package entite;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

//@Named("inscription") enlevé 14-02-2026
//@RequestScoped
public class Inscription implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private Integer round_idround; // mod 10/11/2014
    private Integer player_idplayer;
    private Short inscriptionFinalResult;

    @NotNull(message="{inscription.tee.notnull}")
    private String inscriptionTeeStart;
    
    private Integer inscriptionIdTee; // new 31-03-2019
    private String inscriptionInvitedBy;
    private boolean InscriptionOK = true;
    private String inscriptionMatchplayTeam;
    private String weather;
    private boolean showWeather = false;  // afficher scrolling dans inscription.xhtml
    boolean inscriptionError;
    private String errorStatus;
    public Inscription(){
        inscriptionTeeStart="YELLOW";
        //setInscriptionOK(false);
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(String errorStatus) {
        this.errorStatus = errorStatus;
    }

    public boolean isInscriptionError() {
        return inscriptionError;
    }

    public void setInscriptionError(boolean inscriptionError) {
        this.inscriptionError = inscriptionError;
    }

    public Integer getRound_idround() {
        return round_idround;
    }

    public void setRound_idround(Integer round_idround) {
        this.round_idround = round_idround;
    }

    public Integer getPlayer_idplayer() {
        return player_idplayer;
    }

    public void setPlayer_idplayer(Integer player_idplayer) {
        this.player_idplayer = player_idplayer;
    }

    public Short getInscriptionFinalResult() {
        return inscriptionFinalResult;
    }

    public void setInscriptionFinalResult(Short inscriptionFinalResult) {
        this.inscriptionFinalResult = inscriptionFinalResult;
    }

 //   public String getPlayerGender() {
//        return playerGender;
 //   }

 //   public void setPlayerGender(String playerGender) {
 //       this.playerGender = playerGender;
 //   }

    public String getInscriptionTeeStart() {
        return inscriptionTeeStart;
    }

    public void setInscriptionTeeStart(String inscriptionTeeStart) {
        this.inscriptionTeeStart = inscriptionTeeStart;
    }

    public boolean isInscriptionOK() {
        return InscriptionOK;
    }

    public void setInscriptionOK(boolean InscriptionOK) {
        this.InscriptionOK = InscriptionOK;
    }

    public String getInscriptionInvitedBy() {
        return inscriptionInvitedBy;
    }

    public void setInscriptionInvitedBy(String inscriptionInvitedBy) {
        this.inscriptionInvitedBy = inscriptionInvitedBy;
    }

    public Integer getInscriptionIdTee() {
        return inscriptionIdTee;
    }

    public void setInscriptionIdTee(Integer inscriptionIdTee) {
        this.inscriptionIdTee = inscriptionIdTee;
    }

    public String getInscriptionMatchplayTeam() {
        return inscriptionMatchplayTeam;
    }

    public void setInscriptionMatchplayTeam(String inscriptionMatchplayTeam) {
        this.inscriptionMatchplayTeam = inscriptionMatchplayTeam;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public boolean isShowWeather() {
        return showWeather;
    }

    public void setShowWeather(boolean showWeather) {
        this.showWeather = showWeather;
    }



    @Override
  public String toString() {
        
   
        try {
  //          LOG.debug("starting toString Inscription!");
     if(this.getClass() == null){
         return ("Inscription is null, no print : "  );
    }
  
  
  
            return
                    (NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEW_LINE
                    + NEW_LINE     + " ,<br/>idplayer : "   + this.getPlayer_idplayer()
                    + NEW_LINE     + " ,<br/>Matchplay team : "   + this.getInscriptionMatchplayTeam()
                    + NEW_LINE     + " ,<br/>Final Result : "   + this.getInscriptionFinalResult()
                    + NEW_LINE     + " ,<br/>idround : "   + this.getRound_idround()
                    + NEW_LINE     + " ,<br/>Tee Start avec slashes: "   + this.getInscriptionTeeStart() // ex YELLOW / M / 01-18 / 102
                    + NEW_LINE     + " ,<br/>idtee : "   + this.getInscriptionIdTee()
                    + NEW_LINE     + " ,<br/>invitedBy : " + this.getInscriptionInvitedBy()
                    + NEW_LINE 
                    + "<br/>Error = " + inscriptionError
                    + " , error Status = " + errorStatus
                    + NEW_LINE     + " ,<br/>weather : " + this.weather
                    // à compléter
                    );
        }catch(Exception e){
            String msg = "£££ Exception in Inscription.toString = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return msg;
        } }
  /*
public static Inscription map(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
    Inscription i = new Inscription();
    i.setInscriptionFinalResult(rs.getShort("InscriptionFinalResult"));
    i.setInscriptionTeeStart(rs.getString("InscriptionTeeStart"));
    i.setInscriptionInvitedBy(rs.getString("InscriptionInvitedBy"));
    i.setInscriptionIdTee(rs.getInt("InscriptionIdTee")); // new 31-03-2019
    i.setInscriptionMatchplayTeam(rs.getString("In‌scriptionMatchplayTeam")); // new 20-09-2021

   return i;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map
*/
} // end class