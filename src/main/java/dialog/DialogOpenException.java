
package dialog;

// ========================================
// DialogOpenException - Exception custom
// ========================================

/**
 * Exception levée lors de l'échec de l'ouverture d'un dialogue.
 */
public class DialogOpenException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructeur avec message.
     *
     * @param message le message d'erreur
     */
    public DialogOpenException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause.
     *
     * @param message le message d'erreur
     * @param cause la cause de l'exception
     */
    public DialogOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}
