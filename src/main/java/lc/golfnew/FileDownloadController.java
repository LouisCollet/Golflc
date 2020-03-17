
package lc.golfnew;

import static interfaces.Log.LOG;
import java.io.*;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


@Named
@RequestScoped
public class FileDownloadController{
private StreamedContent file = null;
@Named("fileDownloadC")
public void download() throws IOException{
    InputStream is = null;
try{
    LOG.info("starting FileDownloadController()");
    String contentType = "image/jpg";
    String name = "downloaded_optimus.jpg";
     //   is= new FileInputStream(new File("/images/sofa.png"));
    //    is= SerializableSupplier(new File("/images/sofa.png")); 
   // https://stackoverflow.com/questions/59576891/primefaces-8-0-defaultstreamedcontent-builder-stream-asks-for-serializablesu
//	file = new DefaultStreamedContent.Builder.(is, "image/jpg", "downloaded_optimus.jpg");
      DefaultStreamedContent.builder().contentType(contentType).name(name).stream(() -> is).build();
//DefaultStreamedContent.builder().contentType(contentType)..name(name).stream(() -> new FileInputStream(is)).build();
        LOG.info("file downloaded = " + file);
}catch(Exception e){
            LOG.info("IOException upload Exception LC = " + e.getMessage() );
      }
finally
{
    is.close();
}
}

public StreamedContent getFile()
{
    LOG.info("entering getFile");
        return file;
}

         public void setFile(StreamedContent file)
         {                 this.file = file;
         }


} //end class
