package lc.golfnew;

import entite.Player;
import exceptions.ExceptionGolfLC;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import javax.enterprise.context.SessionScoped;
import javax.imageio.ImageIO;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import utils.LCUtil;

@Named("fileUploadC")
@SessionScoped
public class FileUploadController implements Serializable, interfaces.Log, interfaces.GolfInterface{
    private static UploadedFile uploadedFile; 

public void uploadListener(FileUploadEvent event, Player player, Connection conn){
try{
        LOG.info("starting uploadListener, player = " + player);

        uploadedFile = event.getFile();
            LOG.info(" file name= " + uploadedFile.getFileName()) ;
            LOG.info(" file content type= " + uploadedFile.getContentType() ) ;
            LOG.info(" file size = " + uploadedFile.getSize() ); 

        File file3 = new File(Constants.photos_library + "/" + uploadedFile.getFileName());
              LOG.info("Destination photo file = " + file3 );
  // copy from input to /resources/images/   
        InputStream is = uploadedFile.getInputstream();
        Files.copy(is, file3.toPath(), StandardCopyOption.REPLACE_EXISTING);
  //          LOG.info("after file copy" );
  // resize to 200*200
    //    boolean b = ThumbnailsController.thumbsPhoto("from upload", file3); // new 06/08/2017
        if(ThumbnailsController.thumbsPhoto("from upload", file3)){
            String msg = "<h1>Succesful upload Photo </h1> for file = " + file3.getName();
    //        LOG.info("new size file3 = " + file3.get");
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            BufferedImage image = ImageIO.read(file3);
                LOG.info("New width = " + image.getWidth());
                LOG.info("New wheight = " + image.getHeight());
        }else{
            String msg = "<h1>FAILIURE NO upload Photo </h1> for file = " + file3.getName();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
        }
 //   mise à jour entité player
        player.setPlayerPhotoLocation(file3.getName() );
         LOG.info("file uploaded , player = ! " + player.toString());
 // mise à jour table player     
    //    modify.ModifyPlayerPhotoLocation mppl = new modify.ModifyPlayerPhotoLocation();
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
 }catch (SQLException | ExceptionGolfLC ex){
            String msg = "Exception !  = "// + e.getFile().getFileName()
                    + " is NOT uploaded, message = " + ex.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        }
} //end method

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        FileUploadController.uploadedFile = uploadedFile;
    }

//    public Upload getUpload() {
//        return upload;
//    }

 //   public void setUpload(Upload upload) {
 //       this.upload = upload;
 //   }
 
} //end Class