
package charts;

import entite.Average;
import entite.Course;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LinearAxis;

/**
 *
 * @author collet
 */
public class ChartsBarModel implements interfaces.Log
{
 //   http://www.jqplot.com/deploy/dist/examples/multipleBarColors.html
    
     private static List<Average> listavg;
     private static BarChartModel barModel;

 public BarChartModel getBarModel(final Connection conn, final Player player,
         final Course course, final Round round, final String type) throws SQLException
{
  if (barModel == null)
{ try
    {
        LOG.info(" ... starting getBarModel for type = " + type);
        LOG.info(" ... starting getBarModel for = " + player);

    barModel = new BarChartModel();  
    switch (type) {
        case "round":
                LOG.info(" ... starting getBarModel for = " + round);
                charts.RoundDetail rd = new charts.RoundDetail(); 
            listavg = rd.getRoundDetail(conn, player, course, round);
            break;
        case "course":
                LOG.info(" ... starting getBarModel for = " + course);
            charts.CourseAverage ca =  new charts.CourseAverage();
            listavg = ca.getStatAvg(conn, player, course);
            break;
        default:
                LOG.error("error type : nor course nor round");
            break;
    }

     initBarModel();
//          LOG.info(" ... step 01");
String titre;
    switch (type) {
        case "round":
            titre = "Round = " + round.getIdround() + ", course = " + course.getCourseName();
            barModel.setTitle(titre);
                LOG.info(" ... starting getBarModel for = " + round + " titre = " + titre);
    //          + " (" + listavg.get(0).getCountRounds() + " round(s) )") ;
            break;
        case "course":
                LOG.info(" ... starting getBarModel for = " + course);
            barModel.setTitle("Course = " + course.getIdcourse() + " / " + course.getCourseName()
              + " (" + listavg.get(0).getCountRounds() + " round(s) )") ;
            break;
        default:
                LOG.error("error type : nor course nor round");
            break;
    }

      barModel.setAnimate(true);
      barModel.setLegendPosition("nw");
      barModel.setSeriesColors("000000, FF0000, 0000FF, 008000, FFFFFF, FFFF00");
/*-- http://www.rapidtables.com/web/color/RGB_Color.htm
seriesColors: [ "#4bb2c5", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
        "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"],  // colors that will
         // be assigned to the series.  If there are more series than colors, colors
         // will wrap around and start at the beginning again.
*/
      barModel.setStacked(false);
      barModel.setZoom(true);
      barModel.setAnimate(true);
      barModel.setShadow(false);
      barModel.setShowPointLabels(true);
// ok mais fonctionne pour line !!   barModel.setExtender("ChartExtender"); // javascript dans forms.js
      barModel.setExtender("ChartExtender"); // javascript dans forms.js
  //   LOG.info(" ... .step 02");
 //  barModel.setTitle("this is the title"); //new
     Axis xAxis = barModel.getAxis(AxisType.X);
      xAxis.setLabel("Holes");
  ///  barModel.getAxes().put(AxisType.X, new CategoryAxis("Years")); // new
  ///  barModel.getAxes().put(AxisType.X2, new CategoryAxis("Period")); //new
    
      Axis yAxis = barModel.getAxis(AxisType.Y);
      yAxis.setLabel("Strokes");
      yAxis.setMin(0);
      yAxis.setMax(10);
    // new  4 lines 
      Axis y2Axis = new LinearAxis ("Number");
      y2Axis.setMin(0);
      y2Axis.setMax(10);
      barModel.getAxes().put(AxisType.Y2, y2Axis);
    return barModel;
  }catch (Exception e){
            String msg = "getBarModel : £££ other exception = " + e.getMessage();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
            return null;
   }finally{
     //  return barModel;
        }
}else{ // barModel not null
         LOG.debug("escaped to barModel repetition with lazy loading");
    return barModel;  //plusieurs fois ??
}
} // end method
private void initBarModel()
{try{
        LOG.info("initBarModel : length of list average = " + listavg.size());
            if (listavg.isEmpty()) {
                ChartSeries empty = new ChartSeries();
                empty.setLabel("No Round played on this Course !!");
                empty.set(0, 0); //fake but necessary !
                barModel.addSeries(empty);
  //     LOG.info(" ... step 10");
                    LOG.info("No Round played on this Course = ");
          //      return barModel;
            } else {  // not empty list
                    LOG.info("nombre de rounds sur le parcours  = " + listavg.get(0).getCountRounds());
                ChartSeries par = new ChartSeries();
         //       par.setLabel("Par for " + listavg.get(0).getCountRounds() + " rounds");
                par.setLabel("Par");
 //LOG.info(" ... step 11");
                for (Average i : listavg) {
                    par.set(i.getAvgHole(), i.getAvgPar());
                }
                barModel.addSeries(par);
 //LOG.info(" ... step 12");
                ChartSeries strokes = new ChartSeries();
                strokes.setLabel("Strokes (brut)");
                listavg.forEach((i) -> {
                    strokes.set(i.getAvgHole(), i.getAvgStroke());
            });
                barModel.addSeries(strokes);
 //LOG.info(" ... step 13");
                ChartSeries points = new ChartSeries();
                points.setLabel("Points");
                listavg.forEach((i) -> {
                    points.set(i.getAvgHole(), i.getAvgPoints());
            });
                barModel.addSeries(points);
                    LOG.info("barModel returning from initBarModel !! = " );
            }   // END NOT EMPTY LIST
}catch (Exception e){
            String msg = "initBarModel : £££ other exception = " + e.getMessage();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
}
    } //end initBarModel method

    public static BarChartModel getBarModel() {
        return barModel;
    }

    public static void setBarModel(BarChartModel barModel) {
        ChartsBarModel.barModel = barModel;
    }
} //end class