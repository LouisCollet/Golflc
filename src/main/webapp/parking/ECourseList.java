package entite.composite;

import entite.Classment;
import entite.Club;
import entite.Course;
import entite.Handicap;
import entite.HandicapIndex;
import entite.Hole;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Subscription;
import entite.Tee;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import utils.LCUtil;

@Named
@RequestScoped // new 13-02-2021
public class ECourseList implements Serializable{
    @Inject private Club club; 
    @Inject private Course course;
    @Inject private Tee tee;   // new 09/08/017
    @Inject private Round round;  // new 20/01/2018
    @Inject private Handicap handicap;
    @Inject private HandicapIndex handicapIndex;
    @Inject private Inscription inscription;
    @Inject private Player player;
    @Inject private Classment classment;
    @Inject private ScoreStableford scoreStableford;
    @Inject private Hole hole;
    @Inject private Subscription subscription;
 //   private int totalExtraStrokes;
  //  private Short playerhasroundFinalResult;
@Deprecated 
 public ECourseList(){
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Tee getTee() {
        return tee;
    }

    public void setTee(Tee tee) {
        this.tee = tee;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public Handicap getHandicap() {
        return handicap;
    }

    public void setHandicap(Handicap handicap) {
        this.handicap = handicap;
    }

    public HandicapIndex getHandicapIndex() {
        return handicapIndex;
    }

    public void setHandicapIndex(HandicapIndex handicapIndex) {
        this.handicapIndex = handicapIndex;
    }

    public Inscription getInscription() {
        return inscription;
    }

    public void setInscription(Inscription inscription) {
        this.inscription = inscription;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Classment getClassment() {
        return classment;
    }

    public void setClassment(Classment classment) {
        this.classment = classment;
    }

    public ScoreStableford getScoreStableford() {
        return scoreStableford;
    }

    public void setScoreStableford(ScoreStableford scoreStableford) {
        this.scoreStableford = scoreStableford;
    }

    

    public Hole getHole() {
        return hole;
    }

    public void setHole(Hole hole) {
        this.hole = hole;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
 
 

@Override
public String toString(){ 
 try{
//    LOG.debug("starting toString ECourseList!");
    return 
        (NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase() + NEW_LINE
    //        + "from entite " + getClass().getSimpleName() + " : "
     //  +  club
       + getClub()
       + getCourse()
       + getRound()
       + getTee()
       + getInscription()
       + getClassment()
       + scoreStableford
       + getHole()
       + getSubscription()
       + getHandicapIndex()
        );
    }catch(Exception e){
        String msg = "£££ Exception in ECourseList.toString = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method   
} // end class