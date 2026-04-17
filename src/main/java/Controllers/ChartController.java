package Controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entite.Course;
import entite.Player;
import entite.Average;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.awt.Desktop;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.primefaces.event.ItemSelectEvent;
import software.xdev.chartjs.model.charts.Chart;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.charts.MixedChart;
import software.xdev.chartjs.model.color.RGBAColor;
import software.xdev.chartjs.model.data.LineData;
import software.xdev.chartjs.model.data.MixedData;
import software.xdev.chartjs.model.dataset.BarDataset;
import software.xdev.chartjs.model.dataset.LineDataset;
import software.xdev.chartjs.model.enums.FontStyle;
import software.xdev.chartjs.model.options.LineOptions;
import software.xdev.chartjs.model.options.Options;
import software.xdev.chartjs.model.options.Plugins;
import software.xdev.chartjs.model.options.Title;
import software.xdev.chartjs.model.options.tooltip.TooltipOptions;
import software.xdev.chartjs.model.options.Font;
import static exceptions.LCException.handleGenericException;
import static utils.LCUtil.showMessageFatal;
// voir le doc charts by lc from PF 14.docx
@Named("chartC")
@RequestScoped
public class ChartController implements Serializable{

    private static final long serialVersionUID = 1L;
    @Inject private chartsdevx.CourseAverage courseAverage; // migrated 2026-02-26

    private String lineModel;
    private String lineModel1;
    private String lineModel2;
    private String mixedModel;

    public ChartController() { }

    @PostConstruct
    public void init() {
  //      createLineModel();
     //   createMixedModel();
        LOG.debug("init done");
    }
    
