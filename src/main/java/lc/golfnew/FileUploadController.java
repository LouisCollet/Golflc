package lc.golfnew;

import entite.Player;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import javax.enterprise.context.SessionScoped;
import javax.imageio.ImageIO;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import utils.LCUtil;

@Named("fileUploadC")
@SessionScoped
//@RequestScoped
public class FileUploadController implements Serializable, interfaces.Log, interfaces.GolfInterface{

public void uploadListener(FileUploadEvent event, Player player, Connection conn){
try{
     UploadedFile uploadedFile; 
        LOG.info("starting uploadListener for player = " + player);
        LOG.info("Uploadevent = " + event);

        uploadedFile = event.getFile();
            LOG.info(" file name= " + uploadedFile.getFileName()) ;
            LOG.info(" file content type= " + uploadedFile.getContentType() ); // basé sur extension !
            LOG.info(" file size = " + uploadedFile.getSize() ); 

     String name = event.getFile().getFileName();
        LOG.info("string name = " + name);
     name = new String(name.getBytes(),StandardCharsets.UTF_8);
        LOG.info("string name converted to UTF8 = " + name);
        
        // a faire intégrer controle Tika
        
        
        File file3 = new File(Constants.photos_library + "/" + name);
              LOG.info("Destination photo file = " + file3 );
  // copy from input to /resources/images/   
        InputStream is = uploadedFile.getInputStream();
        Files.copy(is, file3.toPath(), StandardCopyOption.REPLACE_EXISTING);
          LOG.info("after file copy" );
    
        if(ThumbnailsController.thumbsPhoto("from upload", file3)){
            String msg = "<h1>Succesful upload Photo </h1> for file = " + file3.getName();
    //        LOG.info("new size file3 = " + file3.get");
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            BufferedImage image = ImageIO.read(file3);
                LOG.info("New width = " + image.getWidth());
                LOG.info("New wheight = " + image.getHeight());
        }else{
            String msg = "<h1>FAILURE NO upload Photo </h1> for file = " + file3.getName();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
        }
 //   mise à jour entité player
        player.setPlayerPhotoLocation(file3.getName() );
         LOG.info("file uploaded , player = ! " + player.toString());
 // mise à jour table player     
        new modify.ModifyPlayerPhotoLocation().updateRecordFromPlayer(player, conn);
    //        LOG.info("after modify player Photolocation ");
 // mise à jour thumbnails, size 100*100
     //   b = ThumbnailsController.thumbs("from upload", file3);
        if(ThumbnailsController.thumbs("from upload", file3)){
            String msg = "<h1>Succesful upload Thumbnail </h1> for file = " + file3.getName();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            BufferedImage image2 = ImageIO.read(file3);
                LOG.info("Width = " + image2.getWidth());
                LOG.info("Height = " + image2.getHeight());
        }else{
            String msg = "<h1>NO NO upload Thumbnail </h1> for file = " + file3.getName();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
        }
    //    LOG.info("exiting UploadListener ");
}catch (FileAlreadyExistsException ex){
            String msg = "FileAlreadyExistsException,"// + e.getFile().getFileName()
                    + " file NOT uploaded : " + ex.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
}catch (IOException ex){
            String msg = "IOException,"// + e.getFile().getFileName()
                    + " file NOT uploaded : " + ex.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
 }catch (Exception ex){
            String msg = "Exception !  = "// + e.getFile().getFileName()
                    + " is NOT uploaded, message = " + ex.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        }
} //end method
} //end Class