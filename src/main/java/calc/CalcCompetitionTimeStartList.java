package calc;

import entite.composite.ECompetition;
import static interfaces.Log.LOG;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import utils.LCUtil;


public class CalcCompetitionTimeStartList implements interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

public List<String> calc (ECompetition competition){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
     LOG.debug(" -- Start of " + methodName);
     LOG.debug(" with Competition = " + competition);
try {
     LocalDateTime ldt = competition.getCompetitionDescription().getCompetitionDate(); 
      int i = 0 ;
      int interval = 12; // 12 min écart entre flights
      int h = 0;
      List<String> liste = new ArrayList<>();
  //    ldt1 = LocalDateTime.of(2017,Month.SEPTEMBER,29,10,30); 
   //   liste.add(LocalTime.parse("23:59:00") + " - no preference");
      liste.add(" - no time preference");
      while (h < 4){ // n donne le choix pour 4 périodes de départ
   //       LOG.debug("i = " + i);
          LocalDateTime ldt1 = ldt.plusMinutes(i*interval); 
          LocalDateTime ldt2 = ldt1.plusHours(1);  //  départs par tranches d'une heure
          liste.add(ldt1.toLocalTime() + " - " + ldt2.toLocalTime());
          i = i + 6;
          h++;
      }
          LOG.debug("at the end start timelist = " + liste.toString());
   return liste;
 } catch (Exception e) {
      String msg = " -- Error in " + methodName + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
 } finally { }
} // end method

 void main() throws Exception {//throws SQLException // testing purposes
try{
//LOG.debug("price greenfee = " + dd);
        
 } catch (Exception e) {
            String msg = "££ Exception in main CalcTarif= " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
   }finally{
      //   DBConnection.closeQuietly(conn, null, null,null); 
          }
}// end main    


} //end class