
package listeners;

import java.io.Serializable;
import java.sql.*;
import java.util.Enumeration;
import javax.faces.application.Application;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.PreDestroyApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

//@Named
//@SessionScoped
@WebListener
public class CustomSystemEventListener
     //   extends EventListener implements 
        implements Serializable, SystemEventListener, interfaces.GolfInterface, interfaces.Log , ServletContextListener
{
    private String Admin = null;
    
    @Override
    public void processEvent(final SystemEvent event) throws AbortProcessingException
    {
 LOG.info("Application Started hé hé : processEvent occurred!");
     if (event instanceof PostConstructApplicationEvent){ 
            LOG.info(" we are an instance of PostConstructApplicationEvent ");
         try {
            performBootstrap((PostConstructApplicationEvent) event);
        } catch (Exception ex) {
         LOG.info("Exception in processEvent of CustomSystemEventListener " + ex);
        }
       } else if (event instanceof PreDestroyApplicationEvent) {
           LOG.info(" we are an instance of PreDestroyApplicationEvent ");
            performTearDown((PreDestroyApplicationEvent) event);
        }
    }
    
    private void performBootstrap(final PostConstructApplicationEvent event) throws Exception
    {
          //1. PostConstructApplicationEvent – Perform a custom post-configuration after application has started.
        LOG.info("Application Started : performBootstrap occurred! " + event);
        utils.LCUtil.LCstartup();
         
        //    Application application = event.getApplication();
  //         LOG.info("Application = " + application);
  //     ProjectStage stage = application.getProjectStage();
  //        LOG.info("Stage = " + stage);
  //   if (ProjectStage.Development == stage)
   //     {
  //          LifecycleFactory lcFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
  //         Lifecycle lc = lcFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
    //       lc.addPhaseListener(new PhaseListener());

  //  }
    }
        // do other stuff related to bootstrapping:
        // 1) check for availablity of web services
        // 2) init ressources
        // ...
 private void performTearDown(final PreDestroyApplicationEvent event)
 {
        LOG.info("Application Destroyed : performTearDown occurred! " + event);
     //   LOG.info("Application destroyed " + event);
            // 2. PreDestroyApplicationEvent – Perform a custom cleanup task before application is about to be shut down.
    //  {
        //  LOG.info(" Starting predestroy" ); //new 13/12/2013
        //  WARNING: The web application [unknown] registered the JDBC driver [com.mysql.jdbc.Driver]
        // but failed to unregister it when the web application was stopped.
        // To prevent a memory leak, the JDBC Driver has been forcibly unregistered.
          
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements())
        {
            Driver driver = drivers.nextElement();
            try
            {
                DriverManager.deregisterDriver(driver);
                    LOG.info(String.format("deregistering jdbc driver: %s", driver) );
            } catch (SQLException e)
            {
                LOG.info(String.format("Error deregistering driver %s", driver), e);
            }
        } //end while

         LOG.info("PreDestroyApplicationEvent occurred : Application is stopping." + NEW_PAGE);
   //   }
 
} //end method
    
//@Override
//   public boolean isListenerForSource(Object value) {
      //only for Application
//      return (value instanceof Application);
//   }
@Override
    public boolean isListenerForSource(final Object obj)
    {
        boolean result = false;
        result = obj instanceof Application;
        return result;
    }
/*    
@Override

   public void processEvent(final SystemEvent event) throws AbortProcessingException
{
if(event instanceof PostConstructApplicationEvent)
    //1. PostConstructApplicationEvent – Perform a custom post-configuration after application has started.
{
         LOG.info(NEW_PAGE + "Application Started : PostConstructApplicationEvent occurred!");
         
         utils.LCUtil.startup();

      }
      
if(event instanceof PreDestroyApplicationEvent)
    // 2. PreDestroyApplicationEvent – Perform a custom cleanup task before application is about to be shut down.
      {
          LOG.info(" Starting predestroy" ); //new 13/12/2013
        //  WARNING: The web application [unknown] registered the JDBC driver [com.mysql.jdbc.Driver]
        // but failed to unregister it when the web application was stopped.
        // To prevent a memory leak, the JDBC Driver has been forcibly unregistered.
          
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements())
        {
            Driver driver = drivers.nextElement();
            try
            {
                DriverManager.deregisterDriver(driver);
                    LOG.info(String.format("deregistering jdbc driver: %s", driver) );
            } catch (SQLException e)
            {
                LOG.info(String.format("Error deregistering driver %s", driver), e);
            }
        } //end while

         LOG.info("PreDestroyApplicationEvent occurred : Application is stopping." + NEW_PAGE);
      }
}
*/
    public String getAdmin() {
        return Admin;
    }

    public void setAdmin(String Admin) {
        this.Admin = Admin;
    }

 //   @Override
//    public void contextInitialized(ServletContextEvent sce) {
 //       throw new UnsupportedOperationException("contextInitialized is Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  //  }

  //  @Override
  //  public void contextDestroyed(ServletContextEvent sce) {
  //      throw new UnsupportedOperationException("contextDestroyed is Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  //  }



} //end class