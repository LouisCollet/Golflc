package events;

/**
 * CDI Event firé par CourseController.reset().
 * Chaque controller @SessionScoped observe et réinitialise ses champs locaux.
 */
public class ResetEvent {

    private final String source;

    public ResetEvent(String source) {
        this.source = source;
    } // end constructor

    public String getSource() {
        return source;
    } // end method

} // end class
