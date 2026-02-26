
package listeners;

import static interfaces.Log.LOG;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class StartStopAppListener implements ServletContextListener {
    // private Connection conn = null; // removed 2026-02-26 — CDI migration (all services use @Resource DataSource)

 @Override
 public void contextInitialized(ServletContextEvent event) {
  LOG.debug("Application deployed on the server - Servlet Context Initialized ... " + this);

try{
    ServletContext context = event.getServletContext();
    context.setAttribute("playerid", "");
    context.setAttribute("playerlastname", "");
    context.setAttribute("playerage", 0);
    context.setAttribute("creditcardType", "INITIALIZED");
    LOG.debug("creditcardtype attribute = " + context.getAttribute("creditcardType"));
    // Connection removed 2026-02-26 — all services now use CDI @Resource DataSource
    LOG.debug("CDI migration complete — Connection/DBConnection removed from StartStopAppListener");
    LOG.debug("Server info = " + context.getServerInfo());
    LOG.debug("The default session tracking modes: " + event.getServletContext().getDefaultSessionTrackingModes());
  }catch (Exception ex){
        String msg = "Exception in contextInitialized " + ex;
        LOG.error(msg);
  }
 } // end method

  @Override
 public void contextDestroyed(ServletContextEvent servletContextEvent) {
  LOG.debug("Application undeployed from the server - Servlet Context Destroyed ... ");
  // DBConnection.closeQuietly removed 2026-02-26 — CDI manages connections via DataSource pool
  LOG.debug("Database connections managed by CDI DataSource pool — no manual close needed");
 } // end method
}  //end class
