package Controllers;

//mod 24-08-2025 duite version3.0.0 import com.lowagie.text.BadElementException;
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
import org.primefaces.model.map.Circle;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Overlay;

@Named("utilsC")
//@SessionScoped
@RequestScoped

public class UtilsController implements Serializable, interfaces.GolfInterface, interfaces.Log{
@Inject private entite.Settings settings;        // ✅ injection CDI
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
 
public UtilsController() throws IOException {// constructor
    // pourquoi la ligne suivante ??
    super();  
        mapModel = new DefaultMapModel<>();  
        mapZoom = 7;  
        mapCenter = "51.5, 10.49";  
        LatLng latlng = new LatLng(51.6, 10.4);  
   //     Marker<Object> marker = new Marker<>(latlng, "myMarker");  
 ///  enlevé 05/08/2022     mapModel.addOverlay(marker);  
        Circle<Object> circle = new Circle<>(latlng, 50000);  
        circle.setFillColor("yellow");  
        circle.setFillOpacity(0.3);  
  //  enlevé 05/08/2022    mapModel.addOverlay(circle);  
}

@PostConstruct
 public void init(){
  try{
        customizationOptions();
  //  }

  } catch (Exception ex) {
       LOG.debug("error printResultSet{}", ex);
   }          
 } // end init
 //       }
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
    }        

    public ExcelOptions getExcelOpt() {
        return excelOpt;
    }

    public void setExcelOpt(ExcelOptions excelOpt) {
        this.excelOpt = excelOpt;
    }

    public PDFOptions getPdfOpt() {
        return pdfOpt;
    }

    public void setPdfOpt(PDFOptions pdfOpt) {
        this.pdfOpt = pdfOpt;
    }
        
    public void postProcessXLS(Object document) {
		HSSFWorkbook wb = (HSSFWorkbook) document;
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow header = sheet.getRow(0);

		HSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREEN.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		for(int i=0; i < header.getPhysicalNumberOfCells();i++) {
			HSSFCell cell = header.getCell(i);
			cell.setCellStyle(cellStyle);
		}
	}
/* enlevé 06/03/0206 mismatch entre versions enleve 3.0.0 en faveur 2.2.2
public void preProcessPDF(Object document) throws IOException, BadElementException, DocumentException {
try{
    Document pdf = (Document) document;
        pdf.open();
        pdf.setPageSize(PageSize.A4);
   //     pdf.setPageSize(PageSize.A4.rotate());

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
           LOG.debug("path for logo = {}", externalContext.getRealPath(""));
        String logo = externalContext.getRealPath("") + File.separator + "resources"
                + File.separator + "demo" + File.separator + "images" + File.separator + "prime_logo.png";
	pdf.add(Image.getInstance(logo));
 } catch (Exception ex) {
       LOG.debug("preProcessPDF{}", ex);
 }
} //end preProcessPDF       
*/

// utilisé dans datePicker, yearRange
 public int dateYear(int year){ 
     return LocalDate.now().getYear() + year;
 }
  public int dateMonth(int month){ 
     return LocalDate.now().getMonthValue() + month;
 }
  // utilisé dans datePicker, minDate ou maxDate
  public LocalDate localDateWeek(int week){  
 //         LOG.debug("LocalDate minusweek = {}", LocalDate.now().plusWeeks(week));
          return LocalDate.now().plusWeeks(week);
 }
   // utilisé dans datePicker, minDate ou maxDate
  public LocalDate localDateYear(int year){
      if(year < 0){
          return LocalDate.now().minusYears(year);
      }else{
          return LocalDate.now().plusYears(year);
      }
 } 
  
     // utilisé dans datePicker, minDate ou maxDate
  public LocalTime localTime(int hour, int minute){ // migrated from static 2026-03-22
      return LocalTime.of(hour,minute);
 } 
  
//   public void setFmd(String fmd) {
//        this.fmd = fmd;
//    }

    public LocalTime getMinTime() {
        return minTime;
    }

    public void setMinTime(LocalTime minTime) {
        this.minTime = minTime;
    }

    public LocalTime getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(LocalTime maxTime) {
        this.maxTime = maxTime;
    }
    

public void logFile() throws IOException{
    Runtime runtime = Runtime.getRuntime();
    runtime.exec(new String[] { "C:\\Program Files\\JGsoft\\EditPadLite\\EditPadLite7.exe", "C:\\log\\golflc.log" } );
}
// end method runtime.exec(new String[] { "monappli", "un paramètre avec des espaces", "param2" } );

private int count = 20;
public int getCount()
{
	return count;
}
public void setCount(int count){
	this.count = count;
}
public void increment(){
	count--;  // count down
}

public String getContent() {
        return content;
    }

public void setContent(final String content)
    {
        this.content = content;
    }


public void preProcessPDF(Object document) {
    Document pdf = (Document) document;
    pdf.setPageSize(PageSize.A4.rotate());
    pdf.open();
} // end method

public int getElem(PlayingHandicap playingHcp){ // migrated from static 2026-03-22
    // used in ??
    // calcule le nombre de players (de 1/2 à 4 ?)
    int counter_players = 0;
    Double hcp[] = playingHcp.getHcpScr();
    for (Double hcp1 : hcp) {
        if (hcp1 != 0.0) {
            counter_players ++;
            LOG.debug("Scramble Hcp = {}", hcp1);
        }
    }
            LOG.debug("Scramble Hcp number of players  = {}", counter_players);
            
return counter_players;
}

