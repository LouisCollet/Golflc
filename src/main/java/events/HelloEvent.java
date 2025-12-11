package events;

/**
 *
 * @author Collet
 */
public class HelloEvent implements interfaces.Log
{
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
