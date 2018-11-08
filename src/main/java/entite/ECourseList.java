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
    public Course getCourse() {
        return Ecourse;
    }

    public void setCourse(Course Ecourse) {
        this.Ecourse = Ecourse;
    }

    public Tee getTee() {
        return Etee;
    }

    public void setTee(Tee Etee) {
        this.Etee = Etee;
    }

    public Round getRound() {
        return Eround;
    }

    public void setRound(Round Eround) {
        this.Eround = Eround;
    }

    public Handicap getHandicap() {
        return Ehandicap;
    }

    public void setHandicap(Handicap Ehandicap) {
        this.Ehandicap = Ehandicap;
    }

    public PlayerHasRound getInscription() {
        return Eplayerhasround;
    }

    public void setInscription(PlayerHasRound Eplayerhasround) {
        this.Eplayerhasround = Eplayerhasround;
    }

    public Player getPlayer() {
        return Eplayer;
    }

    public void setPlayer(Player Eplayer) {
        this.Eplayer = Eplayer;
    }

    public Inscription getEinscriptionNew() {
        return EinscriptionNew;
    }

    public void setEinscriptionNew(Inscription EinscriptionNew) {
        this.EinscriptionNew = EinscriptionNew;
    }



 //   public int getTotalExtraStrokes() {
 //       return totalExtraStrokes;
 //   }

 //   public void setTotalExtraStrokes(int totalExtraStrokes) {
 //       this.totalExtraStrokes = totalExtraStrokes;
 //   }

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
        );
}   

} // end class