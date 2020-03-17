package lc.golfnew;

import static interfaces.Log.LOG;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named
@RequestScoped
public class FileDownloadView{
 
 private StreamedContent file;
 
 public void download() {
 try{
     LOG.info("entering FileDownloadView");
        file = DefaultStreamedContent.builder()
                .name("downloaded_boromir.jpg")
                .contentType("image/jpg")
                .stream(() -> FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getResourceAsStream("/resources/demo/images/boromir.jpg"))
                .build();
}catch(Exception e){
            LOG.info("Download Exception LC = " + e.getMessage() );
      }
  } //end method
 
    public StreamedContent getFile() {
        return file;
    }
} // end Class
