package Controllers;

import entite.Club;
import entite.composite.EUnavailable;
import entite.Round;
import entite.Structure;
import static interfaces.Log.LOG;
import java.io.Serializable;
import jakarta.inject.Named;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import static exceptions.LCException.handleGenericException;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@Named("unavailableC")
@ApplicationScoped
public class UnavailableController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private lists.UnavailableListForDate unavailableListForDate;
    @Inject
    private read.ReadClub readClubService;       // migrated 2026-02-24
    @Inject
    private update.UpdateClub updateClubService; // migrated 2026-02-24

    public UnavailableController() { }
// public TarifGreenfeeController(){  // constructor
//    }
// @Inject private EUnavailable unavailable;
public EUnavailable inputUnvailableStructure(EUnavailable unavailable) throws Exception{  // used in unavailable_structure.xhtml
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
try{
        LOG.debug("with unavailable = " + unavailable);
 // magic happens here 
    Structure structure = new Structure();
    structure.setCourseId(Integer.valueOf(unavailable.structure().getWorkCourseId()));
    structure.setItem(unavailable.structure().getWorkItem());
    structure.setStatus(null);  // will be completed later
  // add element to List
    unavailable.structure().getStructureList().add(structure);
// house keeping
    unavailable.structure().setWorkItem(null); // init pour le prochain affichage
    unavailable.structure().setWorkCourseId(" "); // mod was null
    unavailable.structure().setItemExists(true); // 19/06/2022 gestion menu
    String msg = "structureList after add = " +  unavailable.structure().getStructureList().toString(); 
        LOG.info(msg);
        showMessageInfo(msg);
//        LOG.debug("end inputUnvailableStructure");
    return unavailable;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} // end method

 public boolean updateClub(EUnavailable unavailable, Club club) throws Exception { //modify club from unavailable_structure.xhtml
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName + " for club = " + club);
 try{
      club = readClubService.read(club);   // pour avoir clubname, etc...
            LOG.debug("club for unavailable = " + club);
      club.setUnavailableStructure(unavailable.structure());
         LOG.debug("input club for modification structure = " + club);
      if(updateClubService.update(club)){
          String msg = "club UnavailableStructure is Modified !!" + unavailable;
          LOG.info(msg);
          showMessageInfo(msg);
          return true;
      }else{
          String msg = "club UnavailableStructure NOT Modified !!" + unavailable;
          LOG.info(msg);
          showMessageFatal(msg); 
          return false;
      }
 //return null;
  } catch (Exception e) {
    handleGenericException(e, methodName);
    return false;
  }
} // end method

  public Structure isRoundUnavailable(final Club club, final Round round) throws Exception { //modify club from unavailable_structure.xhtml
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
   LOG.debug(" for round = " + round);
   LOG.debug(" for club = " + club);
   LOG.debug(" for CourseId = " + round.getCourseIdcourse());
   LOG.debug(" for roundDate = " + round.getRoundDate());
 //  LOG.debug(" for startDate Period = " + unavailable.getPeriod().getStartDate());
 //  LOG.debug(" for endDate Period = " + unavailable.getPeriod().getEndDate());
 try{
      // was: EUnavailable unavailable = new UnavailableListForDate().list(round.getRoundDate(), club, conn);
     EUnavailable unavailable = unavailableListForDate.list(round.getRoundDate(), club); // migrated 2026-02-24
         LOG.debug("result unavailable for date = " + unavailable);
      
      Structure structure = new Structure();
      if(unavailable == null){ 
          LOG.debug("result unavailable = null " + unavailable);
        structure.setCourseId(round.getCourseIdcourse());
        structure.setItem("NO PERIOD FOUND AT THIS DATE!");
        structure.setStatus(false); // mod 26-06-2022 was true
           LOG.debug("ne pas bloquer : il n'y a pas d'indisponibilité pour le course à cette date");
           LOG.debug("result structure = " + structure);
        return structure;
      }
      LOG.debug("unavailable is NOT null");
    var v = unavailable.structure().getStructureList();
       LOG.debug("v = " + v.toString());
    boolean found = false;
    for(int i=0; i<v.size(); i++){
        if(v.get(i).getCourseId().equals(round.getCourseIdcourse())){
           found = true;
           structure.setCourseId(v.get(i).getCourseId());
           structure.setItem(v.get(i).getItem());
           structure.setStatus(unavailable.period().getItemPeriod()[i]);  // from index equivalent dans period !!
              LOG.debug("result str = " + structure);
           return structure;
        } // end if
    } //end for
 //   LOG.debug("out of outer loop courseId with j = " + j);
    if(!found){ // not found
        structure.setCourseId(round.getCourseIdcourse());
        structure.setItem("ITEM NOT FOUND - IN PERIOD");
        structure.setStatus(true);  // ne pas bloquer : le course est disponible à cette date
           LOG.debug("result str = " + structure);
        return structure;
    }
 } catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
  }
    return null;
} // end method
  
 /*
 void main() throws SQLException, Exception{
      Club club = new Club();
      club.setIdclub(101); // la cala
      Round round = new Round();
      round.setIdround(698);  // 19/05/2022 16:01
   // changing data for testing purpose
      round.setCourseIdcourse(90);  // other course was 101
      Structure str = new UnavailableController().isRoundUnavailable(club, round, null);
        LOG.debug("from main, after lp = " + str);
   } // end main
 */     
      
} //end Class