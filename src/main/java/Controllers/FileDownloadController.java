
package Controllers;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


@Named("fileDownloadC")
@RequestScoped
public class FileDownloadController{
private StreamedContent file = null;

public void download() throws IOException{
    InputStream is = null;
try{
    LOG.debug("starting FileDownloadController()");
    String contentType = "image/jpg";
    String name = "downloaded_optimus.jpg";
    file = DefaultStreamedContent.builder().contentType(contentType).name(name).stream(() -> is).build();
        LOG.debug("file downloaded = {}", file);
}catch(Exception e){
            LOG.debug("IOException upload Exception LC = {}", e.getMessage());
      }
finally
{
    if (is != null) is.close();
}
}

public StreamedContent getFile()
{
    LOG.debug("entering getFile");
        return file;
}

         public void setFile(StreamedContent file)
         {                 this.file = file;
         }


} //end class
