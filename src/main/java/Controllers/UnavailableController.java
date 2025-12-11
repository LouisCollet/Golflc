package Controllers;

import entite.Club;
import entite.composite.EUnavailable;
import entite.Round;
import entite.Structure;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import lists.UnavailableListForDate;
import utils.DBConnection;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

// used in CourseController, not a Named bean
public class UnavailableController implements interfaces.Log{
// public TarifGreenfeeController(){  // constructor
//    }
// @Inject private EUnavailable unavailable;
public EUnavailable inputUnvailableStructure(EUnavailable unavailable) throws SQLException, Exception{  // used in unavailable_structure.xhtml
try{
        LOG.debug("entering inputUnvailableStructure with unavailable = " + unavailable);
 // magic happens here 
    Structure structure = new Structure();
    structure.setCourseId(Integer.valueOf(unavailable.getStructure().getWorkCourseId()));
    structure.setItem(unavailable.getStructure().getWorkItem());
    structure.setStatus(null);  // will be completed later
  // add element to List
    unavailable.getStructure().getStructureList().add(structure);
// house keeping
    unavailable.getStructure().setWorkItem(null); // init pour le prochain affichage
    unavailable.getStructure().setWorkCourseId(" "); // mod was null
    unavailable.getStructure().setItemExists(true); // 19/06/2022 gestion menu
    String msg = "structureList after add = " +  unavailable.getStructure().getStructureList().toString(); 
        LOG.info(msg);
        showMessageInfo(msg);
//        LOG.debug("end inputUnvailableStructure");
    return unavailable;
}catch(Exception ex){
    String msg = "inputUnvailableStructure Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} // end method

 public boolean updateClub(EUnavailable unavailable, Club club, Connection conn) throws Exception { //modify club from unavailable_structure.xhtml
   LOG.debug("entering modifyClubUnavailableStructure  for club = " + club);
 try{
      club = new read.ReadClub().read(club,conn);  // pour avoir clubname, etc...
            LOG.debug("club for unavailable = " + club);
      club.setUnavailableStructure(unavailable.getStructure());
         LOG.debug("input club for modification structure = " + club);
      if(new update.UpdateClub().update(club, conn)){
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
  }catch (Exception ex){
            String msg = "Exception in modifyClubUnavailableStructure " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }
} // end modifyClub

  public Structure isRoundUnavailable(final Club club, final Round round, final Connection conn) throws Exception { //modify club from unavailable_structure.xhtml
   LOG.debug("entering isRoundUnavailable");
   LOG.debug(" for round = " + round);
   LOG.debug(" for club = " + club);
   LOG.debug(" for CourseId = " + round.getCourseIdcourse());
   LOG.debug(" for roundDate = " + round.getRoundDate());
 //  LOG.debug(" for startDate Period = " + unavailable.getPeriod().getStartDate());
 //  LOG.debug(" for endDate Period = " + unavailable.getPeriod().getEndDate());
 try{
      EUnavailable unavailable = new UnavailableListForDate().list(round.getRoundDate(),club,conn);
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
    var v = unavailable.getStructure().getStructureList();
       LOG.debug("v = " + v.toString());
    boolean found = false;
    for(int i=0; i<v.size(); i++){
        if(v.get(i).getCourseId().equals(round.getCourseIdcourse())){
           found = true;
           structure.setCourseId(v.get(i).getCourseId());
           structure.setItem(v.get(i).getItem());
           structure.setStatus(unavailable.getPeriod().getItemPeriod()[i]);  // from index equivalent dans period !!
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
 }catch (Exception ex){
            String msg = "Exception in isRoundUnavailable " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
    return null;
} // end isRoundUnavailable
  
 void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
  try{
      Club club = new Club();
      club.setIdclub(101); // la cala
      Round round = new Round();
      round.setIdround(698);  // 19/05/2022 16:01
      round = new read.ReadRound().read(round, conn);
   // changing data for testing purpose
    //  round.setRoundDate(LocalDateTime.parse("2022-06-29T17:11:30"));  // limite 17:10
     // round.setRoundDate(LocalDateTime.MIN);
      round.setCourseIdcourse(90);  // other course was 101
      Structure str = new UnavailableController().isRoundUnavailable(club, round, conn);
        LOG.debug("from main, after lp = " + str);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main     
      
} //end Class