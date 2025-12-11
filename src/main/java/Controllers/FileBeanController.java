
package Controllers;

import static interfaces.Log.LOG;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import utils.LCUtil;
@Named("fileBeanC") // this qualifier  makes a bean EL-injectable (Expression Language)
@SessionScoped
public class FileBeanController implements Serializable{
    
    // ne fontionne pas !!
    
    private StreamedContent file;
//https://stackoverflow.com/questions/16093527/primefaces-file-download-not-working
 //@PostConstruct  
// public void FileDownload() {
 public StreamedContent FileDownload() {
     try{
        LOG.debug("entering FileDownLoad of FileBean");
        LOG.debug("line 00");
        ClassLoader clo = Thread.currentThread().getContextClassLoader();
 //   LOG.debug("ClassLoader clo = " + clo);
  // files sous /src/main/resources/
  LOG.debug("clo name= " + clo.getName());
  InputStream is = clo.getResourceAsStream("c://Users//Collet//Documents//yourfile.pdf");
   LOG.debug("inputstream  = " + is.toString());
  
   //     InputStream stream = this.getClass().getResourceAsStream("c:\\Users\\Collet\\Documents\\yourfile.pdf");
  //      file = new DefaultStreamedContent(stream, "application/pdf", "downloaded_file.pdf");
        
  //      LOG.debug("file length = " + file.getContentLength());
        
  //      LOG.debug("stream = " + stream.toString());
            file = DefaultStreamedContent.builder()
                .name("new downloaded_LC.jpg")
    //https://developer.mozilla.org/fr/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
                    //application/pdf
                .contentType("image/jpg")
                .stream(() -> FacesContext.getCurrentInstance()
                        .getExternalContext()
                     .getResourceAsStream("C:/golf_image.jpg"))
                .build();
            LOG.debug("line 01");
            LOG.debug("file to download = " + file.getName());
             LOG.debug("file length = " + file.getContentLength());
            return file;
    }catch(Exception e){
            LOG.debug("Exception FileDownload LC = " + e.getMessage() );
            return null;
      }    
    }
    public StreamedContent getFile() {
        LOG.debug("entering getFile");
        return FileDownload();
 //       LOG.debug("file transfered to user = " + this.file);
 //       return this.file;
    }
    
public StreamedContent getDownloadValue() throws Exception {
//https://stackoverflow.com/questions/16093527/primefaces-file-download-not-working
//   <p:fileDownload value="#{filemanagement.downloadValue}" />
LOG.debug("entering getDownloadValue");

//InputStream stream = this.getClass().
//      getResourceAsStream("/chapter7/PFSamplePDF.pdf");
//    file = new DefaultStreamedContent(stream,
//      "application/pdf", "PFSample.pdf");



    StreamedContent download = new DefaultStreamedContent();
    File file2 = new File("C:\\file.csv");
    LOG.debug("file.csv found");
    InputStream input = new FileInputStream(file2);
    LOG.debug("line 01");
    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
  //  download = new DefaultStreamedContent(input, externalContext.getMimeType(file.getName()), file.getName()));
    LOG.debug("PREP = " + download.getName());
    return download;
}
/*
    public StreamedContent downloadFileTemplate() {
    try {
        //    // <p:fileDownload value="#{bean.downloadFileTemplate()}" />
        FileInputStream inputStream = new FileInputStream(new File(getClass().getClassLoader()
                       .getResource("path_to_resource/myfile.xlsx").getFile()));

        StreamedContent fileTemplate = new DefaultStreamedContent(
                inputStream
                , "application/vnd.ms-excel"
                , "my_file.xlsx");

        return fileTemplate;
    } catch (Exception e) {
        LOG.error("Error on download...", e);
        return null;
    }
} // end method
*/
  void main() throws ParseException {
  try{
     StreamedContent sc = new FileBeanController().FileDownload();
       LOG.debug("after call on download");
        LOG.debug("StreamedContent name = " + sc.getName());
        LOG.debug("StreamedContent type = " + sc.getContentType());
   //     LOG.debug("StreamedContent length = " + sc.getContentLength());
        LOG.debug("StreamedContent stream = " + sc.getStream().toString());
        
       new FileBeanController().getFile();
        LOG.debug("after call on file");
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
   }

   } // end main//
} // end Class
