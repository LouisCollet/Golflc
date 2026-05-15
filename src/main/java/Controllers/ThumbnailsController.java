package Controllers;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import java.io.File;
import java.io.Serializable;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;

@Named("thumbnailsC")
@ApplicationScoped
public class ThumbnailsController implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Injection CDI — plus de Settings.getProperty() statique
    @Inject private entite.Settings settings;

    public ThumbnailsController() { }

    // ========================================
    // THUMBS — tous les fichiers d'un répertoire
    // ========================================

    /**
     * Génère les thumbnails pour tous les fichiers du répertoire PHOTOS_LIBRARY
     */
    public String thumbs(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering with param = {}", s);
        try {
            File sourceDir      = new File(settings.getProperty("PHOTOS_LIBRARY"));      // ✅ new ajouté
            File destinationDir = new File(settings.getProperty("THUMBNAILS_LIBRARY"));  // ✅ injecté
            LOG.debug("sourceDir      = {}", sourceDir);
            LOG.debug("destinationDir = {}", destinationDir);

            Thumbnails.of(sourceDir.listFiles())
                    .scale(0.30)
                    .outputFormat("jpg")
                    .toFiles(destinationDir, Rename.PREFIX_DOT_THUMBNAIL);

            LOG.debug("finished");
            return "menu.xhtml";

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return "error";
        }
    } // end method

    // ========================================
    // THUMBS — un seul fichier
    // ✅ static → instance (CDI incompatible avec static)
    // ========================================

    /**
     * Génère le thumbnail pour un seul fichier
     */
    public boolean thumbs(String s, File f) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering with param = {}, file = {}", s, f.getName());
        try {
            File destinationDir = new File(settings.getProperty("THUMBNAILS_LIBRARY"));  // ✅ injecté

            Thumbnails.of(settings.getProperty("PHOTOS_LIBRARY") + f.getName())
                    .size(100, 100)
                    .toFiles(destinationDir, Rename.PREFIX_DOT_THUMBNAIL);

            LOG.debug("finished");
            return true;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ========================================
    // THUMBS PHOTO — redimensionnement d'une photo
    // ✅ static → instance (CDI incompatible avec static)
    // ========================================

    /**
     * Redimensionne une photo (200x200) et la sauvegarde dans PHOTOS_LIBRARY
     */
    public boolean thumbsPhoto(String s, File f) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering with param = {}, file = {}", s, f.getAbsoluteFile());
        try {
            File sourceDir      = new File(settings.getProperty("PHOTOS_LIBRARY"));      // ✅ injecté
            File destinationDir = new File(settings.getProperty("THUMBNAILS_LIBRARY"));  // ✅ injecté
            LOG.debug("sourceDir      = {}", sourceDir);
            LOG.debug("destinationDir = {}", destinationDir);

            Thumbnails.of(settings.getProperty("PHOTOS_LIBRARY") + f.getName())
                    .size(200, 200)
                    .toFiles(sourceDir, Rename.NO_CHANGE);

            LOG.debug("finished");
            return true;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method
} // end class