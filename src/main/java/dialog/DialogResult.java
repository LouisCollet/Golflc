
package dialog;

// ========================================
// DialogResult - Record pour résultats
// ========================================

/**
 * Record encapsulant le résultat d'un dialogue.
 * <p>
 * Utilisé pour typer fortement les retours de dialogue.
 * </p>
 *
 * @param <T> le type de données retourné
 * @param success indique si l'opération a réussi
 * @param data les données retournées (peut être null)
 * @param message un message optionnel
 */
public record DialogResult<T>(
    boolean success,
    T data,
    String message
) {
    
    /**
     * Crée un résultat de succès avec données.
     *
     * @param <T> le type de données
     * @param data les données
     * @return un DialogResult de succès
     */
    public static <T> DialogResult<T> success(T data) {
        return new DialogResult<>(true, data, null);
    }
    
    /**
     * Crée un résultat de succès avec données et message.
     *
     * @param <T> le type de données
     * @param data les données
     * @param message le message
     * @return un DialogResult de succès
     */
    public static <T> DialogResult<T> success(T data, String message) {
        return new DialogResult<>(true, data, message);
    }
    
    /**
     * Crée un résultat d'échec avec message.
     *
     * @param <T> le type de données
     * @param message le message d'erreur
     * @return un DialogResult d'échec
     */
    public static <T> DialogResult<T> failure(String message) {
        return new DialogResult<>(false, null, message);
    }
}