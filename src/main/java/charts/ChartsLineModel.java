
package charts;

//import lc.golfnew.ListController;
import entite.Course;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import utils.LCUtil;

/**
 *
 * @author collet
 */
public class ChartsLineModel implements interfaces.Log
{
    
    private static LineChartModel lineModel;

    public LineChartModel createLineModel(final Connection conn, final Player player,
            final Course course, final Round round) throws SQLException 
    {          
            /* voir exemple dans ChartBean.java
         use jqPlot : http://www.jqplot.com/ - A Versatile and Expandable jQuery Plotting Plugin !
         * lineWidth:2,             markerOptions: { style:'dimaond' }
         * showLine:false,             markerOptions: { size: 7, style:"x" }
         *  markerOptions: { style:"circle" }
         * lineWidth:5,             markerOptions: { style:"filledSquare", size:10 }
         * 
         * labelOptions: {            fontFamily: 'Georgia, Serif',            fontSize: '12pt'          }
         if(series.size() > 0)legendPosition="nw"
           y      zoom="true"
           y      animate="true"
           y      title="#{msg['message.stat.parcours']} #{courseC.course.courseName}"
          y       style="height:400px; width:950px; margin-top:20px"
          y       seriesColors="000000, FF0000, 0000FF, 008000, FFFFFF, FFFF00"
           y      stacked="false"
                 xaxisLabel="#{msg['hole.number']}"
                 yaxisLabel="#{msg['score.stroke']}"
                 extender="ChartExtender"
                 widgetVar="chart"/>
         **/

 LOG.info(" ... entering CreateLineModel new API for player = " + player.getIdplayer() +
             " course  = " + course.getIdcourse() + " round = " + round.getIdround() );
  try {
            lineModel = new LineChartModel();
   
            lineModel.setTitle("#{msg['message.stat.parcours']} #{courseC.course.courseName}");
            lineModel.setSeriesColors(NEW_LINE);
            lineModel.setAnimate(true);
            lineModel.setZoom(true);
            lineModel.setStacked(false);
 LOG.info("step 01");
            //0=idround, 1=scorehole, 2=scorestroke, 3=rounddate, 4=coursename, 5=roundholes
            String[][] chart;
           // chart = ListController.getStatArray(player, course);
         //   chart = array.StatisticsArray.LoadStatisticsArray(null, player, null); // mod 22/06/2014
             load.LoadStatisticsArray lstta = new load.LoadStatisticsArray();
             chart = lstta.LoadStatisticsArray(conn, player, round); // mod 22/06/2014
                LOG.info("length of chart array = " + chart.length);
                LOG.info("chart array = " + Arrays.deepToString(chart));
            // array avec tous les tours sur le même course 
            if (chart.length > 0) {
                short t1 = 0;
                short t2 = 0;
                short h = 0; // # holes
                short l1 = (short) (chart.length + 1);
                String[][] tour = null;

                //for(int i=0;(t1+h)<(chart.length+1);i++)
                for (int i = 0; (t1 + h) < l1; i++)
                {
                    h = Short.parseShort(chart[t1][5]); // roundholes
                    // LOG.info("increment     = " + h);
                    t2 = (short) (t1 + h);
                    LOG.info("end of tour   : " + (i + 1) + " ,t2 = " + t2 + " length t2-t1 = " + (t2 - t1));
                    tour = Arrays.copyOfRange(chart, t1, t2);
                    LOG.info("current tour = " + Arrays.deepToString(tour));
                    lineModel.addSeries(getChartData("??", tour));
               // LOG.info("t1 = " + t1);
                    // LOG.info("t2 = " + t2);
                    t1 = t2;
              //  LOG.info("t1 = " + t1);
                    //  LOG.info("h  = " + h);
                } //end for

            } else { //end if - array chart empty
                LineChartSeries series1 = new LineChartSeries();
                series1.setLabel("No Round Found on this Course !!");
                series1.set(0, 0); //fake but necessary !
                lineModel.addSeries(series1);
                LOG.info("No Round Found for this Course = ");
            } //end else
 return lineModel;
        } catch (ArrayIndexOutOfBoundsException cv) {
            String msg = "£££ index out of bounds = if from <0 or from > original.length() " + cv.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } catch (IllegalArgumentException cv) {
            String msg = "£££ illegal argument = if from > to " + cv.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } catch (NullPointerException cv) {
            String msg = "£££ nullPointerException in createLinearModel : " + cv.getMessage(); // null
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } catch (Exception cv) {
            String msg = "£££ other exception = " + cv.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }
        finally
        {   
    //    return lineModel;
            }
   //     }
    } // end method

    
    private static LineChartSeries getChartData(String label, String[][] chart) {
        LineChartSeries serie_course = new LineChartSeries();
        // LOG.info("array chart = " + NEWLINE + Arrays.deepToString(chart));
        LOG.info("array chart length = " + chart.length);
        //series1.setLabel("Round 2012-09-21");
        if (chart[0][0] == null) {
            LOG.info("array null = " + chart[0][0]);
            return null;
        }
        serie_course.setLabel(chart[0][3] + " - " + chart[0][0]); // round date, id round
        serie_course.setMarkerStyle("circle"); // was ok
        //for (int i = 0; i < chart.length; i++) {
        for (String[] chart1 : chart) {
            int holes = Integer.parseInt(chart1[1]); // scorehole
            int strokes = Integer.parseInt(chart1[2]); // scorestroke
            LOG.info("round = " + chart1[0] + " /hole = " + holes + " / strokes = " + strokes);
            serie_course.set(holes, strokes);
        }
        return serie_course;
    }

    public static LineChartModel getLineModel() {
        return lineModel;
    }

    public static void setLineModel(LineChartModel lineModel) {
        ChartsLineModel.lineModel = lineModel;
    }

    
    
} //end class
