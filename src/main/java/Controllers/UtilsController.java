package Controllers;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import entite.PlayingHandicap;
import entite.Round;
import static interfaces.GolfInterface.ZDF_TIME;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.primefaces.component.export.ExcelOptions;
import org.primefaces.component.export.PDFOptions;
import org.primefaces.component.export.PDFOrientationType;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Overlay;

@Named("utilsC")
@RequestScoped
public class UtilsController implements Serializable, interfaces.GolfInterface, interfaces.Log {

    @Inject private entite.Settings settings;

    private String content;
    private ExcelOptions excelOpt;
    private PDFOptions pdfOpt;
    private MapModel<Object> mapModel;
    private String mapCenter;
    private int mapZoom;
    private String infoWindowText;
    private Marker<Object> currentMarker;
    private Overlay<Object> overlay;
    private LocalTime minTime;
    private LocalTime maxTime;
    private int count = 20;

    public UtilsController() {
        mapModel = new DefaultMapModel<>();
        mapZoom = 7;
        mapCenter = "51.5, 10.49";
    } // end constructor

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            customizationOptions();
        } catch (Exception ex) {
            LOG.error("customizationOptions failed: {}", ex.getMessage(), ex);
        }
    } // end method

    public void customizationOptions() {
        excelOpt = new ExcelOptions();
        excelOpt.setFacetBgColor("#F88017");
        excelOpt.setFacetFontSize("10");
        excelOpt.setFacetFontColor("#0000ff");
        excelOpt.setFacetFontStyle("BOLD");
        excelOpt.setCellFontColor("#00ff00");
        excelOpt.setCellFontSize("8");
        excelOpt.setFontName("Verdana");
        pdfOpt = new PDFOptions();
        pdfOpt.setFacetBgColor("#F88017");
        pdfOpt.setFacetFontColor("#0000ff");
        pdfOpt.setFacetFontStyle("BOLD");
        pdfOpt.setCellFontSize("12");
        pdfOpt.setFontName("Courier");
        pdfOpt.setOrientation(PDFOrientationType.PORTRAIT);
    } // end method

    public ExcelOptions getExcelOpt() { return excelOpt; }
    public void setExcelOpt(ExcelOptions excelOpt) { this.excelOpt = excelOpt; }

    public PDFOptions getPdfOpt() { return pdfOpt; }
    public void setPdfOpt(PDFOptions pdfOpt) { this.pdfOpt = pdfOpt; }

    public void postProcessXLS(Object document) {
        HSSFWorkbook wb = (HSSFWorkbook) document;
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow header = sheet.getRow(0);
        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREEN.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        for (int i = 0; i < header.getPhysicalNumberOfCells(); i++) {
            HSSFCell cell = header.getCell(i);
            cell.setCellStyle(cellStyle);
        }
    } // end method

    // utilisé dans datePicker, yearRange
    public int dateYear(int year) {
        return LocalDate.now().getYear() + year;
    }

    public int dateMonth(int month) {
        return LocalDate.now().getMonthValue() + month;
    }

    // utilisé dans datePicker, minDate ou maxDate
    public LocalDate localDateWeek(int week) {
        return LocalDate.now().plusWeeks(week);
    } // end method

    // utilisé dans datePicker, minDate ou maxDate
    public LocalDate localDateYear(int year) {
        return LocalDate.now().plusYears(year);
    } // end method

    // utilisé dans datePicker, minDate ou maxDate
    public LocalTime localTime(int hour, int minute) {
        return LocalTime.of(hour, minute);
    } // end method

    public LocalTime getMinTime() { return minTime; }
    public void setMinTime(LocalTime minTime) { this.minTime = minTime; }

    public LocalTime getMaxTime() { return maxTime; }
    public void setMaxTime(LocalTime maxTime) { this.maxTime = maxTime; }

    public void logFile() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        runtime.exec(new String[] { "C:\\Program Files\\JGsoft\\EditPadLite\\EditPadLite7.exe", "C:\\log\\golflc.log" });
    } // end method

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public void increment() {
        count--;
    } // end method

    public String getContent() { return content; }
    public void setContent(final String content) { this.content = content; }

    public void preProcessPDF(Object document) {
        Document pdf = (Document) document;
        pdf.setPageSize(PageSize.A4.rotate());
        pdf.open();
    } // end method

    public int getElem(PlayingHandicap playingHcp) {
        int counter_players = 0;
        Double[] hcp = playingHcp.getHcpScr();
        for (Double hcp1 : hcp) {
            if (hcp1 != 0.0) {
                counter_players++;
                LOG.debug("Scramble Hcp = {}", hcp1);
            }
        }
        LOG.debug("Scramble Hcp number of players = {}", counter_players);
        return counter_players;
    } // end method

    public void printResultSet(ResultSet rs) throws SQLException {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            LOG.debug("querying SELECT * FROM XXX");
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    String columnValue = rs.getString(i);
                    LOG.debug("{} = {}", rsmd.getColumnName(i), columnValue);
                }
            }
        } catch (Exception ex) {
            LOG.error("printResultSet failed: {}", ex.getMessage(), ex);
        }
    } // end method

    public double getSum(PlayingHandicap playingHcp) {
        double sum = 0;
        Double[] hcp = playingHcp.getHcpScr();
        for (Double i : hcp) {
            sum += i;
            LOG.debug("Scramble Hcp - The sum is : {}", sum);
        }
        LOG.debug("Scramble Hcp - The FINAL sum is : {}", sum);
        return sum;
    } // end method

    public MapModel<Object> getMapModel() { return mapModel; }
    public void setMapModel(MapModel<Object> mapModel) { this.mapModel = mapModel; }

    public String getMapCenter() { return mapCenter; }
    public void setMapCenter(String mapCenter) { this.mapCenter = mapCenter; }

    public int getMapZoom() { return mapZoom; }
    public void setMapZoom(int mapZoom) { this.mapZoom = mapZoom; }

    public String getInfoWindowText() { return infoWindowText; }
    public void setInfoWindowText(String infoWindowText) { this.infoWindowText = infoWindowText; }

    public Marker<Object> getCurrentMarker() { return currentMarker; }
    public void setCurrentMarker(Marker<Object> currentMarker) { this.currentMarker = currentMarker; }

    public Overlay<Object> getOverlay() { return overlay; }
    public void setOverlay(Overlay<Object> overlay) { this.overlay = overlay; }

    public String ViewModificationDate() {
        try {
            String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
            Path path = Paths.get(settings.getProperty("WEBAPP") + viewId);
            Instant instant = Files.getLastModifiedTime(path).toInstant();
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
            return " <b>Last modification :</b> " + ZDF_TIME.format(ldt);
        } catch (Exception ex) {
            String msg = " <br/>££ Exception in ViewModificationDate() " + ex;
            LOG.error(msg);
            return msg;
        }
    } // end method

    public List<SelectItem> ListGameType() {
        var data = Round.GameType.values();
        int le = data.length;
        var items = new SelectItem[le + 1];
        items[0] = new SelectItem("", "Select All Games");
        for (int i = 0; i < le; i++) {
            items[i + 1] = new SelectItem(data[i]);
        }
        return Arrays.asList(items);
    } // end method

} // end class
