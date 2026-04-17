package events;

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@SessionScoped
public class HelloFiring implements Serializable, interfaces.Log {

    private static final long serialVersionUID = 1L;

    @Inject
    private Event<HelloEvent> event; // migrated from static 2026-03-22

    public void doStuff() { // migrated from static 2026-03-22
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        event.fire(new HelloEvent("from bean LC, currentTimeMillis = " + System.currentTimeMillis()));
        LOG.debug("after " + methodName);
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        doStuff();
    } // end main
    */

} // end class