public void printResultSet(ResultSet rs) throws SQLException{ // migrated from static 2026-03-22
    try{
    ResultSetMetaData rsmd = rs.getMetaData();
    LOG.debug("querying SELECT * FROM XXX");
    int columnsNumber = rsmd.getColumnCount();
    while (rs.next()) {
        for (int i = 1; i <= columnsNumber; i++) {
            if (i > 1) LOG.debug(",  ");
            String columnValue = rs.getString(i);  // à supposer que ce sont tous des string ??
            LOG.debug("{} = {}", rsmd.getColumnName(i), columnValue);
        } //end for
        LOG.debug("");
    } //end while
            LOG.debug("Scramble Hcp number of players  = "  );
  } catch (Exception ex) {
       LOG.debug("error printResultSet{}", ex);
   }          
} // end method

public double getSum(PlayingHandicap playingHcp){   //To find the sum of array elements
          double sum=0;
          Double hcp[] = playingHcp.getHcpScr();
          for(Double i:hcp){
              sum += i;
              LOG.debug("Scramble Hcp - The sum is : {}", sum); 
          }
          LOG.debug("Scramble Hcp - The FINAL sum is : {}", sum); 
return sum;
}

    public MapModel<Object> getMapModel() {
        return mapModel;
    }

    public void setMapModel(MapModel<Object> mapModel) {
        this.mapModel = mapModel;
    }

    public String getMapCenter() {
        return mapCenter;
    }

    public void setMapCenter(String mapCenter) {
        this.mapCenter = mapCenter;
    }

    public int getMapZoom() {
        return mapZoom;
    }

    public void setMapZoom(int mapZoom) {
        this.mapZoom = mapZoom;
    }

    public String getInfoWindowText() {
        return infoWindowText;
    }

    public void setInfoWindowText(String infoWindowText) {
        this.infoWindowText = infoWindowText;
    }

    public Marker<Object> getCurrentMarker() {
        return currentMarker;
    }

    public void setCurrentMarker(Marker<Object> currentMarker) {
        this.currentMarker = currentMarker;
    }

    public Overlay<Object> getOverlay() {
        return overlay;
    }

    public void setOverlay(Overlay<Object> overlay) {
        this.overlay = overlay;
    }
    /*
public void onMarkerSelect(OverlaySelectEvent event){  
        LOG.debug("onMarkerSelect: {}", event.getOverlay().getClass().getName());  
        infoWindowText = "blabla";  
    } 
    */


public String ViewModificationDate() throws IOException{
try{
    String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
  //      LOG.debug("viewId = {}", viewId);
   Path path = Paths.get(settings.getProperty("WEBAPP") + viewId);// converts string to path  
   Instant instant = Files.getLastModifiedTime(path).toInstant();
   LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
   return " <b>Last modification :</b> " + ZDF_TIME.format(ldt);
}catch (Exception ex){
    String msg = " <br/>££ Exception in ViewModificationDate() " + ex;
    LOG.error(msg);
  //  throw new Exception(msg);
 //   LCUtil.showMessageFatal(msg);
    return msg;
}    
} // end method

/* 25-08-2023 moved to Round
  public Round.GameType[] GameType() {
 //     LOG.debug("array as list = {}", Arrays.asList(Round.GameType.values()));
        return Round.GameType.values();
  }
*/
public List<SelectItem> ListGameType() {  // from enum to List used in show_played_rounds filterOptions
      var data = Round.GameType.values();
      int le = data.length;
      var items = new SelectItem[le + 1];
      items[0] = new SelectItem("","Select All Games"); // "" = pour réinitialiser la dropdown list
      for (int i=0; i<le; i++){ 
  //        LOG.debug("items i = {} data = {}", i, data[i]);
            items[i+1] = new SelectItem(data[i]);
      }
return Arrays.asList(items);
   } //end method
    
private static SelectItem[] createFilterOptions() throws Exception{  // not used !!
        int le = Round.GameType.values().length;
        var da = Round.GameType.values();
        SelectItem[] options = new SelectItem[le + 1];
        options[0] = new SelectItem("", "Select All Games");
        for(int i = 0; i < da.length; i++){
            options[i + 1] = new SelectItem("",da[i].toString());
        }
     return options;
    }    
    
   /* 25-08-2024 moved to Tee
    public Round.StartType[] StartType() {
      //  LOG.debug("GameType values = {}", Round.GameType.values());
        return Round.StartType.values();
    }
moved to Player
    public Player.LanguageType[] LanguageType() {
      //  LOG.debug("GameType values = {}", Round.GameType.values());
        return Player.LanguageType.values();
    }
*/

/*
public String CleanHelpFile(String str) throws IOException, GeneralSecurityException{
 try{
       int firstIndex = str.indexOf("<br/>"); 
 //          LOG.debug("firstIndex = {}", firstIndex);
       if(firstIndex == -1){
           String msg = "firstIndex not found : " + firstIndex;
           LOG.debug(msg);
           throw new Exception();
       }
       int lastIndex = str.indexOf("</h:outputText>");
//       LOG.debug("lastIndex = {}", lastIndex);
       if(lastIndex == -1){
           String msg = "lastIndex not found : " + lastIndex;
           LOG.debug(msg);
           throw new Exception();
       }
       str = str.substring(firstIndex+5, lastIndex);
       LOG.debug("substring  = {}", str);
return str;
}catch (Exception ex){
    String msg = " <br/>££ Exception in ViewHelpFileName() " + ex;
    LOG.error(msg);
  //  throw new Exception(msg);
 //   LCUtil.showMessageFatal(msg);
    return msg;
}    
} // end method
*/

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class