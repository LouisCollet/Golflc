package Controllers;

import entite.Player;
import entite.Settings;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.annotation.SessionMap;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.util.EscapeUtils;
import utils.LCUtil;

@Named("fileUploadC")
@RequestScoped
public class FileUploadController implements Serializable{
@Inject
@SessionMap
private Map<String, Object> sessionMap;
private String dropZoneText = "Drop zone p:inputTextarea demo.";
//@Inject
//@ApplicationMap 
//private Map<String, Object> applicationMap;

public String upload(UploadedFile uploadedFile) throws SQLException {
    LOG.debug("entering upload()");
     Connection conn = null;
try{  
  //  String fileName = file.getFileName();
  //     LOG.debug("fileName = " + fileName);
  //  String contentType = file.getContentType();
        LOG.debug("fileName = " + uploadedFile.getFileName());
        LOG.debug("contentType = " + uploadedFile.getContentType());
      
 ///   int playerid = Integer.parseInt(sessionMap.get("playerid").toString());
    String fileName = uploadedFile.getFileName();
         //   LOG.debug(" uploaded file name= " + file) ;
  //          LOG.debug(" uploaded file content extension = " + file.getContentType() ); // basé sur extension !
   //         LOG.debug(" uploaded file size = " + file.getSize() ); 
   //     LOG.debug("fName = " + fileName);
     fileName = new String(fileName.getBytes(),StandardCharsets.UTF_8);
        LOG.debug("string name converted to UTF8 = " + fileName);
        //  controle Tika
        String tikaMediaType= tikaDetectDocType(uploadedFile.getInputStream());
           LOG.debug("tikaMediaType =  " + tikaMediaType);
        if(! tikaMediaType.equals(uploadedFile.getContentType())){
            String msg = "Forgery detected by tika in Media Type = " + tikaMediaType + " ,Content Type = " + uploadedFile.getContentType();
            LOG.debug(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }
/*Multipurpose Internet Mail Extensions (type MIME)
Type MIME 	Format d'image
image/gif 	images GIF (compression sans perte, remplacé par PNG)
image/jpeg 	images JPEG
image/png 	images PNG
image/svg+xml 	images SVG (images vectorielles)
image/x-icon    favicon
 */       
    if(tikaMediaType.equals("image/jpeg") || tikaMediaType.equals("image/png") || tikaMediaType.equals("image/gif")){
            // ok
        }else{
            LOG.debug("");
            String msg = "Media type not jpeg or png or gif = " + tikaMediaType;
            LOG.debug(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }
    File file = new File(Settings.getProperty("PHOTOS_LIBRARY") + "/" + fileName);
        LOG.debug("Destination photo file = " + file );
  // copy from input to /resources/images/   
        InputStream is = uploadedFile.getInputStream();
            LOG.debug("file to path = " + file.getPath());
        Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOG.debug("file copy successfull" );
    
        if(ThumbnailsController.thumbsPhoto("from upload", file)){
            String msg = "<h1>Succesful upload Photo </h1> for file = " + file.getName();
    //        LOG.debug("new size file3 = " + file3.get");
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
            BufferedImage image = ImageIO.read(file);
                LOG.debug("New width = " + image.getWidth());
                LOG.debug("New wheight = " + image.getHeight());
        }else{
            String msg = "<h1>FAILURE NO upload Photo </h1> for file = " + file.getName();
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
        }
 //   mise à jour entité player
        Player p = new Player();
        int playerid = Integer.parseInt(sessionMap.get("playerid").toString());
        p.setIdplayer(playerid);
        conn = new utils.DBConnection().getConnection();
        p = new read.ReadPlayer().read(p, conn);
        p.setPlayerPhotoLocation(file.getName() );
   //      LOG.debug("photo location updated for player = ! " + p);
 // mise à jour table player     
        if(new update.UpdatePlayerPhotoLocation().updateRecordFromPlayer(p, conn)){
            LOG.debug(" PhotoLocation updated for player = " + p);
        }else{
            // à compléter
        }
    //        LOG.debug("after modify player Photolocation ");
 // mise à jour thumbnails, size 100*100
        if(ThumbnailsController.thumbs("from upload", file)){
            String msg = "<h1>Succesful upload Thumbnail </h1> for file = " + file.getName();
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
            BufferedImage image2 = ImageIO.read(file);
                LOG.debug("Width = " + image2.getWidth());
                LOG.debug("Height = " + image2.getHeight());
            return "welcome.xhtml?faces-redirect=true";
        }else{
            String msg = "<h1>NO NO upload Thumbnail </h1> for file = " + file.getName();
            LOG.debug(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }
    //    LOG.debug("exiting UploadListener ");
}catch (FileAlreadyExistsException ex){
            String msg = "FileAlreadyExistsException,"// + e.getFile().getFileName()
                    + " file NOT uploaded : " + ex.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}catch (IOException ex){
            String msg = "IOException,"// + e.getFile().getFileName()
                    + " file NOT uploaded : " + ex.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
 }catch (Exception ex){
            String msg = "Exception !  = "// + e.getFile().getFileName()
                    + " is NOT uploaded, message = " + ex.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }
finally{
    utils.DBConnection.closeQuietly(conn, null, null, null);
}
} //end method

public void handleFileUpload(FileUploadEvent event) throws SQLException, IOException{
// upload image choisie par player
 ///   UploadedFile uploadedFile = event.getFile();
 ///       LOG.debug("uploaded File Name (event) = " + uploadedFile.getFileName());
 //   final InputStream inputStream = uploadedFile.getInputStream();  // event from the fileuploader-component
       // LOG.debug("InputStream = " + is.toString());
 //   DefaultStreamedContent fileUploaded = DefaultStreamedContent.builder()
 //                       .contentType("image/png")
 //                       .name("inspec_met_picture_north")
 //                       .stream(() -> inputStream)
  //                      .build();
    //  LOG.debug("pictureNorthtoString = " + fileUploaded.toString());
    upload(event.getFile());
}

//  https://tika.apache.org/
 public static String tikaDetectDocType(InputStream stream) throws IOException {
    Detector detector = new DefaultDetector();
    Metadata metadata = new Metadata();
    MediaType mediaType = detector.detect(stream, metadata);
    return mediaType.toString();
}   
 // new 26-11-2024
public void handleFileUploadTextarea(FileUploadEvent event) {
        String jsVal = "PF('textarea').jq.val";
        String fileName = EscapeUtils.forJavaScript(event.getFile().getFileName());
        PrimeFaces.current().executeScript(jsVal + "(" + jsVal + "() + '\\n\\n" + fileName + " uploaded.')");
    }

    public String getDropZoneText() {
        return dropZoneText;
    }

    public void setDropZoneText(String dropZoneText) {
        this.dropZoneText = dropZoneText;
    }
    
    
void main() throws SQLException, Exception{
 //Connection conn = new utils.DBConnection().getConnection();
 /*   Player player = new Player();
    player.setIdplayer(324713);  // 456781 hans corstjens
    Round round = new Round(); 
    round.setIdround(560);
//   LoadClassment ftes = new LoadClassment();
   Classment cl = new LoadClassment().load(player, round, conn);
       LOG.debug("Classment = " + cl.toString());
*/
//utils.DBConnection.closeQuietly(conn, null, null, null);

}// end main

} //end Class