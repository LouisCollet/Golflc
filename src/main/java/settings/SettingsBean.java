
package settings;

import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Bean JSF exposant les settings pour l'interface.
 * Se met automatiquement à jour dès que le fichier golflc_settings.properties change.
 */
@Named("settingsBean")
//@ApplicationScoped


@ViewScoped
public class SettingsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, String> settingsSnapshot;
    private int reloadCount;
    private LocalDateTime lastReload;

    @PostConstruct
    public void init() {
        Settings.init();
        reload();
    }

    public void reload() {
        Settings.init();
        settingsSnapshot = Settings.snapshot();
        reloadCount++;
        lastReload = LocalDateTime.now();
    }

    public Map<String, String> getSettingsSnapshot() {
        return settingsSnapshot;
    }

    public int getReloadCount() {
        return reloadCount;
    }

    public LocalDateTime getLastReload() {
        return lastReload;
    }
}