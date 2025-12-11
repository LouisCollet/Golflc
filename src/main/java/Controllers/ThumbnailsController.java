
package Controllers;

import entite.Settings;
import exceptions.ExceptionGolfLC;
import static interfaces.Log.LOG;
import java.io.File;
import java.io.IOException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import utils.LCUtil;

@Named("thumbnailsC")
//@SessionScoped mod 14-03-2021
@RequestScoped
public class ThumbnailsController {

    public ThumbnailsController(){
  //  constructor
}

public static String thumbs(String s) throws ExceptionGolfLC, IOException{
    // all files from a directory
    //https://github.com/coobird/thumbnailator/wiki/Examples
try{
        LOG.debug("... entering thumbs all files !! with param = " + s);
     File SOURCEDIR = new File(Settings.getProperty("PHOTOS_LIBRARY"));
        LOG.debug("source dir = " + SOURCEDIR);
     File DESTINATIONDIR = new File(Settings.getProperty("THUMBNAILS_LIBRARY"));
        LOG.debug("destination dir = " + DESTINATIONDIR);
    Thumbnails.of(SOURCEDIR.listFiles())
        .scale(0.30)
        .outputFormat("jpg")
        .toFiles(DESTINATIONDIR, Rename.PREFIX_DOT_THUMBNAIL); // ajoute "thumbnail" au début du file name
    LOG.debug(" finishing thumbsAll ...");
    return "menu.xhtml";
}catch(Exception ex){
    String msg = "IOException in thumbs() " + ex;
    LOG.error(" error = " + msg);
    LCUtil.showMessageFatal(msg);
    return "error";
   //         throw new ExceptionGolfLC(ioex);
} finally {    }
} //end method

// les 2 méthodes portent le même nom mais la signature est différente !!
public static boolean thumbs(String s, File f) throws ExceptionGolfLC, IOException{
    //just for one file - 2e constructor
try{
        LOG.debug("... entering thumbs One Thumbnail in One file !! with param = " + s +" for file = " + f.getName());
     File DESTINATIONDIR = new File(Settings.getProperty("THUMBNAILS_LIBRARY"));
      Thumbnails.of(Settings.getProperty("PHOTOS_LIBRARY") + f.getName())        
        .size(100,100)
        .toFiles(DESTINATIONDIR, Rename.PREFIX_DOT_THUMBNAIL); // ajoute "thumbnail" au début du file name
//    LOG.debug(" finishing thumbsOne ...");
    return true;
}catch(IOException ioex){
    String msg = "IOException in thumbs() : " + ioex;
    LOG.error(" error = " + msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch(Exception ex){
    String msg = "Exception in thumbs() : " + ex;
    LOG.error(" error = " + msg);
    LCUtil.showMessageFatal(msg);
    return false;
} finally {    }
} //end method

public static boolean thumbsPhoto(String s, File f) throws IOException{
    //just for one file - 2e constructor
try{

     LOG.debug("... entering thumbs for one file !! with param = " + s +" for file = " + f.getAbsoluteFile());
   //       File SOURCEDIR = new File(Settings.getPHOTOS_LIBRARY());
     //    new 27-11-2021 
           File SOURCEDIR = new File(Settings.getProperty("PHOTOS_LIBRARY"));
    //      File DESTINATIONDIR = new File(Settings.getTHUMBNAILS_LIBRARY());
   //       new 27-11-2021
      File DESTINATIONDIR = new File(Settings.getProperty("THUMBNAILS_LIBRARY"));
     LOG.debug("source dir = " + SOURCEDIR);
     LOG.debug("destination dir = " + DESTINATIONDIR);
 //    Thumbnails.of(Settings.getPHOTOS_LIBRARY() + f.getName())
         // new 27-11-2021    
      Thumbnails.of(Settings.getProperty("PHOTOS_LIBRARY") + f.getName())       
     //   .scale(0.30)  // fonctionne !! was 0.25
        .size(200,200)
 //       .outputFormat("jpg") // mod 03/08/2017 change le format en jpg et ajout l'extension '.jpg'
         .toFiles(SOURCEDIR, Rename.NO_CHANGE); // conserve le nom du fichier
   //     .toFiles(DESTINATIONDIR, Rename.NO_CHANGE); 
    LOG.debug(" finishing thumbsPhoto" );
    return true;
}catch(IOException ioex){
    String msg = "IOException in thumbs() : " + ioex;
    LOG.error(" error = " + msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch(Exception ex){
    String msg = "Exception in thumbs() : " + ex;
    LOG.error(" error = " + msg);
    LCUtil.showMessageFatal(msg);
    return false;
} finally {    }

} //end method
void main() throws ExceptionGolfLC, IOException  {
      thumbs("test");
//      thumbs("test", "f");
  }
} //end class
