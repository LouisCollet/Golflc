package charts;

//import entite.Average;
import entite.Handicap;
import entite.Player;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.inject.Named;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

@Named
public class HandicapModel implements Serializable, interfaces.Log, interfaces.GolfInterface
{
 //   http://www.jqplot.com/deploy/dist/examples/multipleBarColors.html
    
     private static List<Handicap> liste;
     private static LineChartModel handicapModel;
private static LineChartModel dateModel;

 public LineChartModel getHandicapModel(final Connection conn, final Player player) throws SQLException
{
  if (handicapModel == null)
{ try
    {
        LOG.info(" ... starting HandicapModel for = " + player);
        
     handicapModel = new LineChartModel(); 
     charts.HandicapDetail hd = new charts.HandicapDetail();
     liste = hd.getStatHcp(conn, player);
   ////   createBarModel();
      createDateModel();  // tester l'exemple
      handicapModel = dateModel;
          LOG.info(" ... step 01");
//    String titre;
     String titre = "Handicap = " + player.getPlayerFirstName() + ", " + player.getPlayerLastName();//round.getIdround() + ", course = " + course.getCourseName();
     handicapModel.setTitle(titre);
        LOG.info(" ... starting HandicapModel for handicap with titre = " + titre);
      handicapModel.setAnimate(true);
      handicapModel.setLegendPosition("nw");
      handicapModel.setSeriesColors("000000, FF0000, 0000FF, 008000, FFFFFF, FFFF00");
/*-- http://www.rapidtables.com/web/color/RGB_Color.htm
seriesColors: [ "#4bb2c5", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
        "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],  // colors that will
         // be assigned to the series.  If there are more series than colors, colors
         // will wrap around and start at the beginning again.
*/
   //   handicapModel.setStacked(false);
      handicapModel.setZoom(true);
      handicapModel.getAxis(AxisType.Y).setLabel("Values Handicap");
      DateAxis axis = new DateAxis("Dates");
      axis.setTickAngle(-50);
 //     axis.setMax("01/02/2030");
      axis.setTickFormat("%b %#d, %y");
      
      
  //    handicapModel.setAnimate(true);
   //   handicapModel.setShadow(false);
  //    handicapModel.setShowPointLabels(true);
// ok mais fonctionne pour line !!   barModel.setExtender("ChartExtender"); // javascript dans forms.js
  //    handicapModel.setExtender("ChartExtender"); // javascript dans forms.js
    LOG.info(" ... .step 02");
 
 handicapModel.getAxes().put(AxisType.X, axis);
 LOG.info(" ... .step 03");
    return handicapModel;
  }catch (Exception e){
            String msg = "HandicapModel : £££ other exception = " + e.getMessage();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
   //         utils.LCUtil.showDialogInfo(msg); //
            return null;
   }finally{
     //  return barModel;
        }
}else{ // barModel not null
         LOG.debug("escaped to HandicapModel repetition with lazy loading");
    return handicapModel;  //plusieurs fois ??
}
} // end method
 
 private static void createDateModel() {
        dateModel = new LineChartModel();
        LineChartSeries series1 = new LineChartSeries();
        series1.setLabel("Series 1");
 
        series1.set("2014-01-01", 51);
        series1.set("2014-01-06", 22);
        series1.set("2014-01-12", 65);
        series1.set("2014-01-18", 74);
        series1.set("2014-01-24", 24);
        series1.set("2014-01-30", 51);
 
        LineChartSeries series2 = new LineChartSeries();
        series2.setLabel("Series 2");
 
        series2.set("2014-01-01", 32);
        series2.set("2014-01-06", 73);
        series2.set("2014-01-12", 24);
        series2.set("2014-01-18", 12);
        series2.set("2014-01-24", 74);
        series2.set("2014-01-30", 62);
 
        dateModel.addSeries(series1);
        dateModel.addSeries(series2);
         
        dateModel.setTitle("Zoom for Details");
        dateModel.setZoom(true);
        dateModel.getAxis(AxisType.Y).setLabel("Values");
        DateAxis axis = new DateAxis("Dates");
        axis.setTickAngle(-50);
        axis.setMax("2014-02-01");
        axis.setTickFormat("%b %#d, %y");
         
        dateModel.getAxes().put(AxisType.X, axis);
    }

 
 
private static void createBarModel()
{try{
   
        LOG.info("HandicapModel : length of list average = " + liste.size());
            if (liste.isEmpty()) {
                LineChartSeries empty = new LineChartSeries();
                empty.setLabel("No Handicap found for this player !!");
                empty.set(0, 0); //fake but necessary !
                handicapModel.addSeries(empty);
    //   LOG.info(" ... step 10");
                    LOG.info("No Handicap found for this player ");
            } else {  // not empty list
//                    LOG.info("nombre de rounds sur le parcours  = " + liste.get(0).getCountRounds());
                LineChartSeries hcp = new LineChartSeries();
         //       par.setLabel("Par for " + listavg.get(0).getCountRounds() + " rounds");
                hcp.setLabel("Handicap");
        // LOG.info(" ... step 11");
                for (Handicap i : liste) {
                    hcp.set(SDF.format(i.getHandicapStart()), i.getHandicapPlayer() ); // transform date to string
                }
                handicapModel.addSeries(hcp);
 LOG.info(" ... step 12 "); 

                    LOG.info("barModel returning from initBarModel !! = " + Arrays.toString(liste.toArray()) );
            }   // END NOT EMPTY LIST
}catch (Exception e){
            String msg = "initBarModel : £££ other exception = " + e.getMessage();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
}
    } //end initBarModel method

    public static LineChartModel getHandicapModel() {
        return handicapModel;
    }

    public static void setHandicapModel(LineChartModel barModel) {
        HandicapModel.handicapModel = barModel;
    }
} //end class