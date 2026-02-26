
package utils;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Paths;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.nio.file.Path;
@Named("imageView")
@ApplicationScoped
public class ImageView implements Serializable {          // ✅ Serializable ajouté

    private static final long serialVersionUID = 1L;

    @Inject private entite.Settings settings;

    public byte[] getThumbnail(String photo) {            // ✅ throws supprimé
        final String methodName = utils.LCUtil.getCurrentMethodName();
     //   LOG.debug("entering " + methodName + " - photo = " + photo);
        try {
            Path path = Paths.get(settings.getProperty("THUMBNAILS_LIBRARY") + "thumbnail." + photo);
            return Files.readAllBytes(path);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public byte[] getPhoto(String photo) {                // ✅ throws supprimé
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - photo = " + photo);
        try {
            Path path = Paths.get(settings.getProperty("PHOTOS_LIBRARY") + photo);
            return Files.readAllBytes(path);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

} // end class

/*
//import entite.Settings;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.nio.file.Path;

@Named("imageView")
@RequestScoped
public class ImageView{
@Inject private entite.Settings settings;        // ✅ injection CDI
    /*
The Files.readAllBytes() is the best method for using Java 7, 8 and above.
It reads all bytes from a file and closes the file.
The file is also closed on an I/O error or another runtime exception is thrown.
This method read all bytes into memory in a single statement so do not use it to read large files, else you may face OutOfMemoryError.
    
public byte[] getThumbnail(String photo) throws IOException {
  try{
 //    LOG.debug("entering getThumbnail for = " + photo); afficher une photo
// but = afficher immédiatement après modification, sans attendre le prochain build !
   Path path = Paths.get(settings.getProperty("THUMBNAILS_LIBRARY") + "thumbnail." + photo);// converts string to path  
 //     LOG.debug("path NameCount = " + path.getNameCount());
   return Files.readAllBytes(path);
  }catch(Exception e){
        String msg = "£££ Exception in ImageView.getThumbnail = " + e.getMessage();
        LOG.error(msg);
    //    LCUtil.showMessageFatal(msg);
        return null;
    }
} // end method

  public byte[] getPhoto(String photo) throws IOException {
  try{
      //LOG.debug("entering getPhoto for = " + photo);
   //  String fileName = Settings.getProperty("PHOTOS_LIBRARY") + photo;
     Path path = Paths.get(settings.getProperty("PHOTOS_LIBRARY")+ photo);// converts string to path  
      // LOG.debug("file name = " + fileName);
  //   return Files.readAllBytes(Paths.get(fileName));
     return Files.readAllBytes(path);
 }catch(Exception e){
        String msg = "£££ Exception in ImageView-getPhoto = " + e.getMessage();
        LOG.error(msg);
        return null;
    }
  } //end method 

} // end class
*/