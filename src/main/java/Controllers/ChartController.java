package Controllers;

import entite.Course;
import entite.Player;
import entite.Average;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static utils.LCUtil.showMessageFatal;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.primefaces.event.ItemSelectEvent;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.color.RGBAColor;
import software.xdev.chartjs.model.data.LineData;
import software.xdev.chartjs.model.dataset.LineDataset;
import software.xdev.chartjs.model.enums.FontStyle;
import software.xdev.chartjs.model.options.LineOptions;
import software.xdev.chartjs.model.options.Plugins;
import software.xdev.chartjs.model.options.Title;
import software.xdev.chartjs.model.options.tooltip.TooltipOptions;
import software.xdev.chartjs.model.options.Font;

@Named("chartC")
@RequestScoped
public class ChartController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private chartsdevx.CourseAverage courseAverage;

    private String lineModel;
    private String lineModel1;
    private String lineModel2;
    private String mixedModel;

    public ChartController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end method

    public String lineModelCourse(Player player, Course course) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for course = {}", course);
        try {
            List<Average> listAverage = courseAverage.stat(player, course);
            if (listAverage.isEmpty()) {
                String msg = "no rounds information known for this course: " + course;
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }
            LOG.debug("number of averages = {}", listAverage.size());

            List<String> holes = IntStream.rangeClosed(1, 18).boxed()
                    .map(i -> i.toString()).collect(Collectors.toList());

            List<Double> valuesStrokes = new ArrayList<>();
            List<Short>  valuesPar    = new ArrayList<>();
            List<Double> valuesPoints = new ArrayList<>();
            for (Average average : listAverage) {
                valuesStrokes.add(average.getAvgStroke());
                valuesPar.add(average.getAvgPar());
                valuesPoints.add(average.getAvgPoints());
            }
            LOG.debug("value of strokes = {}", valuesStrokes);
            LOG.debug("value of par     = {}", valuesPar);
            LOG.debug("value of points  = {}", valuesPoints);

            Collection<Number> numberCollection = valuesPar.stream().map(n -> n).collect(Collectors.toList());
            final LineDataset datasetPar = new LineDataset()
                .setLabel("Par")
                .setData(numberCollection)
                .setBorderColor(RGBAColor.GREEN)
                .setBorderWidth(3)
                .setFill(false)
                .setLineTension(0.1f);

            numberCollection = valuesStrokes.stream().map(n -> n).collect(Collectors.toList());
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

            final LineData data = new LineData()
                .setLabels(holes)
                .addDataset(datasetPar)
                .addDataset(datasetStrokes)
                .addDataset(datasetPoints);

            final Font titleFont = new Font()
                    .setFamily("Helvetica")
                    .setSize(38)
                    .setStyle(FontStyle.ITALIC)
                    .setWeight(24);
            final Title title = new Title()
                    .setDisplay(true)
                    .setText("Average per Course")
                    .setColor(RGBAColor.BLUE)
                    .setFullSize(Boolean.TRUE)
                    .setPosition("top")
                    .setFont(titleFont);
            final Plugins plugins = new Plugins()
                   .setTooltip(new TooltipOptions().setMode("index"))
                   .setTitle(title);
            final LineOptions options = new LineOptions()
                   .setPlugins(plugins)
                   .setResponsive(true)
                   .setAnimation(false)
                   .setMaintainAspectRatio(false);
            final LineChart chart = new LineChart()
                   .setData(data)
                   .setOptions(options);
            LOG.debug("end of lineModelCourse with chart = {}", chart.toJson());
            return chart.toJson();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public void itemSelect(ItemSelectEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item selected",
                "Value: " + event.getData()
                + ", Item Index: " + event.getItemIndex()
                + ", DataSet Index:" + event.getDataSetIndex());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    } // end method

    public String getLineModel() { return lineModel; }
    public void setLineModel(String lineModel) { this.lineModel = lineModel; }

    public String getLineModel1() { return lineModel1; }
    public void setLineModel1(String lineModel1) { this.lineModel1 = lineModel1; }

    public String getLineModel2() { return lineModel2; }
    public void setLineModel2(String lineModel2) { this.lineModel2 = lineModel2; }

    public String getMixedModel() { return mixedModel; }
    public void setMixedModel(String mixedModel) { this.mixedModel = mixedModel; }

} // end class
