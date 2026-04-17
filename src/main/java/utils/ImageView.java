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
public class ImageView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private entite.Settings settings;

    public byte[] getThumbnail(String photo) {
        return readFile(settings.getProperty("THUMBNAILS_LIBRARY"), "thumbnail." + photo);
    }

    public byte[] getPhoto(String photo) {
        return readFile(settings.getProperty("PHOTOS_LIBRARY"), photo);
    }

    private byte[] readFile(String baseDir, String fileName) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            Path path = Paths.get(baseDir, fileName);
            return Files.readAllBytes(path);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}