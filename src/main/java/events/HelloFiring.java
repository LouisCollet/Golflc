package events;

//import events.HelloEvent;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
@SessionScoped
public class HelloFiring implements Serializable, interfaces.Log
{
    @Inject
    private static Event<HelloEvent> ev;
    
    public static void doStuff()
    {
        LOG.info("entering dostuff");
        ev.fire(new HelloEvent("from bean LC, curenttimemillis = " + System.currentTimeMillis()));
        LOG.info("after dostuff");
    }  //end method

    public static void main(String[] args) { //throws CardException {
              LOG.info("before");
             doStuff();
    //         LOG.info("after");

     } //end main 
    
    
    
    
    
} // end classe

