package events;

//import events.HelloEvent;
import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
@SessionScoped
public class HelloFiring implements Serializable, interfaces.Log
{
    @Inject
    private static Event<HelloEvent> event;
    
    public static void doStuff()
    {
        LOG.debug("entering dostuff");
        event.fire(new HelloEvent("from bean LC, curenttimemillis = " + System.currentTimeMillis()));
        LOG.debug("after dostuff");
    }  //end method

    void main() { //throws CardException {
              LOG.debug("before");
             doStuff();
    //         LOG.debug("after");

     } //end main 
    
    
    
    
    
} // end classe

