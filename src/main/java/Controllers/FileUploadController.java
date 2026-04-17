package Controllers;

import Controller.refact.PlayerController;
import context.ApplicationContext;
import entite.Player;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.annotation.SessionMap;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.util.EscapeUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Controller de gestion des uploads de fichiers
 * ✅ @RequestScoped — sans état entre requêtes
 * ✅ Settings, ThumbnailsController, PlayerController injectés
 * ✅ Connection supprimée — plus de JDBC legacy
 * ✅ Standards CDI : methodName + handleGenericException
 */
@Named("fileUploadC")
@RequestScoped
public class FileUploadController implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Injections CDI
    @Inject private ApplicationContext          appContext;
    @Inject private entite.Settings             settings;
    @Inject private ThumbnailsController        thumbnailsController;   // ✅ plus static
    @Inject private PlayerController            playerController;       // ✅ plus new
    @Inject private update.UpdatePlayerPhotoLocation updatePlayerPhotoLocation; // ✅ plus new

    // @Inject @SessionMap sessionMap — removed 2026-02-28, migrated to appContext

    private String dropZoneText = "Drop zone p:inputTextarea demo.";

    // ========================================
    // UPLOAD — photo joueur
    // ========================================

    public String upload(UploadedFile uploadedFile) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("fileName    = {}", uploadedFile.getFileName());
            LOG.debug("contentType = {}", uploadedFile.getContentType());

            // ✅ security audit 2026-03-19 — file size limit (5 MB max)
            final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
            if (uploadedFile.getSize() > MAX_FILE_SIZE) {
                String msg = "File too large: " + uploadedFile.getSize() + " bytes (max " + MAX_FILE_SIZE + ")";
                LOG.error("- {}", msg);
                showMessageFatal(msg);
                return null;
            }

            // ✅ Conversion UTF-8
            String fileName = new String(
                    uploadedFile.getFileName().getBytes(), StandardCharsets.UTF_8);
            LOG.debug("fileName UTF-8 = {}", fileName);

            // ✅ Contrôle Tika — détection réelle du type MIME
            String tikaMediaType = tikaDetectDocType(uploadedFile.getInputStream());
            LOG.debug("tikaMediaType = {}", tikaMediaType);

            if (!tikaMediaType.equals(uploadedFile.getContentType())) {
                String msg = "Forgery detected by Tika — tikaMediaType = "
                        + tikaMediaType + " , contentType = " + uploadedFile.getContentType();
                LOG.error("- {}", msg);
                showMessageFatal(msg);
                return null;
            }

            // ✅ Vérification type image
            if (!tikaMediaType.equals("image/jpeg")
                    && !tikaMediaType.equals("image/png")
                    && !tikaMediaType.equals("image/gif")) {
                String msg = "Media type not jpeg/png/gif = " + tikaMediaType;
                LOG.error("- {}", msg);
                showMessageFatal(msg);
                return null;
            }

            // ✅ Security: sanitize filename — prevent path traversal
            String safeName = new File(fileName).getName();  // strips ../  ..\ etc.
            if (safeName.isBlank() || safeName.startsWith(".")) {
                showMessageFatal("Invalid file name");
                return null;
            }

            // ✅ Copie vers PHOTOS_LIBRARY avec vérification canonical path
            java.nio.file.Path basePath = java.nio.file.Path.of(
                    settings.getProperty("PHOTOS_LIBRARY")).toAbsolutePath().normalize();
            java.nio.file.Path targetPath = basePath.resolve(safeName).normalize();

            if (!targetPath.startsWith(basePath)) {
                LOG.error("path traversal attempt: {}", fileName);
                showMessageFatal("Invalid file path");
                return null;
            }

            File file = targetPath.toFile();
            LOG.debug("destination file = {}", file);

            try (InputStream is = uploadedFile.getInputStream()) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                LOG.debug("file copy successful");
            }

            // ✅ Génération thumbnail photo 200x200
            if (thumbnailsController.thumbsPhoto("from upload", file)) {
                String msg = "Successful upload Photo for file = " + file.getName();
                LOG.debug("- {}", msg);
                showMessageInfo(msg);
                BufferedImage image = ImageIO.read(file);
                LOG.debug("width  = {}", image.getWidth());
                LOG.debug("height = {}", image.getHeight());
            } else {
                String msg = "FAILURE upload Photo for file = " + file.getName();
                LOG.error("- {}", msg);
                showMessageFatal(msg);
            }

            // ✅ Mise à jour Player — via injection CDI
            int playerId = appContext.getPlayer().getIdplayer();
            playerController.loadPlayer(playerId);
            Player p = appContext.getPlayer();
            p.setPlayerPhotoLocation(file.getName());
            LOG.debug("photo location updated for player = {}", p);

            // ✅ Mise à jour BDD — via injection CDI, sans Connection
            updatePlayerPhotoLocation.updateRecordFromPlayer(p);
            LOG.debug("PhotoLocation updated in DB");

            // ✅ Génération thumbnail 100x100
            if (thumbnailsController.thumbs("from upload", file)) {
                String msg = "Successful upload Thumbnail for file = " + file.getName();
                LOG.debug("- {}", msg);
                showMessageInfo(msg);
                BufferedImage image2 = ImageIO.read(file);
                LOG.debug("width  = {}", image2.getWidth());
                LOG.debug("height = {}", image2.getHeight());
                return "welcome.xhtml?faces-redirect=true";
            } else {
                String msg = "FAILURE upload Thumbnail for file = " + file.getName();
                LOG.error("- {}", msg);
                showMessageFatal(msg);
                return null;
            }

        } catch (FileAlreadyExistsException e) {
            handleGenericException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // HANDLE FILE UPLOAD — événement PrimeFaces
    // ========================================

    public void handleFileUpload(FileUploadEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            upload(event.getFile());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // HANDLE FILE UPLOAD — textarea
    // ========================================

    public void handleFileUploadTextarea(FileUploadEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String jsVal    = "PF('textarea').jq.val";
            String fileName = EscapeUtils.forJavaScript(event.getFile().getFileName());
            PrimeFaces.current().executeScript(
                    jsVal + "(" + jsVal + "() + '\\n\\n" + fileName + " uploaded.')");
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // TIKA — détection réelle du type MIME
    // ========================================

    public String tikaDetectDocType(InputStream stream) throws IOException { // migrated from static 2026-03-22
        Detector  detector  = new DefaultDetector();
        Metadata  metadata  = new Metadata();
        MediaType mediaType = detector.detect(stream, metadata);
        return mediaType.toString();
    } // end method

    // ========================================
    // GETTERS / SETTERS
    // ========================================

    public String getDropZoneText()                    { return dropZoneText; }
    public void   setDropZoneText(String dropZoneText) { this.dropZoneText = dropZoneText; }

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

    /*
    void main() throws Exception {
        // tests locaux
    } // end main
    */

} // end class

/*
import Controller.refact.PlayerController;
import context.ApplicationContext;
import entite.Player;
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

import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@Named("fileUploadC")
@RequestScoped
public class FileUploadController implements Serializable{
    
    // ✅ Injection du contexte de session
    @Inject private ApplicationContext appContext;
    @Inject private entite.Settings settings;        // ✅ injection CDI
    
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
  //     LOG.debug("fileName = {}", fileName);
  //  String contentType = file.getContentType();
        LOG.debug("fileName = {}", uploadedFile.getFileName());
        LOG.debug("contentType = {}", uploadedFile.getContentType());
      
 ///   int playerid = Integer.parseInt(sessionMap.get("playerid").toString());
    String fileName = uploadedFile.getFileName();
         //   LOG.debug(" uploaded file name= " + file) ;
  //          LOG.debug(" uploaded file content extension = {}", file.getContentType()); // basé sur extension !
   //         LOG.debug(" uploaded file size = {}", file.getSize()); 
   //     LOG.debug("fName = {}", fileName);
     fileName = new String(fileName.getBytes(),StandardCharsets.UTF_8);
        LOG.debug("string name converted to UTF8 = {}", fileName);
        //  controle Tika
        String tikaMediaType= tikaDetectDocType(uploadedFile.getInputStream());
           LOG.debug("tikaMediaType =  {}", tikaMediaType);
        if(! tikaMediaType.equals(uploadedFile.getContentType())){
            String msg = "Forgery detected by tika in Media Type = " + tikaMediaType + " ,Content Type = " + uploadedFile.getContentType();
            LOG.debug(msg);
            showMessageFatal(msg);
            return null;
        }
/*Multipurpose Internet Mail Extensions (type MIME)
Type MIME 	Format d'image
image/gif 	images GIF (compression sans perte, remplacé par PNG)
image/jpeg 	images JPEG
image/png 	images PNG
image/svg+xml 	images SVG (images vectorielles)
image/x-icon    favicon
       
    if(tikaMediaType.equals("image/jpeg") || tikaMediaType.equals("image/png") || tikaMediaType.equals("image/gif")){
            // ok
        }else{
            LOG.debug("");
            String msg = "Media type not jpeg or png or gif = " + tikaMediaType;
            LOG.debug(msg);
            showMessageFatal(msg);
            return null;
        }
    File file = new File(settings.getProperty("PHOTOS_LIBRARY") + "/" + fileName);
        LOG.debug("Destination photo file = {}", file);
  // copy from input to /resources/images/   
        InputStream is = uploadedFile.getInputStream();
            LOG.debug("file to path = {}", file.getPath());
        Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOG.debug("file copy successfull" );
    
        if(ThumbnailsController.thumbsPhoto("from upload", file)){
            String msg = "<h1>Succesful upload Photo </h1> for file = " + file.getName();
    //        LOG.debug("new size file3 = " + file3.get");
            LOG.debug(msg);
            showMessageInfo(msg);
            BufferedImage image = ImageIO.read(file);
                LOG.debug("New width = {}", image.getWidth());
                LOG.debug("New wheight = {}", image.getHeight());
        }else{
            String msg = "<h1>FAILURE NO upload Photo </h1> for file = " + file.getName();
            LOG.debug(msg);
            showMessageInfo(msg);
        }
 //   mise à jour entité player
     //   Player p = new Player();
     //   int playerid = Integer.parseInt(sessionMap.get("playerid").toString());
     //   p.setIdplayer(playerid);
     //   conn = new connection_package.DBConnection().getConnection();
      //  p = new read.ReadPlayer().read(p, conn);
      //  p.setPlayerPhotoLocation(file.getName() );
     // inutilement compliqué !!
        // Récupération de l'id player depuis la session mod 12-02-2026 non testé
        int playerid = Integer.parseInt(sessionMap.get("playerid").toString());
        // Création et initialisation du PlayerController
        PlayerController playerC = new PlayerController();
  ///  à modfier !!!      playerC.init(); // initialise tous les objets internes
        // Chargement du Player via le PlayerController
        playerC.loadPlayer(playerid);
        Player p = appContext.getPlayer();

        // Mise à jour de la photo
       p.setPlayerPhotoLocation(file.getName());

        
   //      LOG.debug("photo location updated for player = ! {}", p);
 // mise à jour table player     
        if(new update.UpdatePlayerPhotoLocation().updateRecordFromPlayer(p, conn)){
            LOG.debug(" PhotoLocation updated for player = {}", p);
        }else{
            // à compléter
        }
    //        LOG.debug("after modify player Photolocation ");
 // mise à jour thumbnails, size 100*100
        if(ThumbnailsController.thumbs("from upload", file)){
            String msg = "<h1>Succesful upload Thumbnail </h1> for file = " + file.getName();
            LOG.debug(msg);
            showMessageInfo(msg);
            BufferedImage image2 = ImageIO.read(file);
                LOG.debug("Width = {}", image2.getWidth());
                LOG.debug("Height = {}", image2.getHeight());
            return "welcome.xhtml?faces-redirect=true";
        }else{
            String msg = "<h1>NO NO upload Thumbnail </h1> for file = " + file.getName();
            LOG.debug(msg);
            showMessageFatal(msg);
            return null;
        }
    //    LOG.debug("exiting UploadListener ");
}catch (FileAlreadyExistsException ex){
            String msg = "FileAlreadyExistsException,"// + e.getFile().getFileName()
                    + " file NOT uploaded : " + ex.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}catch (IOException ex){
            String msg = "IOException,"// + e.getFile().getFileName()
                    + " file NOT uploaded : " + ex.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
 }catch (Exception ex){
            String msg = "Exception !  = "// + e.getFile().getFileName()
                    + " is NOT uploaded, message = " + ex.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
finally{
    connection_package.DBConnection.closeQuietly(conn, null, null, null);
}
} //end method

public void handleFileUpload(FileUploadEvent event) throws SQLException, IOException{
// upload image choisie par player
 ///   UploadedFile uploadedFile = event.getFile();
 ///       LOG.debug("uploaded File Name (event) = {}", uploadedFile.getFileName());
 //   final InputStream inputStream = uploadedFile.getInputStream();  // event from the fileuploader-component
       // LOG.debug("InputStream = {}", is.toString());
 //   DefaultStreamedContent fileUploaded = DefaultStreamedContent.builder()
 //                       .contentType("image/png")
 //                       .name("inspec_met_picture_north")
 //                       .stream(() -> inputStream)
  //                      .build();
    //  LOG.debug("pictureNorthtoString = {}", fileUploaded.toString());
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
       LOG.debug("Classment = {}", cl.toString());

//utils.DBConnection.closeQuietly(conn, null, null, null);

}// end main

} //end Class
*/