package events;

import static interfaces.Log.LOG;

/**
 *
 * @author Collet
 */
public class HelloEvent {
    public final String message;

    public HelloEvent(String message) {
        LOG.debug("entering class HelloEvent with message = " + message);
        this.message = message;
    }

    public String getMessage() {
         LOG.debug("returning helloevent with message = " + message);
        return message;
    }
}
