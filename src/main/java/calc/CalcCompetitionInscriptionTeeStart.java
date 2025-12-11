package calc;

import entite.CompetitionDescription;
import entite.Course;
import entite.composite.ECompetition;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import utils.DBConnection;

public class CalcCompetitionInscriptionTeeStart implements interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

public String calc (ECompetition competition, Connection conn){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
     LOG.debug(" -- Start of " + methodName);
     LOG.debug(" for Competition = " + competition);
try {
    var cde = competition.getCompetitionDescription();
    var cda = competition.getCompetitionData();
       LOG.debug("series handicap = " + Arrays.deepToString(cde.getSeriesHandicap()));
       LOG.debug("gender = " + cda.getCmpDataPlayerGender());
       LOG.debug(" handicap player = " + cda.getCmpDataHandicap());
     Course course = new Course();
     course.setIdcourse(cde.getCompetitionCourseId());
     List<Tee> tees = new lists.TeesCourseList().list(course, conn);
 //       LOG.debug("line 01 - tee size = " + tees.size());
     tees.forEach(item -> LOG.debug("Tees list = " + item));  // java 8 lambda
        LOG.debug(" series handicap = " + Arrays.deepToString(cde.getSeriesHandicap()));
   // faire le calcul ici !     
     String TeeStart = "";
     if(cda.getCmpDataPlayerGender().equals("M")){
         TeeStart = "YELLOW / M / 01-18 / 37";
         LOG.debug("TeeSTart forced to = " + TeeStart);
     }else{
         TeeStart = "BLUE / L / 01-18 / 188";
         LOG.debug("TeeSTart forced to = " + TeeStart);
     }  
return TeeStart;

 } catch (Exception e) {
      String msg = " -- Error in " + methodName + e.getMessage();
      LOG.error(msg);
      utils.LCUtil.showMessageFatal(msg);
      return null;
 } finally { }
} // end method

 void main() throws Exception {
   Connection conn = new DBConnection().getConnection();
try{
   CompetitionDescription cde = new CompetitionDescription();
   cde.setCompetitionId(24);
   List<ECompetition> li = new lists.CompetitionRoundsList().list(cde, conn);
   var firstItem = li.get(0);
   String TeeStart = new CalcCompetitionInscriptionTeeStart().calc(firstItem, conn);
        LOG.debug("from main, TeeStart = " + TeeStart);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   } 
 } //end main
} //end class