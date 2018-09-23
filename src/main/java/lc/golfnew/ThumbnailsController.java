
package lc.golfnew;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import utils.LCUtil;

@Named("thumbnailsC")
@SessionScoped
//@RequestScoped   //quelle différence ??
public class ThumbnailsController implements Serializable, interfaces.Log
{
final private static File SOURCEDIR = new File(Constants.photos_library);
final private static File DESTINATIONDIR = new File(Constants.thumbnails_library);

public static String thumbs(String s) throws ExceptionGolfLC, IOException
{
    //http://thumbnailator.googlecode.com/hg-history/0.4.8/javadoc/net/coobird/thumbnailator/Thumbnails.html
try{
    boolean b = true;
     LOG.info("... entering thumbs all files !! with param = " + s);
    Thumbnails.of(SOURCEDIR.listFiles())
        .scale(0.30)  // fonctionne !!
        .outputFormat("jpg")
 //       .toFiles(destinationDir, Rename.NO_CHANGE); // conserve le nom de fichier
        .toFiles(DESTINATIONDIR, Rename.PREFIX_DOT_THUMBNAIL); // ajoute "thumbnail" au début du file name
    
    LOG.info(" finishing thumbsAll ...");
    if(b == false) // essai, pas utilisé
    {
        throw new ExceptionGolfLC ("ExceptionGolfLC : Something bad happened");
    }
    return "menu.xhtml";
}catch(ExceptionGolfLC ex){
  // Print error and terminate application.
    String msg = "ExceptionGolfLC() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return "error";
}catch(IOException ioex){
    String msg = "IOException in thumbs() " + ioex;
    LOG.error(" error = " + msg);
    LCUtil.showMessageFatal(msg);
    return "error";
   //         throw new ExceptionGolfLC(ioex);
} finally {    }

} //end method

// les 2 méthodes portent le même nom mais la signature est différente !!
public static boolean thumbs(String s, File f) throws ExceptionGolfLC, IOException
{
    //just for one file - 2e constructor
try{
     LOG.info("... entering thumbs One Thumbnail in One file !! with param = " + s +" for file = " + f.getName());
 //   Thumbnails.of(sourceDir.listFiles())
    Thumbnails.of(Constants.photos_library + f.getName())
     //   .scale(0.30)  // fonctionne !! was 0.25
        .size(100,100)   // mod 04/08/2017 
   //     .outputFormat("jpg") // mod 03/08/2017 change le format en jpg et ajout l'extension '.jpg'
        .toFiles(DESTINATIONDIR, Rename.PREFIX_DOT_THUMBNAIL); // ajoute "thumbnail" au début du file name
    LOG.info(" finishing thumbsOne ...");
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



public static boolean thumbsPhoto(String s, File f) throws ExceptionGolfLC, IOException
{
    //just for one file - 2e constructor
try{
  //   LOG.info("... entering thumbs One Photo in One file !! with param = " + s +" for file = " + f.getName());
     LOG.info("... entering thumbs One Photo in One file !! with param = " + s +" for file = " + f.getAbsoluteFile());
  //   LOG.info("... entering thumbs One Photo in One file !! with param = " + s +" for file = " + f.getAbsolutePath());
  //   LOG.info("... entering thumbs One Photo in One file !! with param = " + s +" for file = " + f.getCanonicalPath());
 
  //  Thumbnails.of(f.getAbsoluteFile())
    Thumbnails.of(Constants.photos_library + f.getName())
     //   .scale(0.30)  // fonctionne !! was 0.25
        .size(200,200)   // mod 04/08/2017 
 //       .outputFormat("jpg") // mod 03/08/2017 change le format en jpg et ajout l'extension '.jpg'
         .toFiles(SOURCEDIR, Rename.NO_CHANGE); // conserve le nom du fichier
   //     .toFiles(DESTINATIONDIR, Rename.NO_CHANGE); 
    LOG.info(" finishing thumbsOnePhoto" );
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
public static void main(String[] args) throws ExceptionGolfLC, IOException
  {
      thumbs("test");
//      thumbs("test", "f");
  }
} //end class
