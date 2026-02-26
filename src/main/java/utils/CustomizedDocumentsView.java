package utils;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;          // ✅ remplace HSSFColor deprecated
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.primefaces.component.export.ExcelOptions;
import org.primefaces.component.export.PDFOptions;
import org.primefaces.component.export.PDFOrientationType;

import java.io.Serializable;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;

/**
 * Options de personnalisation des exports Excel et PDF (PrimeFaces)
 * ✅ @ApplicationScoped — options fixes, jamais modifiées par l'utilisateur
 * ✅ customizationOptions() privée — appelée uniquement par @PostConstruct
 * ✅ HSSFColor → IndexedColors (plus deprecated)
 * ✅ Standards CDI : methodName + handleGenericException
 */
@Named
@ApplicationScoped
public class CustomizedDocumentsView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private entite.Settings settings;               // ✅ pour preProcessPDF si réactivé

    private ExcelOptions excelOpt;
    private PDFOptions   pdfOpt;

    // ========================================
    // INITIALISATION
    // ========================================

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        customizationOptions();
    } // end method

    // ✅ Privée — appelée uniquement par @PostConstruct
    private void customizationOptions() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

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

        LOG.debug(methodName + " - excelOpt and pdfOpt initialized");
    } // end method

    // ========================================
    // POST PROCESS XLS
    // ========================================

    public void postProcessXLS(Object document) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            SXSSFWorkbook wb     = (SXSSFWorkbook) document;
            SXSSFSheet    sheet  = wb.getSheetAt(0);
            SXSSFRow      header = sheet.getRow(0);

            CellStyle cellStyle = wb.createCellStyle();
            // ✅ IndexedColors remplace HSSFColor deprecated
            cellStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < header.getPhysicalNumberOfCells(); i++) {
                SXSSFCell cell = header.getCell(i);
                cell.setCellStyle(cellStyle);
            }
            LOG.debug(methodName + " - XLS post-processed");

        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // PRE PROCESS PDF — réactivé avec Settings injecté
    // ========================================

    /*
    public void preProcessPDF(Object document) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Document pdf = (Document) document;
            pdf.open();
            pdf.setPageSize(PageSize.A4);
            // ✅ Settings injecté — plus de Settings.getProperty() statique
            String logo = settings.getProperty("IMAGES_LIBRARY") + "golf man drive.jpg";
            LOG.debug(methodName + " - logo = " + logo);
            pdf.add(Image.getInstance(logo));
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method
    */

    // ========================================
    // GETTERS — setters supprimés (jamais appelés)
    // ========================================

    public ExcelOptions getExcelOpt() { return excelOpt; }
    public PDFOptions   getPdfOpt()   { return pdfOpt;   }

} // end class
/*
import static interfaces.Log.LOG;
//import java.io.IOException;
import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.primefaces.component.export.ExcelOptions;
import org.primefaces.component.export.PDFOptions;
import org.primefaces.component.export.PDFOrientationType;

@Named
@RequestScoped
public class CustomizedDocumentsView implements Serializable {

    private ExcelOptions excelOpt;
    private PDFOptions pdfOpt;

    @PostConstruct
    public void init() {
        customizationOptions();
    }

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

 //   public List<Product> getProducts() {
 //       return products;
 //   }

 //   public List<Product> getProducts2() {
 //       return products2;
 //   }

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

 //   public void setService(ProductService service) {
 //       this.service = service;
 //   }

    public void postProcessXLS(Object document) {
        LOG.debug("enteringpostProcessXLS");
        // https://poi.apache.org/components/spreadsheet/converting.html
        SXSSFWorkbook wb = (SXSSFWorkbook) document;
        SXSSFSheet sheet = wb.getSheetAt(0);
        SXSSFRow header = sheet.getRow(0);

        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREEN.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < header.getPhysicalNumberOfCells(); i++) {
            SXSSFCell cell = header.getCell(i);
            cell.setCellStyle(cellStyle);
        }
    } // end method
/* enlevé 14-08-2025 donne deprecation openpdf 2.4.0
	public void preProcessPDF(Object document) throws IOException, BadElementException, DocumentException {
	//Document pdf = (Document) document;
  // https://github.com/LibrePDF/OpenPDF/blob/master/pdf-toolbox/src/test/java/com/lowagie/examples/directcontent/text/Text.java      
        Document pdf = (Document) document;
        pdf.open();
        pdf.setPageSize(PageSize.A4);
        String logo = Settings.getProperty("IMAGES_LIBRARY") + "golf man drive.jpg";
            LOG.debug("logo " + logo);
        pdf.add(Image.getInstance(logo));

} // end method
       
}
*/