    // http://www.primefaces.org/showcase/ui/chart/line.xhtml?jfwid=584f3
 public String lineModelCourse(Player player, Course course) throws SQLException {  // used in statChartCourse.xhtml
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    LOG.debug("for course = {}", course);
 try{
    List<Average> listAverage = courseAverage.stat(player, course); // migrated 2026-02-26
        if(listAverage.isEmpty()){
            String msg = "no rounds information known for this course !!! in ChartsLineModelCourse = " ;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }else{
            LOG.debug("number of holes = {}", listAverage.size());
            LOG.debug("list average = {}", listAverage.size());
        }
    List<Integer> holesInt =  IntStream.rangeClosed(1, 18).boxed().collect(Collectors.toList());
    List<String>  holes = holesInt.stream().map(i -> i.toString()).collect(Collectors.toList());
    // holes.forEach(item -> LOG.debug("list of holes 1 to 18 = {}", item));
    List<Double> valuesStrokes = new ArrayList<>();
    List<Short>  valuesPar = new ArrayList<>();
    List<Double> valuesPoints = new ArrayList<>();
    for(Average average : listAverage) {
 //              LOG.debug("i.getAvgStroke = {}", i);
        valuesStrokes.add(average.getAvgStroke()); // Double
        valuesPar.add(average.getAvgPar()); // Short
        valuesPoints.add(average.getAvgPoints());  // Double
    } //end for
    LOG.debug("value of strokes = {}", valuesStrokes.toString());
    LOG.debug("value of par     = {}", valuesPar.toString());
    LOG.debug("value of points  = {}", valuesPoints.toString());
//value of strokes = [5.5, 5.5, 4.5, 6.5, 7.0, 6.5, 5.5, 5.0, 7.5, 3.5, 5.0, 5.0, 5.0, 3.0, 7.0, 6.0, 6.5, 6.5] 
// value of par     = [5, 4, 3, 4, 3, 4, 4, 4, 5, 4, 3, 4, 4, 3, 5, 4, 4, 5] 
// value of points  = [2.5, 2.5, 2.5, 1.5, 1.0, 0.5, 2.5, 2.0, 0.5, 3.5, 1.0, 3.0, 2.0, 3.0, 2.0, 2.0, 1.5, 1.5] 
/* à faire remplacer par ce qui suit, plus récent et plus lisible !!*/
  //https://github.com/xdev-software/chartjs-java-model/blob/develop/chartjs-java-model/src/test/java/software/xdev/chartjs/model/LineChartTest.java
  
// complete dataset mod 11-11-2025
    Collection<Number> numberCollection = valuesPar.stream().map(n -> n).collect(Collectors.toList()); // List<Short> to Collection<Number> conversion
    final LineDataset datasetPar = new LineDataset()
	.setLabel("Par")
 	.setData(numberCollection) // needs a Collection<Number>
	.setBorderColor(RGBAColor.GREEN)
        .setBorderWidth(3)
        .setFill(false)
        .setLineTension(0.1f);

    numberCollection = valuesStrokes.stream().map(n -> n).collect(Collectors.toList()); // List<Double> to Collection<Number> conversion
    final LineDataset datasetStrokes = new LineDataset()
	.setLabel("Strokes")
	.setData(numberCollection)
	.setBorderColor(RGBAColor.RED)
        .setBorderWidth(3)
        .setFill(false)
        .setLineTension(0.1f);   

    numberCollection = valuesPoints.stream().map(n -> n).collect(Collectors.toList());
    final LineDataset datasetPoints = new LineDataset()
	.setLabel("Points")
	.setData(numberCollection)
	.setBorderColor(RGBAColor.BLUE)
        .setBorderWidth(3)
        .setFill(true)
        .setLineTension(0.1f);

// complete data
    final LineData data = new LineData()
        .setLabels(holes)
	.addDataset(datasetPar)
	.addDataset(datasetStrokes)
        .addDataset(datasetPoints);
    
// Police du titre
    final Font titleFont = new Font()
            .setFamily("Helvetica")
            .setSize(38)
            .setStyle(FontStyle.ITALIC) //BOLD)
            .setWeight(24);
    final Title title = new Title()
            .setDisplay(true)
            .setText("Average per Course")
            .setColor(RGBAColor.BLUE)
            .setFullSize(Boolean.TRUE)
            .setPosition("top")
            .setFont(titleFont);
 /*/ === Font de la légende ===
    final Font legendFont = new Font()
            .setFamily("Verdana")
            .setStyle(FontStyle.BOLD)
            .setWeight(36)
            .setSize(14);
       //     .setStyle("italic");
 */
 //   --- Plugins + Options ---
    final Plugins plugins = new Plugins()
           .setTooltip(new TooltipOptions().setMode("index"))
           .setTitle(title);
//     
    final LineOptions options = new LineOptions()
           .setPlugins(plugins)
           .setResponsive(true)
           .setAnimation(false)
           .setMaintainAspectRatio(false);

    final LineChart chart = new LineChart()
           .setData(data)
           .setOptions(options);
        LOG.debug("end of linemodelCourse with chart = {}", chart.toJson());
   return chart.toJson();

  } catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
   }
} // end method
 
 public void itemSelect(ItemSelectEvent event) {
            LOG.debug("entering itemSelect");
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item selected",
                "Value: " + event.getData()
                + ", Item Index: " + event.getItemIndex()
                + ", DataSet Index:" + event.getDataSetIndex());

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
 public String createLineModel() { // not used
    LOG.debug("entering createLineModel()");
   // List<String> monthslist = Arrays. asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        List<String> monthsList = new ArrayList<>();
        monthsList.addAll(Arrays.asList(new DateFormatSymbols(Locale.FRANCE).getMonths())); // chercher la locale current
        List<Integer> numbers = Stream.iterate(1, n -> n + 1).limit(18).collect(Collectors.toList());
      //  List<String> numbersStr = Stream.iterate(1, n -> n + 1).limit(18).collect(Collectors.toList());
        numbers.forEach(item -> LOG.debug("list of holes 1 to 18 = {}", item));
        
   //https://github.com/orgs/primefaces/discussions/2133 mixed model     
    //    LOG.debug("frenchMonth list = {}", monthsList.toString());
     //   monthsList.forEach(item -> LOG.debug("list of months = {}", item));  // java 8 lambda
//     List<Integer> dataList = Arrays. asList(30, 40, 35, 60, 45, 40, 55, 65, 45, 40, 55, 65);
 //   var v = dataList.stream().mapToInt(Integer::intValue);
        lineModel = new LineChart()
                .setData(new LineData()
                    .addDataset(new LineDataset()
                        .addData(30).addData(40).addData(35).addData(60).addData(45).addData(40).addData(55).addData(65).addData(45).addData(40).addData(55).addData(65)
                        .setLabel("Par hole ")
                        .setBorderWidth(3)
                        .setBorderColor(RGBAColor.GREEN)
                        .setBackgroundColor(RGBAColor.CHOCOLATE)
                        .setLineTension(0.1f)
                        .setYAxisID("left-y-axis")   
                        .setFill(false))
                    .addDataset(new LineDataset()
                        .addData(55).addData(65).addData(45).addData(40).addData(55).addData(65).addData(30).addData(40).addData(35).addData(60).addData(45).addData(40)
                     //   .setData(65, 59, 80, 81, 56, 55, 40)
                        .setLabel("Strokes")
                        .setBorderWidth(3)
                        .setBorderColor(RGBAColor.RED)
                        .setBackgroundColor(RGBAColor.ORANGE)
                        .setLineTension(0.1f)
                        .setYAxisID("right-y-axis")   
                        .setFill(false))
                    .addDataset(new LineDataset()
                        .addData(55).addData(35).addData(55).addData(30).addData(75).addData(65).addData(30).addData(40).addData(35).addData(60).addData(45)                   .addData(40)
                        .setLabel("Points")
                        .setBorderWidth(3)
                        .setBorderColor(RGBAColor.BLUE)
                        .setBackgroundColor(RGBAColor.DARK_BLUE)
                        .setLineTension(0.1f)
                        .setFill(false))    
                .setLabels(monthsList) ) // end SetData
                .setOptions(new LineOptions()
                        .setResponsive(true)
                        .setMaintainAspectRatio(false)
                        .setPlugins(new Plugins()
                            .setTooltip(new TooltipOptions().setMode("index"))
                            .setTitle(new Title()
                               .setDisplay(true)
                               // .setFont(Font("Aptos")
                               .setText("Average per Course")) // end Title
                        ) //end Plugins
                ).toJson();
        LOG.debug("output = {}", lineModel);
        return lineModel; // sert à rien !!
    } // end method


