/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lc.golfnew;

import static interfaces.Log.LOG;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import utils.LCUtil;
@Named("fileBeanC") // this qualifier  makes a bean EL-injectable (Expression Language)
@SessionScoped
public class FileBean implements Serializable{
    
    // ne fontionne pas !!
    
    private StreamedContent file;
//https://stackoverflow.com/questions/16093527/primefaces-file-download-not-working
 //@PostConstruct  
// public void FileDownload() {
 public StreamedContent FileDownload() {
     try{
        LOG.info("entering FileDownLoad of FileBean");
        LOG.info("line 00");
        ClassLoader clo = Thread.currentThread().getContextClassLoader();
 //   LOG.info("ClassLoader clo = " + clo);
  // files sous /src/main/resources/
  LOG.info("clo name= " + clo.getName());
  InputStream is = clo.getResourceAsStream("c://Users//Collet//Documents//yourfile.pdf");
   LOG.info("inputstream  = " + is.toString());
  
   //     InputStream stream = this.getClass().getResourceAsStream("c:\\Users\\Collet\\Documents\\yourfile.pdf");
  //      file = new DefaultStreamedContent(stream, "application/pdf", "downloaded_file.pdf");
        
  //      LOG.info("file length = " + file.getContentLength());
        
  //      LOG.info("stream = " + stream.toString());
            file = DefaultStreamedContent.builder()
                .name("new downloaded_LC.jpg")
    //https://developer.mozilla.org/fr/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
                    //application/pdf
                .contentType("image/jpg")
                .stream(() -> FacesContext.getCurrentInstance()
                        .getExternalContext()
               //         .getResourceAsStream(Constants.images_library + "activate1.jpg"))
                     .getResourceAsStream("C:/golf_image.jpg"))
                .build();
            LOG.info("line 01");
            LOG.info("file to download = " + file.getName());
             LOG.info("file length = " + file.getContentLength());
            return file;
    }catch(Exception e){
            LOG.info("Exception FileDownload LC = " + e.getMessage() );
            return null;
      }    
    }
    public StreamedContent getFile() {
        LOG.info("entering getFile");
        return FileDownload();
 //       LOG.info("file transfered to user = " + this.file);
 //       return this.file;
    }
    
public StreamedContent getDownloadValue() throws Exception {
//https://stackoverflow.com/questions/16093527/primefaces-file-download-not-working
//   <p:fileDownload value="#{filemanagement.downloadValue}" />
LOG.info("entering getDownloadValue");

//InputStream stream = this.getClass().
//      getResourceAsStream("/chapter7/PFSamplePDF.pdf");
//    file = new DefaultStreamedContent(stream,
//      "application/pdf", "PFSample.pdf");



    StreamedContent download = new DefaultStreamedContent();
    File file2 = new File("C:\\file.csv");
    LOG.info("file.csv found");
    InputStream input = new FileInputStream(file2);
    LOG.info("line 01");
    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
  //  download = new DefaultStreamedContent(input, externalContext.getMimeType(file.getName()), file.getName()));
    LOG.info("PREP = " + download.getName());
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
  public static void main(String[] args) throws ParseException {
  try{
     StreamedContent sc = new FileBean().FileDownload();
       LOG.info("after call on download");
        LOG.info("StreamedContent name = " + sc.getName());
        LOG.info("StreamedContent type = " + sc.getContentType());
   //     LOG.info("StreamedContent length = " + sc.getContentLength());
        LOG.info("StreamedContent stream = " + sc.getStream().toString());
        
       new FileBean().getFile();
        LOG.info("after call on file");
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
   }

   } // end main//
} // end Class
