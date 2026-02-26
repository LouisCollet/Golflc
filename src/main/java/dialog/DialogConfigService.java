
package dialog;

// ========================================

import jakarta.enterprise.context.ApplicationScoped;
import org.primefaces.model.DialogFrameworkOptions;

// DialogConfigService - Configuration
// ========================================

/**
 * Service de configuration pour les dialogues PrimeFaces.
 * <p>
 * Fournit des méthodes pour créer des configurations de dialogue standardisées
 * avec des tailles et comportements prédéfinis.
 * </p>
 */
@ApplicationScoped
public class DialogConfigService {
    
    // Tailles prédéfinies (en pourcentage)
    private static final String SIZE_SMALL_WIDTH = "30%";
    private static final String SIZE_SMALL_HEIGHT = "40%";
    
    private static final String SIZE_MEDIUM_WIDTH = "50%";
    private static final String SIZE_MEDIUM_HEIGHT = "60%";
    
    private static final String SIZE_LARGE_WIDTH = "70%";
    private static final String SIZE_LARGE_HEIGHT = "70%";
    
    private static final String SIZE_EXTRA_LARGE_WIDTH = "90%";
    private static final String SIZE_EXTRA_LARGE_HEIGHT = "90%";
    
    private static final String SIZE_FULL = "100%";
    
    /**
     * Crée un dialogue modal de base avec configuration par défaut.
     *
     * @return un builder de DialogFrameworkOptions
     */
    private DialogFrameworkOptions.Builder createBaseModalDialog() {
        return DialogFrameworkOptions.builder()
                .modal(true)
                .draggable(false)
                .resizable(false)
                .contentWidth(SIZE_FULL)
                .contentHeight(SIZE_FULL);
    }
    
    /**
     * Crée un petit dialogue modal.
     *
     * @return un builder de DialogFrameworkOptions configuré
     */
    public DialogFrameworkOptions.Builder createSmallModalDialog() {
        return createBaseModalDialog()
                .width(SIZE_SMALL_WIDTH)
                .height(SIZE_SMALL_HEIGHT);
    }
    
    /**
     * Crée un dialogue modal de taille moyenne.
     *
     * @return un builder de DialogFrameworkOptions configuré
     */
    public DialogFrameworkOptions.Builder createMediumModalDialog() {
        return createBaseModalDialog()
                .width(SIZE_MEDIUM_WIDTH)
                .height(SIZE_MEDIUM_HEIGHT);
    }
    
    /**
     * Crée un grand dialogue modal.
     *
     * @return un builder de DialogFrameworkOptions configuré
     */
    public DialogFrameworkOptions.Builder createLargeModalDialog() {
        return createBaseModalDialog()
                .width(SIZE_LARGE_WIDTH)
                .height(SIZE_LARGE_HEIGHT);
    }
    
    /**
     * Crée un très grand dialogue modal.
     *
     * @return un builder de DialogFrameworkOptions configuré
     */
    public DialogFrameworkOptions.Builder createExtraLargeModalDialog() {
        return createBaseModalDialog()
                .width(SIZE_EXTRA_LARGE_WIDTH)
                .height(SIZE_EXTRA_LARGE_HEIGHT);
    }
    
    /**
     * Crée un dialogue modal plein écran.
     *
     * @return un builder de DialogFrameworkOptions configuré
     */
    public DialogFrameworkOptions.Builder createFullScreenDialog() {
        return createBaseModalDialog()
                .width(SIZE_FULL)
                .height(SIZE_FULL);
    }
    
    /**
     * Crée un dialogue avec dimensions personnalisées.
     *
     * @param width la largeur en pixels
     * @param height la hauteur en pixels
     * @return un builder de DialogFrameworkOptions configuré
     */
    public DialogFrameworkOptions.Builder createCustomDialog(int width, int height) {
        return createBaseModalDialog()
                .width(width + "px")
                .height(height + "px");
    }
    
    /**
     * Crée un dialogue draggable (déplaçable).
     *
     * @return un builder de DialogFrameworkOptions configuré
     */
    public DialogFrameworkOptions.Builder createDraggableDialog() {
        return createLargeModalDialog()
                .draggable(true)
                .resizable(true);
    }
    
    /**
     * Crée un dialogue non-modal.
     *
     * @return un builder de DialogFrameworkOptions configuré
     */
    public DialogFrameworkOptions.Builder createNonModalDialog() {
        return DialogFrameworkOptions.builder()
                .modal(false)
                .draggable(true)
                .resizable(true)
                .width(SIZE_MEDIUM_WIDTH)
                .height(SIZE_MEDIUM_HEIGHT)
                .contentWidth(SIZE_FULL)
                .contentHeight(SIZE_FULL);
    }
}
