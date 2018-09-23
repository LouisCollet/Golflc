package lc.golfnew;

import static interfaces.Log.LOG;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
//*http://www.mastertheboss.com/javaee/ejb-3/how-to-create-an-ejb-startup-service
@Singleton
@Startup
public class StartupBean // implements interfaces.GolfInterface 
{ 
    public enum States {BEFORESTARTED, STARTED, PAUSED, SHUTTINGDOWN};
    private States state;
   
    @PostConstruct
    public void initialize() {
        state = States.BEFORESTARTED;
        LOG.info("Wildfly - We just created an EJB Startup Service with state = " + state);
   //     LOG.info("intitialize - is this the moment to open the database ??"); // Perform intialization open database ?
       
        state = States.STARTED;
        LOG.info("Wildfly - StartupBean initialized - state " + state);
    }
    
    @PreDestroy
    public void terminate()
    {
        state = States.SHUTTINGDOWN;
        // Perform termination
        LOG.info("Wildfly - terminate - Shut down in progress, state =  " + state);
    }
    public States getState()
    {
        return state;
    }
    public void setState(States state)
    {
        this.state = state;
    }
} //end class
