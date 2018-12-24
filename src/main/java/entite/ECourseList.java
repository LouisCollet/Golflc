package entite;

//import java.io.Serializable;
import static interfaces.GolfInterface.NEWLINE;
import static interfaces.Log.LOG;
import javax.inject.Named;

@Named
public class ECourseList // implements Comparable<ECourseList> 
{
    public Club Eclub; 
    public Course Ecourse;
    public Tee Etee;   // new 09/08/017
    public Round Eround;  // new 20/01/2018
    public Handicap Ehandicap;
    public PlayerHasRound Eplayerhasround;
    public Inscription EinscriptionNew;
    public Player Eplayer;
    public Classment Eclassment;
    public ScoreStableford EscoreStableford;
    public Hole Ehole;
    public Subscription Esubscription;
 //   private int totalExtraStrokes;
  //  private Short playerhasroundFinalResult;

 public ECourseList()
    {

        Eclub = new Club();
        Ecourse = new Course();
        Etee = new Tee();
        Eround = new Round();
        Ehandicap = new Handicap();
        Eplayerhasround = new PlayerHasRound();
        EinscriptionNew = new Inscription();
        Eplayer = new Player();
        Eclassment = new Classment();
        EscoreStableford = new ScoreStableford();
        Ehole = new Hole();
        Esubscription = new Subscription();
    }
 
    public Club getClub() {   // permet d'utiliser #{vc.round.idround} par ex dans selectInscription.xhtml
        return Eclub;
    }
    public void setClub(Club club) {
        this.Eclub = club;
    }
    public Classment getClassment() {
        return Eclassment;
    }
    public void setClassment(Classment classment) {
        this.Eclassment = classment;
    }

    public Subscription getSubscription() {
        return Esubscription;
    }
    public void setSubscription(Subscription subscription) {
        this.Esubscription = subscription;
    }
    
    
    
    
    public Course getCourse() {
        return Ecourse;
    }

    public void setCourse(Course course) {
        this.Ecourse = course;
    }

    public Tee getTee() {
        return Etee;
    }

    public void setTee(Tee tee) {
        this.Etee = tee;
    }

    public Round getRound() {
        return Eround;
    }

    public void setRound(Round round) {
        this.Eround = round;
    }

    public Handicap getHandicap() {
        return Ehandicap;
    }

    public void setHandicap(Handicap handicap) {
        this.Ehandicap = handicap;
    }

    public PlayerHasRound getInscription() {
        return Eplayerhasround;
    }

    public void setInscription(PlayerHasRound inscription) {
        this.Eplayerhasround = inscription;
    }

    public Player getPlayer() {
        return Eplayer;
    }

    public void setPlayer(Player player) {
        this.Eplayer = player;
    }

   public Hole getHole() {
        return Ehole;
    }

    public void setScoreStableford(ScoreStableford score) {
        this.EscoreStableford = score;
    }
    
    public ScoreStableford getScoreStableford() {
        return EscoreStableford;
    }

    public void setHole(Hole hole) {
        this.Ehole = hole;
    }
    
    public Inscription getInscriptionNew() {
        return EinscriptionNew;
    }

    public void setInscriptionNew(Inscription EinscriptionNew) {
        this.EinscriptionNew = EinscriptionNew;
    }

@Override
public String toString()
{ 
    LOG.info("starting toString ECourseList!");
    return 
        (NEWLINE 
            + "from entite " + getClass().getSimpleName() + " : "
       +  Eclub.toString()
       +  Ecourse.toString()
       +  Etee.toString()
       +  Eround.toString()
   //   +  Eplayer.toString()
       +  Eclassment.toString()
       +  EscoreStableford.toString()
       +  Ehole.toString()
       + Esubscription.toString()
        );
}   
} // end class