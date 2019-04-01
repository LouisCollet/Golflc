package entite;

//import custom_validations.Inscription;@Named  this is teh new version of PlayerHasRound.java !!!!!
import static interfaces.GolfInterface.NEWLINE;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

@Named("inscriptionNew")
@SessionScoped
public class Inscription implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer round_idround; // mod 10/11/2014
    private Integer player_idplayer;
    private Short playerhasroundFinalResult;
    private Short playerhasroundZwanzeursResult;
    private Short playerhasroundZwanzeursGreenshirt;
 //   @Inscription(value = playerGender , round = round_idround) // new 19/08/2014 custom validation !!!
    private String playerGender; // new 19/08/2014
    private String inscriptionTeeStart;
    private Integer inscriptionIdTee; // new 31-03-2019
    private String inscriptionInvitedBy;
    private Date playerhasroundModificationDate;
    private boolean InscriptionOK = true; //  set true 01/12/2018

    
    public Inscription() // constructor
    {
        inscriptionTeeStart="YELLOW";
        //setInscriptionOK(false);
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

    public Short getPlayerhasroundFinalResult() {
        return playerhasroundFinalResult;
    }

    public void setPlayerhasroundFinalResult(Short playerhasroundFinalResult) {
        this.playerhasroundFinalResult = playerhasroundFinalResult;
    }

    public Date getPlayerhasroundModificationDate() {
        return playerhasroundModificationDate;
    }

    public void setPlayerhasroundModificationDate(Date playerhasroundModificationDate) {
        this.playerhasroundModificationDate = playerhasroundModificationDate;
    }

    public Short getPlayerhasroundZwanzeursResult() {
        return playerhasroundZwanzeursResult;
    }

    public void setPlayerhasroundZwanzeursResult(Short playerhasroundZwanzeursResult) {
        this.playerhasroundZwanzeursResult = playerhasroundZwanzeursResult;
    }

    public Short getPlayerhasroundZwanzeursGreenshirt() {
        return playerhasroundZwanzeursGreenshirt;
    }

    public void setPlayerhasroundZwanzeursGreenshirt(Short playerhasroundZwanzeursGreenshirt) {
        this.playerhasroundZwanzeursGreenshirt = playerhasroundZwanzeursGreenshirt;
    }

    public String getPlayerGender() {
        return playerGender;
    }

    public void setPlayerGender(String playerGender) {
        this.playerGender = playerGender;
    }

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

   

 //   public String getPlayerhasroundmatchplayresult() {
 //       return playerhasroundmatchplayresult;
 //   }

 //   public void setPlayerhasroundmatchplayresult(String playerhasroundmatchplayresult) {
 //       this.playerhasroundmatchplayresult = playerhasroundmatchplayresult;
 //   }

 @Override
public String toString(){
try {
     LOG.info("starting toString Inscription!");
    return 
        (NEWLINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEWLINE 
               + " ,idplayer : "   + this.getPlayer_idplayer()
               + " ,player Gender : "   + this.getPlayerGender()
               + " ,Final Result : "   + this.getPlayerhasroundFinalResult()
               + " ,idround : "   + this.getRound_idround()
               + " ,Start : "   + this.getInscriptionTeeStart()
               + " ,idtee : "   + this.getInscriptionIdTee()
            
            
            // à compléter
        );
    }catch(Exception e){
        String msg = "£££ Exception in Inscription.toString = " + e.getMessage(); 
        LOG.error(msg);
        showMessageFatal(msg);
        return msg;
  }
    
}
public static Inscription mapInscription(ResultSet rs) throws SQLException{
      String METHODNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
  try{
    Inscription i = new Inscription();
    i.setPlayerhasroundFinalResult(rs.getShort("InscriptionFinalResult"));
 //       LOG.info("PlayerhasroundFinalResult = " + i.getPlayerhasroundFinalResult());
    i.setPlayerhasroundZwanzeursResult(rs.getShort("InscriptionZwanzeursResult") );
    i.setPlayerhasroundZwanzeursGreenshirt(rs.getShort("InscriptionZwanzeursGreenshirt") );
    i.setInscriptionTeeStart(rs.getString("InscriptionTeeStart"));
    i.setInscriptionInvitedBy(rs.getString("InscriptionInvitedBy"));
    i.setInscriptionIdTee(rs.getInt("InscriptionIdTee")); // new 31-03-2019

   return i;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + METHODNAME + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map
} // end class