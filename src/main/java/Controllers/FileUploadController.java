package Controllers;

import context.ApplicationContext;
import entite.Player;
import jakarta.enterprise.context.RequestScoped;
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
