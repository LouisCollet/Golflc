package lc.golfnew;

import entite.ScoreCard;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import lists.ScoreCard3List;
/**
 *
 * @author collet
 */
@Named("totalC")
@SessionScoped

public class TotalController implements Serializable, interfaces.Log
{
    int totalPar = 0;
    private static List<ScoreCard> listsc3 = null; // used in CalculateController !!
//    private static List<ClubCourseRound> listround = null;
     
 public TotalController()  // constructor
    {
       
    }

@PostConstruct
public void init()
{
        LOG.info(" starting PostConstruct init ()    = ");
        listsc3 = ScoreCard3List.getListe();
  ////      listround = RoundList.getListe();
}
public int getTotalPar()
{ int total = 0;
  listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
    {total += golf.getScorePar();}
  return total;
}
public int getTotalExtraStroke()
{ int total = 0;
listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
    {total += golf.getScoreExtraStroke();}
  return total;
}

public int getTotalNet() // new 12/07/2015
{ int total = 0;
  listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
    {total += golf.getScoreStroke() - golf.getScoreExtraStroke();}
  return total;
}

public int getTotalDistance()
{ int total = 0;
listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
    {total += golf.getHoleDistance();}
  return total;
}
public int getTotalStroke()
{ int total = 0;
listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
    {total += golf.getScoreStroke();}
  return total;
}
public int getTotalPoints()
{ int total = 0;
listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
      
    {  // LOG.info("golf = " + golf);
        total += golf.getScorePoints();}
  return total;
}
public int getTotalFairway()
{ int total = 0;
listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
    {  
        total += golf.getScoreFairway();}
  return total;
}
public int getTotalGreen()
{ int total = 0;
listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
    {  
        total += golf.getScoreGreen();}
  return total;
}
public int getTotalPutts()
{ int total = 0;
listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
    {  
        total += golf.getScorePutts();}
  return total;
}
public int getTotalBunker()
{ int total = 0;
listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
    {  
        total += golf.getScoreBunker();
    }
  return total;
}
public int getTotalPenalty()
{ int total = 0;
listsc3 = ScoreCard3List.getListe();
  for (ScoreCard golf : listsc3)
    {  
        total += golf.getScorePenalty();
    }
  return total;
}

public int getTotalZwanzeurs()
{ int total = 0;
/*    listround = lists.__RoundList.getListe();
    if(listround == null)
        {return 0;}
  for (ClubCourseRound golf : listround)
    {
        if(golf.getRoundGame().equals("ZWANZEURS") )
        {
            total += golf.getPlayerhasroundZwanzeursResult();
        }
    }
*/
  return total;
}
public int getTotalGreenshirt()
{ int total = 0;
/*
 listround = __RoundList.getListe();
 if(listround == null)
        {return 0;}
  for (ClubCourseRound golf : listround)
    {  
        if(golf.getRoundGame().equals("ZWANZEURS") )
        {  // LOG.info(" passage for = " + golf.getPlayerhasroundZwanzeursGreenshirt() );
                                             // LOG.info(" passage for idround = " + golf.getIdround() );
         //   LOG.info(" passage for game    = " + golf.getRoundGame() );
            total += golf.getPlayerhasroundZwanzeursGreenshirt();
        }
    }
*/
  return total;
}

public int getTotalIn() // new 22/7/2013 à développer
{ 
	int j = 0;
        int total = 0;
        listsc3 = ScoreCard3List.getListe();
	while (j < 9)
        {
		// LOG.info("while = " + listsc3.get(j).getScorePoints() );
                total += listsc3.get(j).getScorePoints();
		j++;
	}
  return total;
}

public int getTotalDistanceIn() // new 08/09/2013
{ 
	int j = 0;
        int total = 0;
        listsc3 = ScoreCard3List.getListe();
	while (j < 9)
        {
		// LOG.info("while = " + listsc3.get(j).getScorePoints() );
                total += listsc3.get(j).getHoleDistance();
		j++;
	}
  return total;
}

} //end Class
