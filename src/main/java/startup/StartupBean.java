package startup;

import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.time.ZoneId;
import java.util.Map;
import java.util.TimeZone;

//*http://www.mastertheboss.com/javaee/ejb-3/how-to-create-an-ejb-startup-service
//https://www.tomitribe.com/blog/singleton-session-beans-eager-initialization-and-ordering/
@Singleton
@Startup // to enforce eager initialization during the application startup sequence

public class StartupBean{ 
    public enum States {BEFORESTARTED, STARTED, PAUSED, SHUTTINGDOWN};
    private States state;
    public Map<String, Object> sessionMap;
    
     
    @PostConstruct
    public void initialize() {
     //   state = States.BEFORESTARTED;
           LOG.debug("Wildfly - We just created an EJB Singleton Startup Service with state = " + state);
           LOG.debug("ZoneId systemDefault = " + ZoneId.systemDefault());
           LOG.debug("user.timezone = " + System.getProperty("user.timezone"));
        System.setProperty("user.timezone", "UTC"); // très IMPORTANT !!!
      //     LOG.debug("ZoneId systemDefault - should be UTC = " + ZoneId.systemDefault());
           LOG.debug("user.timezone - should be UTC = " + System.getProperty("user.timezone"));
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
           LOG.debug("ZoneId systemDefault - should be UTC = " + ZoneId.systemDefault());
   //     LOG.debug("intitialize - is this the moment to open the database ??"); // Perform intialization open database ?
        state = States.STARTED;
        LOG.debug("Applicationn Golflc - StartupBean initialized - state " + States.STARTED);
   // enlevé le 22-03-2020 provoque error build ?? >> Caused by: java.lang.OutOfMemoryError: Java heap space"}}}}
 //       SystemInfoEnum enumInstance = SystemInfoEnum.INSTANCE;
 //       LOG.debug("enum system = " + enumInstance.getSystemName());
    } 

    public Map<String, Object> getSessionMap() {
        return sessionMap;
    }

    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }
    
    @PreDestroy
    public void terminate(){
        state = States.SHUTTINGDOWN;
        // Perform termination
        LOG.debug("Application Golflc - terminate - Shut down in progress, state =  " + States.SHUTTINGDOWN);
    }
    public States getState(){
        return state;
    }
    public void setState(States state){
        this.state = state;
    }
} //end class