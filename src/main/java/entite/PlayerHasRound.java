package entite;

//import custom_validations.Inscription;@Named
import java.io.Serializable;
import java.util.Date;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named("inscription")
@SessionScoped
public class PlayerHasRound implements Serializable
{
    private static final long serialVersionUID = 1L;

//@NotNull(message="Bean validation : the Round ID must be completed")

  //  private static Integer round_idround;
    private Integer round_idround; // mod 10/11/2014
    private Integer player_idplayer;
    private Short playerhasroundFinalResult;
    private Short playerhasroundZwanzeursResult;
    private Short playerhasroundZwanzeursGreenshirt;
 //   @Inscription(value = playerGender , round = round_idround) // new 19/08/2014 custom validation !!!
    private String playerGender; // new 19/08/2014
    private String inscriptionTeeStart;
 //   
 //   private String playerhasroundmatchplayresult;
    private Date playerhasroundModificationDate;
private boolean InscriptionOK = false; // 02/08/2018

    public PlayerHasRound() // constructor
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

   

 //   public String getPlayerhasroundmatchplayresult() {
 //       return playerhasroundmatchplayresult;
 //   }

 //   public void setPlayerhasroundmatchplayresult(String playerhasroundmatchplayresult) {
 //       this.playerhasroundmatchplayresult = playerhasroundmatchplayresult;
 //   }

 @Override
public String toString()
{ return 
        ("from entite.PlayerHasRound = "
               + " ,idplayer : "   + this.getPlayer_idplayer()
               + " ,idround : "   + this.getRound_idround()
               + " ,Start : "   + this.getInscriptionTeeStart()

        );
}


} // end class