public String createMixedModel() {  // not used
        MixedData mixedData = new MixedData();

        BarDataset barDataset = new BarDataset()
                .setType("bar")
                .setData(120, 113, 175, 143, 118, 159, 110)
                .setLabel("Bar data")
                .setBorderColor(new RGBAColor(255, 99, 132, 1.0))
                .setBackgroundColor(new RGBAColor(255, 99, 132, 0.5))
                .setBorderWidth(1);

        LineDataset lineDataset1 = new LineDataset()
                .setType("line")
                .setData(119, 144, 179, 165, 195, 170, 135)
                .setLabel("Line data")
                .setStepped(true)
                .setBorderColor(new RGBAColor(75, 192, 192, 1.0))
                .setBackgroundColor(new RGBAColor(75, 192, 192, 0.5))
                .setLineTension(0.1f)
                .setFill(false);
        
        LineDataset lineDataset2 = new LineDataset()
                .setType("line")
                .setData(195, 170, 135,119, 144, 179, 165)
                .setLabel("Line data")
                .setStepped(true)
                .setBorderColor(new RGBAColor(75, 192, 192, 1.0))
                .setBackgroundColor(new RGBAColor(75, 192, 192, 0.5))
                .setLineTension(0.1f)
                .setFill(false);
        
        mixedData.addDataset(barDataset);
        mixedData.addDataset(lineDataset1);
        mixedData.addDataset(lineDataset2);

        mixedData.setLabels("January", "February", "March", "April", "May", "June", "July");

        mixedModel = new MixedChart()
                .setData(mixedData)
                .setOptions(new Options<>()
                        .setResponsive(true)
                        .setMaintainAspectRatio(false)
                        .setPlugins(new Plugins()
                                .setTooltip(new TooltipOptions().setMode("index"))
                                .setTitle(new Title()
                                        .setDisplay(true)
                                        .setText("Mixed Chart")
                                )
                        )
                ).toJson();
             LOG.debug("string mixed model = {}", mixedModel);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
             LOG.debug("Results gson format : {}", gson.toJson(mixedModel));
        
        return mixedModel;
}

    public String getLineModel1() {
        return lineModel1;
    }

    public void setLineModel1(String lineModel1) {
        this.lineModel1 = lineModel1;
    }

    public String getLineModel2() {
        return lineModel2;
    }

    public void setLineModel2(String lineModel2) {
        this.lineModel2 = lineModel2;
    }

    public String getMixedModel() {
        return mixedModel;
    }

    public void setMixedModel(String mixedModel) {
        this.mixedModel = mixedModel;
    }


 //   public String getJson() {
 //       return json;
 //   }

 //   public void setJson(String json) {
 //       this.json = json;
 //   }

    public String getLineModel() {
        return lineModel;
    }

    public void setLineModel(String lineModel) {
        this.lineModel = lineModel;
    }

//    public String getLineModelCourse() {
 //       return lineModelCourse;
//    }

 //   public void setLineModelCourse(String lineModelCourse) {
////        this.lineModelCourse = lineModelCourse;
//    }
    
