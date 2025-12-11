package entite.composite;

import entite.CompetitionData;
import entite.CompetitionDescription;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import utils.LCUtil;

@Named
@RequestScoped
//@ViewScoped // new 13-02-2021
public class ECompetition implements Serializable{
   private CompetitionDescription competitionDescription;
   private CompetitionData competitionData;
 // https://docs.jboss.org/weld/reference/1.1.0.Final/en-US/html/injection.html ???
 
 public ECompetition(){  // init dans constructor
        competitionDescription = new CompetitionDescription();
        competitionData = new CompetitionData();
    }

    public CompetitionDescription getCompetitionDescription() {
        return competitionDescription;
    }

    public void setCompetitionDescription(CompetitionDescription competitionDescription) {
        this.competitionDescription = competitionDescription;
    }

    public CompetitionData getCompetitionData() {
        return competitionData;
    }

    public void setCompetitionData(CompetitionData competitionData) {
        this.competitionData = competitionData;
    }

@Override
public String toString(){ 
 try{
//    LOG.debug("starting toString ECompetition !");
    return ( 
          NEW_LINE + "FROM ENTITE : " + getClass().getSimpleName().toUpperCase()
        + NEW_LINE + TAB
               + " ,vers Competition Description : " + getCompetitionDescription()
        + NEW_LINE + TAB
               + " ,vers Competition Data : " + getCompetitionData()
        );
  }catch(Exception e){
        String msg = "£££ Exception in ECompetition.toString = " + e.getMessage(); //+ " for player = " + p.getPlayerLastName();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
} //end method
} // end class