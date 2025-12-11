package lc.golfnew;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named
@RequestScoped
public class FileDownloadView{
 
 private StreamedContent file;
 
 public void download() {
 try{
     LOG.debug("entering FileDownloadView");
        file = DefaultStreamedContent.builder()
                .name("downloaded_boromir.jpg")
                .contentType("image/jpg")
                .stream(() -> FacesContext
                        .getCurrentInstance()
                        .getExternalContext()
                        .getResourceAsStream("/resources/demo/images/boromir.jpg"))
                .build();
}catch(Exception e){
            LOG.debug("Download Exception LC = " + e.getMessage() );
      }
  } //end method
 
    public StreamedContent getFile() {
        return file;
    }
} // end Class