//    @SuppressWarnings("java:S5443") // Only a demo nothing sensitive is here
private static void createAndOpenTestFile(final Chart<?, ?, ?> chart){
try{
    LOG.debug("entering createAndOpenTestFile");
	final Path tmp = Files.createTempFile("chart_test_", ".html");
	Files.writeString(
            tmp,
            String.format("""
                <!DOCTYPE html>
                <html lang='en'>
                \t<head>
                \t\t<meta charset='UTF-8'>
                \t\t<script src="https://cdn.jsdelivr.net/npm/chart.js@4.5.0/dist/chart.umd.js"></script>
                \t</head>
                \t<body>
                \t\t<canvas id='c' style='border:1px solid #555;'></canvas>
                \t\t<script>
                \t\t\tnew Chart(document.getElementById('c').getContext('2d'), %s);
                \t\t</script>
                \t</body>
                </html>""", chart.toJson())
			);
		LOG.debug("is Destop supported ? {}", Desktop.isDesktopSupported());
			Desktop.getDesktop().browse(tmp.toUri());
}catch(final IOException e){
        LOG.debug("exception in ??");
	throw new UncheckedIOException(e);
}
	} //end method

  //  public static void main(final String[] args){
    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class

    //    return new LineChart(data, options).toJson();
		//	this.getWebContainer(),
		//	"SpanGaps"     
        
/* pas trouvé de solution élégante, conservé le hard coding pour setData!
        String lineModelCourse = new LineChart()
                .setData(new LineData()  // Par
                    .addDataset(new LineDataset()
                        .setData(List.of(
                                listAverage.get(0).getAvgPar(),valuesPar.get(1),valuesPar.get(2),valuesPar.get(3),valuesPar.get(4),valuesPar.get(5),
                                valuesPar.get(6),valuesPar.get(7),valuesPar.get(8),valuesPar.get(9),valuesPar.get(10),valuesPar.get(11),valuesPar.get(12),
                                valuesPar.get(13),valuesPar.get(14),valuesPar.get(15),valuesPar.get(16),valuesPar.get(17)
                                         ) 
                                )
                        .setLabel("Par")
                        .setBorderWidth(3)
                        .setBorderColor(RGBAColor.GREEN)
                        .setBackgroundColor(RGBAColor.CHOCOLATE)
                        .setLineTension(0.1f)
                    //    .setYAxisID("left-y-axis")   
                        .setFill(false))
                    .addDataset(new LineDataset() // strokes
                        .setData(listAverage.get(0).getAvgStroke(),valuesStrokes.get(1),valuesStrokes.get(2),valuesStrokes.get(3),valuesStrokes.get(4),
                                valuesStrokes.get(5),valuesStrokes.get(6),valuesStrokes.get(7),valuesStrokes.get(8),
                                 valuesStrokes.get(9),valuesStrokes.get(10),valuesStrokes.get(11),valuesStrokes.get(12),
                                 valuesStrokes.get(13),valuesStrokes.get(14),valuesStrokes.get(15),valuesStrokes.get(16),valuesStrokes.get(17) )
                        .setLabel("Strokes")
                        .setBorderWidth(3)
                        .setBorderColor(RGBAColor.RED)
                        .setBackgroundColor(RGBAColor.ORANGE)
                        .setLineTension(0.1f)
                      //  .setYAxisID("right-y-axis")   
                        .setFill(false))
                    .addDataset(new LineDataset() // points
                        .setData(listAverage.get(0).getAvgPoints(),valuesPoints.get(1),valuesPoints.get(2),valuesPoints.get(3),valuesPoints.get(4),
                                valuesPoints.get(5),valuesPoints.get(6),valuesPoints.get(7),valuesPoints.get(8),
                                 valuesPoints.get(9),valuesPoints.get(10),valuesPoints.get(11),valuesPoints.get(12),
                                 valuesPoints.get(13),valuesPoints.get(14),valuesPoints.get(15),valuesPoints.get(16),valuesPoints.get(17) )
                        .setLabel("Points")
                        .setBorderWidth(3)
                        .setBorderColor(RGBAColor.BLUE)
                        .setBackgroundColor(RGBAColor.DARK_BLUE)
                        .setLineTension(0.1f)
                        .setFill(false))    
                .setLabels(holes) ) // end SetData
                .setOptions(new LineOptions()
                        .setResponsive(true)
                        .setMaintainAspectRatio(false)
                        .setPlugins(new Plugins()
                            .setTooltip(new TooltipOptions().setMode("index"))
                            .setTitle(new Title()
                               .setDisplay(true)
                               // .setFont(Font("Aptos")
                               .setText("Average per Course")) // end Title
                        ) //end Plugins
                ).toJson();
        LOG.debug("output = {}", lineModelCourse);
        return lineModelCourse;
  //  return lineModel;
                */

