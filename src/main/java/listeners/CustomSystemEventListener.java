package listeners;

import static interfaces.Log.LOG;
import jakarta.faces.application.Application;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PostConstructApplicationEvent;
import jakarta.faces.event.PreDestroyApplicationEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.PrintStream;
import java.io.Serializable;
import java.sql.*;
import java.util.Enumeration;

@WebListener
public class CustomSystemEventListener implements Serializable, SystemEventListener, ServletContextListener{
    // fake modification
   // private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
@Override
public void processEvent(final SystemEvent event) throws AbortProcessingException {
      if (event instanceof PostConstructApplicationEvent postConstructApplicationEvent){ 
 //           LOG.debug(" we are an instance of PostConstructApplicationEvent ");
         try {
            Bootstrap(postConstructApplicationEvent);
        } catch (Exception ex) {
         LOG.debug("Exception in processEvent of CustomSystemEventListener " + ex);
        }
       } else if (event instanceof PreDestroyApplicationEvent preDestroyApplicationEvent) {
  //         LOG.debug(" we are an instance of PreDestroyApplicationEvent ");
            TearDown(preDestroyApplicationEvent);
        }
    }
    
  private void Bootstrap(final PostConstructApplicationEvent event) throws Exception{
          //1. PostConstructApplicationEvent – Perform a custom post-configuration after application has started.
        LOG.debug("Application Started hé hé àà: Bootstrap occurred!");
 //       LOG.debug("Application Started : performBootstrap occurred! " + event);
    //     System.setOut(new PrintStream(outContent,true, "UTF8"));.
    //     java 19 console est en cp
         System.setOut(new PrintStream(System.out, true, "UTF8"));
         LOG.debug("new printStreamApplication Started hé hé àà: Bootstrap occurred!");
         LOG.debug("direct out Application Started: Bootstrap occurred!");
    }
 
 private void TearDown(final PreDestroyApplicationEvent event){
        LOG.debug("entering TearDown  "); // + event);
     //   LOG.debug("Application destroyed " + event);
            // 2. PreDestroyApplicationEvent – Perform a custom cleanup task before application is about to be shut down.
    //  {
        //  LOG.debug(" Starting predestroy" ); //new 13/12/2013
        //  WARNING: The web application [unknown] registered the JDBC driver [com.mysql.jdbc.Driver]
        // but failed to unregister it when the web application was stopped.
        // To prevent a memory leak, the JDBC Driver has been forcibly unregistered.
          
        Enumeration<Driver> drivers = java.sql.DriverManager.getDrivers();
   //     java.sql.DriverManager.drivers() // stream
        while (drivers.hasMoreElements()){
            Driver driver = drivers.nextElement();
            try{
                DriverManager.deregisterDriver(driver); //Removes the specified driver from the DriverManager's list of registered drivers. 
  //                  LOG.debug(String.format("deregistering jdbc driver: %s", driver) );
            } catch (SQLException e){
                LOG.debug(String.format("Error deregistering driver %s", driver), e);
            }
        } //end while
 //List<Driver> drivers = DriverManager.getDrivers();       
//for ( Driver driver : drivers ) {
//  DriverManager.deregisterDriver( driver );
 //}
 //drivers.clear();
        
        

         LOG.debug("PreDestroyApplicationEvent occurred : Application is stopping..."); // + NEW_PAGE);
   //   }
 
} //end method
    
@Override
    public boolean isListenerForSource(final Object obj) {
        boolean result = false;
        result = obj instanceof Application;
        return result;
    }

} //end class