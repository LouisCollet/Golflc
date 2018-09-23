/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package charts;

import entite.Average;
import entite.Course;
import entite.Player;
import entite.Round;
import java.sql.SQLException;
import java.util.List;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import utils.LCUtil;

/**
 *
 * @author collet
 */
public class __StatisticsGetBarModel implements interfaces.Log
{
     private static List<Average> listavg;
     private static CartesianChartModel barModel;

 public static CartesianChartModel getBarModel(final Player player, final Course course, Round round) throws SQLException
 {
        try
        {
            
          //  listavg = ListController.getStatAvg(player, course);
 //// à modifier           listavg = CourseAverage.getStatAvg(player, course, round);
            
                LOG.info("length of list average = " + listavg.size());
            barModel = new CartesianChartModel();
            if (listavg.isEmpty()) {
                ChartSeries empty = new ChartSeries();
                empty.setLabel("No Round played on this Course !!");
                empty.set(0, 0); //fake but necessary !
                barModel.addSeries(empty);
                    LOG.info("No Round played on this Course = ");
                return barModel;
            } else {  // not empty list
                    LOG.info("nombre de rounds sur le parcours  = " + listavg.get(0).getCountRounds());
                ChartSeries par = new ChartSeries();
                par.setLabel("Par for " + listavg.get(0).getCountRounds() + " rounds");

                for (Average i : listavg) {
                    par.set(i.getAvgHole(), i.getAvgPar());
                }
                barModel.addSeries(par);

                ChartSeries strokes = new ChartSeries();
                strokes.setLabel("Strokes");
                for (Average i : listavg) {
                    strokes.set(i.getAvgHole(), i.getAvgStroke());
                }
                barModel.addSeries(strokes);

                ChartSeries points = new ChartSeries();
                points.setLabel("Points");
                for (Average i : listavg) {
                    points.set(i.getAvgHole(), i.getAvgPoints());
                }
                barModel.addSeries(points);
                    LOG.info("barModel returned !! = " + barModel.toString());
             //   return barModel;
            }   // END NOT EMPTY LIST
return barModel;
        } catch (Exception e) {
            String msg = "getBarModel : £££ other exception = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return barModel;
        }
        finally {
    //    return barModel;
        }
} // end method

} //end class
