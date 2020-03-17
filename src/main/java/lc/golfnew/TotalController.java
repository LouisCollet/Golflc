package lc.golfnew;

import entite.ECourseList;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
/**
 *https://developer.jboss.org/thread/279087 
 * @author collet
 */
@Named("totalC")
@SessionScoped
//@Stateful
public class TotalController implements Serializable, interfaces.Log{
    int totalPar = 0;
  //  @Inject
private  List<ECourseList> liste;
     
 public TotalController()  // constructor
    {
  //   liste = null;
    }

//@PostConstruct
public void init(){
        LOG.info(" starting TotalController PostConstruct init ()    = ");
        LOG.info("getliste size = " + lists.ScoreCard3List.getListe().size());
        liste = lists.ScoreCard3List.getListe();
        LOG.info("TotalController init - liste = " + liste.toString());
  ////      listround = RoundList.getListe();
}
public int getTotalPar()
{ int total = 0;
 LOG.info(" starting getTotalPar () ");

 //  LOG.info("getliste size = " + lists.ScoreCard3List.getListe().size());
  for (ECourseList golf : liste)
    {total += golf.getScoreStableford().getScorePar();}
  LOG.info("total par = " + total);
  return total;
}
public int getTotalExtraStroke()
{ int total = 0;
//liste = ;
  for (ECourseList golf : lists.ScoreCard3List.getListe())
    {total += golf.EscoreStableford.getScoreExtraStroke();}
  LOG.info("TotalController - total extra strokes = " + total);
  return total;
}

public int getTotalNet() // new 12/07/2015
{ int total = 0;
  liste = lists.ScoreCard3List.getListe();
  for (ECourseList golf : liste)
    {total += golf.EscoreStableford.getScoreStroke() - golf.EscoreStableford.getScoreExtraStroke();}
  LOG.info("total strokes - extrastrokes= " + total);
  return total;
}

public int getTotalDistance()
{ int total = 0;
liste = lists.ScoreCard3List.getListe();
  for (ECourseList golf : liste)
    {total += golf.getHole().getHoleDistance();}
  LOG.info("total distance = " + total);
  return total;
}
public int getTotalStroke()
{ int total = 0;
liste = lists.ScoreCard3List.getListe();
  for (ECourseList golf : liste)
    {total += golf.getScoreStableford().getScoreStroke();}
  LOG.info("total strokes = " + total);
  return total;
}
public int getTotalPoints()
{ int total = 0;
liste = lists.ScoreCard3List.getListe();
  for (ECourseList golf : liste)
    { total += golf.getScoreStableford().getScorePoints();}
  LOG.info("total points = " + total);
  return total;
}
public int getTotalFairway()
{ int total = 0;
liste = lists.ScoreCard3List.getListe();
  for (ECourseList golf : liste)
    {  total += golf.getScoreStableford().getScoreFairway();}
  LOG.info("total fairway = " + total);
  return total;
}
public int getTotalGreen()
{ int total = 0;
liste = lists.ScoreCard3List.getListe();
  for (ECourseList golf : liste)
    {  total += golf.getScoreStableford().getScoreGreen();}
  return total;
}
public int getTotalPutts()
{ int total = 0;
liste = lists.ScoreCard3List.getListe();
  for (ECourseList golf : liste)
    {  total += golf.getScoreStableford().getScorePutts();}
  return total;
}
public int getTotalBunker()
{ int total = 0;
liste = lists.ScoreCard3List.getListe();
  for (ECourseList golf : liste)
    {  total += golf.getScoreStableford().getScoreBunker(); }
  return total;
}
public int getTotalPenalty()
{ int total = 0;
liste = lists.ScoreCard3List.getListe();
  for (ECourseList golf : liste)
    {  total += golf.getScoreStableford().getScorePenalty();}
  return total;
}

public int getTotalZwanzeurs()
{ int total = 0;
  return total;
}
public int getTotalGreenshirt()
{ int total = 0;
  return total;
}

public int getTotalIn() { 
	int j = 0;
        int total = 0;
        liste = lists.ScoreCard3List.getListe();
	while (j < 9)
        {
		// LOG.info("while = " + liste.get(j).getScorePoints() );
                total += liste.get(j).getScoreStableford().getScorePoints();
		j++;
	}
  return total;
}

public int getTotalDistanceIn(){ 
	int j = 0;
        int total = 0;
        liste = lists.ScoreCard3List.getListe();
	while (j < 9)
        {
		// LOG.info("while = " + liste.get(j).getScorePoints() );
                total += liste.get(j).getHole().getHoleDistance();
		j++;
	}
  return total;
}

} //end Class
